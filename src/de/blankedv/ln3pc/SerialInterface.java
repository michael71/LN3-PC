/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingWorker;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import static de.blankedv.ln3pc.MainUI.*;   // DAS SX interface.

/**
 *
 * @author mblank
 */
public class SerialInterface {

    private boolean noPollingFlag;
    private String portName;

    private int baudrate;
    private int dataBits = SerialPort.DATABITS_8;
    private int stopBits = SerialPort.STOPBITS_1;
    private int parity = SerialPort.PARITY_NONE;
    CommPortIdentifier serialPortId;
    Enumeration enumComm;
    SerialPort serialPort;
    OutputStream outputStream;
    InputStream inputStream;

    private List<Integer> pListCopy;

    private boolean sx1Flag = false;
    private int lastBusnumber = 0;

    private static int leftover;
    private static boolean leftoverFlag = false;
    private static long lastReceived = 0;
    Boolean regFeedback = false;
    int regFeedbackAdr = 0;
    boolean connected = false;

    public SerialInterface(String portName, int baud) {

        this.portName = portName;
        this.baudrate = baud;
    }

    public void setPort(String port) {
        portName = port;
    }

    public boolean open() {

        Boolean foundPort = false;
        if (connected != false) {
            System.out.println("Serialport bereits geöffnet");
            return false;
        }
        System.out.println("Öffne Serialport " + portName);
        enumComm = CommPortIdentifier.getPortIdentifiers();
        while (enumComm.hasMoreElements()) {
            serialPortId = (CommPortIdentifier) enumComm.nextElement();
            //System.out.println("port: "+serialPortId.getName());
            if (portName.contentEquals(serialPortId.getName())) {
                foundPort = true;
                break;
            }
        }
        if (foundPort != true) {
            System.out.println("Serialport nicht gefunden: " + portName);
            return false;
        }
        try {
            serialPort = (SerialPort) serialPortId.open("Öffnen und Senden", 500);
        } catch (PortInUseException e) {
            System.out.println("Port belegt");
        }
        try {
            outputStream = serialPort.getOutputStream();
        } catch (IOException e) {
            System.out.println("Keinen Zugriff auf OutputStream");
        }

        try {
            leftoverFlag = false;
            inputStream = serialPort.getInputStream();
        } catch (IOException e) {
            System.out.println("Keinen Zugriff auf InputStream");
        }
        try {
            serialPort.addEventListener(new serialPortEventListener());
        } catch (Exception e) {
            System.out.println("TooManyListenersException für Serialport");
        }
        serialPort.notifyOnDataAvailable(true);

        try {
            serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
        } catch (UnsupportedCommOperationException e) {
            System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
        }

        connected = true;

        return true;
    }

    public void close() {

        if (connected == true) {
            System.out.println("Schließe Serialport");
            serialPort.close();
        } else {
            System.out.println("Serialport bereits geschlossen");
        }
        //connected = false;
    }

    public synchronized void send(Byte[] data, int busnumber) {
        // darf nicht unterbrochen werden
        // TODO check if switch from SX0 to Sx1 is necessary or vice versa
        // ************************************** using 0 only
        return; //
/*    if (connected != true) {
            System.out.println("Fehler beim Senden, serial port nicht geöffnet und simul. nicht gesetzt");
            return;
        }

        // falls im Trix Mode und schreib befehl, dann daten in sxData speichern
        // TODO ??? if ((value & (1L << x)) != 0)
        if ((!noPollingFlag) && ((data[0] & 0x80) != 0)) {
            sxData[data[0] & 0x7f][busnumber] = data[1];
        }
        if ((data[0] & 0x80) != 0) {
            System.out.println("wr-Cmd: adr " + (toUnsignedInt(data[0]) & 0x7f) + " / data " + toUnsignedInt(data[1]));
        } else {
            System.out.println("rd-Cmd: adr " + (toUnsignedInt(data[0]) & 0x7f));
        }

        try {
            //if (fccMode) {
            //    outputStream.write((byte)busnumber);
            //}
            outputStream.write(data[0]);
            outputStream.write(data[1]);
            outputStream.flush();
            // done via polling of sx data in LanbahnUI / this.doLanbahnUpdate((byte)(data[0] & 0x7f), data[1]);
        } catch (IOException e) {
            System.out.println("Fehler beim Senden");
        }
         */
    }

    public synchronized void switchPowerOff() {
        // 127 (ZE ein/aus) +128(schreiben) = 0xFF

        Byte[] b = {(byte) 0xFF, (byte) 0x00};
        try {
            outputStream.write(b[0]);
            outputStream.write(b[1]);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Fehler beim Senden");
        }

    }

    public synchronized void switchPowerOn() {
        // 127 (ZE ein/aus) +128(schreiben) = 0xFF   

        Byte[] b = {(byte) 0xFF, (byte) 0x80};
        try {
            outputStream.write(b[0]);
            outputStream.write(b[1]);
            outputStream.flush();
        } catch (IOException e) {
            System.out.println("Fehler beim Senden");
        }
    }

    public void readPower() {
        Byte[] b = {(byte) 127, (byte) 0x00};   // read power state
        send(b, 0);
    }

    public boolean isConnected() {
        return connected;  //TODO
    }

    class serialPortEventListener implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            switch (event.getEventType()) {
                case SerialPortEvent.DATA_AVAILABLE:
                    connectionOK = true;

                    readSerialPort();

                    break;
                case SerialPortEvent.BI:
                case SerialPortEvent.CD:
                case SerialPortEvent.CTS:
                case SerialPortEvent.DSR:
                case SerialPortEvent.FE:
                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                case SerialPortEvent.PE:
                case SerialPortEvent.RI:
                default:
            }
        }
    }

    String doUpdate() {
        return ""; //TODO
    }

    void readSerialPort() {

        try {
            int adr, data;

            int ch;
            String s = "";
            while ((ch = inputStream.read()) != -1) {
                s = String.format("%02X ", ch);
                if ((ch & 0x80) != 0) {
                    // = Opcode, start new line
                    System.out.println();
                }
                System.out.print(s);

            }

        } catch (IOException e) {
            System.out.println("Fehler beim Lesen empfangener Daten");
        }

    }

    // address range 0 ..127 / 128 ... 255 
    private synchronized void setSX(int adr, int data) {
        if (adr >= 0 && adr < N_SX) {
            // data for SX0 bus
            sxData[adr][0] = data;
            if (DEBUG) {
                //System.out.println("set: SX0[" + adr + "]=" + data + " ");
            }
        } else if (adr >= N_SX && adr < (2 * N_SX)) {
            // data for SX1 bus
            sxData[adr - 128][1] = data;

            if (DEBUG) {
                //System.out.println("set: SX1[" + (adr - SXMAX2) + "]=" + data + " ");
            }
        } else {
            System.out.println("set: ERROR adr=" + adr + " to high");
        }
    }

    public static int toUnsignedInt(byte value) {
        return (value & 0x7F) + (value < 0 ? 128 : 0);
    }

}
