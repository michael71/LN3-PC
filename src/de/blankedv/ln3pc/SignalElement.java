/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.Variables.*;


public class SignalElement extends ActivePanelElement {

    public SignalElement(int a) {
        adr = a;
        state = STATE_UNKNOWN;
    }
     
    public SignalElement() {
        adr = INVALID_INT;
        state = STATE_UNKNOWN;
    }

    public static SignalElement findSignalByAddress(int address) {
        for (PanelElement pe : panelElements) {
            if (pe instanceof SignalElement) {
                if (pe.getAdr() == address) {
                    return (SignalElement) pe;
                }
            }
        }

        return null;

    }

    @Override
    public boolean setState(int state) {
        if (state < N_STATES_SIGNALS) {
            this.state = state;
            lastUpdateTime = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }

    }

    
}
