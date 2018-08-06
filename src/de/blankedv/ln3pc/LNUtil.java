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

import static de.blankedv.ln3pc.Variables.*;

/**
 *
 * @author mblank
 */
public class LNUtil {

    public static void interpret(final byte[] buf, final int count) {
        if ((count == 0) || (count != getLength(buf))) {
            return;
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
            return;
        }

        StringBuilder disp = new StringBuilder();

        System.out.print("msg: ");
        for (int i = 0; i < count; i++) {
            System.out.printf("%02X ", buf[i]);
        }
        System.out.println();

        switch (buf[0]) {
            case (byte) 0xB0:
                /* <0xB0>,<SW1>,<SW2>,<CHK> REQ SWITCH function
<SW1> =<0,A6,A5,A4- A3,A2,A1,A0>, 7 ls adr bits. A1,A0 select 1 of 4 input pairs in a DS54
<SW2> =<0,0,DIR,ON- A10,A9,A8,A7> Control bits and 4 MS adr bits.
,DIR=1 for Closed,/GREEN, =0 for Thrown/RED
,ON=1 for Output ON, =0 FOR output OFF */
                adr = getSwitchAddress(buf);
                dir = getDirection(buf);
                adr = adr + 1; // ???
                state = 0;
                if ((buf[2] & 0x10) != 0) {  //ON/OFF
                    state = 1;
                }
                disp.append("-> switch adr=").append(adr);
                if (dir == 0) {
                    disp.append(" thrown/red");
                } else {
                    disp.append(" closed/green");
                }
                if (state == 0) {
                    disp.append(" OFF");
                } else {
                    disp.append(" ON");
                }

                break;

            case (byte) 0xB2:
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
                if (state == 0) {
                    disp.append(" free");
                } else {
                    disp.append(" occupied");
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
                    //state = getOnOffState(msg);
                    //if (state == 0) {
                    disp.append("-> lack, adr=").append(adr);
                    disp.append(" state=").append(buf[2]);
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
                    if (requestSlotState == State.REQUEST) {
                        slotAquired = buf[2];
                        String stat = Throttle.statusToString(buf[3]);
                        System.out.println("slot=" + slotAquired
                                + " st=0x" + buf[3]
                                + "(" + stat + ")");
                        //btnStart.setEnabled(true);
                        if (!stat.equals("In-Use")) {
                            requestSlotState = State.NULLMOVE;
                            // TODO lnc.send(Messages.nullMove(slotAquired));
                        } else {
                            disp.append(" stealing, aquire finished");
                            requestSlotState = State.HAVE_SLOT;
                        }
                    } else if (requestSlotState == State.NULLMOVE) {
                        disp.append(" aquire finished");
                        requestSlotState = State.HAVE_SLOT;
                    }
                }
                break;
            default:
                disp.append(" -> ?");
                break;
        }
        System.out.println(disp.toString());

    }

    static private int getSwitchAddress(byte[] buf) {
        return (int) (buf[1] & 0x7f) + (int) (buf[2] & 0x0f) * 128;
    }

    static private int getSensorAddress(byte[] buf) {
        int adr = 2 * ((int) (buf[1] & 0x7f) + (int) (buf[2] & 0x0f) * 128);
        int iBit = (int) buf[2] & 0x20;
        if (iBit != 0) {
            iBit = 1;
        }
        return adr + 1 + iBit;
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
    static private int getLength(byte[] buf) {
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
}
