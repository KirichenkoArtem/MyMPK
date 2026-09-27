package com.example.studyproject1;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class NewNoteBottomSheet extends BottomSheetDialogFragment {

    public interface OnNoteCreated { void onCreated(); }
    private OnNoteCreated listener;
    private String group;
    private boolean changed = false;

    public static NewNoteBottomSheet newInstance(String group) {
        NewNoteBottomSheet b = new NewNoteBottomSheet();
        Bundle a = new Bundle();
        a.putString("group", group);
        b.setArguments(a);
        return b;
    }

    public void setOnNoteCreated(OnNoteCreated l) { this.listener = l; }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle s) {
        return inflater.inflate(R.layout.bottom_sheet_new_note, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        group = getArguments() != null ? getArguments().getString("group") : null;

        EditText titleIn   = v.findViewById(R.id.bsTitleInput);
        EditText teacherIn = v.findViewById(R.id.bsTeacherInput);
        EditText bodyIn    = v.findViewById(R.id.bsInput);
        TextView save      = v.findViewById(R.id.bsSave);
        TextView cancel    = v.findViewById(R.id.bsCancel);

        titleIn.requestFocus();

        save.setOnClickListener(x -> {
            String title   = titleIn.getText().toString().trim();
            String teacher = teacherIn.getText().toString().trim();
            String body    = bodyIn.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show();
                return;
            }
            if (body.isEmpty()) {
                Toast.makeText(requireContext(), "Введите текст заметки", Toast.LENGTH_SHORT).show();
                return;
            }
            Context ctx = requireContext();
            NotesStorage.put(ctx, group, title, teacher, body);
            changed = true;
            dismiss();
        });
        cancel.setOnClickListener(x -> dismiss());
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (changed && listener != null) listener.onCreated();
        super.onDismiss(dialog);
    }
}