package de.gubo_io.partyplayer;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter {
    List<Song> songs;

    public MusicListAdapter(List<Song> songs){
        this.songs = songs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    class MusicItem extends  RecyclerView.ViewHolder{

        public MusicItem(View itemView){
            super (itemView);
        }

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
