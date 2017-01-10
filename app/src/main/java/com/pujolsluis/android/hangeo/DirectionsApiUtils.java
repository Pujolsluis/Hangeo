package com.pujolsluis.android.hangeo;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Luis on 1/5/2017.
 */

public final class DirectionsApiUtils {

    private static final String ROUTES = "routes";
    private static final String SUMMARY = "summary";
    private static final String LEGS = "legs";
    private static final String DISTANCE = "distance";
    private static final String TEXT = "text";
    private static final String VALUE = "value";
    private static final String DURATION = "duration";
    private static final String STEPS = "steps";
    private static final String END_LOCATION = "end_location";
    private static final String LATITUDE = "lat";
    private static final String LONGITUDE = "lng";
    private static final String HTML_INSTRUCTION = "html_instructions";
    private static final String POLYLINE = "polyline";
    private static final String POINTS = "points";
    private static final String START_LOCATION = "start_location";
    private static final String BOUNDS = "bounds";
    private static final String OVERVIEW_POLYLINE = "overview_polyline";
    private static final String NORTHEAST = "northeast";
    private static final String SOUTHWEST = "southwest";

/**
 * Helper methods related to requesting and receiving earthquake data from USGS.
 */
    public static final String LOG_TAG = DirectionsApiUtils.class.getSimpleName();
    /** Sample JSON response for a USGS query */
    private static final String SAMPLE_JSON_RESPONSE = "{\"type\":\"FeatureCollection\",\"metadata\":{\"generated\":1462295443000,\"url\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2016-01-01&endtime=2016-01-31&minmag=6&limit=10\",\"title\":\"USGS Earthquakes\",\"status\":200,\"api\":\"1.5.2\",\"limit\":10,\"offset\":1,\"count\":10},\"features\":[{\"type\":\"Feature\",\"properties\":{\"mag\":0.2,\"place\":\"88km N of Yelizovo, Russia\",\"time\":1454124312220,\"updated\":1460674294040,\"tz\":720,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us20004vvx\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us20004vvx&format=geojson\",\"felt\":2,\"cdi\":3.4,\"mmi\":5.82,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":798,\"net\":\"us\",\"code\":\"20004vvx\",\"ids\":\",at00o1qxho,pt16030050,us20004vvx,gcmt20160130032510,\",\"sources\":\",at,pt,us,gcmt,\",\"types\":\",cap,dyfi,finite-fault,general-link,general-text,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":0.958,\"rms\":1.19,\"gap\":17,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 7.2 - 88km N of Yelizovo, Russia\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[158.5463,53.9776,177]},\"id\":\"us20004vvx\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":1.1,\"place\":\"94km SSE of Taron, Papua New Guinea\",\"time\":1453777820750,\"updated\":1460156775040,\"tz\":600,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us20004uks\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us20004uks&format=geojson\",\"felt\":null,\"cdi\":null,\"mmi\":4.1,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":572,\"net\":\"us\",\"code\":\"20004uks\",\"ids\":\",us20004uks,gcmt20160126031023,\",\"sources\":\",us,gcmt,\",\"types\":\",cap,geoserve,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":1.537,\"rms\":0.74,\"gap\":25,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.1 - 94km SSE of Taron, Papua New Guinea\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[153.2454,-5.2952,26]},\"id\":\"us20004uks\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":2.3,\"place\":\"50km NNE of Al Hoceima, Morocco\",\"time\":1453695722730,\"updated\":1460156773040,\"tz\":0,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004gy9\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004gy9&format=geojson\",\"felt\":117,\"cdi\":7.2,\"mmi\":5.28,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":0,\"sig\":695,\"net\":\"us\",\"code\":\"10004gy9\",\"ids\":\",us10004gy9,gcmt20160125042203,\",\"sources\":\",us,gcmt,\",\"types\":\",cap,dyfi,geoserve,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":2.201,\"rms\":0.92,\"gap\":20,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.3 - 50km NNE of Al Hoceima, Morocco\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-3.6818,35.6493,12]},\"id\":\"us10004gy9\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":3.1,\"place\":\"86km E of Old Iliamna, Alaska\",\"time\":1453631430230,\"updated\":1460156770040,\"tz\":-540,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004gqp\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004gqp&format=geojson\",\"felt\":1816,\"cdi\":7.2,\"mmi\":6.6,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":1496,\"net\":\"us\",\"code\":\"10004gqp\",\"ids\":\",at00o1gd6r,us10004gqp,ak12496371,gcmt20160124103030,\",\"sources\":\",at,us,ak,gcmt,\",\"types\":\",cap,dyfi,finite-fault,general-link,general-text,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,trump-origin,\",\"nst\":null,\"dmin\":0.72,\"rms\":2.11,\"gap\":19,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 7.1 - 86km E of Old Iliamna, Alaska\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-153.4051,59.6363,129]},\"id\":\"us10004gqp\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":4.6,\"place\":\"215km SW of Tomatlan, Mexico\",\"time\":1453399617650,\"updated\":1459963829040,\"tz\":-420,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004g4l\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004g4l&format=geojson\",\"felt\":11,\"cdi\":2.7,\"mmi\":3.92,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":673,\"net\":\"us\",\"code\":\"10004g4l\",\"ids\":\",at00o1bebo,pt16021050,us10004g4l,gcmt20160121180659,\",\"sources\":\",at,pt,us,gcmt,\",\"types\":\",cap,dyfi,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":2.413,\"rms\":0.98,\"gap\":74,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.6 - 215km SW of Tomatlan, Mexico\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-106.9337,18.8239,10]},\"id\":\"us10004g4l\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":5.7,\"place\":\"52km SE of Shizunai, Japan\",\"time\":1452741933640,\"updated\":1459304879040,\"tz\":540,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004ebx\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004ebx&format=geojson\",\"felt\":51,\"cdi\":5.8,\"mmi\":6.45,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":720,\"net\":\"us\",\"code\":\"10004ebx\",\"ids\":\",us10004ebx,pt16014050,at00o0xauk,gcmt20160114032534,\",\"sources\":\",us,pt,at,gcmt,\",\"types\":\",associate,cap,dyfi,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,\",\"nst\":null,\"dmin\":0.281,\"rms\":0.98,\"gap\":22,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.7 - 52km SE of Shizunai, Japan\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[142.781,41.9723,46]},\"id\":\"us10004ebx\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":6.1,\"place\":\"12km WNW of Charagua, Bolivia\",\"time\":1452741928270,\"updated\":1459304879040,\"tz\":-240,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004ebw\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004ebw&format=geojson\",\"felt\":3,\"cdi\":2.2,\"mmi\":2.21,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":0,\"sig\":573,\"net\":\"us\",\"code\":\"10004ebw\",\"ids\":\",us10004ebw,gcmt20160114032528,\",\"sources\":\",us,gcmt,\",\"types\":\",cap,dyfi,geoserve,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":5.492,\"rms\":1.04,\"gap\":16,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.1 - 12km WNW of Charagua, Bolivia\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-63.3288,-19.7597,582.56]},\"id\":\"us10004ebw\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":7.2,\"place\":\"74km NW of Rumoi, Japan\",\"time\":1452532083920,\"updated\":1459304875040,\"tz\":540,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004djn\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004djn&format=geojson\",\"felt\":8,\"cdi\":3.4,\"mmi\":3.74,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":0,\"sig\":594,\"net\":\"us\",\"code\":\"10004djn\",\"ids\":\",us10004djn,gcmt20160111170803,\",\"sources\":\",us,gcmt,\",\"types\":\",cap,dyfi,geoserve,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":1.139,\"rms\":0.96,\"gap\":33,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.2 - 74km NW of Rumoi, Japan\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[141.0867,44.4761,238.81]},\"id\":\"us10004djn\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":8.5,\"place\":\"227km SE of Sarangani, Philippines\",\"time\":1452530285900,\"updated\":1459304874040,\"tz\":480,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004dj5\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004dj5&format=geojson\",\"felt\":1,\"cdi\":2.7,\"mmi\":7.5,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":1,\"sig\":650,\"net\":\"us\",\"code\":\"10004dj5\",\"ids\":\",at00o0srjp,pt16011050,us10004dj5,gcmt20160111163807,\",\"sources\":\",at,pt,us,gcmt,\",\"types\":\",cap,dyfi,geoserve,impact-link,impact-text,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,tectonic-summary,\",\"nst\":null,\"dmin\":3.144,\"rms\":0.72,\"gap\":22,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.5 - 227km SE of Sarangani, Philippines\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[126.8621,3.8965,13]},\"id\":\"us10004dj5\"},\n" +
            "{\"type\":\"Feature\",\"properties\":{\"mag\":9,\"place\":\"Pacific-Antarctic Ridge\",\"time\":1451986454620,\"updated\":1459202978040,\"tz\":-540,\"url\":\"http://earthquake.usgs.gov/earthquakes/eventpage/us10004bgk\",\"detail\":\"http://earthquake.usgs.gov/fdsnws/event/1/query?eventid=us10004bgk&format=geojson\",\"felt\":0,\"cdi\":1,\"mmi\":0,\"alert\":\"green\",\"status\":\"reviewed\",\"tsunami\":0,\"sig\":554,\"net\":\"us\",\"code\":\"10004bgk\",\"ids\":\",us10004bgk,gcmt20160105093415,\",\"sources\":\",us,gcmt,\",\"types\":\",cap,dyfi,geoserve,losspager,moment-tensor,nearby-cities,origin,phase-data,shakemap,\",\"nst\":null,\"dmin\":30.75,\"rms\":0.67,\"gap\":71,\"magType\":\"mww\",\"type\":\"earthquake\",\"title\":\"M 6.0 - Pacific-Antarctic Ridge\"},\"geometry\":{\"type\":\"Point\",\"coordinates\":[-136.2603,-54.2906,10]},\"id\":\"us10004bgk\"}],\"bbox\":[-153.4051,-54.2906,10,158.5463,59.6363,582.56]}";

    private static final String SAMPLE_JSON_RESPONSE2 = "{\n   \"geocoded_waypoints\" : [\n      {\n         \"geocoder_status\" : \"OK\",\n         \"place_id\" : \"ChIJvY9FVxiKr44RrbrpS0EVLyM\",\n         \"types\" : [ \"route\" ]\n      },\n      {\n         \"geocoder_status\" : \"OK\",\n         \"place_id\" : \"ChIJ9Rmd28KJr44RcIxbDoulB3Q\",\n         \"types\" : [ \"premise\" ]\n      }\n   ],\n   \"routes\" : [\n      {\n         \"bounds\" : {\n            \"northeast\" : {\n               \"lat\" : 18.4895198,\n               \"lng\" : -69.9285319\n            },\n            \"southwest\" : {\n               \"lat\" : 18.4748466,\n               \"lng\" : -69.9647355\n            }\n         },\n         \"copyrights\" : \"Datos de mapas ©2017 Google\",\n         \"legs\" : [\n            {\n               \"distance\" : {\n                  \"text\" : \"4,8 km\",\n                  \"value\" : 4843\n               },\n               \"duration\" : {\n                  \"text\" : \"15 min\",\n                  \"value\" : 891\n               },\n               \"end_address\" : \"OLA GPSolutions, Calle Zeta 57, Santo Domingo, República Dominicana\",\n               \"end_location\" : {\n                  \"lat\" : 18.4753661,\n                  \"lng\" : -69.9285319\n               },\n               \"start_address\" : \"Calle Buena Ventura Freiter, Santo Domingo, República Dominicana\",\n               \"start_location\" : {\n                  \"lat\" : 18.4873778,\n                  \"lng\" : -69.9641008\n               },\n               \"steps\" : [\n                  {\n                     \"distance\" : {\n                        \"text\" : \"81 m\",\n                        \"value\" : 81\n                     },\n                     \"duration\" : {\n                        \"text\" : \"1 min\",\n                        \"value\" : 24\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 18.4877799,\n                        \"lng\" : -69.9647355\n                     },\n                     \"html_instructions\" : \"Dirígete hacia el \\u003cb\\u003enoroeste\\u003c/b\\u003e hacia \\u003cb\\u003eAv. de Los Próceres\\u003c/b\\u003e\",\n                     \"polyline\" : {\n                        \"points\" : \"cyyoBrz_jL_@v@o@fA\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 18.4873778,\n                        \"lng\" : -69.9641008\n                     },\n                     \"travel_mode\" : \"DRIVING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0,5 km\",\n                        \"value\" : 510\n                     },\n                     \"duration\" : {\n                        \"text\" : \"1 min\",\n                        \"value\" : 72\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 18.4880444,\n                        \"lng\" : -69.96003089999999\n                     },\n                     \"html_instructions\" : \"Gira a la \\u003cb\\u003ederecha\\u003c/b\\u003e en la 1.ª bocacalle hacia \\u003cb\\u003eAv. de Los Próceres\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePasa por FD (a la derecha).\\u003c/div\\u003e\",\n                     \"maneuver\" : \"turn-right\",\n                     \"polyline\" : {\n                        \"points\" : \"s{yoBr~_jLOUOYQe@Me@Kc@E]EYC_@A]?g@@[@q@@UHiIDmCBOBMJU\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 18.4877799,\n                        \"lng\" : -69.9647355\n                     },\n                     \"travel_mode\" : \"DRIVING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0,8 km\",\n                        \"value\" : 823\n                     },\n                     \"duration\" : {\n                        \"text\" : \"1 min\",\n                        \"value\" : 64\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 18.4891658,\n                        \"lng\" : -69.9524732\n                     },\n                     \"html_instructions\" : \"En la rotonda, toma la \\u003cb\\u003esegunda\\u003c/b\\u003e salida\",\n                     \"maneuver\" : \"roundabout-right\",\n                     \"polyline\" : {\n                        \"points\" : \"g}yoBda_jLLMFMBEBG@I@G@G?G?KAGAGCQGKCGKME_@CQ?[DkA?sAGsA_@eDwDaZAA\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 18.4880444,\n                        \"lng\" : -69.96003089999999\n                     },\n                     \"travel_mode\" : \"DRIVING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"1,5 km\",\n                        \"value\" : 1502\n                     },\n                     \"duration\" : {\n                        \"text\" : \"5 min\",\n                        \"value\" : 281\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 18.4841474,\n                        \"lng\" : -69.94071339999999\n                     },\n                     \"html_instructions\" : \"Continúa por \\u003cb\\u003eAv. de los Próceres\\u003c/b\\u003e.\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePasa por El Arabe Burger (a la derecha a 900&nbsp;m)\\u003c/div\\u003e\",\n                     \"polyline\" : {\n                        \"points\" : \"idzoB|q}iLQoBG}AOmD[iJDoA?KDoALmAXmBhCqI?AhCkIz@cCf@y@r@m@^WlGoCr@]dCoAnAm@LIJG\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 18.4891658,\n                        \"lng\" : -69.9524732\n                     },\n                     \"travel_mode\" : \"DRIVING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"1,2 km\",\n                        \"value\" : 1171\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 min\",\n                        \"value\" : 214\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 18.4748466,\n                        \"lng\" : -69.935512\n                     },\n                     \"html_instructions\" : \"Continúa por \\u003cb\\u003eAv. Abraham Lincoln\\u003c/b\\u003e.\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePasa por Santo Domingo Motors (a la izquierda).\\u003c/div\\u003e\",\n                     \"polyline\" : {\n                        \"points\" : \"}dyoBlh{iLLGPK|@i@dBs@rK}E`O_HnJmEdCiA`Ae@BAlD}A?A\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 18.4841474,\n                        \"lng\" : -69.94071339999999\n                     },\n                     \"travel_mode\" : \"DRIVING\"\n                  },\n                  {\n                     \"distance\" : {\n                        \"text\" : \"0,8 km\",\n                        \"value\" : 756\n                     },\n                     \"duration\" : {\n                        \"text\" : \"4 min\",\n                        \"value\" : 236\n                     },\n                     \"end_location\" : {\n                        \"lat\" : 18.4753661,\n                        \"lng\" : -69.9285319\n                     },\n                     \"html_instructions\" : \"Gira a la \\u003cb\\u003eizquierda\\u003c/b\\u003e en Trabajo Genesis hacia \\u003cb\\u003eAvenida Gustavo Mejia Ricart\\u003c/b\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003ePasa por Seguros Alpha (a la derecha).\\u003c/div\\u003e\\u003cdiv style=\\\"font-size:0.9em\\\"\\u003eEl destino está a la derecha.\\u003c/div\\u003e\",\n                     \"maneuver\" : \"turn-left\",\n                     \"polyline\" : {\n                        \"points\" : \"yjwoB|gziLCGEKoAqCUi@ISIYG_@A[@aA@[L}HBuA@SDaEFcF@_AAmA\"\n                     },\n                     \"start_location\" : {\n                        \"lat\" : 18.4748466,\n                        \"lng\" : -69.935512\n                     },\n                     \"travel_mode\" : \"DRIVING\"\n                  }\n               ],\n               \"traffic_speed_entry\" : [],\n               \"via_waypoint\" : []\n            }\n         ],\n         \"overview_polyline\" : {\n            \"points\" : \"cyyoBrz_jLoA~B_@o@_@kAQaAIy@AeANmLH}CNc@T[FMDa@Gm@KSKME_@Cm@DkA?sAGsA_@eDyDcZQoBG}Ak@wOD{ADoALmAXmBhCqIhCmIz@cCf@y@rAeA`ImDtE}Bx@e@|@i@dBs@t[}NzP_IlD_BISeB{DSm@I{@B}AXiRFqJ\"\n         },\n         \"summary\" : \"Av. de los Próceres\",\n         \"warnings\" : [],\n         \"waypoint_order\" : []\n      }\n   ],\n   \"status\" : \"OK\"\n}";

    /**
     * Create a private constructor because no one should ever create a {@link } object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private DirectionsApiUtils() {
    }

    public static List<Route> fetchRouteData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and create an {@link Event} object
        List<Route> routeList = null;
        try {
            routeList = extractRoutes(jsonResponse);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Couldn't extrar the Route List from the extractRoutes method stacktrace:\n");
            e.printStackTrace();
        }

        // Return the {@link Event}
        return routeList;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }


    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }


    /**
     * Return a list of {@link LatLng} objects that has been built up from
     * parsing a JSON response.
     */
    private static List<Route> extractRoutes(String routesJSONResponse) throws Exception {
        try {
            List<Route> routeList = new ArrayList<Route>();
            final JSONObject jSONObject = new JSONObject(routesJSONResponse);
            JSONArray routeJSONArray = jSONObject.getJSONArray(ROUTES);
            Route route;
            JSONObject routesJSONObject;
            for (int m = 0; m < routeJSONArray.length(); m++) {
                route = new Route();

                routesJSONObject = routeJSONArray.getJSONObject(m);
                JSONArray legsJSONArray;

                route.setSummary(routesJSONObject.getString(SUMMARY));

                Bound routeBounds = new Bound();
                LatLng routeNorthEastBoundLatLng = new LatLng(routesJSONObject.getJSONObject(BOUNDS).getJSONObject(NORTHEAST).getDouble(LATITUDE),
                                                        routesJSONObject.getJSONObject(BOUNDS).getJSONObject(NORTHEAST).getDouble(LONGITUDE));
                routeBounds.setNorthEast(routeNorthEastBoundLatLng);

                LatLng routeSouthWestBoundLatLng = new LatLng(routesJSONObject.getJSONObject(BOUNDS).getJSONObject(SOUTHWEST).getDouble(LATITUDE),
                        routesJSONObject.getJSONObject(BOUNDS).getJSONObject(SOUTHWEST).getDouble(LONGITUDE));
                routeBounds.setSouthWest(routeSouthWestBoundLatLng);

                route.setOverviewPolyLine(decodeOverviewPolyline(routesJSONObject.getJSONObject(OVERVIEW_POLYLINE).getString(POINTS)));

                legsJSONArray = routesJSONObject.getJSONArray(LEGS);


                JSONObject legJSONObject;
                Leg leg;

                JSONArray stepsJSONArray;

                for (int b = 0; b < legsJSONArray.length(); b++) {
                    leg = new Leg();
                    legJSONObject = legsJSONArray.getJSONObject(b);
                    leg.setDistance(new Distance(legJSONObject.optJSONObject(DISTANCE).optString(TEXT), legJSONObject.optJSONObject(DISTANCE).optLong(VALUE)));
                    leg.setDuration(new Duration(legJSONObject.optJSONObject(DURATION).optString(TEXT), legJSONObject.optJSONObject(DURATION).optLong(VALUE)));
                    stepsJSONArray = legJSONObject.getJSONArray(STEPS);
                    JSONObject stepJSONObject, stepDurationJSONObject, legPolyLineJSONObject, stepStartLocationJSONObject, stepEndLocationJSONObject;
                    Step step;
                    String encodedString;
                    LatLng stepStartLocationLatLng, stepEndLocationLatLng;
                    for (int i = 0; i < stepsJSONArray.length(); i++) {
                        stepJSONObject = stepsJSONArray.getJSONObject(i);
                        step = new Step();
                        JSONObject stepDistanceJSONObject = stepJSONObject.getJSONObject(DISTANCE);
                        step.setDistance(new Distance(stepDistanceJSONObject.getString(TEXT), stepDistanceJSONObject.getLong(VALUE)));
                        stepDurationJSONObject = stepJSONObject.getJSONObject(DURATION);
                        step.setDuration(new Duration(stepDurationJSONObject.getString(TEXT), stepDurationJSONObject.getLong(VALUE)));
                        stepEndLocationJSONObject = stepJSONObject.getJSONObject(END_LOCATION);
                        stepEndLocationLatLng = new LatLng(stepEndLocationJSONObject.getDouble(LATITUDE), stepEndLocationJSONObject.getDouble(LONGITUDE));
                        step.setEndLocation(stepEndLocationLatLng);
                        step.setHtmlInstructions(stepJSONObject.getString(HTML_INSTRUCTION));
                        legPolyLineJSONObject = stepJSONObject.getJSONObject(POLYLINE);
                        encodedString = legPolyLineJSONObject.getString(POINTS);
                        step.setPoints(decodePolyLines(encodedString));
                        stepStartLocationJSONObject = stepJSONObject.getJSONObject(START_LOCATION);
                        stepStartLocationLatLng = new LatLng(stepStartLocationJSONObject.getDouble(LATITUDE), stepStartLocationJSONObject.getDouble(LONGITUDE));
                        step.setStartLocation(stepStartLocationLatLng);
                        leg.addStep(step);
                    }
                    route.addLeg(leg);
                }
                routeList.add(route);
            }
            return routeList;
        } catch (Exception e) {
            throw e;
        }
    }

    private static List<LatLng> decodePolyLines(String encodedString) {
        return PolyUtil.decode(encodedString);
    }

    private static List<LatLng> decodeOverviewPolyline(String encodedString){
        List<LatLng> polylineLatLngList = PolyUtil.decode(encodedString);
        return polylineLatLngList;
    }

}


