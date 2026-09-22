package com.example.studyproject1;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.JsonToken;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class ScheduleFragment extends Fragment {

    Activity activity;
    LinearLayout upperLinearLayout;
    LinearLayout scrollableLL;
    TextView textView;
    int screenHeight;
    String group_name;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        activity = requireActivity();
        upperLinearLayout = activity.findViewById(R.id.upperLinearLayout);
        textView = activity.findViewById(R.id.groupNameTV);
        scrollableLL = activity.findViewById(R.id.scrollableLL);

        SharedPreferences sharedPreferences = activity.getSharedPreferences("Prefs", MODE_PRIVATE);
        group_name = sharedPreferences.getString("group_name","none");

        textView.setText(group_name);

        AnimationUtils.doMoveAndScaleAnimate(upperLinearLayout, 0, 0, -screenHeight, 1, 1, ()->{
            AnimationUtils.doMoveAndScaleAnimate(upperLinearLayout, 800, 0, 150, 1, 1, ()->{
                AnimationUtils.doMoveAndScaleAnimate(upperLinearLayout, 200, 0, 0, 1, 1, ()->{
                    updateSchedule();
                });
            });
        });
    }

    private void updateSchedule(){
        SharedPreferences sharedPreferences = activity.getSharedPreferences("Prefs", MODE_PRIVATE);
        String group_name = sharedPreferences.getString("group_name","none");
        if (scrollableLL == null || group_name == "none") return;
        scrollableLL.removeAllViews();

        new Thread(()->{
            try {
                JSONArray schedule = Utils.getSchedule(group_name);
                Log.d("Tag",schedule.toString());
                activity.runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < schedule.length(); i++) {
                            JSONArray lessons = schedule.getJSONObject(i).getJSONArray("lessons");
                            LinearLayout weekdayLL = (LinearLayout) getLayoutInflater().inflate(R.layout.schedule_item, scrollableLL, false);

                            for (int j = 0; j < lessons.length(); j++) {
                                JSONObject lesson = lessons.getJSONObject(j);

                                LinearLayout lessonItemLL = (LinearLayout) getLayoutInflater().inflate(R.layout.lesson_item, weekdayLL, false);
                                ((TextView) lessonItemLL.findViewById(R.id.number)).setText(lesson.getString("number"));
                                ((TextView) lessonItemLL.findViewById(R.id.lesson_name)).setText(lesson.getString("title"));
                                ((TextView) lessonItemLL.findViewById(R.id.teacher_fio)).setText(lesson.getString("teacher"));
                                ((TextView) lessonItemLL.findViewById(R.id.lesson_type)).setText(lesson.getString("clearfix"));
                                ((TextView) lessonItemLL.findViewById(R.id.lesson_time)).setText(lesson.getString("time"));
                                ((TextView) lessonItemLL.findViewById(R.id.lesson_classroom)).setText(lesson.getString("auditory"));
                                weekdayLL.addView(lessonItemLL);
                            }

                            scrollableLL.addView(weekdayLL);
                        }
                    } catch (Exception e){
                        Log.e("updateSchedule_ui", e.toString());
                    }
                });
            }catch (Exception e){
                Log.e("updateSchedule", e.toString());
            }
        }).start();

    }
}
