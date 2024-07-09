package com.example.notesapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.notesapp.R;
import com.example.notesapp.model.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> notesList;
    private OnNoteClickListener updateListener;
    private OnNoteClickListener deleteListener;

    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public NotesAdapter(List<Note> notesList, OnNoteClickListener updateListener, OnNoteClickListener deleteListener) {
        this.notesList = notesList;
        this.updateListener = updateListener;
        this.deleteListener = deleteListener;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = notesList.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getContent());

        holder.itemView.setOnClickListener(v -> updateListener.onNoteClick(note));
        holder.deleteButton.setOnClickListener(v -> deleteListener.onNoteClick(note));
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView contentTextView;
        public View deleteButton;

        public NoteViewHolder(View view) {
            super(view);
            titleTextView = view.findViewById(R.id.note_title);
            contentTextView = view.findViewById(R.id.note_content);
            deleteButton = view.findViewById(R.id.btn_delete_note);
        }
    }
}
