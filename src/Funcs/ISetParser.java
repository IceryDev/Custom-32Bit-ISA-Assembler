package Funcs;

import Resources.FlagType;
import Resources.ImmType;
import Resources.Instruction;
import Resources.MemType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.IllegalFormatWidthException;

public class ISetParser {
    public ArrayList<Instruction> iset = new ArrayList<>();
    public final ImmType immType;
    public final FlagType flagType;
    public final MemType memType;
    public final int startIndex;

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
                break;
            case "LIMITED":
                this.memType = MemType.Limited;
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
}
