package com.task.worldmap;

import android.content.Context;

import com.task.worldmap.storage.DBHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

    public class HandlePlaceApi {



        public ArrayList<String> autoComplete(String input) {
            ArrayList<String> arrayList = new ArrayList();
            HttpURLConnection connection = null;
            StringBuilder jsonResult = new StringBuilder();

            //maps.googleapis.com/maps/api/place/autocomplete/json?input=&types=(city)&location=latitude,longitude&radius=1000&key=YOUR_API_KEY
            try {
                //            sb.append("&components=country:DE");
                String sb = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" + "input=" + input +
                        "&key=AIzaSyC0XrsVQGJLBNJ5S4COV1sDqzfoeRG4fto";
                URL url = new URL(sb);
                connection = (HttpURLConnection) url.openConnection();
                InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream());

                int read;

                char[] buff = new char[1024];
                while ((read = inputStreamReader.read(buff)) != -1) {
                    jsonResult.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            try {
                JSONObject jsonObject = new JSONObject(jsonResult.toString());
                JSONArray prediction = jsonObject.getJSONArray("predictions");
                for (int i = 0; i < prediction.length(); i++) {
                    arrayList.add(prediction.getJSONObject(i).getString("description"));//+"+" + prediction.getJSONObject(i).getString("place_id"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return arrayList;
        }
    }

