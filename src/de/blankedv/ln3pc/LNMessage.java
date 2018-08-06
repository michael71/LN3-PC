
package de.blankedv.ln3pc;


/**
 *
 * @author mblank
 */
public class LNMessage {

    String msg;
    String[] msgHexStrings;
    int len;
    byte[] msgBytes;
    boolean valid = false;

    public LNMessage(String m) {
        if (m.contains("RECEIVE ")) {
            msg = m.substring(8);
            toBArray();
            valid = true;
        } else {
            valid = false;
        }
    }
    
    public LNMessage(int[] d) {
        // checksum will be added
        msgBytes = new byte[d.length+1];
        len = d.length + 1;
        for (int i=0; i < d.length; i++) {
            msgBytes[i] = (byte) (d[i] & 0xff);
        }
        byte checksum = msgBytes[0];
        System.out.println();  
        for (int i = 1; i < len; i++) {
            checksum ^= (int) (msgBytes[i]);
            //System.out.print(Integer.toHexString(checksum & 0xff)+" "); 
        }
        msgBytes[len-1] = checksum;
        valid = true;
    }

    public String getOpc() {
        if (valid) {
            return msgHexStrings[0];
        } else {
            return "--";
        }
    }

    public boolean isValid() {

        return valid;
    }

    public String getS(int i) {
        if ((valid) && (i <= len)) {

            return msgHexStrings[i];
        } else {
            return "--";
        }
    }

    public int getB(int i) {
        if ((valid) && (i <= len)) {

            return msgBytes[i];
        } else {
            return 0;
        }
    }

    public void toBArray() {
        msgHexStrings = msg.split(" ");
        len = msgHexStrings.length; // total length
        msgBytes = new byte[len];
        int checksum = 0;
        //System.out.print("msgHexStrings: ");
        for (int i = 0; i < len; i++) {
            int d = Integer.parseInt(msgHexStrings[i], 16);
            //System.out.print(msgHexStrings[i]+"("+d+") ");            
            msgBytes[i] = (byte) (d & 0xff);
        }
        checksum = msgBytes[0];
        //System.out.println();  
        for (int i = 1; i < len; i++) {
            checksum ^= (int) (msgBytes[i]);
            //System.out.print(Integer.toHexString(checksum & 0xff)+" "); 
        }
        if ((checksum & 0xff) != 0xff) {
            System.out.println("cksum error " + Integer.toHexString(checksum & 0xff));
        }
    }
    
    public byte[] getHexString() {
        return msgBytes;
    }
    
    
}
