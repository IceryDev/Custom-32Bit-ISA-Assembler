import Funcs.ISetParser;
import Funcs.ImmediateHandler;
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
    public static final String CONFIG_PATH = "src/isetConfig.xml";

    //Do not modify any of these below unless you are sure what you are doing.
    public static final int OPCODE_MASK = 0xFFFFF000;
    public static final int DEST_MASK = 0x00000F00;
    public static final int REGA_MASK = 0x000000F0;
    public static final int REGB_MASK = 0x0000000F;

    public static final int IMMEDIATE_MAX_BIT_COUNT = 4;
    public static final int OFFSET_MAX_BIT_COUNT = 8;
    public static final int BRANCH_OFFSET_REDUCTION_DUETO_PC_INCREASE = 1;

    public static HashMap<String, Integer> labels = new HashMap<String, Integer>();
    public static ArrayList<Integer> instructions = new ArrayList<>();
    public static ArrayList<String> fileLines = new ArrayList<>();

    public static void main(String[] args){
        if (args.length != 2) {
            System.err.println("Error: Invalid number of arguments. Correct format: java Assembler <input_file> <output_file>");
            return;
        }

        //Initialise Orbyte Assembly (OAS) file.
        String inputFilePath = args[0];
        String outputFilePath = args[1] + ".txt";
        ImmediateHandler immediateHandler = new ImmediateHandler();
        ISetParser isp;
        try {
            isp = new ISetParser(CONFIG_PATH);
        }
        catch(ParserConfigurationException | IOException | SAXException e) {
            System.err.println("ConfigError: Couldn't read XML Config File");
            return;
        }
        catch (IllegalArgumentException e) {
            return;
        }

        System.out.println(isp.memType + " " + isp.flagType + " " + isp.immType + " " + isp.startIndex);

        if (!inputFilePath.endsWith(SCRIPT_EXTENSION)){
            System.err.println("Error: Invalid file extension, file name must end with " + SCRIPT_EXTENSION);
            return;
        }

        if (!readFile(inputFilePath, fileLines)){
            //The error message is logged within the function.
            return;
        }

        if (!searchLabels(fileLines, labels)){
            //The error message is logged within the function.
            return;
        }

        if (!assembleInstructions(fileLines, labels, instructions)) return;

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
                                               HashMap<String, Integer> labelDict, ArrayList<Integer> out) {

        int lineNo = 1;
        int instructionNo = 0;

        for (String line : lines) {
            if (line.isEmpty() || line.startsWith(COMMENT_DELIMITER)) {
                lineNo++;
                continue;
            }
            String[] tokens = line.split(" +");

            boolean flagsSet = tokens[0].toUpperCase().endsWith(FLAG_SUFFIX);
            //Check if nothing illegal comes after label
            if (tokens[0].endsWith(LABEL_SUFFIX)){
                if (tokens.length > 1){
                    System.err.printf("Syntax Error: Garbage following label at line %d\n", lineNo);
                    return false;
                }
            }
//            else if (Instruction.isInstruction((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0])) {
//                Instruction currentInstruction = Instruction.valueOf(
//                        ((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0]).toUpperCase());
//            }

            lineNo++;
        }
        return false;
    }
}
