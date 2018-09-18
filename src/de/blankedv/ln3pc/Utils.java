/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.DEBUG;
import static de.blankedv.ln3pc.Variables.lanbahnData;
import static de.blankedv.ln3pc.Variables.TYPE_ACC_1BIT;
import static de.blankedv.ln3pc.Variables.panelElements;
import de.blankedv.timetable.PanelElement;

/**
 *
 * @author mblank
 */
public class Utils {

    /**
     * returns 1, when bit is set in data d returns 0, when bit is not set in
     * data d
     *
     * @param d
     * @param bit
     * @return
     */
    static public int isSet(int d, int bit) {
        return (d >> (bit - 1)) & 1;
    }

    static public int setBit(int d, int bit) {
        return d | (1 << (bit - 1));  // selectrix bit !!! 1 ..8
    }

    static public int clearBit(int d, int bit) {
        return d & ~(1 << (bit - 1));  // selectrix bit !!! 1 ..8
    }

    /**
     * set or clear a bit depending on "value" variable
     *
     * @param d data value
     * @param bit bit (selectrix bit, 1...8 )
     * @param value (0 or 1)
     * @return new data value
     */
    static public int bitOperation(int d, int bit, int value) {
        if (value == 0) {
            return clearBit(d, bit);
        } else {
            return setBit(d, bit);
        }
    }

    /**
     * is bit (1...8) different in d1 and d2
     *
     */
    static public boolean isSXBitChanged(int b, int d1, int d2) {
        int d1_bit = (d1 >> (b - 1)) & 1;
        int d2_bit = (d2 >> (b - 1)) & 1;
        if (d1_bit == d2_bit) {
            return false;
        } else {
            return true;
        }
    }

    static public void mySleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            System.out.println("ERROR could not sleep");
        }
    }
    
    /** put addr new value to addr lanbahn address, and keep the type unchanged
 or (if the address does not exist so far, create it new with type ACCESSORY
     * 
     * @param addr
     * @param value 
     */
    static public void updateLanbahnData(int addr, int value) {
         LbData lb = lanbahnData.get(addr);
         if (lb == null) {
             // should not happen
             if (DEBUG) System.out.println("ERROR: unknown addr="+addr);
            lb = new LbData(value, 1, "T");
         }
         lanbahnData.put(addr, new LbData(value, lb.getNBit(), lb.getTypeString()));
         for (PanelElement pe : panelElements) {
             if (pe.adr == addr) {
                 pe.state = value;
             }
         }
         if (lanbahnData.get(addr).getData() != value) {
             System.out.println("ERROR setting addr="+addr+" to val="+value+" not successful");
         }
    }

}
