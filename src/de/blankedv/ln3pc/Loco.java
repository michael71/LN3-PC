/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.blankedv.ln3pc;

/**
 *
 * @author mblank
 */
public class Loco {

    private boolean forward = true;
    private int speed = 0;
    private boolean light = false;
    private boolean F1 = false;
    private int lok_adr = 1;

    public Loco() {
    }

    public Loco(int addr) {
        if ((addr > 0) && (addr < 9999)) {
            lok_adr = addr;

        } else {
            // create loco with std addr=3
            lok_adr = 3;
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

    public int getLok_adr() {
        return lok_adr;
    }

    public void setLok_adr(int lok_adr) {
        this.lok_adr = lok_adr;
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

}
