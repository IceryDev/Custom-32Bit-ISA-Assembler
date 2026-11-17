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
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IllegalFormatWidthException;

public class ISetParser {
    public ArrayList<Instruction> iset = new ArrayList<>();
    public final ImmType immType;
    public final FlagType flagType;
    public final MemType memType;
    public final int startIndex;
    public final int shiftAmount;

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
        this.startIndex = Integer.parseInt(elements.item(0).getTextContent().trim());
    }

    public boolean getISet(String isetFilePath) {

        try (BufferedReader fileReader = new BufferedReader(new FileReader(isetFilePath))){
            String line;

            while ((line = fileReader.readLine()) != null){
                line = line.trim();


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
    }
}
