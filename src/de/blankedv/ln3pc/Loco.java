/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.DEBUG;

/**
 *
 * @author mblank
 */
public class Loco {

    
    private boolean forward = true;
    private int speed = 0;
    private boolean light = false;
    private boolean F1 = false;
    private int addr = 1;

    public Loco() {
    }

    public Loco(int addr) {
        if ((addr > 0) && (addr < 9999)) {
            this.addr = addr;

        } else {
            // create loco with std addr=3
            this.addr = 3;
        }
        speed = 0;
        light = false;
        F1 = false;
        forward = true;
    }

    public boolean isForward() {
        return forward;
    }

    public void setForward(boolean forward) {
        this.forward = forward;
    }

    public boolean isF1() {
        return F1;
    }

    public void setF1(boolean F1) {
        this.F1 = F1;
    }

    public boolean isLight() {
        return light;
    }

    public void setLight(boolean light) {
        this.light = light;
    }

    public int getAddr() {
        return addr;
    }

    public void setAddr(int addr) {
        this.addr = addr;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        if (speed < 0) {
            speed = 0;
        }
        if (speed > 31) {
            speed = 31;
        }
        this.speed = speed;
    }

    public void setFromSX(int sxdata) {
        speed = (byte) (sxdata & 0x1f);
        forward = (sxdata & 0x20) == 0x20;
        light = (sxdata & 0x40) == 0x40;
        F1 = (sxdata & 0x80) == 0x80;
        if (DEBUG) System.out.println(toString());
    }

    public int getSX() {
        int sxval = speed;
        if (forward) {
            sxval += 32;
        }
        if (light) {
            sxval += 64;
        }
        if (F1) {
            sxval += 128;
        }
        return sxval;
    }

    public void setDirf(byte dirf) {
        // dirf D5-SL_DIR , D4-SL_F0, D3-SL_F4, D2-SL_F3, D1-SL_F2, D0-SL_F1
        if ((dirf & 0x20) != 0) {
            forward = false;
        } else {
            forward = true;
        }
        if ((dirf & 0x10) != 0) {
            light = true;
        } else {
            light = false;
        }
        if ((dirf & 0x01) != 0) {
            F1 = true;
        } else {
            F1 = false;
        }       
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Loco A=");
        sb.append(addr);
        sb.append(" sp=");
        sb.append(speed);
        sb.append(" forw=").append(forward);
        sb.append(" F0=").append(light);
        return sb.toString();
    }
}
