package com.example.studyproject1;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
    int selectId;
    float screenHeight;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_set, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        screenHeight = getResources().getDisplayMetrics().heightPixels;
        activity = requireActivity();
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
                selectId = (int) id;
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
        if (threadForSearch != null && threadForSearch.isAlive()) threadForSearch.interrupt();

        if (groupNameInput.isPerformingCompletion()){
            goNextTV.setVisibility(View.VISIBLE);
        }else{
            goNextTV.setVisibility(View.INVISIBLE);
        }
        threadForSearch = new Thread(() -> {
            try {
                JSONArray groups = Utils.searchGroupsOnline(new_text);
                if(groups != null && groups.length()!=0) {
                    String[] array = new String[groups.length()];
                    for (int i = 0; i < groups.length(); i++) {
                        array[i] = groups.getJSONObject(i).getString("name");
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
                Log.e("log",e.toString());
            }
        });
        threadForSearch.start();
    }


    private void next(){
        String groupName = groupNameInput.getAdapter().getItem(selectId).toString();
        if (groupName == null || groupName == ""){
            Toast.makeText(activity.getApplicationContext(),"Ошибка названия группы! 😨", Toast.LENGTH_SHORT).show();
        }else{
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
        SetFragment setFragment = new SetFragment();
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, setFragment)
                .addToBackStack(null)
                .commit();
    }

}
