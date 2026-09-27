package com.example.studyproject1;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class NotesFragment extends Fragment {

    private Activity activity;
    private LinearLayout notesContainer;
    private TextView emptyTV, groupNameTV, subtitleTV;
    private FastLog FastLog;
    private String group;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        activity = requireActivity();
        FastLog = ((MainActivity) activity).FastLog;
        group = activity.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
                .getString("group_name", "");

        groupNameTV = v.findViewById(R.id.groupNameTV);
        subtitleTV = v.findViewById(R.id.subtitleTV);
        notesContainer = v.findViewById(R.id.notesContainer);
        emptyTV = v.findViewById(R.id.emptyTV);

        groupNameTV.setText(group);
        subtitleTV.setText("Мои заметки");

        // На заметках: update скрыт, add показан
        v.findViewById(R.id.updateButton).setVisibility(View.GONE);
        View addBtn = v.findViewById(R.id.addButton);
        addBtn.setVisibility(View.VISIBLE);
        addBtn.setOnClickListener(x -> {
            NewNoteBottomSheet bs = NewNoteBottomSheet.newInstance(group);
            bs.setOnNoteCreated(this::loadNotes);
            bs.show(getChildFragmentManager(), "newNote");
        });

        v.findViewById(R.id.changeGroupButton).setOnClickListener(x ->
                ((MainActivity) requireActivity()).onGroupCleared());

        v.setAlpha(0f);
        v.animate().alpha(1f).setDuration(220).start();

        loadNotes();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notesContainer != null) loadNotes();
    }

    private void loadNotes() {
        if (notesContainer == null) return;
        notesContainer.removeAllViews();

        List<NotesStorage.Entry> notes = NotesStorage.getAll(activity, group);

        if (notes.isEmpty()) {
            emptyTV.setVisibility(View.VISIBLE);
            return;
        }
        emptyTV.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        int i = 0;
        for (NotesStorage.Entry e : notes) {
            View card = inflater.inflate(R.layout.lesson_item, notesContainer, false);
            card.findViewById(R.id.number).setVisibility(View.GONE);
            card.findViewById(R.id.lesson_type).setVisibility(View.GONE);
            card.findViewById(R.id.lesson_time).setVisibility(View.GONE);
            card.findViewById(R.id.lesson_classroom).setVisibility(View.GONE);

            ((TextView) card.findViewById(R.id.lesson_name)).setText(e.title);
            String sub = (e.teacher == null || e.teacher.isEmpty())
                    ? "заметка по всем парам с этим названием"
                    : e.teacher + " · заметка по всем парам";
            ((TextView) card.findViewById(R.id.teacher_fio)).setText(sub);

            TextView noteTV = card.findViewById(R.id.noteTV);
            View line = card.findViewById(R.id.noteLine);
            noteTV.setText(e.text);
            noteTV.setVisibility(View.VISIBLE);
            line.setVisibility(View.VISIBLE);

            final String t = e.title, tc = e.teacher;
            card.setOnClickListener(v -> {
                NoteBottomSheet bs = NoteBottomSheet.newInstance(group, t, tc);
                bs.setOnNoteChanged(this::loadNotes);
                bs.show(getChildFragmentManager(), "note");
            });

            card.setAlpha(0f);
            card.setTranslationY(20f);
            card.animate().alpha(1f).translationY(0f)
                    .setStartDelay(i * 40L).setDuration(220).start();
            i++;

            notesContainer.addView(card);
        }
    }
}