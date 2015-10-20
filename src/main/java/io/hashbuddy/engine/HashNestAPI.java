package io.hashbuddy.engine;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Logger;

public class HashNestAPI {
    private static final Logger log = Logger.getLogger(HashNestAPI.class.getSimpleName());
    private static final String baseURL = "https://www.hashnest.com/api/v1";
    private final String apiKey, apiSecret;
    
    private static class Endpoints{
        public static final String ACCOUNT = "/account";
        public static final String WALLET = "/currency_accounts";
        public static final String HASHACCOUNT = "/hash_accounts";
        public static final String MARKETS = "/currency_markets";
        public static final String MARKETHISTORY = "/currency_markets/order_history";
        public static final String MARKETORDERS = "/currency_markets/orders";
        public static final String ORDERS = "/orders";
        public static final String ORDERSHISTORY = "/orders/history";
        public static final String ORDERSACTIVE = "/orders/active";
        public static final String ORDERSCANCEL = "/orders/revoke";
        public static final String ORDERSCANCELLALL = "/orders/quick_revoke";
    }

    public HashNestAPI(){
        this.apiKey = Main.getPref("HASHNESTAPIKEY");
        this.apiSecret = Main.getPref("HASHNESTAPISECRET");
        if(apiKey.isEmpty() || apiSecret.isEmpty()){
            Main.oops("Missing apikey or api secret");
        }
        log.info("HashNestAPI instantiated!");
    }
        
    /**
     *  Used for the 
     * @param nonce
     * @return message = nonce + username + access_key
     */
    private StringBuilder generateMessage(Long nonce){
        //log.info("message: "+nonce+Main.getPref("HASHNESTAPIUSERNAME")+apiKey);
        return new StringBuilder()
                .append(nonce)
                .append(Main.getPref("HASHNESTAPIUSERNAME"))
                .append(apiKey);
    }

    private StringBuilder buildUrl(String endpoint,Long nonce){
        return new StringBuilder()
                .append(baseURL)
                .append(endpoint)
                .append('?')
                .append("access_key=")
                .append(apiKey)
                .append("&nonce=")
                .append(nonce)
                .append("&signature=")
                .append(
                    Crypto.signMessage("HmacSHA256", apiSecret, 
                        generateMessage(nonce).toString()
                    )
                ); 
    }
    
    private String buildUrlNow(String endPoint){
        Long time = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
        return buildUrl(endPoint,time).toString();
    }
    
   
    /**
    * HTTP METHOD: POST
    *   HTTP URL: https://www.hashnest.com/api/v1/account
    *  Param:
    *      access_key: (Details see the verification method)
    *      nonce: (Details see the verification method)
    *      signature: (Details see the verification method)
    * 
    *    Return Result:
    *      {
    *        id: 12,
    *        email: xxx@bitmain.com,
    *        temp_access_token: xxxxxxxx
    *      }
    * @return 
     * @throws com.mashape.unirest.http.exceptions.UnirestException 
    */
    public JsonNode queryAccountInfo() throws UnirestException{
        String url = buildUrlNow(Endpoints.ACCOUNT);
        log.info(url);
        return Unirest.post(url).asJson().getBody();
    }
      
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/currency_accounts
     *   Param:
     *     access_key: (Details see the verification method）
     *     nonce: (Details see the verification method）
     *     signature: (Details see the verification method）
     * 
     *   Return Result:
     *     [
     *       {
     *         currency: {
     *           code: "btc"
     *         },
     *         amount: "41.0449535",
     *         blocked: "0.0",
     *         total: "41.0449535"
     *       }
     *     ]
     * @return the result of the request
     * @throws com.mashape.unirest.http.exceptions.UnirestException
     */    
    public JsonNode checkAccountBalance() throws UnirestException{
        String url = buildUrlNow(Endpoints.WALLET);
        log.info(url);
        return Unirest.post(url).asJson().getBody();
    }
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/hash_accounts
     *   Param:
     *     access_key: (Details see the verification method）
     *     nonce: (Details see the verification method）
     *     signature: (Details see the verification method）
     * 
     *   Return Result:
     *   [
     *     {
     *       currency: {
     *         code: "AntS1"
     *       },
     *       amount: "0.0",
     *       blocked: "0.0",
     *       total: "0.0"
     *     }
     *   ]
     * 
     * @return
     * @throws UnirestException
     */
    public JsonNode checkHashRateBalance() throws UnirestException{
        String url = buildUrl(Endpoints.HASHACCOUNT,System.currentTimeMillis()).toString();
        log.info(url);        
        return Unirest.post(url).asJson().getBody();
    }
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/currency_markets
     *   Param:
     *     access_key: (details see the verification method）
     *     nonce: (details see the verification method）
     *     signature: (details see the verification method）
     * 
     *   Return Result
     *   [
     *     {
     *       id: 11,
     *       name: "ANTS2/BTC"
     *     }
     *   ]
     * @return 
     * @throws com.mashape.unirest.http.exceptions.UnirestException 
     */
    public JsonNode getOpenMarkets() throws UnirestException {
        String url = buildUrlNow(Endpoints.MARKETS);
        log.info(url);
        return Unirest.post(url).asJson().getBody();
    }
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/currency_markets/order_history
     *   Param:
     *     access_key: (details see the verification method）
     *     nonce: (details see the verification method）
     *     signature: (details see the verification method）
     *     category: (entrust type eg: [sale|purchase])
     *     currency_market_id: (Market ID)
     * 
     *   Return Result:
     *   [
     *     {
     *       ppc: "0.00001",
     *       amount: "4.0",
     *       total_price: "0.00004",
     *       created_at: "2014-12-09 01:06:00"
     *     }
     *   ]
     * @param currency_market_id
     * @return 
     * @throws com.mashape.unirest.http.exceptions.UnirestException 
     */
    public JsonNode getMarketHistory(int currency_market_id) throws UnirestException{
        String url = buildUrlNow(Endpoints.MARKETHISTORY);
        //url += "&currency_market_id="+currency_market_id;
        log.info(url);
        return  Unirest.post(url)
                //.field("category", category)
                .field("currency_market_id", currency_market_id)
                .asJson().getBody();
    }

    /**
     * Undocumented call to fetch the entire orderbook for a market
     * @param id
     * @return
     * @throws UnirestException 
     */
    public JsonNode getOrderBooks(int id) throws UnirestException{
        String url = buildUrlNow(Endpoints.MARKETORDERS);
        log.info(url);
        return Unirest.post(url)
                .field("currency_market_id",id)
                .asJson().getBody();
    }
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/orders/active
     *   Param:
     *     access_key: (Details see the verification method）
     *     nonce: (Details see the verification method）
     *     signature: (Details see the verification method）
     *     currency_market_id(交易市场ID): 1...
     * 
     *   Return Result:
     * 
     *   [
     *     {
     *       id: 48544,  #uniquely identify
     *       category: "sale", #catagory of entrust order
     *       amount: "1000.0", #amount of entrust order
     *       ppc: "0.01", #unit price of entrust order
     *       created_at: "2014-12-09 01:00:25" #created time of entrust order
     *     }
     *   ]
     * @param id
     * @return 
     */
    
    public JsonNode getUserActiveOrders(int id) throws UnirestException{
        String url = buildUrlNow(Endpoints.ORDERSACTIVE);
        log.info(url);
        return Unirest.post(url)
                .field("currency_market_id",id)
                .asJson().getBody();
    }
    
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/orders/history
     *   Param:
     *     access_key: (Details see the verification method）
     *     nonce: (Details see the verification method）
     *     signature: (Details see the verification method）
     *     currency_market_id: 1..
     *     page: （Optional, default first page）
     *     page_per_amount: (Optional, default 20 records)
     * 
     *   Return Result:
     *   [
     *     {
     *       ppc: "0.00001",
     *       amount: "4.0",
     *       total_price: "0.00004",
     *       created_at: "2014-12-09 01:06:00"
     *     }
     *   ]
     * @param id
     * @return
     * @throws com.mashape.unirest.http.exceptions.UnirestException
     */
    
    public JsonNode getUserOrderHistory(int id) throws UnirestException{
        String url = buildUrlNow(Endpoints.ORDERSHISTORY);
        return Unirest.post(url)
                .field("currency_market_id",id)
                .asJson().getBody();
    }
    
    /**
     * HTTP METHOD： POST
     *   HTTP URL: https://www.hashnest.com/api/v1/orders
     *   Param:
     *     access_key: (Details see the verification method）
     *     nonce: (Details see the verification method）
     *     signature: (Details see the verification method)
     *     currency_market_id: (Market ID)
     *     amount: (amount of entrust order)
     *     ppc: (unit price of entrust order)
     *     category: (entrust type eg: [sale|purchase])
     * 
     *   Return Result:
     *     Return to details of entrust order if successed
     *     eg:
     *     {
     *       id: 48544,  #uniquely identify
     *       category: "sale", #catagory of entrust order
     *       amount: "1000.0", #Amount of entrust order
     *       ppc: "0.01", #unit price of entrust order
     *       created_at: "2014-12-09 01:00:25" #created time of entrust order
     *     }
     *     Return to the faulty information if failed
     *     eg:
     * 
     * @param currency_market_id
     * @param amount
     * @param ppc
     * @param category
     * @return 
     */
    
    public JsonNode createOrder(int currency_market_id, String amount, String ppc, String category) throws UnirestException{
        
        String training_wheels = Main.getPref("TRAININGWHEELS","ON");
        if(training_wheels.equals("OFF")){
            log.info("Training Wheels are OFF, this order will be sent!");
            String url = buildUrlNow(Endpoints.ORDERS);
            return Unirest.post(url)
                    .field("currency_market_id",currency_market_id)
                    .field("amount", amount)
                    .field("ppc", ppc)
                    .field("category", category)
                    .asJson()
                    .getBody();
        }else{
            log.warning("Training Wheels are ON, framework will not create the order\nIf you want this trade to occur you must log into hashnest and do it manually.");
            return null;
        }
     }
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/orders/revoke
     *   Param:
     *     access_key: (details see the verification method）
     *     nonce: (details see the verification method）
     *     signature: (details see the verification method）
     *     order_id:
     * 
     *   Return Result:
     *   {
     *     success: true|false
     *   } 
     * @param order_id
     * @return 
     */
    public JsonNode cancelOrder(Long order_id) throws UnirestException{
        
        String url = buildUrlNow(Endpoints.ORDERSCANCEL);
        return Unirest.post(url)
                .field("order_id",order_id)
                .asJson()
                .getBody();
    }
    
    /**
     * HTTP METHOD: POST
     *   HTTP URL: https://www.hashnest.com/api/v1/orders/quick_revoke
     *   Param:
     *     access_key: (details see the verification method）
     *     nonce: (details see the verification method）
     *     signature: (details see the verification method）
     *     currency_market_id:
     * 
     *   Return Result:
     *   {
     *     success: true|false
     *   }
     * @param currency_market_id
     * @param category [sale|purchase]
     * @return 
     * @throws com.mashape.unirest.http.exceptions.UnirestException 
     */  
    public JsonNode cancelAllOrders(int currency_market_id, String category) throws UnirestException{
        String url = buildUrlNow(Endpoints.ORDERSCANCELLALL);
        
        return Unirest.post(url)
                .field("currency_market_id", currency_market_id)
                .field("category",category)
                .asJson()
                .getBody();
      }    
    
}