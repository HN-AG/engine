package io.hashbuddy.engine;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
    private static final Logger log = Logger.getLogger(Crypto.class.getSimpleName());
    /**
     * Replacement for node.js createHmac function
     * @param type
     * @param secret
     * @return 
     */
    public static Mac createHmac(String type, String secret){
        //log.log(Level.INFO, "creating {0} with {1}", new Object[]{type, secret});
        try{
            Mac hmac = Mac.getInstance(type);
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes("UTF-8"), type);
            hmac.init(secret_key);

            return hmac;
        }catch(NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException ex){
            Main.oops(ex.getMessage());
        }
        return null;
    }
    
    public static String hexEncode(byte[] bytes){
        //return javax.xml.bind.DatatypeConverter.printBase64Binary(bytes).toLowerCase();
        return javax.xml.bind.DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }
    
    public static String signMessage(String type, String secret, String message){
        //log.log(Level.INFO, "Signing: {0}", message);
        return hexEncode(createHmac(type,secret).doFinal(message.getBytes()));
    }
}
