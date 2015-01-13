package ws.bask.crypto.paddingoracle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.URLConnection;
import org.apache.commons.codec.binary.Base64;

public class PaddingOracle {

    public static String brek(String cypher) {

        //String cypher = "f63b6d4e576d5f49cb3085788d280236"; // IV
        //String cypher = "7796e4ebcbb7a9fa2cbef4bd2b046a4d"; // erster Block
        //String cypher = "f63b6d4e576d5f49cb3085788d2802367796e4ebcbb7a9fa2cbef4bd2b046a4db3488d4e8b98618a5d366fe4c9634b0dff6bb874e18e81e9c217f15623cad973"; // Store 200

        int bs = 16;
        byte[][] blocks;
        int b = 0;
        String solution = "";

        // teile cypher in ByteArray-Bloecke
        int cl = cypher.length() / 2;
        if (( cl % bs == 0 ) && (cl / bs > 1)) {
            blocks = new byte[cl/bs][bs];
            for (int i=0; i<cl*2; i=i+bs*2) {
                blocks[b] = hexStringToByteArray(cypher.substring(i,i+bs*2));
                b++;
            }
        } else {
            blocks = null;
            System.out.println("Cyphertext muss Vielfaches von Blocksize sein!");
            System.out.println("Cyphertext-Laenge: "+ cl + " Blocksize: " + bs);
            System.exit(1);
        }

        // knacke bloecke
        b = 0;
        while (b < blocks.length -1) {
            solution += new String(crackBlock(blocks[b+1], blocks[b]));
            b++;
        }

        // delete padding
        String[] paddings = {"01", "0202", "030303", "04040404", "0505050505", "060606060606", "07070707070707", "0808080808080808", "090909090909090909",
                "0a0a0a0a0a0a0a0a0a0a", "0b0b0b0b0b0b0b0b0b0b0b", "0c0c0c0c0c0c0c0c0c0c0c0c", "0d0d0d0d0d0d0d0d0d0d0d0d0d",
                "0e0e0e0e0e0e0e0e0e0e0e0e0e0e", "0f0f0f0f0f0f0f0f0f0f0f0f0f0f0f"};

        for ( String padding: paddings) {
            String hexPadding = padding;//hexStringToString(padding);
            if ( solution.endsWith( hexPadding ) ) {
                solution = solution.substring(0, solution.length() - hexPadding.length());
            }
        }
        System.out.println("Solution found: " + solution);
        return solution;
    }

    public static byte[] crackBlock( byte[] inputBlock , byte[] vorgaenger) {
        // construct testArray
        int bs = inputBlock.length;
        byte[] testArray = new byte[bs*2];
        System.arraycopy(inputBlock, 0, testArray, bs, bs);

        byte[] loesung = new byte[bs];
        int padding = 1;

        // from last to first byte
        for (int i=bs-1; i>=0; i--) {
            int versuch = 0;
            int code = 0;

            // try all characters
            while ( code != 1 ) {
                testArray[i] += 1;
                if (versuch > 256) {
                    System.out.println("Bittest fehlgeschlagen!");
                    return loesung;
                }
                versuch++;

                code = getCode(testArray);
            }

            System.out.println("Bit gefunden: " + Integer.toHexString(testArray[i] & 0xff) + " in " + byteArrayToHexString(testArray));

            // berechne Loesungs-Byte
            if (padding==0) {
                System.out.println("Padding nicht gesetzt!");
                System.exit(1);
            }
            loesung[i] = (byte) (padding ^ vorgaenger[i] ^ testArray[i]);
            System.out.println("Lösung: " + byteArrayToHexString(loesung) + " / " + new String(loesung));

            // setze almost valid padding fuer naechste Runde
            if ( i > 0 ) {
                padding++;
                for (int j=i; j<bs; j++) {
                    //testArray[j] = (byte) (loesung[j] ^ (padding+16-i) ^ vorgaenger[j]);
                    testArray[j] = (byte) (loesung[j] ^ (padding) ^ vorgaenger[j]);
                }
                System.out.println("Set almost valid padding to " + (padding) + " in " + byteArrayToHexString(testArray));
            }
        }

        return loesung;
    }

    // send secret to server and return code
    public static int getCode(byte[] secret) {

        Integer code = 1;
        String host = "http://localhost:8888/store_secret/";
        String full = host + secret;


            Oracle oracle = new Oracle();
            oracle.connect();
            code = oracle.send(secret, secret.length / 16);
        //new File("test.txt").delete();
        FileWriter fw = null;
        try {
            fw = new FileWriter("test.txt", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fw.write(encode(secret) +";" +code.toString() +"\r\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                fw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        oracle.disconnect();
            /*URL url = new URL(full);
            URLConnection con = url.openConnection();
            con.connect();
            String head = con.getHeaderField(0);*/

        //System.out.println(secret + " " + code);
        return code;
    }

    //Converting a string of hex character to bytes
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2){
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    //Converting a bytes array to string of hex character
    public static String byteArrayToHexString(byte[] b) {
        int len = b.length;
        String data = new String();

        for (int i = 0; i < len; i++){
            data += Integer.toHexString((b[i] >> 4) & 0xf);
            data += Integer.toHexString(b[i] & 0xf);
        }
        return data;
    }

    // Converting Hex to String
    public static String hexStringToString(String hex) {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < hex.length(); i+=2) {
            String str = hex.substring(i, i+2);
            output.append((char)Integer.parseInt(str, 16));
        }
        return output.toString();
    }

    // Do base64 and URL encoding of ByteArray
    public static String encode( byte[] normal ){
        String s = new String();

        try {
            s = bytesToHex(normal);
            //url = URLEncoder.encode(s, "ASCII");
            //System.out.println(s);
            //System.out.println(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}