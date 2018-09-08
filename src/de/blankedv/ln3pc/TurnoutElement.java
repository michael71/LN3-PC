package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.Variables.*;

public class TurnoutElement extends ActivePanelElement {

    public TurnoutElement(int a) {
        adr = a;
        state = STATE_UNKNOWN;
    } 
    
    public TurnoutElement() {
        adr = INVALID_INT;
        state = STATE_UNKNOWN;
    } 

    @Override
    public int setState(int st) {
        if (st < N_STATES_TURNOUTS) {
            state = st;
            lastUpdateTime = System.currentTimeMillis();
            return state;
        } else {
            return state;
        }

    }

    public static TurnoutElement getByAddress(int address) {
        for (PanelElement pe : panelElements) {
            if (pe instanceof TurnoutElement) {
                if (pe.getAdr() == address) {
                    return (TurnoutElement) pe;
                }
            }
        }
        return null;
    }

}
