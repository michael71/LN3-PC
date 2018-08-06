/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import java.util.prefs.Preferences;
import java.util.List;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import static de.blankedv.ln3pc.MainUI.*;
import java.awt.Color;

/**
 *
 * @author mblank
 */
public class FunkreglerUI extends javax.swing.JFrame {

    public static List<FunkreglerUI> fu = new ArrayList<FunkreglerUI>();  //Liste, damit alle Fenster die Updates bekommen
    static int funkreglerUIInstances = 0;

    private int myInstance;  // zählt Fenster hoch (und runter), verwendet für Prefs
    Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    private String name = "";
    private long lastUpdate;
    private  ImageIcon green, yellow, liteyellow;
    private static final int GREEN = 1;
    private static final int YELLOW = 0;
    private String ipaddress ="";

    public static void checkAlive() {
        for (FunkreglerUI f : fu) {
            if ((System.currentTimeMillis() - f.lastUpdate) > 30000) {
                if (DEBUG) {
                    System.out.println("lost Funkregler with name=" + f.name);
                }
                f.savePrefs();
                funkreglerUIInstances--;
                f.setVisible(false);
            };
        }
    }
    
    public static void updateAll() {
        for (FunkreglerUI f : fu) {
            if ((System.currentTimeMillis() - f.lastUpdate) > 10000) {
                f.setAliveIcon(YELLOW);
            } else {
                f.setAliveIcon(GREEN);
            };
        }
    }

    public static void saveAllPrefs() {
        for (FunkreglerUI f : fu) {
            f.savePrefs();
        }
    }

    public String getName() {
        return name;
    }
    
    public String getIP() {
        return ipaddress;
    }

    public static void setAliveByIP(String ip) {
        for (FunkreglerUI f : fu) {
            if (f.getIP().equalsIgnoreCase(ip)) {
                f.setAlive();
            }
        }
    }
    
    public void setAlive() {
        lastUpdate = System.currentTimeMillis();
        setAliveIcon(1);
    }
    public static void updateByName(String n, String[] cmd) {
        if (cmd.length < 4) {
            return; // not enough data
        }
        for (FunkreglerUI f : fu) {
            if (f.getName().equalsIgnoreCase(n)) {
                if (!f.isVisible()) {
                    f.setVisible(true);
                }
                f.update(cmd);
            }
        }

    }

    public static boolean isKnown(String n) {
        for (FunkreglerUI f : fu) {
            if (f.getName().equalsIgnoreCase(n)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates new form FunkreglerUI
     */
    public FunkreglerUI(String name, String[] cmd) {
        initComponents();
        fu.add(this);
        this.name = name;
        myInstance = funkreglerUIInstances++;
        lastUpdate = System.currentTimeMillis();
        loadPrefs(); //myInstance is used here.
        //this.setTitle("Sensor"+ "  [SX"+sxbusControl+"]");
        green = new javax.swing.ImageIcon(getClass().getResource("/de/blankedv/ln3pc/icons/greendot.png"));
        yellow = new javax.swing.ImageIcon(getClass().getResource("/de/blankedv/ln3pc/icons/yellowdot.png"));
        liteyellow = new javax.swing.ImageIcon(getClass().getResource("/de/blankedv/ln3pc/icons/liteyellowdot.png"));
        this.setVisible(true);
        this.update(cmd);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        progBattery = new javax.swing.JProgressBar();
        progRSSI = new javax.swing.JProgressBar();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        lblRSSI = new javax.swing.JLabel();
        lblBatt = new javax.swing.JLabel();
        lblSWRev = new javax.swing.JLabel();
        lblDeviceInfo = new javax.swing.JLabel();
        statusIcon = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Funkregler");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jLabel1.setText("Device");

        jLabel2.setText("Battery");

        progBattery.setMaximum(4400);
        progBattery.setMinimum(3300);

        progRSSI.setMaximum(-40);
        progRSSI.setMinimum(-85);

        jLabel3.setText("RSSI");

        jLabel4.setText("Revision");

        lblRSSI.setText("-");

        lblBatt.setText("-");

        lblSWRev.setText("-");

        lblDeviceInfo.setText("-");

        statusIcon.setText("-");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(lblDeviceInfo, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addGap(18, 18, 18)
                                .addComponent(lblSWRev, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(progRSSI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(progBattery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblRSSI)
                            .addComponent(lblBatt)
                            .addComponent(statusIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblDeviceInfo))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(progBattery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(15, 15, 15)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progRSSI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)
                            .addComponent(lblRSSI))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(lblSWRev)
                            .addComponent(statusIcon))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(lblBatt)
                        .addContainerGap(93, Short.MAX_VALUE))))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        savePrefs();
        funkreglerUIInstances--;
        fu.remove(this);
    }//GEN-LAST:event_formWindowClosing

    
    private void update(String[] cmd) {
        try {
            ipaddress = cmd[2];
            int batt = Integer.parseInt(cmd[3]);
            int rssi = Integer.parseInt(cmd[4]);

            lastUpdate = System.currentTimeMillis();
            lblDeviceInfo.setText(name);
            setBatteryDisplay(progBattery, batt);

            lblBatt.setText("" + batt + " mV");
            setRSSIDisplay(progRSSI, rssi);
            lblRSSI.setText("" + rssi + " dB");
            String swRevision = cmd[5];
            lblSWRev.setText(swRevision);

        } catch (Exception e) {
            System.out.println("lanbahn device info incomplete");
        }

    }
    
   
    
    private void setAliveIcon(int i) {
        if (i == 0) {
            statusIcon.setIcon(yellow);
        } else {
            statusIcon.setIcon(green);
        }
    }

    private void setBatteryDisplay(javax.swing.JProgressBar p, int batt) {
        p.setValue(batt);
        if (batt < 3700) {
            p.setForeground(Color.red);
        } else if (batt < 3900) {
            p.setForeground(Color.yellow);
        } else {
            p.setForeground(Color.green);
        }
    }

    private void setRSSIDisplay(javax.swing.JProgressBar p, int rssi) {
        p.setValue(rssi);
        if (rssi < -80) {
            p.setForeground(Color.red);
        } else if (rssi < -72) {
            p.setForeground(Color.yellow);
        } else {
            p.setForeground(Color.green);
        }
    }

    private void savePrefs() {
        String myInst = "FU" + myInstance;

        prefs.putInt(myInst + "windowX", getX());
        prefs.putInt(myInst + "windowY", getY());
    }

    private void loadPrefs() {
        // reload the positions for the right instance
        String myInst = "FU" + myInstance;
        if (DEBUG) {
            System.out.println("loading Prefs for:" + myInst);
        }

        setLocation(prefs.getInt(myInst + "windowX", 200), prefs.getInt(myInst + "windowY", 200));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel lblBatt;
    private javax.swing.JLabel lblDeviceInfo;
    private javax.swing.JLabel lblRSSI;
    private javax.swing.JLabel lblSWRev;
    private javax.swing.JProgressBar progBattery;
    private javax.swing.JProgressBar progRSSI;
    private javax.swing.JLabel statusIcon;
    // End of variables declaration//GEN-END:variables
}