package com.example.vojjalateja.symphonia;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener {
    private final IBinder musicBind = new MusicBinder();
    private MediaPlayer player;
    private ArrayList<FirstList> songs;
    private int songPosn=0;
    public void onCreate(){
        super.onCreate();
        songPosn=0;
        player = new MediaPlayer();
        initMusicPlayer();
    }
    public void setList(ArrayList<FirstList> theSongs){
        songs=theSongs;
    }
    public void setSong(int songIndex){
        songPosn=songIndex;
    }
    public void playSong() {
        try {
            player.reset();
            player.setDataSource(songs.get(songPosn).SongLink);
            player.prepare();
            player.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }
    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intent=new Intent();
        intent.setAction("com.vojjalateja.complete");
        sendBroadcast(intent);
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }
    @Override
    public void onPrepared(MediaPlayer mp) {

    }
    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }
}
