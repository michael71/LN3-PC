/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import static de.blankedv.ln3pc.Variables.*;
import static de.blankedv.ln3pc.Variables.DCCMAX;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * utility function for the mapping of lanbahn addresses to DCC addresses (and
 * bits) and vice versa
 *
 * @author mblank
 */
public class ReadDCCConfig {

    private static final boolean CFG_DEBUG = true;

    public static void init(String configfilename) {

        readXMLConfigFile(configfilename);

    }

    // code template taken from lanbahnPanel
    private static String readXMLConfigFile(String fname) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e1) {
            System.out.println("ParserConfigException Exception - " + e1.getMessage());
            return "ParserConfigException";
        }
        Document doc;
        try {
            doc = builder.parse(new File(fname));
            parseSignalsAndSensors(doc);

        } catch (SAXException e) {
            System.out.println("SAX Exception - " + e.getMessage());
            return "SAX Exception - " + e.getMessage();
        } catch (IOException e) {
            System.out.println("IO Exception - " + e.getMessage());
            return "IO Exception - " + e.getMessage();
        }

        return "";
    }

    // code template from lanbahnPanel
    private static void parseSignalsAndSensors(Document doc) {
        // assemble new ArrayList of tickets.
        //<layout-config>
//<panel name="Lonstoke West 2">
//<signal x="290" y="100" x2="298" y2="100" adr="763" nbit="2" />   
//     ==> map lanbahn value at address 763 to 2 dcc addresses: 763 (low) and 764 (high bit)

        NodeList items;
        Element root = doc.getDocumentElement();

        items = root.getElementsByTagName("panel");
        if (items.getLength() == 0) {
            return;
        }

        String panelProtocol = parsePanelAttribute(items.item(0), "protocol");

        if (CFG_DEBUG) {
            System.out.println("panelProtocol =" + panelProtocol);
        }

        // NamedNodeMap attributes = item.getAttributes();
        // Node theAttribute = attributes.items.item(i);
        // look for TrackElements - this is the lowest layer
        items = root.getElementsByTagName("signal");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " signals");
        }
        for (int i = 0; i < items.getLength(); i++) {
            DCCMultiAspectSignalMapping tmp = parseDCCMapping(items.item(i));
            if ((tmp != null) && (tmp.lbAddr != INVALID_INT)) {
                System.out.println("signal mapping: " + tmp.toString());
                allSignalMappings.add(tmp);
            }
        }

        items = root.getElementsByTagName("sensor");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " sensors");
        }
        for (int i = 0; i < items.getLength(); i++) {
            int sensAddress = parseDCCSensorAddress(items.item(i));
            if (sensAddress != INVALID_INT) {
                System.out.println("sensor found with address: " + sensAddress);
                allSensors.add(sensAddress);
            }
        }

    }
    // code template from lanbahnPanel

    private static DCCMultiAspectSignalMapping parseDCCMapping(Node item) {

        DCCMultiAspectSignalMapping dccmap = new DCCMultiAspectSignalMapping();

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            // if (CFG_DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.getNodeName().equals("adr")) {
                dccmap.lbAddr = getPositionNode(theAttribute);
            } else if (theAttribute.getNodeName().equals("dccadr")) {
                dccmap.dccAddr = getPositionNode(theAttribute);
            } else if (theAttribute.getNodeName().equals("nbit")) {
                dccmap.nBit = getPositionNode(theAttribute);
            }
        }

        if (dccmap.nBit == 2) {
            // currently implemented only for 4 aspect signals
            if (dccmap.lbAddr != INVALID_INT) {
                // lanbahn address is defined, calculate mainui
                dccmap.dccAddr = dccmap.lbAddr;
                return dccmap;
            } else if ((dccmap.dccAddr != INVALID_INT) && (dccmap.dccAddr <= DCCMAX)) {
                // we have a valid mainui address
                dccmap.lbAddr = dccmap.dccAddr;
                return dccmap;
            } else {
                System.out.println("invalid config data, dccAddr=" + dccmap.dccAddr + " nBit=" + dccmap.nBit);
            }
        }
        return null;
    }

    // code from lanbahnPanel
    private static int getPositionNode(Node a) {
        return Integer.parseInt(a.getNodeValue());
    }
    // code from lanbahnPanel

    private static int getValue(String s) {
        float b = Float.parseFloat(s);
        return (int) b;
    }
    // code from lanbahnPanel

    private static String parsePanelAttribute(Node item, String att) {
        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);

            if (theAttribute.getNodeName().equals(att)) {
                String attrib = theAttribute.getNodeValue();
                return attrib;

            }
        }
        return "";
    }

    /** read a sensor address from XML node
     * 
     * @param item
     * @return 
     */
    private static int parseDCCSensorAddress(Node item) {
        int sensA = INVALID_INT;

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);

            if (theAttribute.getNodeName().equals("adr")) {
                try {
                    int a = Integer.parseInt(theAttribute.getNodeValue());
                    return a;
                } catch (NumberFormatException e) {
                    return INVALID_INT;
                }
            }
        }
        return INVALID_INT;

    }
}
