package ets.mediaplayerserver;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import fi.iki.elonen.NanoHTTPD;

public class MediaPlayerActivity extends AppCompatActivity {

    String TAG = "mps";
    MediaPlayer player;
    Playlist playlist;
    boolean isLooping;

    public static String listRaw()
    {
        Field[] fields = R.raw.class.getFields();

        for (int i = 0; i < fields.length - 1; i++) {
            String name = fields[i].getName();
            Log.d("Test", String.format("Shuffling: %b", name));
        }
        return "bob";
    }

    public String getPath(int id)
    {
        TypedValue value = new TypedValue();
        getResources().getValue(id, value, true);
        if (value == null) {
            return "Not Found";
        }
        String[] path = value.string.toString().split("/");

        return "/songs/" + path[path.length - 1];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);
        playlist = new Playlist(getApplicationContext());
        player = MediaPlayer.create(this, playlist.getCurrentSong());

        MediaServer server = new MediaServer(5000) {
            @Override
            public String getMime(int id) {
                return getMimeDetail(id);
            }

            @Override
            public String getPlaylist() {
                String json = "{\n" +
                        "\t\"songs\": [\n";
                for (Integer i : playlist.playlist) {
                    json += getSongDetails(i);
                }

                json = json.substring(0,json.length() -1) + "]\n" +
                        "} \n";
                return json;
            }

            @Override
            public String getId(int id) {
                return getPath(id);
            }

            @Override
            public void playCommand(int id) {
                playlist.setCurrentSong(id);
                playNewSong();
            }


            @Override
            public void resumeCommand() {
                if (!player.isPlaying()) {
                    player.start();
                }
                Log.d(TAG, String.format("Play: %b", player.isPlaying()));
            }

            @Override
            public void pauseCommand() {
                if (player.isPlaying()) {
                    player.pause();
                }
                Log.d(TAG, String.format("Play: %b", player.isPlaying()));
            }

            @Override
            public void stopCommand() {
                if (player.isPlaying()) {
                    player.stop();
                }
                Log.d(TAG, String.format("Play: %b", player.isPlaying()));
            }

            @Override
            public int nextCommand() {
                playNextSong();
                Log.d("Test", "Next was clicked");
                return playlist.getCurrentSong();
            }

            @Override
            public int backCommand() {
                if (player.getCurrentPosition() > 5000) {
                    player.seekTo(0);
                } else {
                    playPreviousSong();
                }
                Log.d("Test", "Back was clicked");
                return playlist.getCurrentSong();
            }

            @Override
            public boolean repeatCommand() {
                player.setLooping(!isLooping);
                isLooping = !isLooping;
                Log.d("Test", String.format("Loop: %b", player.isLooping()));
                return player.isLooping();
            }

            @Override
            public boolean shuffleCommand() {
                boolean isShuffled = playlist.toggleShuffle();
                Log.d("Test", String.format("Shuffling: %b", playlist.isShuffled));
                return isShuffled;
            }

            @Override
            public void changeVolume(float value) {

                player.setVolume(value,value);
            }

            @Override
            public InputStream getSong(String path) {

                int songId = getIDFromPath(path);
                //return "id:" + songId;
                return getResources().openRawResource(songId);
            }
        };
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView tx = (TextView) findViewById(R.id.IP);
        tx.setText(server.toString());

    }

    public int getIDFromPath(String path)
    {
        String[] songName = path.split("\\.");

        Context context = getApplicationContext();
        return context.getResources().getIdentifier(songName[0], "raw", context.getPackageName());
    }

    public void playNextSong() {
        if (!isLooping)
        {
        player.reset();
        player = MediaPlayer.create(this, playlist.getNextSong());
        player.start();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playNextSong();
            }
        });
        }else
        {
            playNewSong();
        }
    }

    public void playNewSong() {

            player.reset();
            player = MediaPlayer.create(this, playlist.getCurrentSong());
            player.start();

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    playNextSong();
                }
            });


    }

    public void playPreviousSong() {
        player.reset();
        player = MediaPlayer.create(this, playlist.getPreviousSong());
        player.start();

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playNextSong();
            }
        });
    }

    private String getSongDetails(int id) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        Uri myUri = Uri.parse("android.resource://" + getPackageName() + "/" + id);
        retriever.setDataSource(this, myUri);

        String songName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        String artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
        String albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        String length = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return "\t{\n" +
                "\t\t\"ID\": \"" + id + "\",\n" +
                "\t\t\"title\": \"" + songName + "\",\n" +
                "\t\t\"length\": \"" + length + "\",\n" +
                "\t\t\"artist\": \"" + artistName + "\",\n" +
                "\t\t\"album\": \"" + albumName + "\",\n" +
                "\t\t\"image\": \"null\",\n" +
                "\t\t\"path\":\"" + getPath(id) + "\"\n" +
                "\t},";
    }

    private String getMimeDetail(int id) {

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        Uri myUri = Uri.parse("android.resource://" + getPackageName() + "/" + id);
        retriever.setDataSource(this, myUri);

        return retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_media_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


    public abstract class MediaServer extends NanoHTTPD {

        public MediaServer(int port) {
            super(port);

        }

        public MediaServer(String hostname, int port) {
            super(hostname, port);
        }

        public String parseURI(String uri)
        {
            String[] params = uri.split("/");
            Log.d(TAG, "serve() returned: " + params.length);
            if (params.length > 1) {
                return "/" + params[1];
            }
            return "/null";
        }

        public String getValue(String uri)
        {
            Log.d(TAG, "value: " + uri.split("/").length);
            String[] params = uri.split("/");
            for (String s : params
                    ) {
                Log.d(TAG, s);
            }
            Log.d(TAG, "length:" + params[2]);
            if (params.length > 2) {
                return params[2];
            }
            return "-1";
        }


        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod() == Method.GET) {
                String uri = session.getUri();
                Log.d(TAG, "uri: " + uri);
                String command = parseURI(uri);
                Log.d(TAG, "serve() returned: " + command);
                if (command.equals("/playlist")) {
                    String playlist = getPlaylist();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, playlist);
                } else if (command.equals("/id")) {
                    int value = Integer.valueOf(getValue(uri));
                    if (value != -1) {
                        String song = getId(value);
                        if (song.equals("NotFound")) {
                            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "song not found");
                        }
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, song);
                    }
                    return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "id invalid");

                }else if (command.equals("/play")) {
                    int value = Integer.valueOf(getValue(uri));
                    if (value != -1) {
                        String song = getId(value);
                        playCommand(value);
                        if (song.equals("NotFound")) {
                            return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "song not found");
                        }
                        return new Response(Response.Status.OK, MIME_PLAINTEXT, song);
                    }
                    return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "id invalid");
                }
                else if (command.equals("/resume")) {
                    resumeCommand();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "" + playlist.getCurrentSong());
                } else if (command.equals("/pause")) {
                    pauseCommand();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "" + playlist.getCurrentSong());
                } else if (command.equals("/stop")) {
                    stopCommand();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "" + playlist.getCurrentSong());
                } else if (command.equals("/next")) {
                    nextCommand();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "" + playlist.getCurrentSong());
                } else if (command.equals("/back")) {
                    backCommand();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "" + playlist.getCurrentSong());
                } else if (command.equals("/repeat")) {
                    repeatCommand();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "Repeat:" + isLooping);
                } else if (command.equals("/shuffle")) {
                    boolean shuffle = shuffleCommand();
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "Shuffle:" + shuffle);
                } else if (command.equals("/volume")) {
                    float value = Float.valueOf(getValue(uri));
                    changeVolume(value);
                    return new Response(Response.Status.OK, MIME_PLAINTEXT, "" + value);
                } else if (command.equals("/songs")) {
                    String value = getValue(uri);

                    InputStream data = getSong(value);

                    return new Response(Response.Status.OK, getMime(getIDFromPath(value)), data);
                }/*

                return new Response("<http><head><title>404</title></head><body><h1>Error 404</h1></body></http>");
            */
            }
            return super.serve(session);

        }

        public abstract String getMime(int id);
        public abstract String getPlaylist();

        public abstract String getId(int id);

        public abstract void playCommand(int id);

        public abstract void resumeCommand();
        public abstract void pauseCommand();

        public abstract void stopCommand();

        public abstract int nextCommand();

        public abstract int backCommand();

        public abstract boolean repeatCommand();

        public abstract boolean shuffleCommand();

        public abstract void changeVolume(float value);

        public abstract InputStream getSong(String path);


    }
}