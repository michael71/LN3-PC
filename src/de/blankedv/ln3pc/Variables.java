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
     * {@value #LBMAX} =maximum lanbahn channel number
     */
    public static final int LBMAX = 9999;
    /**
     * {@value #DCCMAX} =maximum DCC address used
     */
    public static final int DCCMAX = 1199;
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

    static final int SPEED_STEP = 20;   // increase speed by .
    static final int MAX_SPEED = 28;  // TODO !!!!

    static final int STOP = 0;
    static final int RUNNING = 1;   // automatic increase and reverse speed ONLY when "RUNNING"

    public static ConcurrentHashMap<Integer, LbData> lanbahnData = new ConcurrentHashMap<>(N_LANBAHN);

    public static ArrayList<LocoSlot> locoSlots = new ArrayList<>();   // slot to Loco mapping
    public static ArrayList<Loco> allLocos = new ArrayList<>();   // all Locos we have heard of (via sxnet)
    public static ArrayList<Trip> allTrips = new ArrayList<>();   // all Locos we have heard of (via sxnet)
    public static ArrayList<Timetable> allTimetables = new ArrayList<>();
    public static ArrayList<PanelElement> panelElements = new ArrayList<>();
    public static ArrayList<Route> allRoutes = new ArrayList<>();
    public static ArrayList<CompRoute> allCompRoutes = new ArrayList<>();

    static int progState = STOP;

    static byte awaitingLack = 0;
    static int lastAddress = 0;   // used for LACK response

    static final int STATE_IDLE = 0;
    static final int STATE_REQUEST = 1;
    static final int STATE_NULLMOVE = 2;
    static final int STATE_HAVE_SLOT = 3;

    /**
     * {@value #MAX_ROUTES} =maximum number of allRoutes in a compound route
     */
    static final int MAX_ROUTES = 20;

    /**
     * {@value #MAX_TRIPS} =maximum number of trips in a single Timetable
     */
    static final int MAX_TRIPS = 20;

    static final int SELECTED_LISSY = 1;

    static final int POWER_ON = 1;
    static final int POWER_OFF = 0;
    static final int POWER_UNKNOWN = INVALID_INT;

    // type of lanbahn data
    static final int TYPE_ACC_1BIT = 0;   // values 0 or 1
    static final int TYPE_SIGNAL_1BIT = 1; // values 0 or 1
    static final int TYPE_SIGNAL_2BIT = 2; // values 0 .. 3
    static final int TYPE_SIGNAL_3BIT = 3;
    static final int TYPE_SENSOR = 10;     // sensor for occupation (and route-lighting with 'adr2')
    static final int TYPE_2BIT = 11;     // doubleslip with 2 addresses
    static final int TYPE_ROUTE = 20;   // this is a route.

    

    // signals
    static final int STATE_RED = 0;
    static final int STATE_GREEN = 1;
    static final int STATE_YELLOW = 2;
    static final int STATE_YELLOW_FEATHER = 3;
    static final int STATE_SWITCHING = 4;

    static final int AUTO_CLEAR_ROUTE_TIME_SEC = 30;    // clear allRoutes automatically after 30secs
}
