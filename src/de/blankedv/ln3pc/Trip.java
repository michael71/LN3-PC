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

import static de.blankedv.ln3pc.Variables.INVALID_INT;
import static de.blankedv.ln3pc.Variables.SENSOR_OCCUPIED;
import static de.blankedv.ln3pc.Variables.allTrips;
import static de.blankedv.ln3pc.Variables.lanbahnData;

/**
 *
 * @author mblank
 */
public class Trip implements Comparable<Trip> {

    int id = INVALID_INT;
    String route = "";  // all routes to activate, separated by comma
    int sens1 = INVALID_INT;     // startSensor
    int sens2 = INVALID_INT;     // stopSensor
    String locoString = "";    // adr,dir,speed
    int locoAddr = INVALID_INT;
    int locoDir = INVALID_INT;
    int locoSpeed = INVALID_INT;
    int stopDelay = INVALID_INT;  // milliseconds
    boolean active = false;
    Loco loco = null;

    public boolean convertLocoData() {
        // convert locoString string to int values for address, direction and speed
        String[] lData = locoString.split(",");
        if (lData.length < 2) {
            return false;
        }

        try {
            locoAddr = Integer.parseInt(lData[0]);
            locoDir = Integer.parseInt(lData[1]);
            if ((locoDir != 0) && (locoDir != 1)) {
                return false;
            }
            if (lData.length >= 3) {
                locoSpeed = Integer.parseInt(lData[2]);
            } else {
                locoSpeed = 28;
            }
            loco = new Loco(locoAddr, locoDir, locoSpeed);

        } catch (NumberFormatException e) {
            return false;

        }
        return true;
    }

    public boolean start() {
        // check if start sensor is occupied and rest of route is cleared
        
        // set the route(s)
        // int[] routes = 
        
        // start loco
        
        // activate listener for "end sensor"
        
        return true;
    }
    
    public boolean checkEndSensor() {
        if (lanbahnData.get(sens2).data == SENSOR_OCCUPIED) {
            // this trip ends
            loco.setSpeed(0);  // stop loco
                // TODO free loco
            return true;
        } else {
            return false;
        }
    }
    // to be able to sort the trips by their ID
    @Override
    public int compareTo(Trip o) {
        if (this.id < o.id) {
            return 1;
        } else {
            return -1;
        }
    }
    
    
    static Trip get(int index) {
        for (Trip t: allTrips) {
            if (t.id == index) {
                return t;
            }
        }
        return null;  // not found
    }
}
