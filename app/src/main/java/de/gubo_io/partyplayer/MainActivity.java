package de.gubo_io.partyplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Error;
import com.spotify.sdk.android.player.PlaybackState;
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
    private PlaybackState mCurrentPlaybackState;
    private String spotifyAccessToken;
    List<SongInformation> mSongList = new ArrayList<>();
    final MusicListAdapter mMusicListAdapter = new MusicListAdapter();
    private boolean isPlayer = true;
    private int groupId = 1;

    private int currentSong = -1;

    private BroadcastReceiver mNetworkStateReceiver;

    private TextView mCurrentSongNameView;
    private TextView mCurrentInterpretView;
    private ToggleButton mPlayPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_v2);

        SharedPreferences sharedPref = getSharedPreferences("playerPref", Context.MODE_PRIVATE);
        groupId = sharedPref.getInt("groupId", -1);


        if (isPlayer) {
            spotifyAccessToken = sharedPref.getString(ACCESS_TOKEN_NAME, "");
            if(spotifyAccessToken.equals(""))
                showSpotifyLoginDialog();
            else
                setSpotifyPlayer();
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

        mPlayPauseButton = findViewById(R.id.tbPlayPause);

        if (!isPlayer)
            mPlayPauseButton.setVisibility(View.GONE);

        mPlayPauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    if (mSongList != null && mSongList.size() > 0) {
                        playCurrentSong();
                    } else
                        mPlayPauseButton.setChecked(false);
                else
                    pauseSong();
            }
        });

        RecyclerView mSongListView = findViewById(R.id.rvSongList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mSongListView.setLayoutManager(mLayoutManager);
        mMusicListAdapter.setContext(getApplicationContext());
        mSongListView.setAdapter(mMusicListAdapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadSongs();
            }
        });

    }

    private final Player.OperationCallback mOperationCallback = new Player.OperationCallback() {
        @Override
        public void onSuccess() {
            Log.d("operation callback:","OK!");
        }

        @Override
        public void onError(Error error) {
            Log.e("operation callback:",error.toString());
        }
    };

    private Connectivity getNetworkConnectivity(Context context) {
        ConnectivityManager connectivityManager;
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return Connectivity.fromNetworkType(activeNetwork.getType());
        } else {
            return Connectivity.OFFLINE;
        }
    }

    void showSpotifyLoginDialog() {

        LayoutInflater inflater = this.getLayoutInflater();
        View popupLayout = inflater.inflate(R.layout.login_to_spotify_popup, null);

        final PopupWindow loginPop = new PopupWindow(popupLayout, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        Button mLoginButton = popupLayout.findViewById(R.id.btAcceptLogin);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginToSpotify();
                loginPop.dismiss();
            }
        });

        findViewById(R.id.clRoot).post(new Runnable() {
            @Override
            public void run() {
                loginPop.showAtLocation(findViewById(R.id.clRoot), Gravity.CENTER, 0, 0);
            }
        });

    }

    void loginToSpotify() {
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "playlist-read", "playlist-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            switch (response.getType()) {
                case TOKEN:
                    spotifyAccessToken = response.getAccessToken();

                    setSpotifyPlayer();

                    SharedPreferences prefs = getSharedPreferences("playerPref", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(ACCESS_TOKEN_NAME, spotifyAccessToken);
                    editor.apply();

                    break;

                case ERROR:
                    showLoginFailedAlert();
                    Log.e("spotify login error", response.getError());

            }
        }
    }

    void setSpotifyPlayer() {
        Log.e("accessToken", spotifyAccessToken);
        if (mPlayer == null) {
            Config playerConfig = new Config(this, spotifyAccessToken, CLIENT_ID);
            mPlayer = Spotify.getPlayer(playerConfig, this, new SpotifyPlayer.InitializationObserver() {
                @Override
                public void onInitialized(SpotifyPlayer spotifyPlayer) {
                    mPlayer = spotifyPlayer;
                    mPlayer.setConnectivityStatus(mOperationCallback, getNetworkConnectivity(MainActivity.this));
                    mPlayer.addConnectionStateCallback(MainActivity.this);
                    mPlayer.addNotificationCallback(MainActivity.this);
                }

                @Override
                public void onError(Throwable throwable) {
                    Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                }
            });
        } else {
            mPlayer.login(spotifyAccessToken);
        }
    }

    void loadSongs() {
        NetworkUtils networkUtils = new NetworkUtils();
        networkUtils.setOnSongsReceivedListener(new NetworkUtils.OnSongsReceivedListener() {
            @Override
            public void onSongsReceived(List<SongInformation> songs) {
                mSongList = songs;
                mMusicListAdapter.setSongList(mSongList);
                //setCurrentSongInfo();
            }
        });
        networkUtils.getSongs(groupId, getApplicationContext());
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

    void showPostSongAlert(final SongInformation song) {
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

    void showLoginFailedAlert() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Anmeldung fehlgeschlagen!")
                .setMessage("Erneut probieren?")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        loginToSpotify();
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    void playCurrentSong() {
        String currentTrackUri = "spotify:track:" + mSongList.get(currentSong).getSpotifyId();
        mPlayer.playUri(null, currentTrackUri, 0, 0);

        setCurrentSongInfo();
    }

    void setCurrentSongInfo() {
        if (mSongList.size() > currentSong) {
            SongInformation currentSongInfo = mSongList.get(currentSong);

            mCurrentSongNameView.setText(currentSongInfo.getName());
            mCurrentInterpretView.setText(currentSongInfo.getArtists());

            mMusicListAdapter.setCurrentSongIndex(currentSong);
            mMusicListAdapter.notifyDataSetChanged();
        }
    }

    void pauseSong() {
        mPlayer.pause(null);
    }


    @Override
    public void onPlaybackEvent(PlayerEvent playerEvent) {
        Log.d("MainActivity", "Playback event received: " + playerEvent.name());
        switch (playerEvent) {
            case kSpPlaybackNotifyTrackChanged:

                Log.e("spotify", "track changed");

                break;

            case kSpPlaybackNotifyAudioDeliveryDone:
                Log.e("spotify", "track ended");

                if (mSongList.size() > currentSong + 1){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!(mSongList.size()>currentSong + 1))
                                currentSong = -1;

                            currentSong++;

                            playCurrentSong();
                        }
                    }, 2000);
                } else {
                    mPlayPauseButton.setChecked(false);
                }
                break;

            case kSpPlaybackNotifyBecameInactive:
                Log.e("spotify", "became inactive");

                break;


            default:
                break;
        }

        mCurrentPlaybackState = mPlayer.getPlaybackState();
        Log.d("current Playback State:", mCurrentPlaybackState.toString());

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
        Toast toast = Toast.makeText(getApplicationContext(), "Login war erfolgreich!", Toast.LENGTH_SHORT);
        toast.show();

        if(mPlayer!=null)
            mPlayPauseButton.setClickable(true);

        loadSongs();
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Error error) {
        Log.e("login error", error.toString());

        showLoginFailedAlert();
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mNetworkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mPlayer != null) {
                    Connectivity connectivity = getNetworkConnectivity(getBaseContext());
                    Log.d("Network state changed:", connectivity.toString());
                    mPlayer.setConnectivityStatus(mOperationCallback, connectivity);
                }
            }
        };

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkStateReceiver, filter);

        if (mPlayer != null) {
            mPlayer.addNotificationCallback(MainActivity.this);
            mPlayer.addConnectionStateCallback(MainActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkStateReceiver);

        if (mPlayer != null) {
            mPlayer.removeNotificationCallback(MainActivity.this);
            mPlayer.removeConnectionStateCallback(MainActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }
}
