package com.example.studyproject1;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    FragmentContainerView fragmentContainerView;
    BottomNavigationView bottomNav;
    public FastLog FastLog = new FastLog();

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

        fragmentContainerView = findViewById(R.id.fragmentContainer);
        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            Class<?> target;
            int id = item.getItemId();
            if (id == R.id.nav_day)        target = DayScheduleFragment.class;
            else if (id == R.id.nav_week)  target = WeekScheduleFragment.class;
            else                            target = NotesFragment.class;

            if (current != null && current.getClass().equals(target)) return true;

            Fragment next;
            if (target == DayScheduleFragment.class)        next = new DayScheduleFragment();
            else if (target == WeekScheduleFragment.class)  next = new WeekScheduleFragment();
            else                                             next = new NotesFragment();

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fragmentContainer, next)
                    .commit();
            return true;
        });

        String group = getSharedPreferences("Prefs", Context.MODE_PRIVATE)
                .getString("group_name", "");
        if (group == null || group.isEmpty() || group.equals("none")) {
            // Группа не выбрана — только экран выбора
            bottomNav.setVisibility(View.GONE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new SetFragment())
                    .commit();
        } else {
            new Thread(() -> {
                try {
                    org.json.JSONArray sched = Utils.getSchedule(group);
                    if (sched != null) ScheduleCache.save(this, group, sched.toString());
                } catch (Exception ignored) {}
            }).start();
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.setSelectedItemId(R.id.nav_day);
        }
    }

    /** Вызывается из SetFragment после успешного выбора группы. */
    public void onGroupSelected() {
        if (bottomNav.getVisibility() != View.VISIBLE) {
            bottomNav.setAlpha(0f);
            bottomNav.setTranslationY(120f);
            bottomNav.setVisibility(View.VISIBLE);
            bottomNav.animate().alpha(1f).translationY(0f).setDuration(280).start();
        }
        bottomNav.setSelectedItemId(R.id.nav_day);
    }

    /** Сброс группы (например, с экрана заметок). */
    public void onGroupCleared() {
        getSharedPreferences("Prefs", Context.MODE_PRIVATE)
                .edit().remove("group_name").apply();
        bottomNav.animate().alpha(0f).translationY(120f).setDuration(220)
                .withEndAction(() -> bottomNav.setVisibility(View.GONE)).start();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fragmentContainer, new SetFragment())
                .commit();
    }
}