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
import static de.blankedv.ln3pc.MainUI.DEBUG;
import static de.blankedv.ln3pc.Variables.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

            allRoutes = parseRoutes(doc); // can be done only after all panel
            // elements have been read
            Route.calcOffendingRoutes(); // calculate offending allRoutes
            allCompRoutes = parseCompRoutes(doc); // can be done only after allRoutes
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
//<signal x="290" y="100" x2="298" y2="100" adr="76s3" nbit="2" />   
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
            addPanelElement("T", items.item(i));
        }

        items = root.getElementsByTagName("signal");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " signals");
        }

        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement("Si", items.item(i));
        }

        items = root.getElementsByTagName("sensor");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " sensors");
        }
        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement("BM", items.item(i));
        }

        items = root.getElementsByTagName("doubleslip");
        if (CFG_DEBUG) {
            System.out.println("config: " + items.getLength() + " doubleslips");
        }
        for (int i = 0; i < items.getLength(); i++) {
            addPanelElement("DS", items.item(i));
        }

        items = root.getElementsByTagName("trip");
        for (int i = 0; i < items.getLength(); i++) {
            Trip tr = parseTrip(items.item(i));
            if (tr != null) {
                System.out.println("trip id=" + tr.id);
                allTrips.add(tr);
            }
        }

        items = root.getElementsByTagName("timetable");
        for (int i = 0; i < items.getLength(); i++) {
            Timetable ti = parseTimetable(items.item(i));
            if (ti != null) {
                System.out.println("timetable id=" + ti.id);
                allTimetables.add(ti);
            }
        }

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
    private static ArrayList<Integer> parseAddressArray(Node a) {
        NamedNodeMap attributes = a.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("adr")) {
                String s = theAttribute.getNodeValue();
                s = s.replace(".", "");
                //s = s.replace("\\s+", "");
                String[] sArr = s.split(",");
                ArrayList<Integer> iArr = new ArrayList<>();

                for (String s2 : sArr) {
                    int addr = INVALID_INT;
                    try {
                        addr = Integer.parseInt(s2);
                    } catch (NumberFormatException ex) {
                    }
                    iArr.add(addr);
                }
                return iArr;

            }
        }
        return null;
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
                t.locoString = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("stopdelay")) {
                t.stopDelay = getIntValueOfNode(theAttribute);
            }
        }

        // check if Trip information is complete
        if ((t.id != INVALID_INT)
                && (!t.route.isEmpty())
                && (t.sens1 != INVALID_INT)
                && (t.sens2 != INVALID_INT)
                && (t.convertLocoData())) {
            // we have the minimum info needed

            if (t.stopDelay == INVALID_INT) {
                t.stopDelay = 0;
            }
            return t;
        } else {
            System.out.println("invalid trip, id=" + t.id);
            return null;
        }
    }

    private static Timetable parseTimetable(Node item) {

        int id = INVALID_INT;
        String sTime = "";
        String sTrip = "";
        String sNext = "";

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            if (theAttribute.getNodeName().equals("id")) {
                id = getIntValueOfNode(theAttribute);
            } else if (theAttribute.getNodeName().equals("time")) {
                sTime = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("trip")) {
                sTrip = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("next")) {
                sNext = theAttribute.getNodeValue();
            }
        }

        // check if Trip information is complete
        if ((id != INVALID_INT)
                && (!sTime.isEmpty())
                && (!sTrip.isEmpty())) {
            // we have the minimum info needed

            return new Timetable(id, sTime, sTrip, sNext);
        } else {
            System.out.println("invalid Timetable, id=" + id);
            return null;
        }
    }

    private static ArrayList<Route> parseRoutes(Document doc) {
        // assemble new ArrayList of tickets.
        ArrayList<Route> myroutes = new ArrayList<>();
        NodeList items;
        Element root = doc.getDocumentElement();

        // items = root.getElementsByTagName("panel");
        // look for allRoutes - this is the lowest layer
        items = root.getElementsByTagName("route");
        if (DEBUG) {
            System.out.println("config: " + items.getLength() + " routes");
        }
        for (int i = 0; i < items.getLength(); i++) {
            Route rt = parseRoute(items.item(i));
            if (rt != null) {
                myroutes.add(rt);
            }
        }

        return myroutes;
    }

    private static Route parseRoute(Node item) {
        // ticket node can be Incident oder UserRequest
        int id = INVALID_INT;
        int btn1 = INVALID_INT;
        int btn2 = INVALID_INT;
        String route = null, sensors = null;
        String offending = ""; // not mandatory

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            // if (DEBUG_PARSING) System.out.println(TAG+theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.getNodeName().equals("id")) {
                id = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("route")) {
                route = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("sensors")) {
                sensors = theAttribute.getNodeValue();
            } else if (theAttribute.getNodeName().equals("offending")) {
                offending = theAttribute.getNodeValue();
            }
        }

        // check for mandatory and valid input data
        if (id == INVALID_INT) {
            // missing info, log error
            System.out.println("missing id= info in route definition");
            return null;
        } else if (route == null) {
            System.out.println("missing route= info in route definition");
            return null;
        } else if (sensors == null) {
            System.out.println("missing sensors= info in route definition");
            return null;
        } else {
            // everything is o.k.

            return new Route(id, route, sensors, offending);
        }

    }

    private static ArrayList<CompRoute> parseCompRoutes(Document doc) {
        // assemble new ArrayList of tickets.
        ArrayList<CompRoute> myroutes = new ArrayList<>();
        NodeList items;
        Element root = doc.getDocumentElement();

        // look for comp allRoutes - this is the lowest layer
        items = root.getElementsByTagName("comproute");
        if (DEBUG) {
            System.out.println("config: " + items.getLength() + " comproutes");
        }
        for (int i = 0; i < items.getLength(); i++) {
            CompRoute rt = parseCompRoute(items.item(i));
            if (rt != null) {
                myroutes.add(rt);
            }
        }

        return myroutes;
    }

    private static CompRoute parseCompRoute(Node item) {
        //
        int id = INVALID_INT;
        int btn1 = INVALID_INT;
        int btn2 = INVALID_INT;
        String routes = null;

        NamedNodeMap attributes = item.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            Node theAttribute = attributes.item(i);
            // if (DEBUG_PARSING) System.out.println(TAG+theAttribute.getNodeName() + "=" +
            // theAttribute.getNodeValue());
            if (theAttribute.getNodeName().equals("id")) {
                id = Integer.parseInt(theAttribute.getNodeValue());
            } else if (theAttribute.getNodeName().equals("routes")) {
                routes = theAttribute.getNodeValue();
            }
        }

        // check for mandatory and valid input data
        if (id == INVALID_INT) {
            // missing info, log error
            System.out.println("missing id= info in route definition");
            return null;
        } else if (routes == null) {
            System.out.println("missing routes= info in route definition");
            return null;
        } else {
            // everything is o.k.

            return new CompRoute(id, routes);
        }

    }

    private static void addPanelElement(String type, Node a) {
        ArrayList<Integer> addressArr = parseAddressArray(a);
        if (addressArr != null) {
            if ((addressArr.size() >= 1) && (addressArr.get(0) != INVALID_INT)) {

                if ((addressArr.size() >= 2) && (addressArr.get(1) != INVALID_INT)) {
                    // check if we have 2 doubleslip addresses
                    System.out.println(type + " adr=" + addressArr.get(0) + " sec-adr=" + addressArr.get(1));
                    lanbahnData.put(addressArr.get(0), new LbData(0, 2, type));
                    panelElements.add(new PanelElement(type, addressArr.get(0), addressArr.get(1)));  // 
                } else {
                    System.out.println(type + " adr=" + addressArr.get(0) + " no sec-adr.");
                    lanbahnData.put(addressArr.get(0), new LbData(0, 1, type));
                    panelElements.add(new PanelElement(type, addressArr.get(0)));
                }

            }
        }
    }

}
