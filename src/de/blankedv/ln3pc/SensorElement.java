/*
 *  sensor state
 *     bit0, mapped to occupied ( cleared = free)
 *     bit1, mapped to "ausleuchtung" (cleared = not in a route)
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.Variables.*;

public class SensorElement extends ActivePanelElement {

    public SensorElement(int adr) {
        this.adr = adr;
        this.secondaryAdr = INVALID_INT;
        state = 0;
    }

    public SensorElement() {
        state = 0;
    }

    SensorElement(int adr, int adr2) {
        this.adr = adr;
        this.secondaryAdr = adr2;
        state = 0;
    }
    
    public int setOccupied(boolean occ) {
        lastUpdateTime = System.currentTimeMillis();
        if (occ) {
            // set bit 0
            state |= 0x01;
        } else {
            state &= ~(0x01);
        }
        return state;
    }
    
    public int setInRoute(boolean occ) {
        lastUpdateTime = System.currentTimeMillis();
        if (occ) {
            // set bit 0
            state |= 0x02;
        } else {
            state &= ~(0x02);
        }
        return state;
    }
     
    public boolean isOccupied() {
        if ((state & 0x01) != 0) {
            return true;
        } else {
            return false;
        }
    }
     public boolean isInRoute() {
        if ((state & 0x02) != 0) {
            return true;
        } else {
            return false;
        }
    }

    // sensors can have 2 different pair of states
    // 1. pair (bit0) FREE or OCCUPIED
    // 2. pair (bit1) NOT_INROUTE or INROUTE
    @Override
    public int setState(int state) {
        //do nothing
        System.out.println("ERROR: setState() cannot be used for sensors");
        return state;

    }
     
    public static SensorElement getByAddress(int a) {
        for (PanelElement se : panelElements) {
            if ((se instanceof SensorElement) && (se.adr == a)) {
                return (SensorElement) se;
            }
        }
        return null;  // not found
    }

}
