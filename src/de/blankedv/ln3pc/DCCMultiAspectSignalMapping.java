package de.blankedv.ln3pc;

import static de.blankedv.ln3pc.Variables.*;

/**
 * how to map lanbahn multi-aspect signal values to DCC address channels (which
 * only can be 0 or 1)
 *
 * @author mblank
 */
public class DCCMultiAspectSignalMapping {
    int lbAddr;
    int dccAddr;
    int nBit;
    
    DCCMultiAspectSignalMapping() {
        lbAddr = INVALID_INT;
        dccAddr = INVALID_INT;
        nBit = INVALID_INT;
    }
    
    DCCMultiAspectSignalMapping(int lba, int nBits) {
        this.lbAddr = lba;
        this.dccAddr = lba;
        if ((dccAddr < 1) || (dccAddr > DCCMAX)) {
            this.lbAddr = INVALID_INT;   // not possible to create a mapping
        } else {
             this.nBit = nBits;
        }
    }
    
    static boolean isLanbahnMultiAspect(int lba) {    
            
       for (DCCMultiAspectSignalMapping sm : allSignalMappings) {
           if (sm.lbAddr == lba) return true;
       }
       return false;
    }
    
    static boolean isDCCMultiAspect(int dccAddr) {    
            
       for (DCCMultiAspectSignalMapping sm : allSignalMappings) {
           switch (sm.nBit) {
               case 1:
                    if (sm.dccAddr == dccAddr) return true;
                    break;
               case 2:
                    if ((sm.dccAddr +1) == dccAddr) return true;
                    break;
               default:
                   throw new java.lang.RuntimeException("nbit other than 1 or 2 not yet implemented");
           }
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
