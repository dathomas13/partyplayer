package de.gubo_io.partyplayer;

import android.app.Activity;
import android.arch.core.util.Function;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.SpotifyPlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkUtils {

    static int errorCounter = 0;

    interface OnGroupIdRecievedListener{
       public void onGroupIdRecieved(int groupId);
    }
    private OnGroupIdRecievedListener onGroupIdRecievedListener;
    public void setOnGroupIdRecievedListener(OnGroupIdRecievedListener listener){
        this.onGroupIdRecievedListener = listener;
    }
    public void createGroup(Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://gubo-io.de/partyplayer/create_group.php";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        int groupId = Integer.parseInt(response);
                        onGroupIdRecievedListener.onGroupIdRecieved(groupId);
                    }
                }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Volley",""+error.getMessage());
                    }
                });

        queue.add(stringRequest);
    }

    public interface OnMetaReceivedListener{
        public void onMetaReceived(SongInformation mSongInfo);
    }

    private OnMetaReceivedListener onMetaReceivedListener;

    public void setOnMetaReceivedListener(OnMetaReceivedListener onMetaReceivedListener) {
        this.onMetaReceivedListener = onMetaReceivedListener;
    }

    public interface OnTokenExpiredListener{
        void onTokenExpired(String spotifyId);
    }

    private OnTokenExpiredListener onTokenExpiredListener;

    public void setOnTokenExpiredListener(OnTokenExpiredListener onTokenExpiredListener) {
        this.onTokenExpiredListener = onTokenExpiredListener;
    }

    public void getSongMeta(final String spotifyId, final String accessToken, Context context) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://api.spotify.com/v1/tracks/" + spotifyId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject albumInfo = response.getJSONObject("album");

                            String name = response.getString("name");

                            String artistsStr = "";
                            JSONArray artists = albumInfo.getJSONArray("artists");
                            for (int i = 0; i < artists.length(); i++){
                                JSONObject artist = artists.getJSONObject(i);
                                if (artists.length()>1) {
                                    if (i < artists.length() - 1)
                                        artistsStr += "und";
                                    else
                                        artistsStr += ", ";
                                }
                                artistsStr += artist.getString("name");
                            }

                            String coverUrl = "";
                            JSONArray images = albumInfo.getJSONArray("images");
                            if (images.length()==3){
                                coverUrl = images.getJSONObject(1).getString("url");
                            }
                            SongInformation mSongInfo = new SongInformation(name, artistsStr, spotifyId);
                            mSongInfo.setCoverUrl(coverUrl);

                            onMetaReceivedListener.onMetaReceived(mSongInfo);


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Response", jsonError);

                            try {
                                JSONObject jsonErrorObj = new JSONObject(jsonError).getJSONObject("error");
                                int errorStatus = jsonErrorObj.getInt("status");
                                String errorMessage = jsonErrorObj.getString("message");

                                if (errorStatus == 401){
                                    if (errorMessage.equals("The access token expired")){
                                        if(errorCounter < 5)
                                            onTokenExpiredListener.onTokenExpired(spotifyId);

                                        errorCounter++;
                                    }
                                }
                            } catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                        Log.d("ERROR","error => " + error.toString());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + accessToken);
                return params;
            }
        };

        queue.add(jsonObjectRequest);
    }

    static void addSong(SongInformation song, int groupId, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://gubo-io.de/partyplayer/add_song.php";

        Map<String, String> postParam= new HashMap<String, String>();
        postParam.put("groupId", groupId+"");
        postParam.put("spotifyId", song.getSpotifyId());
        postParam.put("name", song.getName());
        postParam.put("artists", song.getArtists());
        postParam.put("coverUrl", song.getCoverUrl());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(postParam), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            Log.d("status", status);


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Response", jsonError);
                        }
                        Log.d("ERROR","error => " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    public interface OnSongsReceivedListener{
        void onSongsReceived(List<SongInformation> songs, SongInformation currentSong);
    }

    private OnSongsReceivedListener onSongsReceivedListener;

    public void setOnSongsReceivedListener(OnSongsReceivedListener onSongsReceivedListener) {
        this.onSongsReceivedListener = onSongsReceivedListener;
    }

    void getSongs(final int groupId, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://gubo-io.de/partyplayer/get_songs.php";

        Map<String, String> postParam= new HashMap<String, String>();
        postParam.put("groupId", groupId+"");

        int postParams[] = {groupId};

        JSONArray postArr = new JSONArray();
        try {
            postArr = new JSONArray(postParams);
        } catch (Exception e){
            e.printStackTrace();
        }

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.POST, url, postArr, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String status = response.toString();


                            List<SongInformation> mSongList = new ArrayList<>();
                            SongInformation currentSong= new SongInformation();
                            for (int i = 0; i < response.length(); i++){
                                SongInformation songInformation = new SongInformation();
                                JSONObject song = response.getJSONObject(i);
                                songInformation.setName(song.getString("name"));
                                songInformation.setArtists(song.getString("artists"));
                                songInformation.setSpotifyId(song.getString("spotifyId"));
                                songInformation.setUpVotes(song.getInt("upVotes"));
                                songInformation.setDownVotes(song.getInt("downVotes"));
                                songInformation.setIsCurrentSong(song.getInt("isCurrentSong"));
                                songInformation.setGroupId(groupId);
                                if(songInformation.getIsCurrentSong()==0)
                                    mSongList.add(songInformation);
                                else
                                    currentSong = songInformation;
                            }
                            Collections.sort(mSongList, new Comparator<SongInformation>() {
                                @Override
                                public int compare(SongInformation o1, SongInformation o2) {
                                    int votes1 = o1.getUpVotes() - o1.getDownVotes();
                                    int votes2 = o2.getUpVotes() - o2.getDownVotes();
                                    return votes2-votes1;

                                }
                            });

                            onSongsReceivedListener.onSongsReceived(mSongList,currentSong);

                            Log.d("status", status);


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Response", jsonError);
                        }
                        Log.d("ERROR","error => " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    static void Vote(SongInformation song, int groupId, String upDown, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://gubo-io.de/partyplayer/vote_song.php";

        Map<String, String> postParam= new HashMap<String, String>();
        postParam.put("groupId", groupId+"");
        postParam.put("spotifyId", song.getSpotifyId());
        postParam.put("upDown", upDown);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(postParam), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            Log.d("status", status);


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Response", jsonError);
                        }
                        Log.d("ERROR","error => " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    static void updateCurrentSong(SongInformation song, int groupId, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://gubo-io.de/partyplayer/set_current_song.php";

        Map<String, String> postParam= new HashMap<String, String>();
        postParam.put("groupId", groupId+"");
        postParam.put("spotifyId", song.getSpotifyId());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(postParam), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            Log.d("status", status);


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Response", jsonError);
                        }
                        Log.e("ERROR","error => " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }

    static void removeSong(SongInformation song, int groupId, Context context){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://gubo-io.de/partyplayer/remove_song.php";

        Map<String, String> postParam= new HashMap<String, String>();
        postParam.put("groupId", groupId+"");
        postParam.put("spotifyId", song.getSpotifyId());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, new JSONObject(postParam), new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            Log.d("status", status);


                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.data != null) {
                            String jsonError = new String(networkResponse.data);
                            Log.e("Response", jsonError);
                        }
                        Log.e("ERROR","error => " + error.toString());
                    }
                });

        queue.add(jsonObjectRequest);
    }

}
