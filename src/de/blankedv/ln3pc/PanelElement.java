package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.*;
import static de.blankedv.ln3pc.Variables.INVALID_INT;
import static de.blankedv.ln3pc.Variables.panelElements;
import java.awt.Graphics2D;
import java.awt.Point;

/**
 * generic panel element - this can be a passive (never changing) panel element
 * or an active (lanbahn status dependent) element.
 *
 * @author mblank
 *
 */
public class PanelElement {

    protected int state = 0;
    protected int adr = INVALID_INT;
    protected int adr2 = INVALID_INT;  // needed for DCC sensors/signals 
                           // with 2 addresses (adr1=occ/free, 2=in-route)
    protected String route = "";

    public PanelElement() {
    }


    public void toggle() {
        // do nothing for non changing element
    }

    public int getAdr() {
        return INVALID_INT;
    }
    
    public int getAdr2() {
        return INVALID_INT;
    }

    public void setAdr(int a) {

    }
    
    public void setAdr2(int a) {

    }

    public boolean hasAdrX(int address) {
        return false;
    }

    /** search for a panel element when only the address is known
     * 
     * @param address
     * @return 
     */
    public static PanelElement getPeByAddress(int address) {
		for (PanelElement pe : panelElements) {
			if (pe.getAdr() == address) {
				return pe;
			}
		}
		return null;
	}

    public int getState() {
        return 0;
    }
    
    public boolean setState(int a) {
        return false;
    }
 
}
