package com.example.vojjalateja.symphonia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Movie extends AppCompatActivity{
    String searchUrl,searchedFor;
    String imagelink;
    String downloadlink="",format="",result="";
    Intent downloadintent;
    Context context;
    CollapsingToolbarLayout collapsing_container;
    Toolbar toolbar;
    ImageView imageview;
    List<FirstList> flist=new ArrayList<FirstList>();
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerviewlayout);
        toolbar = (Toolbar) findViewById(R.id.technique_four_toolbar);
        collapsing_container = (CollapsingToolbarLayout) findViewById(R.id.collapsing_container);
        setSupportActionBar(toolbar);
        imageview=(ImageView)findViewById(R.id.imgToolbar);
        context=this;
        ConnectionDetector connectionDetector=new ConnectionDetector(context);
        if(connectionDetector.isConnectingToInternet()==false)
        {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
        Intent i=getIntent();
        searchedFor=i.getExtras().getString("searchedfor");
        searchUrl=searchedFor;
        collapsing_container.setTitle(i.getExtras().getString("MOVIE"));
        Picasso.with(context).load(i.getExtras().getString("IMAGE")).placeholder(R.drawable.loading).error(R.drawable.loading).into(imageview);
        new searchResults().execute((Void[]) null);
    }
    private class searchResults extends AsyncTask<Void,Void,List<FirstList>>  {
        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setTitle("Processing...");
            pd.setMessage("Please wait.");
            pd.setCancelable(false);
            pd.setIndeterminate(true);
            pd.show();
        }
        @Override
        protected List<FirstList> doInBackground(Void... params) {
            Document document=null;
            try {
                document= Jsoup.connect(searchUrl).timeout(10000).get();
                Elements srls=document.select("div.link-item");
                for(org.jsoup.nodes.Element srl:srls)
                {
                    FirstList fl=new FirstList();
                    fl.Name=srl.select("div.link").text();
                    fl.SongLink=srl.select("a").get(0).attr("href").substring(10);
                    for(int c=0;c<fl.SongLink.length();c++){
                        if(fl.SongLink.charAt(c)=='/')
                        {
                            fl.SongLink=fl.SongLink.substring(0,c);
                            break;
                        }
                    }
                    fl.Type="Song";
                    fl.ImageLink =imagelink;
                    flist.add(fl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements li=document.select("li.page");
            int pages=li.size();
            for(int page=1;page<pages;page++){
                try {
                    searchUrl="http://www.songsmp3.co"+li.get(page).select("a").attr("href");
                    document= Jsoup.connect(searchUrl).timeout(10000).get();
                    Elements srls=document.select("div.link-item");
                    for(org.jsoup.nodes.Element srl:srls)
                    {
                        FirstList fl=new FirstList();
                        fl.Name=srl.select("div.link").text();
                        fl.SongLink=srl.select("a").get(0).attr("href").substring(10);
                        for(int c=0;c<fl.SongLink.length();c++){
                            if(fl.SongLink.charAt(c)=='/')
                            {
                                fl.SongLink=fl.SongLink.substring(0,c);
                                break;
                            }
                        }
                        fl.Type="Song";
                        fl.ImageLink =imagelink;
                        flist.add(fl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return flist;
        }
        @Override
        protected void onPostExecute(List<FirstList> flist) {
            if (pd!=null) {
                pd.dismiss();
            }
            super.onPostExecute(flist);
            RVAdapter adapter = new RVAdapter(flist);
            RecyclerView rv = (RecyclerView)findViewById(R.id.rv);
            LinearLayoutManager llm = new LinearLayoutManager(context);
            rv.setLayoutManager(llm);
            rv.setAdapter(adapter);
        }
    }
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.SongViewHolder>{

        List<FirstList> songs;
        RVAdapter(List<FirstList> songs){
            this.songs=songs;
        }
        public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView cv;
            TextView Name;
            TextView Type;
            SongViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.cv2);
                Name = (TextView)itemView.findViewById(R.id.name);
                Type = (TextView)itemView.findViewById(R.id.type);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                final int position=getAdapterPosition();
                downloadintent=new Intent(context,DownloadActivity.class);
                result=songs.get(position).SongLink;
                SharedPreferences getData= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String et=getData.getString("list", "3");
                if(et.contentEquals("3")) {
                    CharSequence type[] = new CharSequence[]{"128", "320","Select Default"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Choose a Format");
                    builder.setItems(type, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(which==2){
                                Intent prefs=new Intent(context,prefsactivity.class);
                                startActivity(prefs);
                            }
                            else {
                                if (which == 0)
                                    format = "128";
                                else
                                    format = "0";
                                result = "http://dl.smp3dl.com/fileDownload/Songs/" + format + "/" + result + ".mp3";
                                Log.d("Movie",result);
                                downloadintent.putExtra("downloadurl", result);
                                downloadintent.putExtra("songname", songs.get(position).Name);
                                startActivity(downloadintent);
                            }
                        }
                    });
                    builder.show();
                }
                else
                {
                    if(et.contentEquals("2"))
                        format="0";
                    else
                        format="128";
                    result = "http://dl.smp3dl.com/fileDownload/Songs/" + format + "/" + result + ".mp3";
                    downloadintent.putExtra("downloadurl", result);
                    downloadintent.putExtra("songname", songs.get(position).Name);
                    startActivity(downloadintent);
                }
            }
        }
        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardlayout2, viewGroup, false);
            SongViewHolder svh = new SongViewHolder(v);
            return svh;
        }

        @Override
        public void onBindViewHolder(SongViewHolder songViewHolder, int i) {
            songViewHolder.Name.setText(songs.get(i).Name);
            songViewHolder.Type.setText(songs.get(i).Type);
        }

        @Override
        public int getItemCount() {
            return songs.size();
        }
        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }
    }
    @Override
    protected void onDestroy() {
        if (pd!=null) {
            pd.dismiss();
        }
        super.onDestroy();
    }
}
