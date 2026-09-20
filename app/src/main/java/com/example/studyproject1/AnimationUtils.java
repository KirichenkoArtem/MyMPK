package com.example.studyproject1;

import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class AnimationUtils {
    public static void doMoveAndScaleAnimate(
            View view,
            long duration,
            float moveToX,
            float moveToY,
            float scaleX,
            float scaleY,
            Runnable runnable
    ){
        view.animate()
                .translationY(moveToY)
                .translationX(moveToX)
                .scaleX(scaleX)
                .scaleY(scaleY)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(duration)
                .setListener(null)
                .withEndAction(runnable)
                .start();
    }
}
