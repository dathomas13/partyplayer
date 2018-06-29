package de.gubo_io.partyplayer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicItemViewHolder> {
    private Context context;
    private List<SongInformation> mSongList = new ArrayList<>();

    void setContext(Context context){
        this.context = context;
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
        TextView mVotes;
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
            mVotes = itemView.findViewById(R.id.txtVotes);
        }

        void bind(int listIndex){
            SongInformation currentSong = mSongList.get(listIndex);

            mSongName.setText(currentSong.getName());
            mInterpret.setText(currentSong.getArtists());

            mIndex.setText("#" + listIndex);


            mVoteUpButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(mVoteUpButton.isClickable()){
                        mVoteUpButton.setPressed(true);
                        mVoteDownButton.setPressed(false);
                        mVoteUpButton.setClickable(false);
                        mVoteDownButton.setClickable(false);
                        int votes = Integer.parseInt(mVotes.getText().toString());
                        votes++;
                        mVotes.setText(""+votes);
                    }
                    return false;
                }
            });


            mVoteDownButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(mVoteDownButton.isClickable()){
                        mVoteDownButton.setPressed(true);
                        mVoteUpButton.setPressed(false);
                        mVoteUpButton.setClickable(false);
                        mVoteDownButton.setClickable(false);
                        int votes = Integer.parseInt(mVotes.getText().toString());
                        votes--;
                        mVotes.setText(""+votes);
                    }
                    return false;
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }
}
