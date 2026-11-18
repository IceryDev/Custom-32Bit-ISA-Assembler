import Funcs.ISetParser;
import Funcs.ImmediateHandler;
import Resources.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Assembler {

    public static final String COMMENT_DELIMITER = "//";
    public static final String IMMEDIATE_DELIMITER = "#";
    public static final String ADDRESS_DELIMITER = "&";
    public static final String SCRIPT_EXTENSION = ".oas";
    public static final String FLAG_SUFFIX = "S"; //Make sure this is a capital letter
    public static final String LABEL_SUFFIX = ":";
    public static String configPath = "src/isetConfig.xml";
    public static String isaPath = "src/isa.csv";

    //Do not modify any of these below unless you are sure what you are doing.
    public static final int REGA_MASK = 0x000000F0;
    public static final int REGB_MASK = 0x0000000F;

    public static final int PAYLOAD_SLOT_WIDTH = 4;
    public static final int PAYLOAD_SLOTS = 3;
    public static final int OFFSET_MAX_BIT_COUNT = 8;
    public static final int OPCODE_SIZE_BITS = 20;
    public static final int BRANCH_OFFSET_REDUCTION_DUETO_PC_INCREASE = 1;
    public static final int BRANCH_OP_COUNT = 1;

    public static HashMap<String, Integer> labels = new HashMap<String, Integer>();
    public static ArrayList<Integer> instructions = new ArrayList<>();
    public static ArrayList<Integer> memory = new ArrayList<>();
    public static ArrayList<String> fileLines = new ArrayList<>();

    public static void main(String[] args){
        if (args.length != 4) {
            System.err.println("Error: Invalid number of arguments. Correct format: java Assembler " +
                    "<configFile> <isaFile> <input_file> <output_file>");
            return;
        }

        //Initialise Orbyte Assembly (OAS) file.
        configPath = args[0];
        isaPath = args[1];
        String inputFilePath = args[2];
        String outputFilePath = args[3] + ".txt";
        ImmediateHandler immediateHandler = new ImmediateHandler();
        ISetParser isp;
        try {
            isp = new ISetParser(configPath);
        }
        catch(ParserConfigurationException | IOException | SAXException e) {
            System.err.println("ConfigError: Couldn't read XML Config File");
            return;
        }
        catch (IllegalArgumentException e) {
            System.err.println("XMLSyntaxError: XML Config File format is incorrect.");
            return;
        }

        if (!isp.getISet(isaPath)) return; //The error message is logged within the function.

        if (!inputFilePath.endsWith(SCRIPT_EXTENSION)){
            System.err.println("Error: Invalid file extension, file name must end with " + SCRIPT_EXTENSION);
            return;
        }

        if (!readFile(inputFilePath, fileLines)) return; //The error message is logged within the function.

        if (!searchLabels(fileLines, labels)) return; //The error message is logged within the function.

        if (!assembleInstructions(fileLines, labels, isp, instructions)) return;

    }

    //Reads the file to generate a list of each line, removes comments and blank spaces
    public static boolean readFile(String inputFilePath, ArrayList<String> lines) {
        try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFilePath))){
            String line;

            while ((line = fileReader.readLine()) != null){
                line = line.trim();
                int p = line.indexOf(COMMENT_DELIMITER);

                lines.addLast((p != -1) ? line.substring(0, p) : line);
            }
            return true;
        }
        catch (FileNotFoundException error){
            System.err.printf("Error: File %s not found.\n", inputFilePath);
            return false;
        }
        catch (IOException error){
            System.err.println("Error: An error occurred while reading file: " + error.getMessage());
            return false;
        }
    }

    //Finds all labels in the file, if it occurs twice yields an error (returns false to end the program)
    //Puts all those labels names and the index of the instruction after it (to calculate offset later on)
    //to a dictionary, also checks for unknown instructions not in the ISA
    public static boolean searchLabels(ArrayList<String> lines, HashMap<String, Integer> labelDict){

        int instructionNo = 0;
        int lineNo = 1;

        for (String line : lines){
            String[] tokens = line.split(" +");
            if (line.isEmpty()) { lineNo++; continue; }

            if (tokens[0].equals(LABEL_SUFFIX)){
                System.err.printf("Label Error: Label name cannot be empty at line %d\n", lineNo);
                return false;
            }

            //Assume all instructions are valid, that is checked in the assembly section
            if (tokens[0].endsWith(LABEL_SUFFIX)){
                String labelName = tokens[0].substring(0, tokens[0].length() - 1);
                if (labelDict.containsKey(labelName)){
                    System.err.printf("Label Error: Same file cannot have two identical labels \"%s\" at line %d\n",
                            labelName, lineNo);
                    return false;
                }
                labelDict.put(labelName, instructionNo + 1);
            }
            else {
                instructionNo++;
            }
            lineNo++;
        }
        return true;
    }

    public static boolean assembleInstructions(ArrayList<String> lines,
                                               HashMap<String, Integer> labelDict,
                                               ISetParser isp,
                                               ArrayList<Integer> out) {

        ImmediateHandler immediateHandler = new ImmediateHandler();
        int lineNo = 1;
        int instructionNo = 0;

        for (String line : lines) {
            if (line.isEmpty() || line.startsWith(COMMENT_DELIMITER)) {
                lineNo++;
                continue;
            }
            String[] tokens = line.split(" +");

            //Check if nothing illegal comes after label
            if (tokens[0].endsWith(LABEL_SUFFIX)){
                if (tokens.length > 1){
                    System.err.printf("Syntax Error: Garbage following label at line %d\n", lineNo);
                    return false;
                }
            }
            else {
                //Check if valid
                int tokenLastIndexWithoutFlagBit = tokens[0].length()-1;
                boolean flagsSet = tokens[0].toUpperCase().endsWith(FLAG_SUFFIX) &&
                        isp.iset.containsKey(tokens[0].substring(0, tokenLastIndexWithoutFlagBit));
                Instruction currentInstruction;
                String currentInstName;
                if (isp.iset.containsKey(tokens[0])) {
                    currentInstName = tokens[0];
                    flagsSet = false;
                }
                else if (isp.flagType != FlagType.Hardware && flagsSet){
                    currentInstName = tokens[0].substring(0, tokenLastIndexWithoutFlagBit);
                }
                else {
                    System.err.printf("Syntax Error: Unknown instruction \"%s\" on line %d\n", tokens[0], lineNo);
                    return false;
                }

                currentInstruction = isp.iset.get(currentInstName);
                instructionNo++;
                int opBits = currentInstruction.opCode();

                //Check correct operand count
                int operandCount = (currentInstruction.isBranch() ? BRANCH_OP_COUNT : currentInstruction.opCount());
                if(tokens.length < (operandCount + 1)){
                    System.err.printf("Syntax Error: Invalid number of operands at line %d, "+
                            "required %d but provided %d\n", lineNo, (operandCount + 1), tokens.length);
                    return false;
                }
                else if (tokens.length > (operandCount + 1)){
                    System.err.printf("Syntax Error: Garbage following instruction at line %d\n", lineNo);
                    return false;
                }

                //Check branch
                if (currentInstruction.isBranch()) {
                    opBits = opBits << (OPCODE_SIZE_BITS-isp.userDefOpcodeSize);

                    String labelName = tokens[BRANCH_OP_COUNT];
                    if (!labels.containsKey(labelName)){
                        System.err.printf("Syntax Error: No such label defined as %s at line %d\n",
                                labelName, lineNo);
                        return false;
                    }

                    int offset = labelDict.get(labelName) - instructionNo - BRANCH_OFFSET_REDUCTION_DUETO_PC_INCREASE;
                    if (offset >= Math.pow(2, OFFSET_MAX_BIT_COUNT-1)
                            || offset < (-1) * Math.pow(2, OFFSET_MAX_BIT_COUNT-1)){
                        System.err.printf("Error: Branch offset at line %d is too big\n", lineNo);
                        return false;
                    }

                    offset = immediateHandler.shortenNegative(offset, OFFSET_MAX_BIT_COUNT);
                    offset = ((offset & REGA_MASK) << PAYLOAD_SLOT_WIDTH) + (offset & REGB_MASK);

                    opBits = (opBits << (PAYLOAD_SLOTS*PAYLOAD_SLOT_WIDTH)) | offset;
                    out.add(opBits);
                    lineNo++;
                    continue;
                }

                //Check if flags are valid (unless it is Hardware mode, then we don't care about it)
                if (!(isp.flagType == FlagType.Hardware || currentInstruction.isCompare()) && !currentInstruction.setFlags() && flagsSet) {
                    System.err.printf("Syntax Error: Operation %s cannot have a flag setter suffix \"%s\"\n",
                            currentInstName, FLAG_SUFFIX);
                    return false;
                }

                boolean hasImmediate = tokens[operandCount].startsWith(IMMEDIATE_DELIMITER);
                if (isp.immType == ImmType.Separate && currentInstruction.allowReg() != currentInstruction.allowImm()) {
                    System.err.printf("Value Error: On Separate Immediate Mode, non-branch instructions" +
                                    " must strictly allow one of immediate or register operands. (line %d)\n", lineNo);
                    return false;
                }

                if (hasImmediate && !currentInstruction.allowImm()) {
                    System.err.printf("Operation Error: The instruction \"%s\" on line %d does not allow for immediate values\n",
                            currentInstName, lineNo);
                    return false;
                }

                int immediateValue = 0;
                try {
                    if (hasImmediate) {
                        immediateValue = immediateHandler.parseImmediateToInt(tokens[operandCount]);
                        if ((!currentInstruction.isLoad() || isp.memType == MemType.Limited) &&
                                !immediateHandler.isInRange(immediateValue, PAYLOAD_SLOT_WIDTH, false)) {
                            System.err.printf("Range Error: The immediate operand at line %d cannot fit the payload of size %d bits\n",
                                    lineNo, PAYLOAD_SLOT_WIDTH);
                            return false;
                        }
                    }
                }
                catch (NumberFormatException e) {
                    System.err.printf("Syntax Error: The immediate operand at line %d must be"+
                            " either binary, decimal, or hexadecimal\n", lineNo);
                    return false;
                }

                opBits = assembleOpCode(opBits,
                        isp.immType == ImmType.Incremental && currentInstruction.allowImm(), hasImmediate, flagsSet, currentInstruction.isCompare(), isp);
                if (!Register.isRegister(tokens[1].toUpperCase())){
                    System.err.printf("Syntax Error: Invalid operand \"%s\" at line %d. Operands other than"+
                                    " the last operand must be a register mnemonic (i.e. R0 or R1)\n",
                            tokens[1], lineNo);
                    return false;
                }
                opBits = (opBits << PAYLOAD_SLOT_WIDTH) + Register.valueOf(tokens[1].toUpperCase()).getRegCode();

                if (operandCount == 2 && hasImmediate) {
                    if (currentInstruction.isLoad() && isp.memType != MemType.Limited) {
                        if (memory.size() >= isp.memSize) {
                            System.err.println("Error: Too many memory spaces to address, try using less load instructions.");
                            return false;
                        }
                        opBits = (opBits << ((PAYLOAD_SLOTS-1)*PAYLOAD_SLOT_WIDTH)) + (memory.size());
                        memory.add(immediateValue);
                    }
                    else {
                        opBits = (opBits << ((PAYLOAD_SLOTS-1)*PAYLOAD_SLOT_WIDTH)) + immediateValue;
                    }

                    out.add(opBits);
                }
                else {
                    String secondRegisterName = tokens[2].toUpperCase();
                    boolean isAddr = secondRegisterName.startsWith(ADDRESS_DELIMITER);
                    if (!(isAddr == currentInstruction.isLoad())) {
                        System.err.printf("Syntax Error: Invalid operand \"%s\" at line %d. Memory access"+
                                        " must be done via addressing if no immediate is used\n",
                                tokens[2], lineNo);
                        return false;
                    }
                    if (isAddr) secondRegisterName = secondRegisterName.substring(ADDRESS_DELIMITER.length());

                    if (!Register.isRegister(secondRegisterName)){
                        System.err.printf("Syntax Error: Invalid operand \"%s\" at line %d. Operands other than"+
                                        " the last operand must be a register mnemonic (i.e. R0 or R1)\n",
                                tokens[2], lineNo);
                        return false;
                    }
                    opBits = (opBits << PAYLOAD_SLOT_WIDTH) + Register.valueOf(secondRegisterName).getRegCode();
                    if (isAddr) {
                        out.add(opBits << PAYLOAD_SLOT_WIDTH);
                        lineNo++;
                        continue;
                    }

                    if (operandCount == 2) out.add(opBits << PAYLOAD_SLOT_WIDTH);
                    else if (hasImmediate) out.add((opBits << PAYLOAD_SLOT_WIDTH) + immediateValue);
                    else {
                        if (!Register.isRegister(tokens[3].toUpperCase())){
                            System.err.printf("Syntax Error: Invalid operand \"%s\" at line %d. Last operand must either be a register or a immediate\n",
                                    tokens[3], lineNo);
                            return false;
                        }
                        out.add((opBits << PAYLOAD_SLOT_WIDTH) + Register.valueOf(tokens[3].toUpperCase()).getRegCode());
                    }
                }
            }

            lineNo++;
        }
        return true;
    }

    /**
     * Applies different options for the immediates and flags to the opcode to generate unique opcodes.
     * @param opCode The opcode without the S or Imm Bits if they are set
     * @param immAllowAndInc Is immediate allowed AND also set to Increment for this instruction
     * @param hasImm Does this operation have an immediate operand
     * @param hasFlag Does this operation have flags set
     * @param isp {@link ISetParser} object for configuration details
     * @return Resultant 20-bit opcode.
     */
    private static int assembleOpCode(int opCode, boolean immAllowAndInc, boolean hasImm, boolean hasFlag, boolean isCmp, ISetParser isp) {

        boolean flagIncrement = isp.flagType == FlagType.Incremental;
        if (flagIncrement) {
            opCode = isp.flagType.encode(opCode, hasFlag, immAllowAndInc, isCmp);
            opCode = isp.immType.encode(opCode, hasImm);
        }
        else {
            opCode = isp.immType.encode(opCode, hasImm);
            opCode = isp.flagType.encode(opCode, hasFlag, immAllowAndInc, isCmp);
        }
        return opCode;
    }
}
