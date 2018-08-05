package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * hanles one session (=1 mobile device)
 */
public class SXnetSession implements Runnable {

    private static int session_counter = 0;  // class variable !

    private int sn; // session number
    private final Socket incoming;
    private PrintWriter out;

    // list of channels which are of interest for this device
    private final int[][] sxDataCopy;
    private final HashMap<Integer, Integer> lanbahnDataCopy = new HashMap<>(N_LANBAHN);
    private final int ERROR = INVALID_INT;  // ERROR kept for readability

    /**
     * Constructs a handler.
     *
     * @param sock the incoming socket
     */
    public SXnetSession(Socket sock) {
        incoming = sock;
        sxDataCopy = new int[128][2];
        sn = session_counter++;
    }

    public void run() {
        try {
            OutputStream outStream = incoming.getOutputStream();
            out = new PrintWriter(outStream, true /* autoFlush */);
            InputStream inStream = incoming.getInputStream();
            Scanner in = new Scanner(inStream);

            Timer timer = new Timer();
            timer.schedule(new Task(), 200, 50);

            sendMessage("SXnet-Server 3.0 - " + sn);  // welcome string

            while (in.hasNextLine()) {
                String msg = in.nextLine().trim().toUpperCase();
                if (msg.length() > 0) {
                    if (DEBUG) {
                        System.out.println("sxnet" + sn + " read: " + msg);
                    }
                    String[] cmds = msg.split(";");
                    for (String cmd : cmds) {
                        sendMessage(handleCommand(cmd.trim()));  // handleCommand returns "OK" or error msg
                    }

                } else {
                    // ignore empty lines
                    if (DEBUG) {
                        System.out.println("sxnet" + sn + " read empty line");
                    }
                }
                mySleep(100);

            }
            SXnetServerUI.taClients.append("client" + sn + " disconnected " + incoming.getRemoteSocketAddress().toString() + "\n");
        } catch (IOException e) {
            System.out.println("SXnetServerHandler" + sn + " Error: " + e);
        }
        try {
            incoming.close();
        } catch (IOException ex) {
            System.out.println("SXnetServerHandler" + sn + " Error: " + ex);
        }

        System.out.println("Closing SXnetserverHandler" + sn + "\n");
    }

    // handles feedback, if the sxData have been changed on the SX-Bus
    // feedback both for low (<256) addresses == SX-only (+ Lanbahn if mapping exists)
    // and for high "lanbahn" type addresses
    class Task extends TimerTask {

        public void run() {
            checkForChangedSXDataAndSendUpdates();
            checkForLanbahnChangesAndSendUpdates();
            mySleep(300);  // send update only every 300msecs
        }
    }

    /**
     * SX Net Protocol (all msg terminated with '\n')
     *
     * client sends | SXnetServer Response
     * ---------------------------------------|------------------- R cc = Read
     * channel cc (0..127) | "X" cc dd B cc b = SetBit Ch. cc Bit b (1..8) |
     * "OK" (and later, when changed in CS: X cc dd ) C cc b = Clear Ch cc Bit b
     * (1..8) | "OK" (and later, when changed in CS: X cc dd ) S cc dd = set
     * channel cc Data dd (<256)| "OK" (and later, when changed in CS: X cc dd )
     * DSDF 89sf (i.e. garbage) | "ERROR" *********** NO LONGER BIT MESSAGES
     * ************ July 2018 ********** *********** protocol 3
     * **************************************
     *
     * channel 127 bit 8 == Track Power
     *
     * for a list of channels (which the client has set or read in the past) all
     * changes are transmitted back to the client
     *
     * ,
     */
    private String handleCommand(String m) {
        String[] param = m.split("\\s+");  // remove >1 whitespace
        if (param == null) {
            return "ERROR";
        }
        if (param.length < 2) {
            System.out.println("not enough params in msg: " + m);
            return "ERROR";
        }

        switch (param[0]) {
            case "R":
                return createSXFeedbackMessage(param);
            case "READ":
                return createLanbahnFeedbackMessage(param);
            case "S":
            case "SX":
                return setSXMessage(param);
            case "SET":
            case "SL":
                return setLanbahnMessage(param);
            case "LOCO":
                return setSXMessage(param);
            default:
                return "";

        }

    }

    private String createSXFeedbackMessage(String[] par) {
        if (DEBUG) {
            System.out.println("createSXFeedbackMessage");
        }
        int adr = getSXAddrFromString(par[1]);
        if (adr == INVALID_INT) return "ERROR";
        if (DEBUG) {
            System.out.println(" adr="+adr);
        }
        if (adr <= 127) {  // SX0
            return "X " + adr + " " + sxData[adr][0];
        } else  { //SX1
            return "X " + adr + " " + sxData[adr - 128][1];
        } 
    }

    private String setSXMessage(String[] par) {
        if (par.length < 3) {
            return "ERROR";
        }
        if (DEBUG) {
            System.out.println("setSXMessage");
        }
        int adr = getSXAddrFromString(par[1]);
        int data = getByteFromString(par[2]);

        if ((adr == INVALID_INT) || (data == INVALID_INT)) return "ERROR";
        
        if (adr <= 127) {  // SX0  (bus=0)
            sxData[adr][0] = data;
            //sxi.send2SXBusses(adr, data);
            return "X " + adr + " " + sxData[adr][0];
        } else { //SX1 (bus=1)
            sxData[adr - 128][1] = data;
            //sxi.send2SXBusses(adr, data);
            return "X " + adr + " " + sxData[adr - 128][1];
        } 
    }

    private String createLanbahnFeedbackMessage(String[] par) {
        if (DEBUG) {
            //System.out.println("createLanbahnFeedbackMessage");
        }
        int lbAddr = getLanbahnAddrFromString(par[1]);
        if (lbAddr == ERROR) {
            return "ERROR";
        }

        if (isPureLanbahnAddressRange(lbAddr)) {
            if (!lanbahnData.containsKey(lbAddr)) {
                // initialize to "0" (=start simulation and init to "0")
                // if not already exists
                lanbahnData.put(lbAddr, 0);
            }
            // send lanbahnData, when already set
            return "XL " + lbAddr + " " + lanbahnData.get(lbAddr);
        }  else {
            return "ERROR";
        }
    }

    private boolean isPureLanbahnAddressRange(int a) {
        if ((a == INVALID_INT) || (a < 0)) {
            return false;
        }

        if ((a > LBMIN_LB) && (a <= LBMAX)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isLanbahnOverlapAddressRange(int a) {
        if ((a == INVALID_INT) || (a < 0) ) {
            return false;
        }
        if (a <= LBMIN_LB) {
            return true;
        } else {
            return false;
        }
    }
    
    private String setLanbahnMessage(String[] par) {
        if (DEBUG) {
           // System.out.println("setLanbahnMessage");
        }

        // convert the lanbahn "SET" message to an SX-S Message if in SX address range
        if (par.length <= 2) {
            return "ERROR";
        }
        int lbadr = getLanbahnAddrFromString(par[1]);
        int lbdata = getLanbahnDataFromString(par[2]);
        if ((lbadr == INVALID_INT) || (lbdata == INVALID_INT)) {
            return "ERROR";
        }

        // check whether we are in an SX or lanbahn address range
        if (isPureLanbahnAddressRange(lbadr)) {
            lanbahnData.put(lbadr, lbdata);  // update (or create) data    
            // send lanbahnData
            return "XL " + lbadr + " " + lanbahnData.get(lbadr);
        } else {
            // we are using SX
            int sxaddr = lbadr / 10;
            if (!isValidSXAddress(sxaddr)) {
                return "ERROR"; // for SX addresses like "120"    
            }            // depending on nBits() function, only data 0 ... 15 is allowed

            // lanbahn address to SX address mapping: divide by 10, modulo is sxbit
            // for example 987 => SX-98, bit 7 (!! bit from 1 .. 8)
            // must fit into SX channel range, maximum 1278 is allowed !!
            int sxadr = lbadr / 10;
            
            int bit = lbadr % 10;
            if ((bit < 1) || (bit > 8)) {
                return "ERROR";
            }

            return "TODO";
            /* TODO    check mapping for the following bit also
                    switch (sx.nbit) {
....
                        case 3:
                            dOut = (d >> (sx.bit - 1)) & 0x07;  // three consecutive bits
                            break;
                        case 4:
                            dOut = (d >> (sx.bit - 1)) & 0x0f;  // four consecutive bits
                            break;
 . */
        }
    }

    int nBits(int lbaddr) {
        // TODO implement
        // if a single lanbahnchannel has more than 0/1 values for example
        // for multi-aspect signals
        return 1;
    }

    /** calculate the lanbahn value from state of SX system (only controlbus)
     * at a given sxaddr = lbaddr / 10 and sxbit = lbaddr % 10
     * 
     * @param lbAddress
     * @return lbValue (or INVALID_INT)
     * 
     * (TODO implement for range of sxaddr 128 ..255 */
    
   
    


    private int getBitFromString(String s) {
        // converts String to an integer between 1 and 8 (=SX Bit)
        Integer bit = ERROR;
        try {
            bit = Integer.parseInt(s);
            if ((bit < 1) || (bit > 8)) {
                bit = ERROR;
            }
        } catch (Exception e) {
            bit = ERROR;
        }
        return bit;
    }

    private int getByteFromString(String s) {
        // converts String to integer between 0 and 255 
        //    (= range of SX Data and of Lanbahn data values)
        Integer data;
        try {
            data = Integer.parseInt(s);
            if ((data >= 0) && (data <= 255)) {  // 1 byte
                return data;
            }
        } catch (Exception e) {
            //
        }
        return ERROR;
    }

    private int getLanbahnDataFromString(String s) {
        // converts String to integer between 0 and 15
        //    (= range Lanbahn data values)
        Integer data;
        try {
            data = Integer.parseInt(s);
            if ((data >= LBDATAMIN) && (data <= LBDATAMAX)) {
                return data;
            }
        } catch (Exception e) {
            //
        }
        return ERROR;
    }

    /** extract the selectrix address from a string, only valid addresses
     * 0...111,127 and 128..139,255 are allowed, else "INVALID_INT" is returned
     * @param s
     * @return addr (or INVALID_INT)
     */
    int getSXAddrFromString(String s) {
        if (DEBUG) System.out.println("get SXAddr from " + s);
        Integer channel = ERROR;
        try {
            channel = Integer.parseInt(s);
            if (isValidSXAddress(channel)) {
                // SX channel polling einschalten, wenn nicht schon passiert
                if (!sx.getpList().contains(channel)) {
                    sx.addToPlist(channel);
                }
                return channel;
            } else {
                return ERROR;
            }
            
        } catch (Exception e) {
            System.out.println("ERROR: number conversion error input=" + s);
            return ERROR;
        }
    }
    
    /** is the address a valid SX0 or SX1 address ?
     * 
     * @param address 
     * @return true or false
     */
    private boolean isValidSXAddress(int a) {

        if (((a >= SXMIN) && (a <= SXMAX)) || (a == SXPOWER)) {
            //if (DEBUG) System.out.println("isValidSXAddress? "+a + " true (SX0");
            return true;  // 0..111 or 127
        }
        
        //if (DEBUG) System.out.println("isValidSXAddress? "+a + " false");
        return false;
    }
    
    /** parse String to extract a lanbahn address
     * 
     * @param s
     * @return lbaddr (or INVALID_INT)
     */
    int getLanbahnAddrFromString(String s) {
        //System.out.println("getLanbahnAddrFromString s=" + s);
        Integer lbAddr;
        try {
            lbAddr = Integer.parseInt(s);
            if ((lbAddr >= LBMIN) && (lbAddr <= LBMAX)) {
                return lbAddr;
                // OK, valid lanbahn channel
            } else {
                System.out.println("ERROR: lbAddr=" + lbAddr + " not valid");
                return ERROR;
            }
        } catch (Exception e) {
            System.out.println("ERROR: number conversion error input=" + s);
            return ERROR;
        }
    }

    private void sendMessage(String res) {
        out.println(res);
        out.flush();
        if (DEBUG) {
            System.out.println("sxnet" + sn + " send: " + res);
        }
    }

    private void mySleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Logger.getLogger(SXnetSession.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**  if channel data changed, send update to clients
     * 
     * @param bus (0 or 1)
     * @param sxaddr (valid sxaddr)
     */
    private void sendSXUpdates(int bus, int sxaddr) {
  
        sxDataCopy[sxaddr][bus] = sxData[sxaddr][bus];
        int chan = sxaddr + (bus * 128);
        String msg = "X " + chan + " " + sxDataCopy[sxaddr][bus];  // SX Feedback Message
        if (DEBUG) {
            System.out.println("sent: " + msg + " (bus=" + bus +")");
        }
        // check for dependent "lanbahn" feedback
        if (bus == 0) {  // only for control bus, BUS=0, i.e. sxadr <= 127
            for (int i = 1; i <= 8; i++) {
                // convert SX data to lanbahn
                int lbaddr = sxaddr * 10 + i;
                int sxvalue = sxDataCopy[sxaddr][0];
                int lbvalue = 0;
                switch (nBits(lbaddr)) {
                    case 1:
                        lbvalue = sxvalue >> (i - 1) & 0x01;   // check if bit 'i' is set
                        break;
                    case 2:
                        lbvalue = sxvalue >> (i - 1) & 0x03;   // 2 bits                   
                        break;
                    case 3:
                        lbvalue = sxvalue >> (i - 1) & 0x07;   // 3 bits                    
                        break;
                    case 4:
                        lbvalue = sxvalue >> (i - 1) & 0x0f;   // 4 bits                    
                        break;
                    default:
                        break;
                }
                msg += ";XL " + lbaddr + " " + lbvalue;  // Lanbahn Message
            }
        }
        if (DEBUG) {
            System.out.println("TL:" + msg);
        }
        sendMessage(msg);  // send all messages, separated with ";"

    }

    /**
     * check for changed sxData and send update in case of change
     */
    private void checkForChangedSXDataAndSendUpdates() {

        // power channel
        if (sxData[127][0] != sxDataCopy[127][0]) {
            sendSXUpdates(0, 127);
        }
        // other channels
        for (int bus = 0; bus < 2; bus++) {
            for (int ch = 0; ch < 112; ch++) {
                if (sxData[ch][bus] != sxDataCopy[ch][bus]) {
                    // channel data changed, send update to mobile device
                    sendSXUpdates(bus, ch);
                }
            }
        }
    }

    /**
     * check for changed (exclusiv) lanbahn data and send update in case of
     * change
     *
     */
    private void checkForLanbahnChangesAndSendUpdates() {
        StringBuilder msg = new StringBuilder();
        boolean first = true;
        for (Map.Entry<Integer, Integer> e : lanbahnData.entrySet()) {
            Integer key = e.getKey();
            Integer value = e.getValue();
            if (lanbahnDataCopy.containsKey(key)) {
                if (lanbahnDataCopy.get(key) != lanbahnData.get(key)) {
                    // value has changed
                    lanbahnDataCopy.put(key, value);
                    if (!first) {
                        msg.append(";");
                    }
                    msg.append("XL " + key + " " + value);
                    first = false;
                    if (msg.length() > 60) {
                        sendMessage(msg.toString());
                        msg.setLength(0);  // =delete content
                        first = true;
                    }
                }
            } else {
                lanbahnDataCopy.put(key, value);
            }
        }
        if (msg.length() > 0) {
            sendMessage(msg.toString());
        }
    }
}
