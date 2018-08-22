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
 * You should have received addr copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.Variables.*;


/**
 * declares a generic multiaspect Signal with its first DCC address and the
 * Type : 1,2 or 3 bit (=2,4,8 aspects)  
 * 2 bit => 2 addresses : addr, addr+1  etc
 * 
 * @author mblank
 */
public class Signal {
    int addr;   // first address
    int sigType;      // type: 1,2 or 3 bit (=2,4,8 aspects)
    
    Signal (int addr, int type) {
        this.addr = addr;
        sigType = type;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (addr == INVALID_INT) return ("invalid");
        sb.append("a=").append(addr);
        switch(sigType) {
            case TYPE_ACCESSORY:
            case TYPE_SIGNAL_1BIT:
                sb.append (" acc");
                break;
            case TYPE_SIGNAL_2BIT:
                sb.append (" sig2bit");
                break;
            case TYPE_SIGNAL_3BIT:
                sb.append (" sig3bit - not supported.");
                break;
            case TYPE_SENSOR:
                sb.append (" sensor");
                break;
            default:
                sb.append(" unknown type");
                break;
        }
        return sb.toString();
    }
}
