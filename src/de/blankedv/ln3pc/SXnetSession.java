package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.*;
import static de.blankedv.ln3pc.Variables.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
    private final HashMap<Integer, LbData> lanbahnDataCopy = new HashMap<>(N_LANBAHN);
    private int globalPowerCopy = INVALID_INT;

    private byte[] lastDirFunc = {0x00, 0x00, 0x00, 0x00};

    /**
     * Constructs a handler.
     *
     * @param sock the incoming socket
     */
    public SXnetSession(Socket sock) {
        incoming = sock;
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

            while (in.hasNextLine() && running) {
                String msg = in.nextLine().trim().toUpperCase();
                if (msg.length() > 0) {
                    if (DEBUG) {
                        System.out.println("sxnet" + sn + " read: " + msg);
                    }
                    String[] cmds = msg.split(";");
                    for (String cmd : cmds) {
                        sendMessage(handleCommand(cmd.trim()));  // handleCommand returns "OK" or error msg or nothing (empty String)
                    }

                } else {
                    // ignore empty lines
                    if (DEBUG) {
                        System.out.println("sxnet" + sn + " read empty line");
                    }
                }
                mySleep(100);

            }
            sendMessage("QUIT");   // TODO does not work as expected
            mySleep(100);
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
            while (running) {
                checkForChangesAndSendUpdates();
                mySleep(300);  // send update only every 300msecs
            }
        }
    }

    /**
     * SX Net Protocol (all msg terminated with '\n')
     *
     * TODO ,
     */
    private String handleCommand(String m) {
        String[] param = m.split("\\s+");  // remove >1 whitespace
        if (param == null) {
            return "ERROR";
        }
        if (param.length < 1) {
            System.out.println("not enough params in msg: " + m);
            return "ERROR";
        }
        // commands without parameters
        switch (param[0]) {
            case "READPOWER":
                return createPowerFeedbackMessage();
        }

        if (param.length < 2) {
            System.out.println("not enough params in msg: " + m);
            return "ERROR";
        }

        switch (param[0]) {

            case "READ":
                return createLanbahnFeedbackMessage(param);
            case "SET":
            case "SL":
                return setLanbahnMessage(param);
            case "LOCO":
                return setLocoMessage(param);
            // TODO READLOCO
            default:
                return "";

        }

    }

    private String setLocoMessage(String[] par) {
        if (par.length < 3) {
            return "ERROR";
        }
        if (DEBUG) {
            System.out.println("setLocoMessage");
        }
        int adr = getDCCAddrFromString(par[1]);
        int data = getByteFromString(par[2]);
        if ((adr == INVALID_INT) || (data == INVALID_INT)) {
            return "ERROR";
        }
        // TODO check, if we have a new loco - or a Loco with an existing slot-mapping
        LocoSlot locoS = null;
        for (LocoSlot ls : locoSlots) {
            if (adr == ls.loco.getLok_adr()) {
                locoS = ls;
                break;
            }
        }

        if (locoS == null) {
            byte[] buf = LNUtil.aquireLoco(adr,data);
            if (buf != null) {
            serialIF.send(buf);
            }
            // TODO - what to do next ??
            return "";
        } else {
            locoS.loco.setFromSX(data);
            byte[] buf = LNUtil.getLNLocoSpeed(locoS);
            if (buf != null) {
                serialIF.send(buf);
            }
            buf = LNUtil.getLNLocoDirAndFunctions(locoS); // TODO
            if ((buf != null) && (!Arrays.equals(buf, lastDirFunc))) {
                serialIF.send(buf);
            }
            return "";
        }

    }

    private String createPowerFeedbackMessage() {
        StringBuilder msg = new StringBuilder();

        msg.append("XPOWER ");
        msg.append(globalPower);

        return msg.toString();
    }

    private String createLanbahnFeedbackMessage(String[] par) {
        /* if (DEBUG) {
            System.out.println("createLanbahnFeedbackMessage");
        } */
        int lbAddr = getLanbahnAddrFromString(par[1]);
        if (lbAddr == INVALID_INT) {
            return "ERROR";
        }

        if (isSimulationAddressRange(lbAddr)) {
            if (!lanbahnData.containsKey(lbAddr)) {
                // this should never happen if a proper laýout-config file is read
                // initialize to "0" (=start simulation and init to "0")
                // if not already exists
                lanbahnData.put(lbAddr, new LbData(0, TYPE_ACCESSORY));
            }
        }
        if (lanbahnData.containsKey(lbAddr)) {
            // send lanbahnData, when already set
            return "XL " + lbAddr + " " + lanbahnData.get(lbAddr);
        } else {
            // check if sensor address - 
            /*if (isSwitchAddress(lbAddr)) {
                byte[] buf = LNUtil.makeREQ_SW_STATE(lbAddr - 1);
                serialIF.send(buf);
                try {
                    Thread.sleep(20);
                } catch (InterruptedException ex) {
                    System.out.println("ERROR: " + ex.getMessage());
                }
                return "";
            } else { */
            // SHOULD NOT HAPPEN ANY MORE (i.e. correct layout file)
            System.out.println("ERROR: uninitialized address");
            return "ERROR";

        }
    }

    private boolean isSimulationAddressRange(int a) {
        if ((a == INVALID_INT) || (a < 0)) {
            return false;
        }

        if ((a > DCCMAX) && (a <= LBMAX)) {
            return true;
        } else {
            return false;
        }
    }

    private String setLanbahnMessage(String[] par) {
        if (DEBUG) {
            System.out.println("setLanbahnMessage");
        }
        if (par.length <= 2) {
            return "ERROR";
        }

        // convert the lanbahn "SET" message to a DCC Message if in DCC address range
        int lbAddr = getLanbahnAddrFromString(par[1]);
        int lbdata = getLanbahnDataFromString(par[2]);
        if ((lbAddr == INVALID_INT) || (lbdata == INVALID_INT)) {
            return "ERROR";
        }

        // check whether we are in an DCC or pure lanbahn (simulation) address range
        if (lbAddr > DCCMAX) {
            lanbahnData.put(lbAddr, new LbData(lbdata, TYPE_ACCESSORY));  // update (or create) data    
            // send lanbahnData
            return "XL " + lbAddr + " " + lanbahnData.get(lbAddr);
        } else {
            // we are in DCC address range, check if it is no sensor
            if (lanbahnData.get(lbAddr).type == TYPE_SENSOR) {
                return ""; // not allowed to set sensors
            }
            lanbahnData.put(lbAddr, new LbData(lbdata, TYPE_ACCESSORY));  // update  local data

            byte[] buf;
            switch (lbdata) {
                case 0:
                case 2: // for the time being, see above
                    // invert logic !!
                    buf = LNUtil.makeOPC_SW_REQ(lbAddr - 1, 1, 1);
                    break;
                case 1:
                case 3: // for the time being, see above
                    buf = LNUtil.makeOPC_SW_REQ(lbAddr - 1, 0, 1);
                    break;
                default:
                    System.out.println("INVALID DCC DATA=" + lbdata);
                    return "ERROR";
            }
            serialIF.send(buf);
            return "XL " + lbAddr + " " + lanbahnData.get(lbAddr);  // success
        }
    }

    int nBits(int lbaddr) {
        // TODO implement
        // if a single lanbahnchannel has more than 0/1 values for example
        // for multi-aspect signals
        return 1;
    }

    private int getByteFromString(String s) {
        // converts String to integer between 0 and 255 
        //    (= range of DCC Data and of Lanbahn data values)
        Integer data;
        try {
            data = Integer.parseInt(s);
            if ((data >= 0) && (data <= 255)) {  // 1 byte
                return data;
            }
        } catch (NumberFormatException e) {
            //
        }
        return INVALID_INT;
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
        } catch (NumberFormatException e) {
            //
        }
        return ERROR;
    }

    /**
     * extract the selectrix address from a string, only valid addresses
     * 0...111,127 and 128..139,255 are allowed, else "INVALID_INT" is returned
     *
     * @param s
     * @return addr (or INVALID_INT)
     */
    int getDCCAddrFromString(String s) {
        if (DEBUG) {
            System.out.println("INVALID_INTXAddr from " + s);
        }
        Integer channel = ERROR;
        try {
            channel = Integer.parseInt(s);
            if (isValidDCCAddress(channel)) {
                return channel;
            } else {
                return ERROR;
            }

        } catch (Exception e) {
            System.out.println("ERROR: number conversion error input=" + s);
            return ERROR;
        }
    }

    /**
     * is the address a valid DCC address ?
     *
     * @param address
     * @return true or false
     */
    private boolean isValidDCCAddress(int a) {

        if (((a >= 0) && (a <= DCCMAX))) {

            return true;
        }

        return false;
    }

    /**
     * parse String to extract a lanbahn address
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

    /**
     * if channel data changed, send update to clients
     *
     * @param bus (0 or 1)
     * @param dccAddr (valid dccAddr)
     */
    private void sendDCCUpdates(int bus, int dccAddr) {

        /* TODO  sxDataCopy[dccAddr][bus] = sxData[dccAddr][bus];
        int chan = dccAddr + (bus * 128);
        String msg = "X " + chan + " " + sxDataCopy[dccAddr][bus];  // SX Feedback Message
        if (DEBUG) {
            System.out.println("sent: " + msg + " (bus=" + bus +")");
        }
        // check for dependent "lanbahn" feedback
        if (bus == 0) {  // only for control bus, BUS=0, i.e. sxadr <= 127
            for (int i = 1; i <= 8; i++) {
                // convert SX data to lanbahn
                int lbaddr = dccAddr * 10 + i;
                int sxvalue = sxDataCopy[dccAddr][0];
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
         */
    }

    /**
     * check for changed (exclusiv) lanbahn data and send update in case of
     * change
     *
     */
    private void checkForChangesAndSendUpdates() {
        StringBuilder msg = new StringBuilder();
        boolean first = true;  // all but the first messages will start with a semicolon

        if ((globalPowerCopy != globalPower) && (globalPower != INVALID_INT)) {
            // power state has changed
            globalPowerCopy = globalPower;
            msg.append("XPOWER ");
            msg.append(globalPower);
            first = false;

        }

        for (Map.Entry<Integer, LbData> e : lanbahnData.entrySet()) {
            Integer key = e.getKey();
            LbData lbd = e.getValue();
            Integer value = lbd.data;
            if (!lanbahnDataCopy.containsKey(key) || (!Objects.equals(lanbahnDataCopy.get(key), lanbahnData.get(key)))) {  // null-safe '=='
                // value is new or has changed
                lanbahnDataCopy.put(key, lbd);
                if (!first) {
                    msg.append(";");
                }
                msg.append("XL ").append(key).append(" ").append(value);
                first = false;
                if (msg.length() > 60) {
                    sendMessage(msg.toString());
                    msg.setLength(0);  // =delete content
                    first = true;
                }

            }
        }
        if (msg.length() > 0) {
            sendMessage(msg.toString());
        }
    }
}
