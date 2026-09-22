package com.example.studyproject1;

import static java.net.HttpURLConnection.HTTP_OK;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.net.ssl.HttpsURLConnection;

public class Utils {
    final static String groupsUrl = "https://timetable.magtu.ru/api/v2/groups";
    final static String findUrl = "https://timetable.magtu.ru/api/v2/search?q=";
    final static String scheduleUrl = "https://timetable.magtu.ru/";



    public static JSONArray getSchedule(String group_url) {
        String html = http(scheduleUrl + group_url);
        try {
            Document doc = Jsoup.parse(html);
            JSONArray days = new JSONArray();
            Log.d("Tag222",scheduleUrl + group_url);
            for (Element dayEl : doc.select("div.day")) {
                JSONObject day = new JSONObject();
                Element dayName = dayEl.selectFirst("div.day-name");
                day.put("date", dayName != null ? dayName.text() : "");

                JSONArray lessons = new JSONArray();
                for (Element lessonEl : dayEl.select("div.less")) {
                    JSONObject lesson = new JSONObject();
                    Element title = lessonEl.selectFirst("div.title");
                    Element clearfix = lessonEl.selectFirst("div.ad.clearfix");
                    Element teacher  = lessonEl.selectFirst("div.teacher");
                    Element auditory = lessonEl.selectFirst("div.aud");
                    Element time = lessonEl.selectFirst("div.time");
                    Element number = lessonEl.selectFirst("div.couple-number");
                    lesson.put("title",  title  != null ? title.text()  : "");
                    lesson.put("clearfix",  clearfix  != null ? clearfix.ownText().trim()  : "");
                    lesson.put("teacher",  teacher  != null ? teacher.text()  : "");
                    lesson.put("auditory",  auditory  != null ? auditory.text()  : "");
                    lesson.put("time",  time  != null ? time.text()  : "");
                    lesson.put("number",  number  != null ? number.text()  : "");
                    lessons.put(lesson);
                }
                day.put("lessons", lessons);
                days.put(day);
            }
            return days;
        } catch (Exception e) {
            Log.e("Tag",e.toString());
            return null;
        }
    }
    public static JSONArray searchGroupsOnline(String word) {
        String url = findUrl+word;
        String json = http(url);
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            throw new RuntimeException(e);
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
