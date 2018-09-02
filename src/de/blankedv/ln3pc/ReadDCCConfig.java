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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
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

    // code template taken from lanbahnPanel
    public static String readXML(String fname) {
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
            parsePEsAndTimetable(doc);
            // sort the trips by ID
            Collections.sort(allTrips, (a, b) -> b.compareTo(a));

        } catch (SAXException e) {
            System.out.println("SAX Exception - " + e.getMessage());
            return "SAX Exception - " + e.getMessage();
        } catch (IOException e) {
            System.out.println("IO Exception - " + e.getMessage());
            return "IO Exception - " + e.getMessage();
        } catch (Exception e) {
            System.out.println("other Exception - " + e.getMessage());
            return "other Exception - " + e.getMessage();
        }

        return "OK";
    }

    // code template from lanbahnPanel
    private static void parsePEsAndTimetable(Document doc) {
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
        items = root.getElementsByTagName("turnout");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " turnouts");
        }
        for (int i = 0; i < items.getLength(); i++) {
            int aTurnout = parseAddress(items.item(i));
            if ( aTurnout != INVALID_INT) {
                System.out.println("turnout a=" +  aTurnout );
                lanbahnData.put( aTurnout, new LbData(0, TYPE_ACCESSORY));
            }
        }
        
        items = root.getElementsByTagName("signal");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " signals");
        }
        
        for (int i = 0; i < items.getLength(); i++) {
            Signal maSig = parseMultiAspectAddress(items.item(i));
            if ((maSig != null) && (maSig.addr != INVALID_INT)) {
                System.out.println("signal a/t = " + maSig.toString());
                lanbahnData.put(maSig.addr, new LbData(0, maSig.sigType));
            }
        }

        items = root.getElementsByTagName("sensor");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " sensors");
        }
        for (int i = 0; i < items.getLength(); i++) {
            int sensAddress = parseAddress(items.item(i));
            if (sensAddress != INVALID_INT) {
                System.out.println("sensor a=" + sensAddress);
                lanbahnData.put( sensAddress, new LbData(0, TYPE_SENSOR));
            }
        }
        
        items = root.getElementsByTagName("trip");
        for (int i = 0; i < items.getLength(); i++) {
            Trip tr = parseTrip(items.item(i));
            if (tr != null) {
                System.out.println("trip id=" + tr.id);
                allTrips.add(tr);
            }
        }
        

    }
    // code template from lanbahnPanel

    private static Signal parseMultiAspectAddress(Node item) {

        Signal dccmap = new Signal(INVALID_INT, 0);
        int addr = INVALID_INT;
        int nBit = INVALID_INT;

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            // if (CFG_DEBUG_PARSING) Log.d(TAG,theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.getNodeName().equals("adr")) {
                addr = getIntValueOfNode(theAttribute);
            } else if (theAttribute.getNodeName().equals("nbit")) {
               nBit = getIntValueOfNode(theAttribute);
            }
        }

        if (addr != INVALID_INT) {                 
            switch (nBit) {
                case 1:
                case INVALID_INT:  // == nBit defaults to 1
                    return new Signal(addr, TYPE_SIGNAL_1BIT);
                case 2:
                   return new Signal(addr, TYPE_SIGNAL_2BIT);
                case 3:
                   return new Signal(addr, TYPE_SIGNAL_3BIT);
                default:
                   return null;
            } 
        }
        return null;
    }

    // code from lanbahnPanel
    private static int getIntValueOfNode(Node a) {
        return Integer.parseInt(a.getNodeValue());
    }
    // code from lanbahnPanel

    /* private static float getFloatValueOfNode(String s) {
        float b = Float.parseFloat(s);
        return  b;
    } */
    
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

    /** read an address 'adr' from XML node
     * 
     * @param item
     * @return 
     */
    private static int parseAddress(Node item) {
        int sensA = INVALID_INT;

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("adr")) {
                try {
                    int a = Integer.parseInt(theAttribute.getNodeValue());
                    // this is the FIRST address, the attribute can be of 
                    // type adr="1,2" with two addresses 1 (for occupation) and
                    // 2 for "in-route"
                    return a;
                } catch (NumberFormatException e) {
                    return INVALID_INT;
                }
            }
        }
        return INVALID_INT;

    }
    
    private static Trip parseTrip(Node item) {

        Trip t = new Trip();

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("id")) {
                t.id = getIntValueOfNode(theAttribute);
            } else if (theAttribute.getNodeName().equals("route")) {
                t.route = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("sens1")) {
                t.sens1 = getIntValueOfNode(theAttribute);
            } else if (theAttribute.getNodeName().equals("sens2")) {
               t.sens2 = getIntValueOfNode(theAttribute);
            } else if (theAttribute.getNodeName().equals("loco")) {
               t.locoString =theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("stopdelay")) {
               t.stopDelay = getIntValueOfNode(theAttribute);
            }
        }

        // check if Trip information is complete
        if ((t.id != INVALID_INT) && 
                (!t.route.isEmpty())&&
                ( t.sens1 != INVALID_INT) &&
                ( t.sens2 != INVALID_INT) &&
                ( t.convertLocoData() ) ) {
            // we have the minimum info needed
          
            if (t.stopDelay == INVALID_INT) t.stopDelay = 0;
            return t;
        } else {
            System.out.println("invalid trip, id=" + t.id);
            return null;
        }
    }

}
