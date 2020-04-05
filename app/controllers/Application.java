package controllers;

import play.Play;
import play.mvc.Controller;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;

/**
 * Class Application Controller
 *
 * @author quanna
 */
public class Application extends Controller {

    /**
     * Index home page
     */
    public static void index() {
        render();
    }

    /**
     * Config CORS
     */
    public static void options() {
        response.headers.put("Access-Control-Allow-Origin", new Http.Header("Access-Control-Allow-Origin", "*"));
        response.headers.put("Access-Control-Allow-Credentials", new Http.Header("Access-Control-Allow-Credentials",
                "true"));
        response.headers.put("Access-Control-Allow-Methods",
                new Http.Header("Access-Control-Allow-Methods", "*"));
        response.headers.put("Access-Control-Allow-Headers", new Http.Header("Access-Control-Allow-Headers",
                "x-token,Content-Type"));
    }

    public static void checkAvailableRoomList() {
        boolean available = Play.configuration.getProperty("screen.availableRoomList", "false").equalsIgnoreCase("true");
        renderJSON(available);
    }

}