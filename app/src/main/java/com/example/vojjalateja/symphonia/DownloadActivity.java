package com.example.vojjalateja.symphonia;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadActivity extends Activity {
    DownloadManager downloadManager;
    String downloadFileUrl = "",Song_Name="";
    private long myDownloadReference;
    Context context;
    URLConnection conection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downloadactivity);
        context=this;
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/symphonia_downloads");
        if (!direct.exists()) {
            direct.mkdirs();
        }
        Toast.makeText(context,Song_Name+" is enqued to downloads",Toast.LENGTH_LONG).show();
        new DownloadAsync().execute();

        onBackPressed();

    }

    class DownloadAsync extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... params) {
            downloadFileUrl=getIntent().getExtras().getString("downloadurl");
            Song_Name=getIntent().getExtras().getString("songname");
            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            String retURL = "";

            try {
                URL url = new URL(downloadFileUrl);
                conection = url.openConnection();
                conection.getURL();
                Log.d("URL1",conection.getURL().toString());
                conection.connect();
                Log.d("URL2",conection.getURL().toString());

                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(conection.getInputStream(), 8192);
                retURL = conection.getURL().toString();
                Log.d("URL3",conection.getURL().toString());
                //Log.d("URL3",conection.getHeaderFields().get("Content-Location").get(0));
            }catch (Exception e){
                retURL = conection.getURL().toString();
                Log.d("URL4",conection.getURL().toString());
                //Log.d("URL4",conection.getHeaderFields().get("Content-Location").get(0));
            }
            retURL = retURL.replaceAll("\\s","%20");
            Log.d("URL5",retURL);
            return retURL;
        }

        @Override
        protected void onPostExecute(String retURL) {
            super.onPostExecute(retURL);
            Uri uri = Uri.parse(retURL);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDescription("Downloading...")
                    .setTitle(Song_Name);
            request.setDestinationInExternalPublicDir("/symphonia_downloads", Song_Name + ".mp3");
            request.setVisibleInDownloadsUi(true);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                    | DownloadManager.Request.NETWORK_MOBILE);
            downloadManager.enqueue(request);
        }
    }

}
