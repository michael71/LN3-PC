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
        state = 0;
    }

    public SensorElement() {
        state = 0;
    }

    // sensors can have 2 different pair of states
    // 1. pair (bit0) FREE or OCCUPIED
    // 2. pair (bit1) NOT_INROUTE or INROUTE
    @Override
    public boolean setState(int state) {
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
                return false;

        }

            lastUpdateTime = System.currentTimeMillis();
            return true;


    }

}
