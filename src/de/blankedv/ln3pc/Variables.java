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

/**
 *
 * @author mblank
 */
public class Variables {
    
    static boolean shutdownFlag = false;
    static int speed = 0;   // =DCC speed
    static int loco = 0;    // loco address
    static int lissySpeed = 0;   // speed measured by lissy
    static int direction = 0;
    static int slot = -1, slotAquired;

    static final int SPEED_STEP = 20;   // increase speed by ...

    static final int STOP = 0;
    static final int RUNNING = 1;   // automatic increase and reverse speed ONLY when "RUNNING"

    static int progState = STOP;

    static byte awaitingLack = 0;
    static int lastAddress = 0;   // used for LACK response

    static enum State {
        IDLE, REQUEST, NULLMOVE, HAVE_SLOT
    };

    static State requestSlotState;

    static final int SELECTED_LISSY = 1;
}
