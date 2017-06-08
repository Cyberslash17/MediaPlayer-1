package ets.mediaplayerserver;

/**
 * Created by AK90090 on 2017-06-08.
 */

public class Song {

    private String name;
    private String album;
    private String artist;
    private int androidID;
    //private BufferedImage image;

    public Song(int id)
    {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getAndroidID() {
        return androidID;
    }

    public void setAndroidID(int androidID) {
        this.androidID = androidID;
    }
}
