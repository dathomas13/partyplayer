package de.gubo_io.partyplayer;

import android.graphics.Bitmap;

public class SongInformation {
    private String name;
    private String artists;
    private String spotifyId;
    private String coverUrl;
    private Bitmap cover;
    private int groupId;
    private int upVotes;
    private int downVotes;

    public SongInformation(){}

    public SongInformation(String name, String artists, String spotifyId) {
        this.name = name;
        this.artists = artists;
        this.spotifyId = spotifyId;
    }

    public SongInformation(String name, String artists, String spotifyId, int upVotes, int downVotes) {
        this.name = name;
        this.artists = artists;
        this.spotifyId = spotifyId;
        this.upVotes = upVotes;
        this.downVotes = downVotes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setArtists(String artists) {
        this.artists = artists;
    }

    public void setSpotifyId(String spotifyId) {
        this.spotifyId = spotifyId;
    }

    public String getName() {
        return name;
    }

    public String getArtists() {
        return artists;
    }

    public String getSpotifyId() {
        return spotifyId;
    }

    public int getUpVotes() {
        return upVotes;
    }

    public void setUpVotes(int upVotes) {
        this.upVotes = upVotes;
    }

    public int getDownVotes() {
        return downVotes;
    }

    public void setDownVotes(int downVotes) {
        this.downVotes = downVotes;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }


    public Bitmap getCover() {
        return cover;
    }

    public void setCover(Bitmap cover) {
        this.cover = cover;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
