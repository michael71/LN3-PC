/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.*;
import static de.blankedv.ln3pc.Variables.lanbahnData;
import java.awt.Color;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 *
 * @author mblank
 */
public class LanbahnMonitorUI extends javax.swing.JFrame {

    private static final long serialVersionUID = 5313156456415L;
    static final int ROWS = 16;
    static final int COLS = 10; // *2

    private Map<Integer, Integer> lbCopy, oldLbCopy;

    Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    /**
     * Creates new form LanbahnMonitorUI
     */
    public LanbahnMonitorUI() {
        initComponents();

        this.setTitle("Virtual Channels Monitor");
        Color c1 = new Color(140, 140, 255);
        Container con1 = this.getContentPane();
        con1.setBackground(c1);

        loadPrefs();
        initTable1();  // mit leer strings initialisieren
        lbCopy = new HashMap<>();
        oldLbCopy = new HashMap<>();
        update(); // from Lanbahn data
        this.setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setTitle("Lanbahn Monitor");
        setBackground(new java.awt.Color(240, 241, 242));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTable1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jTable1.setFont(jTable1.getFont().deriveFont(jTable1.getFont().getSize()-2f));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "LB-A", "Value", "LB-A", "Value", "LB-A", "Value", "LB-A", "Value", "LB-A", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setRowSelectionAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 690, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        savePrefs();
        lbmon = null;  // to enable opening a MonitorUI window again in Interface UI
    }//GEN-LAST:event_formWindowClosing

    public void update() {

        lbCopy = new HashMap<>(lanbahnData);  // make a copy of the current data
        // the lanbahnData hashmap only can grow, no values will be deleted
        // initTable() => not necessary

        ArrayList<Integer> keys = new ArrayList<>(lbCopy.keySet());
        Collections.sort(keys);
        Iterator it = keys.iterator();

        for (int i = 0; i < COLS - 1; i = i + 2) {
            for (int j = 0; j < ROWS; j++) {
                if (it.hasNext()) {
                    Integer key = (Integer) it.next();
                    //System.out.println("LBMon: "+key + " "+lbCopy.get(key));
                    // it.remove(); // avoids a ConcurrentModificationException
                    jTable1.setValueAt(key, j, i);
                    StringBuffer s;
                    int value = lbCopy.get(key);
                    
                    // display in different color (Red) when value has changed
                    // after the last call of update()
                    if (!Objects.equals(lbCopy.get(key), oldLbCopy.get(key))) {
                        s = new StringBuffer("<html><p bgcolor='#FF8800'>" + value + "</p></html>");
                    } else {
                        s = new StringBuffer("<html><p bgcolor='#FFFF00'>" + value + "</p></html>");
                    }
                    jTable1.setValueAt(s.toString(), j, i + 1);
                }
            }
        }

        oldLbCopy = lbCopy;  // save the data for later "hasChanged" detection
    }

    private void initTable1() {
        int count = 0;
        // set adresses
        for (int i = 0; i < COLS; i = i + 2) {
            for (int j = 0; j < ROWS; j++) {
                jTable1.setValueAt(" ", j, i);
                //oldSxData[count] = 0;
                count++;
            }
        }
    }

    /**
     * saves window position
     */
    public void savePrefs() {
        prefs.putInt("lbmonitorwindowX", getX());
        prefs.putInt("lbmonitorwindowY", getY());
    }

    /**
     * loads last window position
     */
    private void loadPrefs() {
        // reload the positions for the right instance
        setLocation(prefs.getInt("lbmonitorwindowX", 200), prefs.getInt("lbmonitorwindowY", 200));

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
