package io.hashbuddy.engine;

import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;


public class BlockChainInfoAPI {
    public static JsonNode FetchPoolBlocks() throws UnirestException{
        String url = "https://blockchain.info/blocks/AntPool?format=json";
        return Unirest.get(url).asJson().getBody();
    }
}
