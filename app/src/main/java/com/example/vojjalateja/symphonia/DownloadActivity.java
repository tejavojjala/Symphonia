package com.example.vojjalateja.symphonia;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import java.io.File;

public class DownloadActivity extends Activity {
    DownloadManager downloadManager;
    String downloadFileUrl = "",Song_Name="";
    private long myDownloadReference;
    Context context;
    private BroadcastReceiver receiverDownloadComplete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloadactivity);
        downloadFileUrl=getIntent().getExtras().getString("downloadurl");
        context=this;
        Song_Name=getIntent().getExtras().getString("songname");
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/symphonia_downloads");
        if (!direct.exists()) {
            direct.mkdirs();
        }
        Uri uri = Uri.parse(downloadFileUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setDescription("Downloading...")
                .setTitle(Song_Name);
        request.setDestinationInExternalPublicDir("/symphonia_downloads", Song_Name + ".mp3");
        request.setVisibleInDownloadsUi(true);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE);
        myDownloadReference = downloadManager.enqueue(request);
        receiverDownloadComplete=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Intent inten=new Intent();
                inten.setAction("com.vojjalateja.deleteaction");
                sendBroadcast(inten);
                unregisterReceiver(receiverDownloadComplete);
            }
        };
        IntentFilter inte=new IntentFilter();
        inte.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(receiverDownloadComplete, inte);
        super.onBackPressed();
    }
}
