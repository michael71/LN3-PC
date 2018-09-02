/*
 * Copyright (C) 2018 mblank
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.serialIF;
import static de.blankedv.ln3pc.MainUI.fahrplanActive;
import static de.blankedv.ln3pc.MainUI.running;
import static de.blankedv.ln3pc.Variables.allTrips;
import static de.blankedv.ln3pc.Variables.lanbahnData;
import java.awt.Color;
import java.awt.Component;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author mblank
 */
public class FahrplanUI extends javax.swing.JFrame {

    private static boolean active = false;
    private static Trip activeTrip = null;
    public static volatile int activeRow = -1;

    /**
     * Creates new form FahrplanUI
     */
    public FahrplanUI() {
        initComponents();

        setTbtnStartStopText();

        initFromTrips();

        Timer timer = new Timer();
        timer.schedule(new FahrplanTask(), 200, 250);
    }

    private void setTbtnStartStopText() {
        if (tbtnStartStop.isSelected()) {
            tbtnStartStop.setText("FP aktiv!");
            initTimeTable();
            active = true;
            activeRow = 0;
            jTable1.repaint();

        } else {
            tbtnStartStop.setText("FP nicht aktiv");
            active = false;
            activeRow = -1;
            jTable1.repaint();
        }
    }

    // start with lowest ID
    private void initTimeTable() {
        int idMin = 10000;
        activeTrip = null;
        for (Trip t : allTrips) {
            if (t.id < idMin) {
                idMin = t.id;
                activeTrip = t;
            }
        }
        if (activeTrip == null) {
            System.out.println("could not start new trip");
        } else {
            startNewTrip(activeTrip);
        }

    }

    private void startNewTrip(Trip t) {
        // check if start sensor is occupied and endsensor is free
        if ((lanbahnData.get(t.sens1).data != 1) && (lanbahnData.get(t.sens2).data == 0)) {
            System.out.println("start sensor occ and end sensor free, we can start the trip");
            // aquire locoString and start 'full' speed
            int dirBit = 0;
            if (t.locoDir == 1) {
                dirBit = 32;
            }
            int d = 25 + dirBit;
            LNUtil.aquireLoco(t.locoAddr, d);
            t.isActive = true;

        } else {
            System.out.println("start sensor free or end sensor occupied, we CANNOT start the trip");
        }

    }

    class FahrplanTask extends TimerTask {

        public void run() {
            if (active && running) {
                checkFahrplan();
            }
        }
    }

    private void checkFahrplan() {
        LbData S3 = lanbahnData.get(3);
        LbData S7 = lanbahnData.get(7);
        System.out.println("checking FP S3=" + S3.data + " S7=" + S7.data);
        if ((activeTrip != null) && (activeTrip.isActive)) {
            // check for stop
            if (lanbahnData.get(activeTrip.sens2).data != 0 ) {
                activeTrip.loco.setSpeed(0);  // stop loco
                // TODO free loco
            }
        }

    }

    private void initFromTrips() {
        int row = 0;
        for (Trip t : allTrips) {
            jTable1.setValueAt("" + t.id, row, 0);
            jTable1.setValueAt(t.route, row, 1);
            jTable1.setValueAt("S" + t.sens1, row, 2);
            jTable1.setValueAt("S" + t.sens2, row, 3);
            jTable1.setValueAt("" + t.locoString, row, 4);
            jTable1.setValueAt(t.stopDelay, row, 5);
            row++;
        }

        // set initial table column widths
        TableColumnModel columnModel = jTable1.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(40);
        columnModel.getColumn(1).setPreferredWidth(120);
        columnModel.getColumn(2).setPreferredWidth(40);
        columnModel.getColumn(3).setPreferredWidth(40);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(80);

        // center all table cell content
        DefaultTableCellRenderer tableRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground((row == activeRow) ? Color.cyan : Color.WHITE);

                return c;
            }
        };
        tableRenderer.setHorizontalAlignment(JLabel.CENTER); //Aligning the table data centrally.
        jTable1.setDefaultRenderer(Object.class, tableRenderer);

        JTableHeader Theader = jTable1.getTableHeader();
        ((DefaultTableCellRenderer) Theader.getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER); // center header text
        Theader.setBackground(Color.green); // change the Background color

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tbtnStartStop = new javax.swing.JToggleButton();
        btnPowerOff = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setTitle("Fahrplan Steuerung");

        tbtnStartStop.setText("jToggleButton1");
        tbtnStartStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbtnStartStopActionPerformed(evt);
            }
        });

        btnPowerOff.setText("POWER OFF");
        btnPowerOff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPowerOffActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "id", "route", "Start", "End", "Loco,Dir,Sp", "StopDelay"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setSelectionBackground(new java.awt.Color(171, 236, 243));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tbtnStartStop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 310, Short.MAX_VALUE)
                        .addComponent(btnPowerOff)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tbtnStartStop)
                    .addComponent(btnPowerOff))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tbtnStartStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbtnStartStopActionPerformed
        System.out.println("Fahrplan status=" + tbtnStartStop.isSelected());
        fahrplanActive = tbtnStartStop.isSelected();
        setTbtnStartStopText();
    }//GEN-LAST:event_tbtnStartStopActionPerformed

    private void btnPowerOffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPowerOffActionPerformed
        serialIF.switchPowerOff();
    }//GEN-LAST:event_btnPowerOffActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnPowerOff;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JToggleButton tbtnStartStop;
    // End of variables declaration//GEN-END:variables
}
