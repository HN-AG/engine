package io.hashbuddy.engine;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;


public class BitcoinAverageAPI {
    public JsonNode getPriceFeed() throws UnirestException{
        return Unirest.get("https://api.bitcoinaverage.com/ticker/USD/").asJson().getBody();
    }
}
