package com.example.studyproject1;

import static java.net.HttpURLConnection.HTTP_OK;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class Utils {
    final static String groupsUrl = "https://timetable.magtu.ru/api/v2/groups";
    final static String findUrl = "https://timetable.magtu.ru/api/v2/search?q=";
    final static String scheduleUrl = "https://timetable.magtu.ru/";

    public static JSONArray searchGroupsOnline(String word) {
        String url = findUrl+word;
        String json = http(url);
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.remove("url_name");
            }
            return jsonArray;
        } catch (Exception e) {
            return null;
        }
    }
    public static String http(String url){
        try {
            // Получаем
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Результаты
            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    response.append(inputLine);
                }
                in.close();
                if(response != null) {
                    Log.d("log", url);
                    Log.d("log", response.toString());
                    return response.toString();
                }
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return "";
    }
}
