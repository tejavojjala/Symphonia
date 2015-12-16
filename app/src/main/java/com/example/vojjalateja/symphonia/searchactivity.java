package com.example.vojjalateja.symphonia;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

import java.util.ArrayList;

public class searchactivity extends AppCompatActivity{
    String searchUrl,searchedFor;
    String downloadlink="",result2="";
    Intent downloadintent;
    String format="";
    ConnectionDetector connectionDetector;
    Context context;
    private ProgressDialog pd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerviewlayout2);
        context=this;
        searchedFor=getIntent().getExtras().getString("searchedfor");
        searchUrl="http://www.songsmp3.com/category/search?search="+searchedFor;
        connectionDetector=new ConnectionDetector(context);
        if(connectionDetector.isConnectingToInternet())
        {
            new searchResults().execute((Void[]) null);
        }
        else
        {
            Toast.makeText(context,"No Internet Connection",Toast.LENGTH_SHORT).show();
            onBackPressed();
        }
    }
    private class searchResults extends AsyncTask<Void,Void,ArrayList<FirstList>> {
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
        protected ArrayList<FirstList> doInBackground(Void... params) {
            ArrayList<FirstList> flist=new ArrayList<FirstList>();
            try {
                Document document= Jsoup.connect(searchUrl).timeout(10000).get();
                Elements srls=document.select("div.search_results_list");
                for(org.jsoup.nodes.Element srl:srls)
                {
                    FirstList fl=new FirstList();
                    Elements ul=srl.select("ul");
                    Elements li=ul.select("li");
                    fl.Name=li.get(0).text().substring(7);
                    fl.SongLink=fl.SongLink+srl.select("div.slcol").select("a").attr("href");
                    fl.Type=li.get(1).text().substring(7);
                    fl.ImageLink =fl.ImageLink+Jsoup.connect(fl.SongLink).timeout(10000).get().select("div.movie_cover").select("img").attr("src");
                    flist.add(fl);
                }
                Elements li2=document.select("li.page");
                //Change this if u want more search results
                int pages=Math.min(2, li2.size());
                String searchUrl2="";
                for(int page=2;page<=pages;page++)
                {
                    searchUrl2=searchUrl+"&search_page="+String.valueOf(page);
                    document= Jsoup.connect(searchUrl2).timeout(10000).get();
                    srls = document.select("div.search_results_list");
                    for(org.jsoup.nodes.Element srl:srls)
                    {
                        FirstList fl=new FirstList();
                        Elements ul=srl.select("ul");
                        Elements li=ul.select("li");
                        fl.Name=li.get(0).text().substring(7);
                        fl.SongLink=fl.SongLink+srl.select("div.slcol").select("a").attr("href");
                        fl.Type=li.get(1).text().substring(7);
                        fl.ImageLink =fl.ImageLink+Jsoup.connect(fl.SongLink).timeout(10000).get().select("div.movie_cover").select("img").attr("src");
                        flist.add(fl);
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return flist;
        }

        @Override
        protected void onPostExecute(ArrayList<FirstList> flist) {
            if (pd!=null) {
                pd.dismiss();
            }
            super.onPostExecute(flist);
            if(flist.size()==0)
            {
                Toast.makeText(context,"No results found",Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
            String temp="";
            for(int i=0;i<flist.size();i++)
            {
                temp=temp+flist.get(i).Name+"\n"+flist.get(i).Type+"\n";
            }
            RVAdapter adapter = new RVAdapter(flist);
            RecyclerView rv = (RecyclerView)findViewById(R.id.rv2);
            StaggeredGridLayoutManager sglm=new StaggeredGridLayoutManager(2,1);
            LinearLayoutManager llm=new LinearLayoutManager(context);
            rv.setLayoutManager(sglm);
            rv.setAdapter(adapter);
        }
    }
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.SongViewHolder>{

        ArrayList<FirstList> songs;
        RVAdapter(ArrayList<FirstList> songss){
            songs=songss;
        }
        public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView cv;
            TextView Name;
            ImageView songPhoto;

            SongViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.cv3);
                Name = (TextView)itemView.findViewById(R.id.name);
                songPhoto = (ImageView)itemView.findViewById(R.id.song_photo);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                final int position=getAdapterPosition();
                if(songs.get(position).Type.equals("Song"))
                {
                    AsyncTask<Void,Void,String> task=new AsyncTask<Void, Void, String>() {
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
                        protected String doInBackground(Void... params) {
                            FirstList song=songs.get(position);
                            try {
                                Document document= Jsoup.connect(song.SongLink).timeout(10000).get();
                                Elements linkitems=document.select("div.link-item");
                                for(org.jsoup.nodes.Element linkitem:linkitems)
                                {
                                    String temp=linkitem.select("div.link").text();
                                    if(temp.contains(song.Name)||song.Name.contains(temp)){
                                        Elements all_a=linkitem.select("a");
                                        downloadlink=all_a.get(0).attr("href");
                                        downloadlink=downloadlink.substring(10);
                                        for(int c=0;c<downloadlink.length();c++)
                                        {
                                            if(downloadlink.charAt(c)=='/'){
                                                downloadlink=downloadlink.substring(0,c);
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return downloadlink;
                        }
                        @Override
                        protected void onPostExecute(String result) {
                            super.onPostExecute(result);
                            if (pd!=null) {
                                pd.dismiss();
                            }
                            result2=result;
                            downloadintent=new Intent(context,DownloadActivity.class);
                            SharedPreferences getData= PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            String et=getData.getString("list","3");
                            if(et.contentEquals("3")) {
                                CharSequence type[] = new CharSequence[]{"128", "320","Select Default"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setTitle("Choose a Format");
                                builder.setItems(type, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (which == 3) {
                                            Intent i = new Intent(context, prefsactivity.class);
                                            startActivity(i);
                                        } else {
                                            if (which == 0)
                                                format = "128";
                                            else
                                                format = "0";
                                            result2 = "http://dl.songsmp3.com/fileDownload/Songs/" + format + "/" + result2 + ".mp3";
                                            downloadintent.putExtra("downloadurl", result2);
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
                                result2 = "http://dl.songsmp3.com/fileDownload/Songs/" + format + "/" + result2 + ".mp3";
                                downloadintent.putExtra("downloadurl", result2);
                                downloadintent.putExtra("songname", songs.get(position).Name);
                                startActivity(downloadintent);
                            }
                        }
                    };
                    task.execute();
                }
                else if(songs.get(position).Type.equals("Movies")){
                    if(connectionDetector.isConnectingToInternet()) {
                        Intent movieintent = new Intent(context, Movie.class);
                        FirstList song = songs.get(position);
                        movieintent.putExtra("searchedfor", song.SongLink);
                        movieintent.putExtra("MOVIE", song.Name);
                        movieintent.putExtra("IMAGE", song.ImageLink);
                        startActivity(movieintent);
                    }
                    else
                    {
                        Toast.makeText(context,"No Internet Connection",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.latestsongsxml, viewGroup, false);
            SongViewHolder svh = new SongViewHolder(v);
            return svh;
        }

        @Override
        public void onBindViewHolder(SongViewHolder songViewHolder, int i) {
            songViewHolder.Name.setText(songs.get(i).Name+"\n"+songs.get(i).Type);
            Picasso.with(context).load(songs.get(i).ImageLink).placeholder(R.drawable.loading).error(R.drawable.loading).into(songViewHolder.songPhoto);
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
