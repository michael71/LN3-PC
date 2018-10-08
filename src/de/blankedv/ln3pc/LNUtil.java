/*
 * Copyright (C) 2018 mblank
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.blankedv.ln3pc;

import de.blankedv.timetable.LbUtils;
import de.blankedv.timetable.PanelElement;
import static de.blankedv.ln3pc.Variables.*;
import static de.blankedv.ln3pc.MainUI.*;

/**
 *
 * @author mblank
 */
public class LNUtil {

    private static int requestSlotState;
    private static Loco aquiringLoco = null;

    public static boolean interpret(final byte[] buf, final int count) {
        if ((count == 0) || (count != getLength(buf))) {
            return false;
        }
        int adr, iBit, state, dir;
        //System.out.print("interpret, count="+count);
        byte chk = 0;
        for (int i = 0; i < count; i++) {
            chk ^= buf[i];
            //System.out.printf("%02X ", buf[i]);
        }
        // System.out.printf(" chk=%02X ", chk);
        // System.out.println();

        if (chk != (byte) 0xff) {
            return false;
        }

        StringBuilder disp = new StringBuilder();

        switch (buf[0]) {
            case (byte) 0xA0:
            case (byte) 0xA1:
                disp.append(" -> slot cmd");  // ==> for the time being, ignore loco messages  TODO
                break;
            case (byte) 0x83:
                globalPower = POWER_ON;
                disp.append("-> power on");
                break;
            case (byte) 0x82:
                globalPower = POWER_OFF;
                disp.append("-> power off");
                break;
            case (byte) 0xB0:  // switch
                /* <0xB0>,<SW1>,<SW2>,<CHK> REQ SWITCH function
<SW1> =<0,A6,A5,A4- A3,A2,A1,A0>, 7 ls adr bits. A1,A0 select 1 of 4 input pairs in a DS54
<SW2> =<0,0,DIR,ON- A10,A9,A8,A7> Control bits and 4 MS adr bits.
,DIR=1 for Closed,/GREEN, =0 for Thrown/RED
,ON=1 for Output ON, =0 FOR output OFF */
                adr = getSwitchAddress(buf);
                dir = getDirection(buf);
                state = 0;
                if ((buf[2] & 0x10) != 0) {  //ON/OFF
                    state = 1;
                }
                disp.append("-> switch adr=").append(adr);
                if (dir == 0) {
                    disp.append(" thrown/red");
                    LbUtils.updateLanbahnData(adr, 1);  // LOGIC REVERSED !!!
                } else {
                    disp.append(" closed/green");
                    LbUtils.updateLanbahnData(adr, 0);  // LOGIC REVERSED !!!
                }
                if (state == 0) {
                    disp.append(" OFF");
                } else {
                    disp.append(" ON");
                }

                break;

            case (byte) 0xB2:   // Sensor
                /* sensor message 0xB2 ; General SENSOR Input codes; <0xB2>, <IN1>, <IN2>, <CHK>
<IN1> =<0,A6,A5,A4- A3,A2,A1,A0>, 7 ls adr bits. A1,A0 select 1 of 4 inputs pairs in a DS54
<IN2> =<0,X,I,L- A10,A9,A8,A7>
Report/status bits and 4 MS adr bits.
"I"=0 for DS54 "aux" inputs and 1 for "switch" inputs mapped to 4K SENSOR space.
(This is effectively a LEAST SIGNIFICANT Address bit when using DS54 input configuration)
"L"=0 for input SENSOR now 0V (LO) , 1 for Input sensor >=+6V (HI)
"X"=1, control bit , 0 is RESERVED for future! */
                adr = getSensorAddress(buf);
                state = getOnOffState(buf);
                disp.append("-> sensor adr=").append(adr);
                PanelElement se = PanelElement.getByAddress(adr);
                if (se != null) {
                    if (state == 0) {
                        disp.append(" free");
                        int st = se.setBit0(false);   // lanbahn sensors have 4 different states
                        LbUtils.updateLanbahnData(adr, st);
                        disp.append(" st=");
                        disp.append(st);
                    } else {
                        disp.append(" occupied");
                        int st = se.setBit0(true);    // lanbahn sensors have 4 different states
                        LbUtils.updateLanbahnData(adr, st);
                        disp.append(" st=");
                        disp.append(st);
                    }
                }

                break;

            case (byte) 0xB4:
                /* 0xB4 ;Long acknowledge
;<0xB4>,<LOPC>,<ACK1>,<CHK> Long acknowledge
;<LOPC> is COPY of OPCODE responding to (msb=0).
;LOPC=0 (unused OPC) is also VALID fail code
;<ACK1> is appropriate response code for the OPCode*/
                if (buf[1] == awaitingLack) {
                    adr = lastAddress;
                    dir = getDirection(buf);
                    //if (state == 0) {
                    disp.append("-> lack, adr=").append(adr);
                    String s = String.format("%02X", buf[2]);
                    disp.append(" ACK1=").append(s).append(" dir=").append(dir);
                    LbUtils.updateLanbahnData(adr, (1 - dir));
                } else {
                    // reset LACK 
                    awaitingLack = 0;
                    disp.append("-> lack reset");
                }
                break;
            case (byte) 0xBC:
                /* 0xBC ;REQ state of SWITCH
;<0xBC>,<SW1>,<SW2>,<CHK> REQ state of SWITCH */
                lastAddress = getSwitchAddress(buf);
                awaitingLack = 0xBC & 0x7f;
                disp.append("-> req switch state, adr=").append(lastAddress);
                break;
            case (byte) 0xE4:
                /*
                (m[2] & 0x03) + 0x01 = train category (1..4)
                (m[3] & 0x1F ) *128 + (m[4] & 0x7F) = lissy address (1 .. 4095)
                (m[5] * 128) + (m[6] & 0x7F) = loco address
                 */

                if (buf[1] == (byte) 0x08) {

                    int lissyMsgType = buf[3] & 0x60;  // mask bits
                    int lissyAdr = (buf[3] & 0x1F) * 128 + (buf[4] & 0x7F);
                    int trainCat = (buf[2] & 0x03) + 0x01;

                    disp.append("-> lissy# ").append(lissyAdr);
                    switch (lissyMsgType) {
                        case 0x60:
                            // loco adr message
                            loco = (int) (buf[5] & 0x7f) * 128 + (int) (buf[6] & 0x7f);
                            System.out.println(" loco# " + loco + " cat=" + trainCat + " dir S2->S1");
                            break;
                        case 0x40:
                            // loco adr message
                            loco = (int) (buf[5] & 0x7f) * 128 + (int) (buf[6] & 0x7f);
                            System.out.println(" loco# " + loco + " cat=" + trainCat + " dir S1->S2");
                            break;
                        case 0x00:
                            // loco adr message
                            loco = (int) (buf[5] & 0x7f) * 128 + (int) (buf[6] & 0x7f);
                            System.out.println(" loco# " + loco + " cat=" + trainCat);
                            break;
                        case 0x20:
                            lissySpeed = (int) (buf[5] & 0x7f) * 128 + (int) (buf[6] & 0x7f);
                            System.out.println(" speed# " + lissySpeed);
                            break;
                        default:
                            System.out.println(" ??");
                            break;
                    }
                }
                break;
            case (byte) 0xE7:
                if (buf[1] == (byte) 0x0E) {
                    //   OPC_SL_RD_DATA    0xE7 ;SLOT DATA return, 10 bytes
                    // ;<0xE7>,<0E>,<SLOT#>,<STAT>,<ADR>,<SPD>,<DIRF>,<TRK>
                    // ;<SS2>,<ADR2>,<SND>,<ID1>,<ID2>,<CHK>

                    if (requestSlotState == STATE_REQUEST) {
                        slotAquired = buf[2];
                        String stat = Throttle.statusToString(buf[3]);
                        System.out.println("slot=" + slotAquired
                                + " st=0x" + buf[3]
                                + "(" + stat + ")");
                        //byte spd = buf[5];
                        // byte dirf = buf[6];   // dir,F0-4 state
                        //byte trk = buf[7];
                        //btnStart.setEnabled(true);
                        if (!stat.equals("In-Use")) {
                            requestSlotState = STATE_NULLMOVE;
                            serialIF.send(makeNullMove(slotAquired));
                            disp.append(" sending nullMove slot=" + slotAquired);
                        } else {
                            disp.append(" stealing, aquire finished");
                            requestSlotState = STATE_HAVE_SLOT;
                            initLoco(slotAquired, aquiringLoco, (byte) (0x07 & buf[3]), buf[5], buf[6], buf[7]);
                        }
                    } else if (requestSlotState == STATE_NULLMOVE) {
                        disp.append(" aquire finished");
                        requestSlotState = STATE_HAVE_SLOT;
                        initLoco(slotAquired, aquiringLoco, (byte) (0x07 & buf[3]), buf[5], buf[6], buf[7]);
                    }
                }
                break;
            default:
                disp.append(" -> ?");
                break;  // complete command, but we cannot understand it
        }
        if (DEBUG) {
            System.out.println(disp.toString());
        }
        return true;

    }

    static private int getSwitchAddress(byte[] buf) {
        return (int) (buf[1] & 0x7f) + (int) (buf[2] & 0x0f) * 128 + 1;
    }

    static private int getSensorAddress(byte[] buf) {
        int adr = 2 * ((int) (buf[1] & 0x7f) + (int) (buf[2] & 0x0f) * 128);
        int iBit = (int) buf[2] & 0x20;
        if (iBit != 0) {
            iBit = 1;
        }
        return adr + 1 + iBit;
    }

    static private void initLoco(int slotAquired, Loco lo, byte sp, byte dectype, byte dirf, byte trk) {

        setGlobalPower(trk);
        if (DEBUG) {
            printTRKinfo(trk);
        }
        LocoSlot ls = new LocoSlot(slotAquired, lo, is128SpeedSteps(dectype));
        locoSlots.add(ls);   // TODO check if it exists already
        lo.setDirf(dirf);
        lo.setSpeed(0); // stop loco first (int)sp);
        byte[] buf = getLNLocoSpeed(ls);
        serialIF.send(buf);

    }

    /**
     * calculate number of speedsteps for this slot from last 3 bits of "decoder
     * type"
     *
     * @param dtype
     * @return
     */
    static boolean is128SpeedSteps(byte dtype) {
        // D2..D0  BITS for Decoder TYPE encoding for this SLOT
// 011=send 128 speed mode packets
//;010=14 step MODE
//;001=28 step. Generate Trinary packets for this Mobile ADR
// 000=28 step/ 3 BYTE PKT regular mode
//;111=128 Step decoder, Allow Advanced DCC consisting
//;100=28 Step decoder ,Allow Advanced DCC consisting
        boolean step128;
        switch (dtype) {
            case 0b010:
                System.out.println("ERROR:  14 speedsteps not allowed!!!");
                step128 = false;
                break;
            case 0:
            case 0b01:

                step128 = false;
                break;
            default:
                step128 = true;
                break;
        }
        if (step128) {
            System.out.println("128 speed steps");
        } else {
            System.out.println("28 speed steps");
        }
        return step128;
    }

    static public void printTRKinfo(byte t) {
        // TRK byte: D7-D4 Reserved, D3 = prog.track.is.busy
        // D2 = Loconet 1.1 cap.
        // D1 = Track is NOT paused (no emergency stop)
        // D0 = Global Power ON
        if ((t & 0x80) != 0) {
            System.out.println("prog track busy");
        }
        if ((t & 0x40) != 0) {
            System.out.println("loconet 1.1 cap.");
        }
        if ((t & 0x20) != 0) {
            System.out.println("no emergency stop");
        } else {
            System.out.println("EMERGENCY stop");
        }

    }

    static private void setGlobalPower(byte t) {
        if ((t & 0x10) != 0) {
            System.out.println("globalPower is ON");
            globalPower = POWER_ON;
        } else {
            System.out.println("globalPower is OFF");
            globalPower = POWER_OFF;
        }
    }

    static private int getDirection(byte[] buf) {
        int dir = (int) buf[2] & 0x20;  // GREEN/RED
        if (dir != 0) {
            dir = 1;
        }
        return dir;
    }

    static private int getOnOffState(byte[] buf) {
        int state = 0;
        if ((buf[2] & 0x10) != 0) {  //ON/OFF
            state = 1;
        }

        return state;
    }

    /**
     * see loconet documentation two bytes in the opcode define the length of
     * the message
     *
     * @param buf
     * @return
     */
    static public int getLength(byte[] buf) {
        byte opcode = buf[0];
        int b = opcode & 0x60;   // in java 0x60 is an int !!
        switch (b) {
            case 0x60:
                return buf[1];
            case 0x00:
                return 2;
            case 0x20:
                return 4;
            case 0x40:
                return 6;
            default:
                return 0;
        }
    }

    static public String bufToString(byte[] buf) {
        int count = getLength(buf);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%02X ", buf[i]));
        }
        return sb.toString();
    }

    /**
     * static public byte[] test() { //A0 01 00 5e byte[] buf = new byte[4];
     * buf[0] = (byte) 0xa0; buf[1] = (byte) 0x01; buf[2] = (byte) 0x00; buf[3]
     * = (byte) ((byte) 0xff ^ buf[0] ^ buf[1] ^ buf[2]); if (buf[3] != (byte)
     * 0x5e) { System.out.println(String.format("ERROR, chk=%02X", buf[3])); }
     * return buf; }
     */

    /* NOT USED
     static public byte[] makePOWER_ON_OFF(int onoff) {

        byte[] buf = new byte[2];
        if (onoff == 1) {
            buf[0] = (byte)0x83;
            globalPower = POWER_ON;
            buf[1] = (byte) ((byte) 0xff ^ buf[0]);
            return buf;
        } else if (onoff == 0) {
             buf[0] = (byte)0x82;
            globalPower = POWER_OFF;
            buf[1] = (byte) ((byte) 0xff ^ buf[0]);
            return buf;
        } else {
            return null;
        }
    } */
    static public byte[] makeOPC_SW_REQ(int address, int dir, int onoff) {

        byte[] buf = new byte[4];
        buf[0] = (byte) 0xb0;
        buf[1] = (byte) (address & 0x7f);
        buf[2] = (byte) 0x00;
        if (dir == 1) {
            buf[2] |= (byte) 0x20;
        }
        if (onoff == 1) {
            buf[2] |= (byte) 0x10;
        }
        buf[2] |= (byte) ((address >> 7) & 0x0F);
        buf[3] = (byte) ((byte) 0xff ^ buf[0] ^ buf[1] ^ buf[2]);

        return buf;
    }

    static public byte[] makeREQ_SW_STATE(int address) {
        byte[] buf = new byte[4];
        buf[0] = (byte) 0xbc;
        buf[1] = (byte) (address & 0x7f);
        buf[2] = (byte) 0x00;
        buf[2] |= (byte) ((address >> 7) & 0x0F);
        buf[3] = (byte) ((byte) 0xff ^ buf[0] ^ buf[1] ^ buf[2]);

        return buf;
    }

    static public byte[] aquireLoco(int addr, int data) {
        if ((addr < 0) || (addr > 9999)) {
            System.out.println("ERROR: invalid address in aquireLoco");
            return null;
        }

        System.out.println("aquiring addr=" + addr);
        requestSlotState = STATE_REQUEST;
        aquiringLoco = null;
        for (Loco l : allLocos) {
            if (l.getAddr() == addr) {
                aquiringLoco = l; // we found the loco

            }
        }
        if (aquiringLoco == null) {
            // create a new one
            aquiringLoco = new Loco(addr);
            allLocos.add(aquiringLoco);
        }
        aquiringLoco.setFromSX(data);

        byte[] buf = new byte[4];
        buf[0] = (byte) 0xbf;
        buf[1] = (byte) 0x00;  // TODO: impl long addresses
        buf[2] = (byte) (addr & 0x7f);
        //buf[2] = (byte) ((addr >> 7) & 0x0F);
        buf[3] = (byte) ((byte) 0xff ^ buf[0] ^ buf[1] ^ buf[2]);

        return buf;
    }

    static public byte[] makeNullMove(int slot) {
        byte[] buf = new byte[4];
        buf[0] = (byte) 0xBA;
        buf[1] = (byte) slot;
        buf[2] = (byte) slot;
        buf[3] = (byte) ((byte) 0xff ^ buf[0] ^ buf[1] ^ buf[2]);

        return buf;
    }

    static public byte[] getLNLocoSpeed(LocoSlot ls) {
        // 0xA1 ;SET SLOT dir,F0-4 state
        // 0xA0 ;SET SLOT speed
        byte[] buf = new byte[4];
        if (ls.loco.getAddr() == INVALID_INT) {
            return null;
        }

        buf[0] = (byte) 0xa0;
        buf[1] = (byte) (ls.slot);
        buf[2] = (byte) (ls.loco.getSpeed() * DCC_SPEED_FACTOR);
        if (buf[2] == (byte) 0x01) {
            buf[2] = (byte) 0x02;
        }  // 0x01 is emergency stop !!
        buf[3] = (byte) ((byte) 0xff ^ buf[0] ^ buf[1] ^ buf[2]);

        return buf;
    }

    static public byte[] getLNLocoDirAndFunctions(LocoSlot ls) {
        // 0xA1 ;SET SLOT dir,F0-4 state
        // 0xA0 ;SET SLOT speed
        byte[] buf = new byte[4];
        if (ls.loco.getAddr() == INVALID_INT) {
            return null;
        }

        buf[0] = (byte) 0xa1;
        buf[1] = (byte) (ls.slot);
        buf[2] = (byte) (0x00);

        if (ls.loco.isForward()) {
            buf[2] += (byte) 0x20;
        }
        if (ls.loco.isLight()) {
            buf[2] += (byte) 0x10;
        }
        if (ls.loco.isF1()) {
            buf[2] += (byte) 0x01;
        }

        buf[3] = (byte) ((byte) 0xff ^ buf[0] ^ buf[1] ^ buf[2]);

        return buf;
    }

    public static void sendLanbahnToLocoNet(int addr, int data) {
        LbData lb = lanbahnData.get(addr);
        if (lb == null) {
            lb = new LbData(data, 1, "T");
        }
        switch (lb.getNBit()) {
            case 1:
                data &= 0x01;  // only last bit is used for LocoNet/DCC
                LbUtils.updateLanbahnData(addr, data);   // don't change type, only change data              
                serialIF.send(LNUtil.makeOPC_SW_REQ(addr - 1, (1 - data), 1));   // TODO test
                break;
            case 2:
                int data0 = data & 0x01;  // only last bit is used for LocoNet/DCC sensor reporting
                LbUtils.updateLanbahnData(addr, data);   // don't change type, only change data              
                serialIF.send(LNUtil.makeOPC_SW_REQ(addr - 1, (1 - data0), 1));   // TODO test             
                PanelElement se = PanelElement.getByAddress(addr);
                if ((se != null) && (se.secondaryAdr != INVALID_INT)) {
                    int data1 = (data >> 1) & 0x01;  // for route-lighting bit1 and se.secondaryAdr are used
                    serialIF.send(LNUtil.makeOPC_SW_REQ(se.secondaryAdr - 1, (1 - data1), 1));   // TODO test
                }
                break;
            default:
                // cannot set other types
                if (DEBUG) {
                    System.out.println("ERROR, cannot set data for nbit=" + lb.getNBit());
                }
                break;
        }
    }
}
