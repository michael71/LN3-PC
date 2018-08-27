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

/**
 *
 * @author mblank
 */
public class LocoSlot {
    public int slot = INVALID_INT;
    public Loco loco = null;
    public boolean step128 = true;   // 128 or 28 speed steps 
 
    public LocoSlot() {
    }
    
    public LocoSlot(int sl, Loco lo, boolean st128) {
        slot = sl;
        loco = lo;
        step128 = st128;
    }
    
}
