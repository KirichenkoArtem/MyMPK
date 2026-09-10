package com.example.studyproject1;

import static java.net.HttpURLConnection.HTTP_OK;

import android.app.appsearch.SearchResult;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        new Thread(()->{
            String[] allGroups = getGroupsNames();
            Log.d("MyLog","allGroups: "+allGroups.length);


            String[] matches = searchGroupsOnline("*",10);
            Log.d("MyLog","findMatchOf:\n"+ Arrays.toString(matches));

        }).start();
    }

    final String groupsUrl = "https://timetable.magtu.ru/api/v2/groups";
    final String findUrl = "https://timetable.magtu.ru/api/v2/search?q=";
    final String scheduleUrl = "https://timetable.magtu.ru/{url}";
    private String[] getGroupsNames(){
        try {
            // Получаем
            URL url = new URL(groupsUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
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

                // json string to string[]
                JSONArray jsonArray = new JSONArray(response.toString());
                String[] array = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++){
                    array[i] = jsonArray.getString(i);
                }
                return array;
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return new String[]{};
    }
    public String[] searchGroupsOnline(String word, int limit) {
        try {

            URL url = new URL(findUrl + word);
            String json = ""; // Твой метод для HTTP-запроса
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            int responseCode = connection.getResponseCode();
            if (responseCode == HTTP_OK){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null){
                    response.append(inputLine);
                }
                in.close();
                json = response.toString();
            }
            if (json.isEmpty()) return new String[]{};

            JSONArray jsonArray = new JSONArray(json);
            int resultSize = Math.min(limit, jsonArray.length());
            String[] names = new String[resultSize];

            for (int i = 0; i < resultSize; i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                // Поле "name" от API уже содержит уточнение подгруппы
                names[i] = obj.getString("name");
            }
            return names;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}