package de.gubo_io.partyplayer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerEvent;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SpotifyPlayer.NotificationCallback, ConnectionStateCallback {

    private static final String CLIENT_ID = "46a64197f3754fefaef5daa520f2ef5d";
    private static final String REDIRECT_URI = "http://gubo-io.de/";
    private static final int REQUEST_CODE = 1337;

    private static final String ACCESS_TOKEN_NAME = "spotifyAccessToken";

    private Player mPlayer;
    private String spotifyAccessToken;
    List<SongInformation> mSongList = new ArrayList<>();
    private boolean isPlayer = true;
    private int groupId = 1;

    private int currentSong = 0;

    private TextView mCurrentSongNameView;
    private  TextView mCurrentInterpretView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        spotifyAccessToken = sharedPref.getString(ACCESS_TOKEN_NAME, "");


        if (!spotifyAccessToken.equals("")){
            setSpotifyPlayer();
        } else {
            showSpotifyLoginDialog();
        }

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSharedText(intent);
            }
        }

        mCurrentSongNameView = findViewById(R.id.tvCurrentSongName);
        mCurrentInterpretView = findViewById(R.id.tvCurrentInterpret);

        final ToggleButton mPlayPauseButton = findViewById(R.id.tbPlayPause);
        if (!isPlayer)
            mPlayPauseButton.setVisibility(View.GONE);
        mPlayPauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    if(mSongList!=null&&mSongList.size()>0) {
                        playSong("spotify:track:" + mSongList.get(currentSong).getSpotifyId());
                        Log.d("play song", mSongList.get(currentSong).getSpotifyId());
                    }
                    else
                        mPlayPauseButton.setChecked(false);
                else
                    pauseSong();
            }
        });

        RecyclerView mSongListView = findViewById(R.id.rvSongList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mSongListView.setLayoutManager(mLayoutManager);
        final MusicListAdapter mMusicListAdapter = new MusicListAdapter(getApplicationContext(), mSongList);
        mSongListView.setAdapter(mMusicListAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Utils.getSongMeta("11dFghVXANMlKmJXsNCbNl", spotifyAccessToken, getApplicationContext());
                //Utils.addSong("11dFghVXANMlKmJXsNCbNl", 1, getApplicationContext());

                NetworkUtils networkUtils = new NetworkUtils();
                networkUtils.setOnSongsReceivedListener(new NetworkUtils.OnSongsReceivedListener() {
                    @Override
                    public void onSongsReceived(List<SongInformation> songs) {
                        mSongList = songs;
                        mMusicListAdapter.setSongList(mSongList);
                    }
                });
                networkUtils.getSongs(groupId, getApplicationContext());

            }
        });

        /*mSongList.add(new SongInformation("Song", "Interpret", "1e6kpeolD7NzTG5l6rsarL",2, 1));
        mSongList.add(new SongInformation("Song", "Interpret", "4ebCcjMhmnjN2gTZ84rO8c",2, 1));
        mSongList.add(new SongInformation("Song", "Interpret", "5SYHmSFd6XPK0dLYWML1vU",2, 1));
        mSongList.add(new SongInformation("Song", "Interpret", "155l3GPsJHOSb77dtt0lV8",2, 1));
        mSongList.add(new SongInformation("Song", "Interpret", "059sONalAfYqzIICi4Ti6R",2, 1));*/




    }

    void showSpotifyLoginDialog(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Mit Spotify Anmelden")
                .setMessage("Um Lieder in die Gruppe posten zu können must du dich mit deinem Spotify Account Anmelden")
                .setPositiveButton("Anmelden", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        loginToSpotify();
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    void loginToSpotify(){
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    void setSpotifyPlayer(){
        Log.e("accessToken", spotifyAccessToken);
        Config playerConfig = new Config(this, spotifyAccessToken, CLIENT_ID);
        Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
            @Override
            public void onInitialized(SpotifyPlayer spotifyPlayer) {
                mPlayer = spotifyPlayer;
                mPlayer.addConnectionStateCallback(MainActivity.this);
                mPlayer.addNotificationCallback(MainActivity.this);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
            }
        });
    }

    void handleSharedText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            final String trackId = sharedText.substring(31);

            final NetworkUtils networkUtils = new NetworkUtils();
            networkUtils.setOnMetaReceivedListener(new NetworkUtils.OnMetaReceivedListener() {
                @Override
                public void onMetaReceived(SongInformation mSongInfo) {
                    showPostSongAlert(mSongInfo);
                }
            });
            networkUtils.setOnTokenExpiredListener(new NetworkUtils.OnTokenExpiredListener() {
                @Override
                public void onTokenExpired(String spotifyId) {
                    loginToSpotify();
                    networkUtils.getSongMeta(trackId, spotifyAccessToken, getApplicationContext());
                }
            });
            networkUtils.getSongMeta(trackId, spotifyAccessToken, this);

        }
    }

    void showPostSongAlert(final SongInformation song){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Möchtest du " + song.getName() + " zu dieser Gruppe hinzufügen?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        NetworkUtils.addSong(song, groupId, getApplicationContext());
                    }
                })
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    void playSong(String url){
        mPlayer.playUri(null, url, 0, 0);

        setCurrentSongInfo();
    }

    void setCurrentSongInfo(){
        SongInformation currentSongInfo = mSongList.get(currentSong);

        mCurrentSongNameView.setText(currentSongInfo.getName());
        mCurrentInterpretView.setText(currentSongInfo.getArtists());
    }

    void pauseSong(){
        mPlayer.pause(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                Log.e("accessToken", response.getAccessToken());
                setSpotifyPlayer();

                spotifyAccessToken = response.getAccessToken();

                SharedPreferences prefs = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(ACCESS_TOKEN_NAME, spotifyAccessToken);
                editor.apply();
            }
        }
    }

    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            // Handle event type as necessary
            case kSpPlaybackNotifyNext:
                currentSong++;
                playSong(mSongList.get(currentSong).getSpotifyId());

            default:
                break;
        }
    }

    @Override
    public void onPlaybackError(Error error) {
        Log.d("MainActivity", "Playback error received: " + error.name());
        switch (error) {
            // Handle error type as necessary
            default:
                break;
        }
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.e("login error", error.toString());
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }
}
