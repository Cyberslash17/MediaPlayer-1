package ets.mediaplayerserver;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Pierre-Luc on 2017-06-08.
 */

public class Playlist {

    public ArrayList<Integer> playlist;
    private int songPlaying = 0;
    public Boolean isShuffled = false;
    private Context context;

    public Playlist(Context applicationContext) {
        context = applicationContext;
        setPlaylist();
    }

    public void setPlaylist() {
        ArrayList<Integer> musicList = new ArrayList<>();
        Field[] fields = R.raw.class.getFields();

        for (int i = 0; i < fields.length; i++) {
            String songName = fields[i].getName();

            Log.d("TestSong", i + " " + songName + " type:" + fields[i].getGenericType());
            if (!songName.equals("$change") && !songName.equals("serialVersionUID")) {
                int songId = context.getResources().getIdentifier(songName, "raw", context.getPackageName());
                musicList.add(songId);
            }
        }

        playlist = musicList;
    }

    public void setRandomPlaylist() {
        Collections.shuffle(playlist);
    }

    public int getCurrentSong() {
        return playlist.get(songPlaying);
    }

    public void setCurrentSong(int id)
    {
        songPlaying = playlist.indexOf(id);
    }

    public int getNextSong() {
        if (songPlaying >= playlist.size() - 1) {
            songPlaying = 0;
        }
        else {
            songPlaying++;
        }
        return playlist.get(songPlaying);
    }

    public int getPreviousSong() {
        if (songPlaying == 0) {
            songPlaying = playlist.size() - 1;
        }
        else {
            songPlaying--;
        }
        return playlist.get(songPlaying);
    }

    public boolean toggleShuffle() {
        if (isShuffled) {
            isShuffled = false;
            setPlaylist();
        }
        else {
            isShuffled = true;
            setRandomPlaylist();

        }
        return isShuffled;
    }

    public void setIsShuffled(Boolean shuffled) {
        isShuffled = shuffled;
    }

    public int size() {
        return playlist.size();
    }


}