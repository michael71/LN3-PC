/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.*;
import static de.blankedv.ln3pc.Variables.INVALID_INT;
/**
 * all active panel elements, like turnouts, signals, trackindicators (=sensors)
 * are derviced from this class. These elements have a "state" which is exactly
 * the same number as the "data" of the lanbahn messages "SET 810 2" => set
 * state of panel element with address=810 to state=2
 *
 * a panel element has only 1 address (=> double slips are 2 panel elements)
 *
 * @author mblank
 *
 */
public abstract class ActivePanelElement extends PanelElement {

    // these constants are defined just for easier understanding of the
    // methods of the classes derived from this class
    // turnouts
    protected static final int STATE_CLOSED = 0;
    protected static final int STATE_THROWN = 1;
    protected static final int N_STATES_TURNOUTS = 2;

    // signals
    protected static final int STATE_RED = 0;
    protected static final int STATE_GREEN = 1;
    protected static final int STATE_YELLOW = 2;
    protected static final int STATE_YELLOW_FEATHER = 3;
    protected static final int STATE_SH1 =3;
    protected static final int N_STATES_SIGNALS = 4;

    // buttons
    protected static final int STATE_NOT_PRESSED = 0;
    protected static final int STATE_PRESSED = 1;

    // sensors
    protected static final int STATE_FREE = 0;
    protected static final int STATE_OCCUPIED = 1;
    protected static final int STATE_INROUTE = 2;
    protected static final int N_STATES_SENSORS = 3;

    protected static final int STATE_UNKNOWN = INVALID_INT;


    protected long lastToggle = 0L;
    protected long lastUpdateTime = 0L;

    public ActivePanelElement() {
    }

    /**
     * constructor for an ACTIVE panel element with 1 address default state is
     * "CLOSED" (="RED")
     *
     * @param type
     * @param x
     * @param y
     * @param name
     * @param adr
     */
    public ActivePanelElement(int adr) {
        this.state = STATE_UNKNOWN;
        this.adr = adr;
        lastUpdateTime = System.currentTimeMillis();
    }

    @Override
    public int getAdr() {
        return adr;
    }
    
    @Override
    public int getAdr2() {
        return adr2;
    }

    @Override
    public int getState() {
        return state;
    }
    
    @Override
    public boolean setState(int a) {
        return false;
    }

    @Override
    public boolean hasAdrX(int address) {
        if (adr == address) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setAdr(int adr) {
        this.adr = adr;
        this.state = STATE_UNKNOWN;
        this.lastUpdateTime = System.currentTimeMillis();
        if (adr != INVALID_INT) {
            //TODO sendQ.add("READ " + adr); // request update for this element
        }
    }
    
    public void setAdr2(int adr) {
        this.adr2 = adr;
    }

   
}
