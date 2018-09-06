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
    public boolean setState(int state) {
        if (state < N_STATES_TURNOUTS) {
            this.state = state;
            lastUpdateTime = System.currentTimeMillis();
            return true;
        } else {
            return false;
        }

    }

    public static TurnoutElement findTurnoutByAddress(int address) {
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
