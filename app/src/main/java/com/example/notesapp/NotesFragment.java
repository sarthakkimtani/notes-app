package com.example.notesapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.adapter.NotesAdapter;
import com.example.notesapp.database.NotesDatabaseHelper;
import com.example.notesapp.model.Note;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.List;
import java.util.UUID;

public class NotesFragment extends Fragment {
    private NotesDatabaseHelper dbHelper;
    private NotesAdapter notesAdapter;
    private List<Note> notesList;
    private GoogleSignInClient signInClient;
    private String userEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new NotesDatabaseHelper(getContext());
        setHasOptionsMenu(true);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("notes_app", Context.MODE_PRIVATE);
        userEmail = sharedPreferences.getString("user_email", null);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        signInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        Toolbar toolbar = requireActivity().findViewById(R.id.appbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_notes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notesList = dbHelper.getAllNotes(userEmail);
        notesAdapter = new NotesAdapter(notesList, this::showUpdateNoteDialog, this::deleteNote);
        recyclerView.setAdapter(notesAdapter);

        view.findViewById(R.id.btn_add_note).setOnClickListener(v -> showAddNoteDialog());

        return view;
    }

    private void showAddNoteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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

    private void showUpdateNoteDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
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
        dbHelper.addNote(note, userEmail);
    }

    private void updateNoteInDatabase(Note note) {
        dbHelper.updateNote(note, userEmail);
    }

    private void deleteNoteFromDatabase(Note note) {
        dbHelper.deleteNote(note.getId(), userEmail);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notes_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            signInClient.signOut().addOnCompleteListener(task -> {
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("notes_app", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("user_email");
                editor.apply();
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new LoginFragment())
                        .commit();
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
