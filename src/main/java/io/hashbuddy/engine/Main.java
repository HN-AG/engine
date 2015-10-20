package io.hashbuddy.engine;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.json.JSONException;

public class Main {
    
    private static final Logger log = Logger.getLogger(Main.class.getCanonicalName());
    private static final Preferences prefs = Preferences.userNodeForPackage(Main.class);
    private static final AtomicBoolean running = new AtomicBoolean(true); 
    private final ScriptEngine engine;
    
    HashNestAPI hashnest; 
    AntPoolAPI antpool;
    HashMap<String, Object> world;
    ScheduledThreadPoolExecutor timer;
    private Main(String[] args){
        parseArgs(args);
        world = new HashMap<>();
        hashnest = new HashNestAPI();
        antpool = new AntPoolAPI();
        timer = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
        ScriptEngineManager factory = new ScriptEngineManager();
        engine = factory.getEngineByName(prefs.get("ENGINE", "nashorn"));
        engine.put("BitcoinAverageAPI", new BitcoinAverageAPI());
        engine.put("HashNestAPI", hashnest);
        engine.put("AntPoolAPI", antpool);
        engine.put("StorageAPI", prefs);
    }
    
    public static void main(String[] args) throws ScriptException{
        Main app = new Main(args);
        ScheduledFuture<?> runThread = null;
        try{
            runThread = app.start();
            while(running.get()){
                Thread.yield();
            }
        }catch(Exception ex){
            oops(ex.getMessage());
        }finally{
            app.engine.eval("onQuit()");
            if(runThread != null){
                runThread.cancel(true);
                
            }
        }
    }
    
    ScheduledFuture<?> start() throws SQLException, ScriptException, FileNotFoundException{
        //Returning this allows us to have something to cancel
        //db = new DBConnection();
        File scriptfile = new File(prefs.get("STRATEGY", "strategies/main.js"));
       if(scriptfile.exists()){
            FileReader reader = new FileReader(scriptfile);
            engine.eval(reader);
            engine.eval("onInit()");
       }else{
           log.severe("No file found at "+scriptfile.getAbsolutePath());
       }
        Integer tickRate = Integer.parseInt(prefs.get("TICKRATE","300"));
        return timer.scheduleWithFixedDelay(new OnTimerFiredAction(),0, tickRate, TimeUnit.SECONDS);
    }
    
    void stop(){
        running.set(false);
    }
    private class OnTimerFiredAction implements Runnable{

        @Override
        public void run() {
            try {
                world.put("PREFS", prefs);
                world.put("POOLINFO", antpool.getPoolStats());
                world.put("POOLBLOCKS",BlockChainInfoAPI.FetchPoolBlocks());
                engine.put("world", world);
                engine.eval("onTick()");
                System.gc();
               
            } catch (UnirestException | NoSuchAlgorithmException | InvalidKeyException | JSONException | ScriptException ex) {
                if(ex.getMessage().contains("java.net") || ex.getMessage().contains("org.apache.http")){
                    log.info("Your internet is down again!  I'll try again in a minute or so.");
                    log.info("Error was : "+ex.getMessage());
                    //returns about here
                }else{
                    ex.printStackTrace();
                    oops(ex.getMessage());
                }
            }
        }
    }
    
    public static void oops(String msg){
        try {
            running.set(false);
            Unirest.shutdown();
            log.severe(msg);
            new Exception().printStackTrace();
            log.severe("Unrecoverable, exiting now");
        } catch (IOException ex) {
            log.severe(ex.getMessage());
        }finally{
            System.exit(1);
        }
    }
     /**
     * Convenience function to get at Preferences object for the app
     * @param key
     * @return 
     */
    public static String getPref(String key) {
        return prefs.get(key, "");
    }
    
    public static String getPref(String key,String def) {
        return prefs.get(key, def);
    }
    
    private static void parseArgs(String[] args) {
        for(String arg : args){
            String[] data = arg.split("=");
            String key = data[0].replace("-", "").toUpperCase();

            if(key.equals("HELP")){
                printUsage();
                System.exit(0);
            }
            if(key.equals("CLEARALL")){
                try {
                    prefs.clear();
                    System.exit(0);
                } catch (BackingStoreException ex) {
                    oops(ex.getMessage());
                }
            }
            String value = data[1];
            prefs.put(key, value);
        }
    }
    
    private static void printUsage() {
        log.info("Used to actively manage accounts at HashNest, all options are persisted by default and only need to be given once");
        log.info("--clear-all (clears all persisted options)");
        log.info("--help (display this info)");
        log.info("--hashnest-api-key=1234567890 (required on first run)");
        log.info("--hashnest-api-secret=somesecret (required on first run)");
        log.info("--script-engine=nashorn (optional)");
        log.info("--strategy=/path/to/main.js (optional)");
        log.info("--script-entry=main (optional)");
    }
    
    
}
