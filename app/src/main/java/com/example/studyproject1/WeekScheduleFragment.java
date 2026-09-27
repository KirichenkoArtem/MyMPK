package com.example.studyproject1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WeekScheduleFragment extends Fragment {

    private Activity activity;
    private LinearLayout daysContainer;
    private TextView groupNameTV, subtitleTV;
    private ProgressBar loadingProgress;
    private View updateButton, updateIV, scroll;
    private FastLog FastLog;
    private String group;
    private String cachedJson;
    private int scrollToIdx = -1;
    private volatile boolean viewAlive = true;
    private boolean rendered = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_week_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        viewAlive = true;
        rendered = false;
        activity = requireActivity();
        FastLog = ((MainActivity) activity).FastLog;
        group = activity.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
                .getString("group_name", "");

        groupNameTV = v.findViewById(R.id.groupNameTV);
        subtitleTV = v.findViewById(R.id.subtitleTV);
        updateButton = v.findViewById(R.id.updateButton);
        updateIV = v.findViewById(R.id.updateIV);
        daysContainer = v.findViewById(R.id.daysContainer);
        scroll = v.findViewById(R.id.scroll);
        loadingProgress = v.findViewById(R.id.loadingProgress);

        groupNameTV.setText(group);
        subtitleTV.setText("Расписание на 2 недели");
        updateButton.setOnClickListener(x -> refresh(updateIV));

        v.findViewById(R.id.changeGroupButton).setOnClickListener(x ->
                ((MainActivity) requireActivity()).onGroupCleared());

        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(220).start();

        showInitial();
    }

    @Override
    public void onDestroyView() {
        viewAlive = false;
        super.onDestroyView();
    }

    private void showInitial() {
        JSONArray cached = ScheduleCache.load(activity, group);
        boolean cacheHit = cached != null && cached.length() > 0;

        if (cacheHit) {
            cachedJson = cached.toString();
            loadingProgress.setVisibility(View.GONE);
            renderChunked(cached);
            rendered = true;
        } else {
            loadingProgress.setVisibility(View.VISIBLE);
        }

        if (!cacheHit || ScheduleCache.isStale(activity, group)) {
            fetchAndApply(false, null);
        }
    }

    private void refresh(View icon) {
        icon.animate().rotationBy(360f).setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator()).start();
        fetchAndApply(true, icon);
    }

    private void fetchAndApply(boolean force, @Nullable View icon) {
        new Thread(() -> {
            try {
                JSONArray fresh = Utils.getSchedule(group);
                if (fresh == null || fresh.length() == 0) {
                    if (viewAlive) activity.runOnUiThread(() ->
                            loadingProgress.setVisibility(View.GONE));
                    return;
                }
                final String freshStr = fresh.toString();
                ScheduleCache.save(activity, group, freshStr);

                if (!viewAlive || !isAdded()) return;

                if (freshStr.equals(cachedJson)) {
                    activity.runOnUiThread(() -> loadingProgress.setVisibility(View.GONE));
                    return;
                }

                activity.runOnUiThread(() -> {
                    if (!viewAlive || getView() == null) return;
                    cachedJson = freshStr;
                    loadingProgress.setVisibility(View.GONE);
                    if (!rendered) {
                        renderChunked(fresh);
                        rendered = true;
                    } else {
                        crossfadeReplace(fresh);
                    }
                });
            } catch (Exception e) {
                FastLog.log("WeekSchedule error: " + e);
                if (viewAlive) activity.runOnUiThread(() ->
                        loadingProgress.setVisibility(View.GONE));
            }
        }).start();
    }

    private void crossfadeReplace(JSONArray data) {
        daysContainer.animate().alpha(0f).setDuration(140).withEndAction(() -> {
            if (!viewAlive) return;
            daysContainer.removeAllViews();
            scrollToIdx = -1;
            renderChunked(data);
            daysContainer.setAlpha(0f);
            daysContainer.animate().alpha(1f).setDuration(200).start();
        }).start();
    }

    private void renderChunked(JSONArray schedule) {
        if (!isAdded() || getContext() == null) return;
        renderDayAt(schedule, 0);
    }

    private void renderDayAt(JSONArray schedule, int i) {
        if (!viewAlive || !isAdded() || getView() == null) return;
        if (i >= schedule.length()) {
            scrollToClosest();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        Calendar cal = Calendar.getInstance();
        int todayKey = (cal.get(Calendar.MONTH) + 1) * 100 + cal.get(Calendar.DAY_OF_MONTH);

        try {
            JSONObject day = schedule.getJSONObject(i);
            LinearLayout dayView = (LinearLayout) inflater.inflate(
                    R.layout.schedule_item, daysContainer, false);
            LinearLayout container = dayView.findViewById(R.id.lessonsContainer);

            String dateStr = day.optString("date");
            ((TextView) dayView.findViewById(R.id.weekdayTV)).setText(day.optString("weekday"));
            ((TextView) dayView.findViewById(R.id.date_TV)).setText(dateStr);
            ((TextView) dayView.findViewById(R.id.chetnost_TV)).setText(day.optString("weekname"));

            int key = dateKey(dateStr);
            if (scrollToIdx < 0 && key >= todayKey) scrollToIdx = i;

            boolean today = isToday(dateStr);
            JSONArray lessons = day.getJSONArray("lessons");

            if (lessons.length() == 0) {
                TextView empty = new TextView(requireContext());
                empty.setText("Пар нет");
                empty.setGravity(android.view.Gravity.CENTER);
                int pad = (int)(getResources().getDisplayMetrics().density * 20);
                empty.setPadding(0, pad, 0, pad);
                empty.setTextColor(0xFF666666);
                empty.setTextSize(14);
                container.addView(empty);
            } else {
                for (int j = 0; j < lessons.length(); j++) {
                    JSONObject lesson = lessons.getJSONObject(j);
                    View item = inflater.inflate(R.layout.lesson_item, container, false);

                    ((TextView) item.findViewById(R.id.number)).setText(lesson.optString("number"));
                    ((TextView) item.findViewById(R.id.lesson_name)).setText(lesson.optString("title"));
                    ((TextView) item.findViewById(R.id.teacher_fio)).setText(lesson.optString("teacher"));
                    ((TextView) item.findViewById(R.id.lesson_type)).setText(lesson.optString("type"));
                    ((TextView) item.findViewById(R.id.lesson_time)).setText(lesson.optString("time"));
                    ((TextView) item.findViewById(R.id.lesson_classroom)).setText(lesson.optString("auditory"));

                    if (today && isLessonNow(lesson.optString("time"))) {
                        item.findViewById(R.id.activeTint).setVisibility(View.VISIBLE);
                    }

                    bindNote(item, lesson.optString("title"), lesson.optString("teacher"));
                    attachNoteClick(item, lesson.optString("title"), lesson.optString("teacher"));
                    container.addView(item);
                }
            }

            dayView.setAlpha(0f);
            dayView.setTranslationY(16f);
            dayView.animate().alpha(1f).translationY(0f).setDuration(180).start();

            daysContainer.addView(dayView);
        } catch (Exception e) {
            FastLog.log("renderDayAt(" + i + "): " + e);
        }

        daysContainer.post(() -> renderDayAt(schedule, i + 1));
    }

    private void scrollToClosest() {
        if (scrollToIdx < 0) return;
        final int idx = scrollToIdx;
        daysContainer.post(() -> {
            if (!viewAlive || daysContainer == null) return;
            View target = daysContainer.getChildAt(idx);
            if (target != null)
                ((android.widget.ScrollView) scroll).smoothScrollTo(0, target.getTop() - 20);
        });
    }

    private void refreshNotes() {
        if (cachedJson == null) return;
        try {
            JSONArray arr = new JSONArray(cachedJson);
            daysContainer.removeAllViews();
            scrollToIdx = -1;
            renderChunked(arr);
        } catch (Exception e) {
            FastLog.log("refreshNotes: " + e);
        }
    }

    private int dateKey(String date) {
        if (date == null || date.equals("—")) return Integer.MAX_VALUE;
        String[] p = date.trim().toLowerCase().split("\\s+");
        if (p.length < 2) return Integer.MAX_VALUE;
        Map<String,Integer> M = new HashMap<>();
        M.put("января",1); M.put("февраля",2); M.put("марта",3);
        M.put("апреля",4); M.put("мая",5); M.put("июня",6);
        M.put("июля",7); M.put("августа",8); M.put("сентября",9);
        M.put("октября",10); M.put("ноября",11); M.put("декабря",12);
        try {
            int d = Integer.parseInt(p[0]);
            Integer m = M.get(p[1]);
            return m == null ? Integer.MAX_VALUE : m * 100 + d;
        } catch (Exception e) { return Integer.MAX_VALUE; }
    }

    private boolean isToday(String dateStr) {
        Calendar c = Calendar.getInstance();
        String[] names = {"января","февраля","марта","апреля","мая","июня",
                "июля","августа","сентября","октября","ноября","декабря"};
        String today = c.get(Calendar.DAY_OF_MONTH) + " " + names[c.get(Calendar.MONTH)];
        return today.equalsIgnoreCase(dateStr == null ? "" : dateStr.trim());
    }

    private int parseHM(String s) {
        try {
            String[] p = s.trim().split(":");
            return Integer.parseInt(p[0]) * 60 + Integer.parseInt(p[1]);
        } catch (Exception e) { return -1; }
    }

    private boolean isLessonNow(String timeStr) {
        if (timeStr == null) return false;
        String[] parts = timeStr.split("\\s*[—\\-–]\\s*");
        if (parts.length < 2) return false;
        int start = parseHM(parts[0]);
        int end = parseHM(parts[1]);
        if (start < 0 || end < 0) return false;
        Calendar c = Calendar.getInstance();
        int now = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
        return now >= start && now < end;
    }

    private void bindNote(View item, String title, String teacher) {
        String note = NotesStorage.get(activity, group, title, teacher);
        TextView noteTV = item.findViewById(R.id.noteTV);
        View noteLine = item.findViewById(R.id.noteLine);
        if (note != null && !note.isEmpty()) {
            noteTV.setText(note);
            noteTV.setVisibility(View.VISIBLE);
            noteLine.setVisibility(View.VISIBLE);
        } else {
            noteTV.setVisibility(View.GONE);
            noteLine.setVisibility(View.GONE);
        }
    }

    private void attachNoteClick(View item, String title, String teacher) {
        item.setOnClickListener(v -> {
            NoteBottomSheet bs = NoteBottomSheet.newInstance(group, title, teacher);
            bs.setOnNoteChanged(this::refreshNotes);
            bs.show(getChildFragmentManager(), "note");
        });
    }
}