package com.example.notesapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.adapter.NotesAdapter;
import com.example.notesapp.database.NotesDatabaseHelper;
import com.example.notesapp.model.Note;

import java.util.List;
import java.util.UUID;

public class NotesFragment extends Fragment {
    private NotesDatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private NotesAdapter notesAdapter;
    private List<Note> notesList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new NotesDatabaseHelper(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notesList = dbHelper.getAllNotes();
        notesAdapter = new NotesAdapter(notesList, this::updateNote, this::deleteNote);
        recyclerView.setAdapter(notesAdapter);

        view.findViewById(R.id.btn_add_note).setOnClickListener(v -> showAddNoteDialog());

        return view;
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.edit_note_title);
        EditText editContent = dialogView.findViewById(R.id.edit_note_content);

        builder.setTitle("Add Note")
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String content = editContent.getText().toString().trim();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                        Toast.makeText(getContext(), "Both fields are required", Toast.LENGTH_SHORT).show();
                    } else {
                        Note note = new Note(UUID.randomUUID().toString(), title, content);
                        saveNoteToDatabase(note);
                        notesList.add(note);
                        notesAdapter.notifyItemInserted(notesList.size() - 1);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void updateNote(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_note, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.edit_note_title);
        EditText editContent = dialogView.findViewById(R.id.edit_note_content);

        editTitle.setText(note.getTitle());
        editContent.setText(note.getContent());

        builder.setTitle("Update Note")
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String content = editContent.getText().toString().trim();

                    if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                        Toast.makeText(getContext(), "Both fields are required", Toast.LENGTH_SHORT).show();
                    } else {
                        note.setTitle(title);
                        note.setContent(content);
                        updateNoteInDatabase(note);
                        notesAdapter.notifyItemChanged(notesList.indexOf(note));
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void deleteNote(Note note) {
        deleteNoteFromDatabase(note);
        int position = notesList.indexOf(note);
        notesList.remove(position);
        notesAdapter.notifyItemRemoved(position);
    }

    private void saveNoteToDatabase(Note note) {
        dbHelper.addNote(note);
    }

    private void updateNoteInDatabase(Note note) {
        dbHelper.updateNote(note);
    }

    private void deleteNoteFromDatabase(Note note) {
        dbHelper.deleteNote(note.getId());
    }
}
