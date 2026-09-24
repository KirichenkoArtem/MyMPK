package com.example.studyproject1;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class FastLog {
    Toast toast = null;
    public void toast(Context context, String text){
        if (toast != null) {
            toast.cancel();
        }
        toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setText(text);
        toast.show();

        Log.i("FastLog — Toast", text);
    }
    public void log(String text){
        Log.i("FastLog — Log.i", text);
    }
}
