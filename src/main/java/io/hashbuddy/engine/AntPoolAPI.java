package io.hashbuddy.engine;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class AntPoolAPI {
    private static final Logger log = Logger.getLogger(HashNestAPI.class.getSimpleName());
    private final String baseURL, apiKey, apiSecret, apiUserName;
    
    private static class Endpoints{
        public static final String POOLSTATS = "/poolStats.htm";
    }
    
    public AntPoolAPI(){
        this.apiKey = Main.getPref("ANTPOOLAPIKEY");
        this.apiSecret = Main.getPref("ANTPOOLAPISECRET");
        this.apiUserName = Main.getPref("ANTPOOLUSERNAME");
        
        if(Main.getPref("ANTPOOLAPIURL").isEmpty()){
            this.baseURL = "https://antpool.com/api";
        } else {
            this.baseURL = Main.getPref("ANTPOOLAPIURL");
        }
        
        if(apiKey.isEmpty() || apiSecret.isEmpty()){
            Main.oops("Missing apikey or api secret");
        }
        log.info("AntPoolAPI instantiated!");
    }
    
    public String apiSign(Long nonce) throws NoSuchAlgorithmException, InvalidKeyException{
        
        String data = apiUserName+apiKey+nonce;
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(apiSecret.getBytes(), "HmacSHA256");
        hmacSha256.init(secretKey);
        return Hex.encodeHexString(hmacSha256.doFinal(data.getBytes())).toUpperCase();
    }
    
    public JsonNode getPoolStats() throws UnirestException, NoSuchAlgorithmException, InvalidKeyException{
        //String url = buildUrlNow(Endpoints.POOLSTATS); 
        String url = baseURL + Endpoints.POOLSTATS;
        log.info(url.toString());
        Long nonce = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        return Unirest.post(url)
                .header("accept", "application/json")
                .field("key", apiKey)
                .field("nonce",nonce)
                .field("signature",apiSign(nonce))
                .asJson().getBody();
        
    }
}
