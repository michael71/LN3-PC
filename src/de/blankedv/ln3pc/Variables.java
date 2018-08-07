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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author mblank
 */
public class Variables {
     
    /**
     * {@value #N_LANBAHN} number of entries in lanbahn array (i.e. maximum
     * number of usable lanbahn addresses)
     */
    public static final int N_LANBAHN = 500;
    /**
     * {@value #LBMIN} minimum lanbahn channel number
     */
    public static final int LBMIN = 1; // 
    /**
     */
    public static final int LBMIN_LB = 1278;
    /**
     * {@value #LBMAX} =maximum lanbahn channel number
     */
    public static final int LBMAX = 9999;
    /**
     * {@value #DCCMAX} =maximum DCC address used
     */
    public static final int DCCMAX = 1200;
    /**
     * {@value #LBDATAMIN} =minimum lanbahn data value
     */
    public static final int LBDATAMIN = 0;
    /**
     * {@value #LBDATAMAX} =maximum lanbahn data value (== 4 bits in SX world)
     */
    public static final int LBDATAMAX = 15;  // 
    /**
     * {@value #INVALID_INT} = denotes a value as invalid (not usable)
     */
    public static final int INVALID_INT = -1;
    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_NOT_CONNECTED = 0;
    
    static boolean shutdownFlag = false;
    static int speed = 0;   // =DCC speed
    static int loco = 0;    // loco address
    static int lissySpeed = 0;   // speed measured by lissy
    static int direction = 0;
    static int slot = -1, slotAquired;

    static final int SPEED_STEP = 20;   // increase speed by ...

    static final int STOP = 0;
    static final int RUNNING = 1;   // automatic increase and reverse speed ONLY when "RUNNING"
    
    static final int TYPE_SENSOR = 0;
    static final int TYPE_SWITCH = 0;
    
    public static final ConcurrentHashMap<Integer, Integer> lanbahnData = new ConcurrentHashMap<Integer, Integer>(N_LANBAHN);
    public static final ArrayList<DCCMultiAspectSignalMapping> allSignalMappings = new ArrayList<DCCMultiAspectSignalMapping>();
    public static final ArrayList<Integer> allSensors = new ArrayList<Integer>();   // contains addresses which are sensors - needed to distinguish sensors from accessories for DCC commands

    static int progState = STOP;

    static byte awaitingLack = 0;
    static int lastAddress = 0;   // used for LACK response

    static enum State {
        IDLE, REQUEST, NULLMOVE, HAVE_SLOT
    };

    static State requestSlotState;

    static final int SELECTED_LISSY = 1;
    
    static final int POWER_ON = 1;
    static final int POWER_OFF = 0;
    static final int POWER_UNKNOWN = INVALID_INT;
}
