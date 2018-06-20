package de.gubo_io.partyplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicItemViewHolder> {
    private Context context;
    int groupId;
    private List<SongInformation> mSongList;

    public MusicListAdapter(Context context, List<SongInformation> songList){
        this.context = context;
        this.mSongList = songList;
    }

    void setSongList(List<SongInformation> songList){
        mSongList = songList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public MusicItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutIdForListItem = R.layout.music_element;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;
        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);

        return new MusicItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicItemViewHolder holder, int position) {
        holder.bind(position);
    }

    class MusicItemViewHolder extends  RecyclerView.ViewHolder{

        ImageView mSongCover;
        TextView mSongName;
        TextView mInterpret;
        TextView mIndex;
        Button mVoteUpButton;
        Button mVoteDownButton;

        public MusicItemViewHolder(View itemView){
            super (itemView);

            mSongCover = itemView.findViewById(R.id.ivSongCover);
            mSongName = itemView.findViewById(R.id.tvSongName);
            mInterpret = itemView.findViewById(R.id.tvInterpret);
            mIndex = itemView.findViewById(R.id.tvIndex);
            mVoteUpButton = itemView.findViewById(R.id.btVoteUp);
            mVoteDownButton = itemView.findViewById(R.id.btVoteDown);
        }

        void bind(int listIndex){
            SongInformation currentSong = mSongList.get(listIndex);

            mSongName.setText(currentSong.getName());
            mInterpret.setText(currentSong.getArtists());

            mIndex.setText("#" + listIndex);

            mVoteUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            mVoteDownButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }
}
