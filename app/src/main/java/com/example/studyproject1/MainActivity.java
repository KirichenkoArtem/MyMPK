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
            List<Group> allGroups = getGroupsNames();
//            Log.d("MyLog","allGroups count: "+allGroups.length);
            Log.d("fff", "name: "+((Group)allGroups.get(0)).name+"; id:"+((Group)allGroups.get(0)).id+"; subgroup:"+((Group)allGroups.get(0)).subgroup);
            Log.d("fff", "name: "+((Group)allGroups.get(1)).name+"; id:"+((Group)allGroups.get(1)).id+"; subgroup:"+((Group)allGroups.get(1)).subgroup);


//            String[] matches = searchGroupsOnline("*",10);
//            Log.d("MyLog","findMatchOf:\n"+ Arrays.toString(matches));

        }).start();
    }





    // another class need
    final String groupsUrl = "https://timetable.magtu.ru/api/v2/groups";
    final String findUrl = "https://timetable.magtu.ru/api/v2/search?q=";
    final String scheduleUrl = "https://timetable.magtu.ru/";

    public static class Group{
        public int id;
        public String name;
        public int subgroup;
    }

    private List<Group> getGroupsNames(){
        List<Group> output = new ArrayList<>();
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
                Group newg = new Group();
                JSONObject counter = new JSONObject();
                for (int i = 0; i<jsonArray.length();i++){
                    newg.id = jsonArray.getJSONObject(i).getInt("id");
                    newg.name = jsonArray.getJSONObject(i).getString("name");
                    if(counter.has(newg.name)) {
                        counter.put(newg.name, counter.getInt(newg.name)+1);
                    }else{
                        counter.put(newg.name, 1);
                    }
                    newg.subgroup = counter.getInt(newg.name);
                    output.add(newg);
                    newg = new Group();
                }
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
        return output;
    }
}