package com.example.studyproject1;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import org.json.JSONArray;

public class SetFragment extends Fragment {

    private Thread threadForSearch;
    private Activity activity;
    private AutoCompleteTextView groupNameInput;
    private TextView goNextTV;
    private TextView upperTV;
    private String selectedUrl;
    float screenHeight;
    private JSONArray groups;
    FastLog FastLog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        activity = requireActivity();
        FastLog = ((MainActivity) requireActivity()).FastLog;

        goNextTV = activity.findViewById(R.id.goNext);
        upperTV = activity.findViewById(R.id.upperText);
        groupNameInput = activity.findViewById(R.id.groupNameInput);

        goNextTV.setVisibility(View.INVISIBLE);
        goNextTV.setOnClickListener(v -> next());

        groupNameInput.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            selectedUrl = (item != null) ? item.toString() : null;
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(groupNameInput.getWindowToken(), 0);
            }
        });

        groupNameInput.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null) onChange(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });

        playEnterAnimation();
    }
    private void playEnterAnimation() {
        if (groupNameInput == null || goNextTV == null || upperTV == null) return;

        final float h = screenHeight;

        // Стартовые позиции за экраном (мгновенно, duration = 0)
        AnimationUtils.doMoveAndScaleAnimate(upperTV,        0, 0, -h, 1f, 1f, null);
        AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 0, 0,  h, 1f, 1f, null);
        AnimationUtils.doMoveAndScaleAnimate(goNextTV,       0, 0,  h, 1f, 1f, null);

        // 1) Заголовок сверху — первым
        AnimationUtils.doMoveAndScaleAnimate(upperTV, 400, 0, 0, 1f, 1f, null);

        // 2) Поле ввода — снизу, +100 мс
        upperTV.postDelayed(() ->
                        AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 400, 0, 0, 1f, 1f, null),
                100);

        // 3) Кнопка — снизу, +180 мс
        upperTV.postDelayed(() ->
                        AnimationUtils.doMoveAndScaleAnimate(goNextTV, 400, 0, 0, 1f, 1f, null),
                180);
    }
    private void playExitAnimation(Runnable onEnd) {
        if (groupNameInput == null || goNextTV == null || upperTV == null) {
            if (onEnd != null) onEnd.run();
            return;
        }

        // Не даём повторно тапнуть во время уборки
        goNextTV.setClickable(false);
        groupNameInput.setEnabled(false);

        final float h = screenHeight;

        // Поле ввода уезжает вниз
        AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 320, 0, h, 1f, 1f, null);

        // Кнопка уезжает вниз с задержкой (обратный стаггер)
        groupNameInput.postDelayed(() ->
                        AnimationUtils.doMoveAndScaleAnimate(goNextTV, 320, 0, h, 1f, 1f, null),
                70);

        // Заголовок улетает вверх — и это самая длинная анимация,
        // поэтому колбэк навигации вешаем именно на неё
        AnimationUtils.doMoveAndScaleAnimate(upperTV, 400, 0, -h, 1f, 1f, onEnd);
    }

    private void onChange(String new_text){
        selectedUrl = null;
        if (threadForSearch != null && threadForSearch.isAlive()) threadForSearch.interrupt();

        if (groupNameInput.isPerformingCompletion()){
            goNextTV.setVisibility(View.VISIBLE);
            return;
        }else{
            goNextTV.setVisibility(View.INVISIBLE);
        }
        threadForSearch = new Thread(() -> {
            try {
                groups = Utils.searchGroupsOnline(new_text);
                if(groups != null && groups.length()!=0) {
                    String[] array = new String[groups.length()];
                    for (int i = 0; i < groups.length(); i++) {
                        array[i] = groups.getJSONObject(i).getString("url");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            activity, android.R.layout.simple_dropdown_item_1line,
                            array
                    );
                    activity.runOnUiThread(() -> {
                        groupNameInput.setAdapter(adapter);
                        groupNameInput.showDropDown();
                    });
                }
            } catch (Exception e) {
                FastLog.log("setFragment onChangeText"+e);
            }
        });
        threadForSearch.start();
    }


    private void next() {
        String groupName = selectedUrl;
        if (groupName == null || groupName.isEmpty()) {
            FastLog.toast(activity, "Ошибка названия группы! 😨");
            return;
        }

        new Thread(() -> {
            SharedPreferences sharedPreferences = activity.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            sharedPreferences.edit().putString("group_name", groupName).apply();
            // Префетч расписания в кэш, чтобы Day/Week экраны открылись мгновенно
            try {
                org.json.JSONArray sched = Utils.getSchedule(groupName);
                if (sched != null) ScheduleCache.save(activity, groupName, sched.toString());
            } catch (Exception ignored) {}
        }).start();

        playExitAnimation(this::goToSchedule);
    }

    private void goToSchedule(){
        ((MainActivity) requireActivity()).onGroupSelected();
    }

}
