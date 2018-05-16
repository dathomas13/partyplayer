package de.gubo_io.partyplayer;

public class Song {
    private String name;
    private String interpret;
    private int upVotes;
    private int downVotes;

    Song(String name, String interpret, int upVotes, int downVotes){
        this.name = name;
        this.interpret = interpret;
        this.upVotes = upVotes;
        this.downVotes = downVotes;
    }
}
