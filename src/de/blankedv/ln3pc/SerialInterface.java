/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import static de.blankedv.ln3pc.MainUI.*;   // DAS SX interface.
import purejavacomm.*;
import static jtermios.JTermios.*;

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

    private final int NBUF = 40;
    private byte[] buf = new byte[NBUF];
    private int count = 0;

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

    public synchronized void close() {

        if ((serialPort != null) && (connected == true)) {
            serialPort.removeEventListener();
            try {
                inputStream.close();
                outputStream.close();
            } catch (IOException ex) {
                System.out.println("ERROR " + ex.getMessage());
            }
            serialPort.close();
            System.out.println("Serialport closed.");

        } else {
            System.out.println("Serialport bereits geschlossen");
        }
        connected = false;
    }

    /**
     * send some bytes to loconet add checksum
     */
    public synchronized void send(byte[] buf) {

        int count = LNUtil.getLength(buf);
        // darf nicht unterbrochen werden

        if (connected != true) {
            System.out.println("Fehler beim Senden, serial port nicht geöffnet und simul. nicht gesetzt");
            return;
        }

        try {
            for (int i = 0; i < count; i++) {
                outputStream.write(buf[i]);
            }

            outputStream.flush();
            if (DEBUG) {
                System.out.println("serial sent: " + LNUtil.bufToString(buf));
            }
            // done via polling of mainui data in LanbahnUI / this.doLanbahnUpdate((byte)(data[0] & 0x7f), data[1]);
        } catch (IOException e) {
            System.out.println("Fehler beim Senden");
        }
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
        //send(b, 0); TODO - there is no readPower in Loconet, only set/unset
    }

    public boolean isConnected() {
        return connected;  //TODO
    }

    class serialPortEventListener implements SerialPortEventListener {

        @Override
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
            int ch;
            while ((ch = inputStream.read()) != -1) {

                if (((ch & 0x80) != 0) || (count >= NBUF)) {
                    count = 0;
                    buf[count] = (byte) ch;
                    count++;
                } else {
                    buf[count] = (byte) ch;
                    count++;
                }
                LNUtil.interpret(buf, count);
            }

        } catch (IOException e) {
            System.out.println("Fehler beim Lesen empfangener Daten");
        }

    }

    public static int toUnsignedInt(byte value) {
        return (value & 0x7F) + (value < 0 ? 128 : 0);
    }

}
