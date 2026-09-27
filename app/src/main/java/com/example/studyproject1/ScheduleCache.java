package com.example.studyproject1;

import android.content.Context;
import org.json.JSONArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class ScheduleCache {
    private static final String DIR = "schedule_cache";
    private static final long STALE_MS = 3L * 60 * 60 * 1000;

    private static File file(Context c, String group) {
        File dir = new File(c.getFilesDir(), DIR);
        if (!dir.exists()) dir.mkdirs();
        String safe = group.replaceAll("[^a-zA-Z0-9._-]", "_");
        return new File(dir, safe + ".json");
    }

    public static void save(Context c, String group, String json) {
        try (FileOutputStream fos = new FileOutputStream(file(c, group))) {
            fos.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ignored) {}
    }

    public static JSONArray load(Context c, String group) {
        try {
            File f = file(c, group);
            if (!f.exists()) return null;
            byte[] buf = new byte[(int) f.length()];
            try (FileInputStream fis = new FileInputStream(f)) { fis.read(buf); }
            return new JSONArray(new String(buf, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    public static long lastModified(Context c, String group) {
        File f = file(c, group);
        return f.exists() ? f.lastModified() : 0L;
    }

    public static boolean isStale(Context c, String group) {
        long t = lastModified(c, group);
        return t == 0 || (System.currentTimeMillis() - t) > STALE_MS;
    }
}