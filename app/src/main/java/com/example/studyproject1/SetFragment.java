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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import org.json.JSONArray;

public class SetFragment extends Fragment {

    private Thread threadForSearch;
    private Activity activity;
    private AutoCompleteTextView groupNameInput;
    private TextView goNextTV;
    private TextView upperTV;
    FragmentContainerView fragmentContainerView;
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
        goNextTV.setVisibility(View.INVISIBLE);
        goNextTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next();
            }
        });
        groupNameInput = activity.findViewById(R.id.groupNameInput);
        groupNameInput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                selectedUrl = (item != null) ? item.toString() : null;
            }
        });
        groupNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null) onChange(s.toString());
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });


        if (groupNameInput!=null && goNextTV!=null && upperTV!=null){
            AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 0, 0, screenHeight, 1, 1, ()->{
                AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 800, 0, -150, 1.25f, 1, ()->{
                    AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 200, 0, 0, 1, 1, null);
                });
            });
            AnimationUtils.doMoveAndScaleAnimate(upperTV, 0, 0, -screenHeight, 1, 1, ()->{
                AnimationUtils.doMoveAndScaleAnimate(upperTV, 800, 0, 150, 1.25f, 1, ()->{
                    AnimationUtils.doMoveAndScaleAnimate(upperTV, 200, 0, 0, 1, 1, null);
                });
            });
        }

    }

    private void onChange(String new_text){
        selectedUrl = null;
        if (threadForSearch != null && threadForSearch.isAlive()) threadForSearch.interrupt();

        if (groupNameInput.isPerformingCompletion()){
            goNextTV.setVisibility(View.VISIBLE);
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


    private void next(){
        String groupName = selectedUrl;
        if (groupName == null || groupName == ""){

            FastLog.toast(activity, "Ошибка названия группы! 😨");
        }else{
            new Thread(()->{
                SharedPreferences sharedPreferences = activity.getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("group_name", groupName);
                editor.apply();
            }).start();
            AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 125, 0, -125, 1.25f, 1, ()->{
                AnimationUtils.doMoveAndScaleAnimate(groupNameInput, 650, 0, screenHeight, 0f, 1, null);
            });
            AnimationUtils.doMoveAndScaleAnimate(goNextTV, 125, 0, -125, 1.25f, 1, ()->{
                AnimationUtils.doMoveAndScaleAnimate(goNextTV, 650, 0, screenHeight, 0f, 1, null);
            });
            AnimationUtils.doMoveAndScaleAnimate(upperTV, 125, 0, 125, 1.25f, 1, ()->{
                AnimationUtils.doMoveAndScaleAnimate(upperTV, 660, 0, -screenHeight, 0f, 1, this::goToMainActivity);
            });
        }

    }

    private void goToMainActivity(){
        fragmentContainerView = activity.findViewById(R.id.fragmentContainer);
        ScheduleFragment scheduleFragment = new ScheduleFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, scheduleFragment)
                .addToBackStack(null)
                .commit();
    }

}
