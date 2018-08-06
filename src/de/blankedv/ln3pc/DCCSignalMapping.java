package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.MainUI.INVALID_INT;
import static de.blankedv.ln3pc.MainUI.DCCMAX;
import static de.blankedv.ln3pc.MainUI.allSignalMappings;

/**
 * how to map lanbahn multi-aspect signal values to DCC address channels (which
 * only can be 0 or 1)
 *
 * @author mblank
 */
public class DCCSignalMapping {
    int lbAddr;
    int dccAddr;
    int nBit;
    
    DCCSignalMapping() {
        lbAddr = INVALID_INT;
        dccAddr = INVALID_INT;
        nBit = INVALID_INT;
    }
    
    DCCSignalMapping(int lba, int nBits) {
        this.lbAddr = lba;
        this.dccAddr = lba;
        if ((dccAddr < 1) || (dccAddr > DCCMAX)) {
            this.lbAddr = INVALID_INT;   // not possible to create a mapping
        } else {
             this.nBit = nBits;
        }
    }
    
   
    
    static boolean exists(int lba) {
       for (DCCSignalMapping sm : allSignalMappings) {
           if (sm.lbAddr == lba) return true;
       }
       return false;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        sb.append("lbAddr=");
        sb.append(lbAddr);
        sb.append(" dccAddr=");
        sb.append(dccAddr);
        sb.append(" nBit=");
        sb.append(nBit);
        return sb.toString();
    }
}
