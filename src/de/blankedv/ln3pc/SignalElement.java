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
    
    public SignalElement(int a, int a2) {
        adr = a;
        secondaryAdr = a2;
        state = STATE_UNKNOWN;
    }
     
    public SignalElement() {
        adr = INVALID_INT;
        secondaryAdr = INVALID_INT;
        state = STATE_UNKNOWN;
    }

    public static SignalElement getByAddress(int address) {
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
    public int setState(int st) {
        if (st < N_STATES_SIGNALS) {
            state = st;
            lastUpdateTime = System.currentTimeMillis();
            return state;
        } else {
            return state;
        }

    }

    
}
