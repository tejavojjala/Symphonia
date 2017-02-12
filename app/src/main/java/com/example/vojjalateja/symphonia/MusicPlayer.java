package com.example.vojjalateja.symphonia;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MusicPlayer extends Activity implements SeekBar.OnSeekBarChangeListener{

    private TextView btnPlay;
    private TextView btnNext;
    private ImageView songImage;
    private TextView btnPrevious;
    private SeekBar songProgressBar;
    private TextView songTitleLabel;
    private TextView songCurrentDurationLabel;
    private TextView songTotalDurationLabel;
    MediaMetadataRetriever metadataRetriever;
    private Handler mHandler = new Handler();
    private SongsManager songManager=null;
    private Utilities utils;
    public static int currentSongIndex = 0;
    Typeface font;
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private BroadcastReceiver completereceiver;
    public static ArrayList<FirstList> songsList = new ArrayList<FirstList>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentSongIndex = 0;
        songsList.clear();
        setContentView(R.layout.player2);
        font = Typeface.createFromAsset(this.getAssets(),"fontawesome-webfont.ttf");
        songImage = (ImageView) findViewById(R.id.playingsong);
        btnPlay = (TextView) findViewById(R.id.btnPlay);
        btnNext = (TextView) findViewById(R.id.btnNext);
        btnPrevious = (TextView) findViewById(R.id.btnPrevious);
        btnPlay.setTypeface(font);
        btnNext.setTypeface(font);
        metadataRetriever=new MediaMetadataRetriever();
        btnPrevious.setTypeface(font);
        btnPrevious.setText("\uf0d9");
        btnNext.setText("\uf0da");
        songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);
        songTitleLabel = (TextView) findViewById(R.id.songTitle);
        songManager = new SongsManager();
        utils = new Utilities();
        songProgressBar.setOnSeekBarChangeListener(this);
        songsList=songManager.getPlaylist();
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(isPlaying()){
                    pause();
                    btnPlay.setText("\uf01d");
                }
                else
                {
                    if(musicSrv!=null) {
                        start();
                        btnPlay.setText("\uf28c");
                    }
                }

            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if(currentSongIndex < (songsList.size() - 1)){
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                }else{
                    playSong(0);
                    currentSongIndex = 0;
                }

            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(currentSongIndex > 0){
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                }else{
                    playSong(songsList.size() - 1);
                    currentSongIndex = songsList.size() - 1;
                }

            }
        });
        completereceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUI(currentSongIndex);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(currentSongIndex);
        IntentFilter intent=new IntentFilter();
        intent.addAction("com.vojjalateja.complete");
        registerReceiver(completereceiver,intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(completereceiver);
    }

    private ServiceConnection musicConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder)service;
            musicSrv = binder.getService();
            musicSrv.setList(songsList);
            musicBound = true;
            currentSongIndex=getIntent().getExtras().getInt("position");
            playSong(currentSongIndex);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    @Override
    protected void onStart() {
        super.onStart();
        if(playIntent==null){
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    public void  playSong(int songIndex){
        try {
            musicSrv.playSong(songIndex);
            updateUI(songIndex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void updateUI(int songIndex){
        String songTitle = songsList.get(songIndex).Name;
        songTitleLabel.setText(songTitle);
        metadataRetriever.setDataSource(songsList.get(songIndex).SongLink);
        byte []ar=metadataRetriever.getEmbeddedPicture();
        if(null != ar) {
            songImage.setImageBitmap(BitmapFactory.decodeByteArray(ar, 0, ar.length));
        } else{
            songImage.setImageDrawable(getResources().getDrawable(R.drawable.musicsearch));
        }
        btnPlay.setText("\uf28c");
        songProgressBar.setProgress(0);
        songProgressBar.setMax(100);
        updateProgressBar();
    }
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = getDuration();
            long currentDuration = getCurrentPosition();
            songTotalDurationLabel.setText(""+utils.milliSecondsToTimer(totalDuration));
            songCurrentDurationLabel.setText(""+utils.milliSecondsToTimer(currentDuration));
            int progress = (int)(utils.getProgressPercentage(currentDuration, totalDuration));
            songProgressBar.setProgress(progress);
            mHandler.postDelayed(this, 100);
        }
    };
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
    }
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        seekTo(currentPosition);
        updateProgressBar();
    }
    public void onComplet() {
        if(currentSongIndex < (songsList.size() - 1)){
            playSong(currentSongIndex + 1);
            currentSongIndex = currentSongIndex + 1;
        }
        else{
            playSong(0);
            currentSongIndex = 0;
        }
    }
    public int getCurrentPosition() {
        if(musicSrv!=null && musicBound==true && musicSrv.isPng())
            return musicSrv.getPosn();
        else return 0;
    }
    public boolean isPlaying() {
        if(musicSrv!=null && musicBound)
            return musicSrv.isPng();
        return false;
    }
    public void pause() {
        musicSrv.pausePlayer();
    }
    public void seekTo(int pos) {
        musicSrv.seek(pos);
    }
    public int getDuration()
    {
        if(musicSrv!=null)
            return musicSrv.getDur();
        return 0;
    }
    public void start() {
        musicSrv.go();
    }
    @Override
    protected void onDestroy()
    {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }
}