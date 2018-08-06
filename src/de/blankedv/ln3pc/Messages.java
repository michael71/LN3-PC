package de.blankedv.ln3pc;


/**
 * predefined throttle messages
 *
 * @author mblank
 */
public class Messages {

    
    public static String forward(int slotNumber) {
        String s = "A1 0" + slotNumber + " 30";

        return ("SEND " + addChecksum(s));
    }

    public static String backward(int slotNumber) {
        String s = "A1 0" + slotNumber + " 10";
        return ("SEND " + addChecksum(s));
    }

    /* convert integer (0...255) to two char hexstring
        10 => "0A" etc.
     
     */
    public static String twoCharFromInt(int i) {
        if (i > 255) {
            return "00";
        }
        String s = Integer.toHexString(i & 0xFF);
        if (s.length() == 1) {
            s = "0" + s;
        }
        return s.toUpperCase();
    }

    public static String aquire(int locoAddr) {
        String s;
        if (locoAddr < 0) {
            return "";
        }

        int hAddr = (locoAddr & 0xFF80) >> 7;
        int lAddr = locoAddr & 0x007F;
        s = "BF " + twoCharFromInt(hAddr) + " " + twoCharFromInt(lAddr);

        System.out.println(s);
        return ("SEND "+addChecksum(s));      

    }
    
    public static String nullMove(int slot) {
        
        String s = "BA " + twoCharFromInt(slot) + " " + twoCharFromInt(slot) ;
        return ("SEND "+addChecksum(s));      

    }

    /**
     * add a checksum to a loconet message in hex form for example "A1 00 20"
     *
     * @param s
     * @return
     */
    public static String addChecksum(String s) {
        String[] hexStrings = s.split(" ");
        int len = hexStrings.length; // total length
        byte[] msgBytes = new byte[len];
        int checksum = 0;
        for (int i = 0; i < len; i++) {
            int d = Integer.parseInt(hexStrings[i], 16);
            //System.out.print(hexStrings[i]+" ");            
            msgBytes[i] = (byte) (d & 0xff);
        }
        checksum = msgBytes[0];
        //System.out.print("cksum calc: " + Integer.toHexString(checksum & 0xff).toUpperCase()+" ");
        //System.out.println();  
        for (int i = 1; i < len; i++) {
            checksum ^= (int) (msgBytes[i]);
            //System.out.print(Integer.toHexString(checksum & 0xff).toUpperCase()+" "); 
        }
        checksum ^= (int) 0xff;
        return s + " " + Messages.twoCharFromInt(checksum & 0xff);
    }
}
