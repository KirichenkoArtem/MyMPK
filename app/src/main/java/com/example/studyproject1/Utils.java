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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class Utils {
    // Urls
    final static String groupsUrl = "https://timetable.magtu.ru/api/v2/groups";
    final static String findUrl = "https://timetable.magtu.ru/api/v2/search?q=";
    final static String scheduleUrl = "https://timetable.magtu.ru/";

    // Months
    private static final Map<String, Integer> MONTHS = new HashMap<>();
    static {
        MONTHS.put("января", 1);
        MONTHS.put("февраля", 2);
        MONTHS.put("марта", 3);
        MONTHS.put("апреля", 4);
        MONTHS.put("мая", 5);
        MONTHS.put("июня", 6);
        MONTHS.put("июля", 7);
        MONTHS.put("августа", 8);
        MONTHS.put("сентября", 9);
        MONTHS.put("октября", 10);
        MONTHS.put("ноября", 11);
        MONTHS.put("декабря", 12);
    }

    private static int dateKey(String date) {
        if (date == null || date.equals("—")) return Integer.MAX_VALUE;
        String[] p = date.trim().toLowerCase().split("\\s+");
        if (p.length < 2) return Integer.MAX_VALUE;
        try {
            int day = Integer.parseInt(p[0]);
            Integer month = MONTHS.get(p[1]);
            if (month == null) return Integer.MAX_VALUE;
            return month * 100 + day;
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
    public static JSONArray getSchedule(String group_url) {
        // Получет ссылку на группу
        // Парсит данные из html `scheduleUrl+group_url`
        // Возвращает JSONArray дней

        String html = http(scheduleUrl + group_url);
        try {
            // Получаем сам html и создаём пустой JSONArray дней
            Document doc = Jsoup.parse(html);
            JSONArray days = new JSONArray();
            // Проходим по всем `<div class=day>...</div>`
            for (Element dayEl : doc.select("div.day")) {
                // Экземпляр дня
                JSONObject day = new JSONObject();
                Element dayName = dayEl.selectFirst("div.day-name");
                Element weekName = null;
                // Проходим по всем соседям слева у всех родителей, чтобы найти `<div class=week-name>...</div>`
                for (Element el = dayEl; el != null && weekName == null; el = el.parent()) {
                    weekName = el.previousElementSiblings().select("div.week-name").first();
                }
                String[] weekday_date = dayName.text().split("\\s+", 2);
                // Заполняем день его информацией
                day.put("weekday", weekday_date.length>1 ? weekday_date[0] : "—");
                day.put("date", weekday_date.length>1 ? weekday_date[1] : "—");
                day.put("weekname", weekName != null ? weekName.ownText().trim() : "—");
                // Экземпляр уроков, урока и проходим по `<div class=less>...</div>`
                JSONArray lessons = new JSONArray();
                for (Element lessonEl : dayEl.select("div.less")) {
                    JSONObject lesson = new JSONObject();
                    // Берём все данные с урока
                    Element title = lessonEl.selectFirst("div.title");
                    Element clearfix = lessonEl.selectFirst("div.ad.clearfix");
                    Element teacher  = lessonEl.selectFirst("div.teacher");
                    Element auditory = lessonEl.selectFirst("div.aud");
                    Element time = lessonEl.selectFirst("div.time");
                    Element number = lessonEl.selectFirst("div.couple-number");
                    // Заполняем экземпляр урока
                    lesson.put("title",  title  != null ? title.text()  : "");
                    lesson.put("type",  clearfix  != null ? clearfix.ownText().trim()  : "");
                    lesson.put("teacher",  teacher  != null ? teacher.text()  : "");
                    lesson.put("auditory",  auditory  != null ? auditory.text()  : "");
                    lesson.put("time",  time  != null ? time.text()  : "");
                    lesson.put("number",  number  != null ? number.text()  : "");
                    // Добавляем ко всем урокам
                    lessons.put(lesson);
                }
                // Добавляем уроки к дню и день к дням
                day.put("lessons", lessons);
                days.put(day);
            }
            // Собираем в List, сортируем, Возвращаем к JSONArray
            List<JSONObject> list = new ArrayList<>();
            for (int i = 0; i < days.length(); i++) {
                list.add(days.getJSONObject(i));
            }
            list.sort(Comparator.comparingInt(o -> dateKey(o.optString("date", ""))));
            days = new JSONArray();
            for (JSONObject o : list) days.put(o);
            return days;
        } catch (Exception e) {
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
                    return response.toString();
                }
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return "";
    }
}
