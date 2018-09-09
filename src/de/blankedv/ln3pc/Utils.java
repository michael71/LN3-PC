/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.Variables.TYPE_ACCESSORY;
import static de.blankedv.ln3pc.Variables.lanbahnData;

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

    static void mySleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            System.out.println("ERROR could not sleep");
        }
    }
    
    /** put a new value to a lanbahn address, and keep the type unchanged
     * or (if the address does not exist so far, create it new with type ACCESSORY
     * 
     * @param a
     * @param v 
     */
    static void updateLanbahnData(int a, int v) {
         LbData lb = lanbahnData.get(a);
         if (lb == null) {
            lb = new LbData(v, TYPE_ACCESSORY);
         }
         lanbahnData.put(a, new LbData(v, lb.getType()));
         if (lanbahnData.get(a).getData() != v) {
             System.out.println("ERROR setting addr="+a+" to val="+v+" not successful");
         }
    }

}