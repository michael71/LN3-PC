/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    
    public boolean isOccupied() {
        if ((state &= 0x01) == STATE_OCCUPIED) {
            return true;
        } else {
            return false;
        }
    }
     public boolean isInRoute() {
        if ((state &= 0x02) == STATE_INROUTE) {
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
        switch (state) {
            case SENSOR_FREE:
                state &= ~0x01;   // clear bit0
                break;
            case SENSOR_OCCUPIED:
                state |= 0x01;   // set bit0
                break;
            case SENSOR_NOT_INROUTE:
                state &= ~0x02;   // clear bit1
                break;
            case SENSOR_INROUTE:
                state |= 0x02;   // set bit1
                break;
            default:
                return INVALID_INT;

        }
        System.out.println("setState sensor=" + adr + " val=" + state);
        lastUpdateTime = System.currentTimeMillis();
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
