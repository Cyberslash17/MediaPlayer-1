package ets.mediaplayerserver;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Field;

import fi.iki.elonen.NanoHTTPD;

public class MediaPlayerActivity extends AppCompatActivity {

    MediaPlayer player;
    Boolean shuffling = false;

    public void play(View view) {
        Button button = (Button) view;
        if (player.isPlaying()){
            button.setText(R.string.play);
            player.pause();
        }else{
            button.setText(R.string.pause);
            player.start();
        }
        Log.d("Test", String.format("Play: %b",player.isPlaying() ));
    }

    public void next(View view) {
//        player.getCurrentPosition();
//                player.getSelectedTrack();
//        player.getTrackInfo();
//        player.selectTrack(2);
        Log.d("Test", "Next was clicked");
    }

    public void back(View view) {
        player.seekTo(0);

        Log.d("Test", "Back was clicked");
    }

    public void loop(View view) {
        if (player.isLooping()) {
            player.setLooping(false);
        }
        else {
            player.setLooping(true);
        }
        Log.d("Test", String.format("Loop: %b",player.isLooping() ));
    }

    public void shuffle(View view) {
        if (shuffling) {
            shuffling = false;
        }
        else {
            shuffling = true;
        }
        Log.d("Test", String.format("Shuffling: %b",shuffling ));
    }


    public static String listRaw()
    {
        Field[] fields = R.raw.class.getFields();

        for (int i = 0; i < fields.length - 1; i++) {
            String name = fields[i].getName();
            Log.d("Test", String.format("Shuffling: %b",name));
        }
        return "bob";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        player = MediaPlayer.create(this ,R.raw.shrekanthem);

        MediaServer server = new MediaServer(8080);
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        TextView tx = (TextView ) findViewById(R.id.IP);
        tx.setText(server.toString());

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


    public class MediaServer extends NanoHTTPD
    {

        public MediaServer(int port) {
            super(port);

        }

        public MediaServer(String hostname, int port) {
            super(hostname, port);
        }

        public String parseURI(String uri)
        {
            return "string";
        }

        public String getValue(String uri)
        {
            return "0";
        }

        @Override
        public Response serve(IHTTPSession session) {
            if (session.getMethod() == Method.GET)
            {
                String uri = session.getUri();
                String command = parseURI(uri);

                if(command.equals("playlist"))
                {

                }else if(command.equals("id"))
                {
                    String value = getValue(uri);

                }else if(command.equals("play"))
                {

                }else if(command.equals("pause"))
                {

                }else if(command.equals("stop"))
                {

                }else if(command.equals("next"))
                {

                }else if(command.equals("back"))
                {

                }else if(command.equals("repeat"))
                {

                }else if(command.equals("shuffle"))
                {

                }else if(command.equals("volume"))
                {
                    String value = getValue(uri);
                }


                return new Response("<http><head><title>404</title></head><body><h1>Error 404</h1></body></http>");
            }
            return super.serve(session);
        }
    }
}