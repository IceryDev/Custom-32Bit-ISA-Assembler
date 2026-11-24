package Funcs;

import Resources.FlagType;
import Resources.ImmType;
import Resources.Instruction;
import Resources.MemType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.util.HashMap;
import java.util.IllegalFormatWidthException;

public class ISetParser {
    public static final String TRUE_VAL = "1";
    public static final int HEADER_SIZE = 10;

    public HashMap<String, Instruction> iset = new HashMap<>();
    public final ImmType immType;
    public final FlagType flagType;
    public final MemType memType;
    public final int memIndex;
    public final int memSize;
    public final int instStart;
    public final int shiftAmount;
    public int userDefOpcodeSize = 20; //In bits

    /**
     * Reads the config file and stores it in the ISP
     */
    public ISetParser (String configFilePath)
                                    throws ParserConfigurationException, IOException, SAXException, IllegalArgumentException {

        Document configFile = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(configFilePath));
        NodeList elements = configFile.getElementsByTagName("ImmType");
        if (elements.getLength() != 1) {
            throw new IllegalFormatWidthException(elements.getLength());
        }
        switch (elements.item(0).getTextContent().trim().toUpperCase()) {
            case "IMMBIT":
                this.immType = ImmType.ImmBit;
                break;
            case "SEPARATE":
                this.immType = ImmType.Separate;
                break;
            case "INCREMENTAL":
                this.immType = ImmType.Incremental;
                break;
            default:
                throw new IllegalArgumentException("Error: ImmType is not valid in config file!");
        }
        userDefOpcodeSize -= this.immType.OP_SHRINK;

        elements = configFile.getElementsByTagName("FlagType");
        if (elements.getLength() != 1) {
            throw new IllegalFormatWidthException(elements.getLength());
        }
        switch (elements.item(0).getTextContent().trim().toUpperCase()) {
            case "HARDWARE":
                this.flagType = FlagType.Hardware;
                break;
            case "SBIT":
                this.flagType = FlagType.SBit;
                break;
            case "INCREMENTAL":
                this.flagType = FlagType.Incremental;
                break;
            default:
                throw new IllegalArgumentException("Error: FlagType is not valid in config file!");
        }
        userDefOpcodeSize -= this.flagType.OP_SHRINK;

        elements = configFile.getElementsByTagName("MemType");
        if (elements.getLength() != 1) {
            throw new IllegalFormatWidthException(elements.getLength());
        }
        switch (elements.item(0).getTextContent().trim().toUpperCase()) {
            case "INJECT":
                this.memType = MemType.Inject;
                this.shiftAmount = 0;
                break;
            case "LIMITED":
                this.memType = MemType.Limited;
                this.shiftAmount = 0;
                break;
            case "SHIFT":
                this.memType = MemType.Shift;
                this.shiftAmount = Integer.parseInt(((Element) elements.item(0)).getAttribute("amnt").trim());
                break;
            default:
                throw new IllegalArgumentException("Error: MemType is not valid in config file!");
        }

        elements = configFile.getElementsByTagName("Start");
        if (elements.getLength() != 1) {
            throw new IllegalFormatWidthException(elements.getLength());
        }
        int tmpStart = Integer.parseInt(elements.item(0).getTextContent().trim());

        switch(this.memType) {
            case MemType.Inject:
                this.memIndex = 0;
                this.memSize = tmpStart;
                this.instStart = this.memSize;
                break;
            case MemType.Shift:
                this.instStart = tmpStart;
                this.memIndex = 1 << this.shiftAmount;
                if (this.instStart >= this.memIndex && this.instStart <= this.memIndex + 16) throw new IllegalStateException("Error: Memory and instruction locations clash!");
                this.memSize = 16;
                break;
            case MemType.Limited:
                this.memIndex = -1;
                this.memSize = 0;
                this.instStart = tmpStart;
                break;
            default:
                throw new IllegalArgumentException("Error: MemType is not valid!");
        }
    }

    /**
     * Reads a formatted .csv file to retrieve instructions and opcodes.
     * @param isetFilePath File path to the .csv file
     * @return Whether the operation was successful
     */
    public boolean getISet(String isetFilePath) {

        try (BufferedReader fileReader = new BufferedReader(new FileReader(isetFilePath))){
            String line;
            line = fileReader.readLine();
            String[] headers = line.trim().split(",\\s*");
            if (headers.length != HEADER_SIZE) {
                System.err.println("Error: Header mismatch, couldn't read CSV.");
                return false;
            }
            while ((line = fileReader.readLine()) != null){
                line = line.trim();

                String[] tokens = line.split(",\\s*");
                if (tokens.length != HEADER_SIZE) {
                    System.err.println("Error: Missing elements in ISA spec. Please fill all fields.");
                    return false;
                }
                Instruction tmp = new Instruction(Integer.decode(tokens[1]),
                        tokens[2].compareTo(TRUE_VAL) == 0,
                        tokens[3].compareTo(TRUE_VAL) == 0,
                        tokens[4].compareTo(TRUE_VAL) == 0,
                        Integer.parseInt(tokens[5]),
                        tokens[6].compareTo(TRUE_VAL) == 0,
                        tokens[7].compareTo(TRUE_VAL) == 0,
                        tokens[8].compareTo(TRUE_VAL) == 0,
                        Integer.parseInt(tokens[9]));

                if (tmp.opCount() > 3) {
                    System.err.println("Error: This assembler only supports instructions with 2 or 3 operands. " +
                            "Additionally, branch instructions must have only 1 operand, otherwise that field will be ignored.");
                    return false;
                }
                if (this.iset.containsKey(tokens[0])) {
                    System.err.println("Error: Duplicate entries in the ISA.");
                    return false;
                }
                this.iset.put(tokens[0], tmp);

            }
            return true;
        }
        catch (FileNotFoundException error){
            System.err.printf("Error: File %s not found.\n", isetFilePath);
            return false;
        }
        catch (IOException error){
            System.err.println("Error: An error occurred while reading file: " + error.getMessage());
            return false;
        }
        catch (NumberFormatException error) {
            System.err.println("Error: Invalid number format in ISA file");
            return false;
        }
    }

    //  For testing
//    public void printInstructions() {
//        if (this.iset.isEmpty()) return;
//
//        for (String val : this.iset.keySet()) {
//            Instruction tmp = this.iset.get(val);
//            System.out.printf("%s, %d, %d, %b, %b\n", val, tmp.opCode(), tmp.opCount(), tmp.isBranch(), tmp.isCompare());
//        }
//    }
}
