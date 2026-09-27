package com.example.studyproject1;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NoteBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_GROUP = "group";
    private static final String ARG_TITLE = "title";
    private static final String ARG_TEACHER = "teacher";

    public interface OnNoteChanged { void onChange(); }
    private OnNoteChanged listener;
    private boolean changed = false;

    public static NoteBottomSheet newInstance(String group, String title, String teacher) {
        NoteBottomSheet b = new NoteBottomSheet();
        Bundle a = new Bundle();
        a.putString(ARG_GROUP, group);
        a.putString(ARG_TITLE, title);
        a.putString(ARG_TEACHER, teacher);
        b.setArguments(a);
        return b;
    }

    public void setOnNoteChanged(OnNoteChanged l) { this.listener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        Bundle a = getArguments();
        String group   = a != null ? a.getString(ARG_GROUP) : null;
        String title   = a != null ? a.getString(ARG_TITLE, "") : "";
        String teacher = a != null ? a.getString(ARG_TEACHER, "") : "";

        TextView tvTitle = v.findViewById(R.id.bsTitle);
        TextView tvSub   = v.findViewById(R.id.bsSubtitle);
        EditText input   = v.findViewById(R.id.bsInput);
        TextView save    = v.findViewById(R.id.bsSave);
        TextView delete  = v.findViewById(R.id.bsDelete);

        tvTitle.setText(title);
        if (!teacher.isEmpty()) {
            tvSub.setText(teacher + " · заметка ко всем парам с этим названием и преподавателем");
        } else {
            tvSub.setText("Заметка ко всем парам с этим названием");
        }

        Context ctx = requireContext();
        String existing = NotesStorage.get(ctx, group, title, teacher);
        if (existing != null) input.setText(existing);
        else delete.setVisibility(View.GONE);

        input.requestFocus();

        save.setOnClickListener(x -> {
            NotesStorage.put(ctx, group, title, teacher, input.getText().toString());
            changed = true;
            dismiss();
        });
        delete.setOnClickListener(x -> {
            NotesStorage.remove(ctx, group, title, teacher);
            changed = true;
            dismiss();
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (changed && listener != null) listener.onChange();
        super.onDismiss(dialog);
    }
}