package com.example.studyproject1;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotesStorage {

    private static final String PREF = "NotesPrefs";
    private static final String SEP = "::";

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private static String nrm(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static String key(String group, String title, String teacher) {
        return nrm(group) + SEP + nrm(title) + SEP + nrm(teacher);
    }

    /** Возвращает текст заметки или null. */
    public static String get(Context c, String group, String title, String teacher) {
        String raw = sp(c).getString(key(group, title, teacher), null);
        if (raw == null) return null;
        try {
            JSONObject o = new JSONObject(raw);
            String t = o.optString("text", "");
            return t.isEmpty() ? null : t;
        } catch (Exception e) {
            // на всякий случай — старое значение было чистым текстом
            return raw.isEmpty() ? null : raw;
        }
    }

    public static void put(Context c, String group, String title, String teacher, String text) {
        if (text == null || text.trim().isEmpty()) {
            remove(c, group, title, teacher);
            return;
        }
        try {
            JSONObject o = new JSONObject();
            o.put("title", title == null ? "" : title);
            o.put("teacher", teacher == null ? "" : teacher);
            o.put("text", text.trim());
            sp(c).edit().putString(key(group, title, teacher), o.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static void remove(Context c, String group, String title, String teacher) {
        sp(c).edit().remove(key(group, title, teacher)).apply();
    }

    public static class Entry {
        public String title;
        public String teacher;
        public String text;
    }

    /** Все заметки группы. */
    public static List<Entry> getAll(Context c, String group) {
        List<Entry> out = new ArrayList<>();
        if (group == null) return out;
        String prefix = nrm(group) + SEP;
        for (Map.Entry<String, ?> e : sp(c).getAll().entrySet()) {
            if (!e.getKey().startsWith(prefix)) continue;
            if (!(e.getValue() instanceof String)) continue;
            try {
                JSONObject o = new JSONObject((String) e.getValue());
                Entry en = new Entry();
                en.title = o.optString("title", "");
                en.teacher = o.optString("teacher", "");
                en.text = o.optString("text", "");
                if (!en.text.isEmpty()) out.add(en);
            } catch (Exception ignored) {}
        }
        return out;
    }
}