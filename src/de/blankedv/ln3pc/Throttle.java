package de.blankedv.ln3pc;

/**
 *
 * @author mblank
 * 
 * adapted from arduino lib "loconet"
 */
public class Throttle {

    
    public static final int STAT1_SL_SPURGE = 0x80;
    /* internal use only, not seen on net */
    public static final int STAT1_SL_CONUP = 0x40;
    /* consist status                     */
    public static final int STAT1_SL_BUSY = 0x20;
    /* used with STAT1_SL_ACTIVE,         */
    public static final int STAT1_SL_ACTIVE = 0x10;
    /*                                    */
    public static final int STAT1_SL_CONDN = 0x08;
    /*                                    */
    public static final int STAT1_SL_SPDEX = 0x04;
    /*                                    */
    public static final int STAT1_SL_SPD14 = 0x02;
    /*                                    */
    public static final int STAT1_SL_SPD28 = 0x01;
    /*                                    */
    public static final int STAT2_SL_SUPPRESS = 0x01;
    /* 1 = Adv. Consisting supressed      */
    public static final int STAT2_SL_NOT_ID = 0x04;
    /* 1 = ID1/ID2 is not ID usage        */
    public static final int STAT2_SL_NOTENCOD = 0x08;
    /* 1 = ID1/ID2 is not encoded alias   */
    public static final int STAT2_ALIAS_MASK = (STAT2_SL_NOTENCOD | STAT2_SL_NOT_ID);
    public static final int STAT2_ID_IS_ALIAS = STAT2_SL_NOT_ID;

    public static final int LOCOSTAT_MASK = (STAT1_SL_BUSY | STAT1_SL_ACTIVE);
    public static final int LOCO_IN_USE = (STAT1_SL_BUSY | STAT1_SL_ACTIVE);
    public static final int LOCO_IDLE = (STAT1_SL_BUSY);
    public static final int LOCO_COMMON = (STAT1_SL_ACTIVE);
    public static final int LOCO_FREE = (0);
    
    
    public static String statusToString(int stat) {
        switch (stat & LOCOSTAT_MASK) {
            case LOCO_IN_USE:
                return "In-Use";
            case LOCO_IDLE:
                return "Idle";
            case LOCO_COMMON:
                return "Common";
            default:
                return "Free";
        }
    }

}
