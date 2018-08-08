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
public class IntPair {
    int a;
    int t;
    
    IntPair (int addr, int type) {
        a = addr;
        t = type;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (a == INVALID_INT) return ("invalid");
        sb.append("a=").append(a);
        switch(t) {
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
