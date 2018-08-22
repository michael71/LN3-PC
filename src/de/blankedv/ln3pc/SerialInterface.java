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
import static de.blankedv.ln3pc.Variables.*;
import static de.blankedv.ln3pc.MainUI.*;
import purejavacomm.*;

/**
 *
 * @author mblank
 */
public class SerialInterface {

    private String portName;

    private int baudrate;

    CommPortIdentifier serialPortId;
    Enumeration enumComm;
    SerialPort serialPort;
    OutputStream outputStream;
    InputStream inputStream;

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

    public String getPortName() {
        return portName;
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
            serialPort.setSerialPortParams(baudrate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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
            System.out.println("Fehler beim Senden, serial port nicht geöffnet und Simulation nicht gesetzt");
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
            System.out.println("Fehler beim Senden: " + e.getMessage());
        }
    }

    public synchronized void switchPowerOff() {
        // loconet global power off

        Byte[] b = {(byte) 0x82, (byte) 0x7D};
        try {
            outputStream.write(b[0]);
            outputStream.write(b[1]);
            outputStream.flush();
            globalPower = POWER_OFF;
        } catch (IOException e) {
            System.out.println("Fehler beim Senden: " + e.getMessage());
        }

    }

    public synchronized void switchPowerOn() {
        // loconet global power on   

        Byte[] b = {(byte) 0x83, (byte) 0x7C};
        try {
            outputStream.write(b[0]);
            outputStream.write(b[1]);
            outputStream.flush();
            globalPower = POWER_ON;
        } catch (IOException e) {
            System.out.println("Fehler beim Senden: " + e.getMessage());
        }
    }

    public int readPower() {
        return globalPower;
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
                
                boolean completeCMD = LNUtil.interpret(buf, count);
                if (completeCMD && DEBUG) {
                    System.out.print("serial read: ");
                    for (int i = 0; i < count; i++) {
                        System.out.printf("%02X ", buf[i]);
                    }
                    System.out.println();
                }
            }

        } catch (IOException e) {
            System.out.println("Fehler beim Lesen empfangener Daten: " + e.getMessage());
        }

    }

}
