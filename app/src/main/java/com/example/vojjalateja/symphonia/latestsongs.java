package com.example.vojjalateja.symphonia;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class latestsongs extends Fragment{

    Context context;
    static List<FirstList> flist;
    public latestsongs() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=getActivity();
        if(MainActivity.firsttime==0) {
            flist = new ArrayList<>();
            ConnectionDetector connectionDetector = new ConnectionDetector(getActivity());
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Document document = Jsoup.connect("http://www.songsmp3.co").timeout(10000).get();
                        Elements li = document.select("div.image_box").select("li");
                        Element l;
                        for (int i = 0; i < li.size(); i++) {
                            l = li.get(i);
                            FirstList flis = new FirstList();
                            flis.ImageLink = flis.ImageLink + l.select("img").attr("src");
                            flis.ImageLink = flis.ImageLink.replaceAll("\\s","%20");
                            flis.Name = l.select("img").attr("alt");
                            flis.SongLink = flis.SongLink + l.select("a").attr("href");
                            flist.add(flis);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    super.onPostExecute(aVoid);
                    Intent intent=new Intent();
                    intent.setAction("com.vojjalateja.deleteaction");
                    getActivity().sendBroadcast(intent);
                }
            };
            if (connectionDetector.isConnectingToInternet())
                task.execute();
        }
        context=getActivity();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.staggeredrecyclerview, container, false);
        RVAdapter2 adapter = new RVAdapter2(flist);
        RecyclerView rv = (RecyclerView)view.findViewById(R.id.staggered_recycler_view);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(1,1);
        rv.setLayoutManager(sglm);
        rv.setAdapter(adapter);
        return view;
    }
    private class RVAdapter2 extends RecyclerView.Adapter<RVAdapter2.SongViewHolder>{

        List<FirstList> songs;
        RVAdapter2(List<FirstList> songs){
            this.songs=songs;
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
                int position=getAdapterPosition();
                ConnectionDetector connectionDetector=new ConnectionDetector(context);
                boolean connected=connectionDetector.isConnectingToInternet();
                Intent movieintent=new Intent(context,Movie.class);
                if(connected==true) {
                    FirstList song = songs.get(position);
                    movieintent.putExtra("searchedfor", song.SongLink);
                    movieintent.putExtra("MOVIE", song.Name);
                    movieintent.putExtra("IMAGE", song.ImageLink);
                    startActivity(movieintent);
                }
                else{
                    Toast.makeText(context, "No Internet Connection", Toast.LENGTH_LONG).show();
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
            FirstList song=songs.get(i);
            songViewHolder.Name.setText(song.Name);
            Log.d("image",song.ImageLink);
            Picasso.with(context).load(song.ImageLink).fit().centerCrop().placeholder(R.drawable.loading).error(R.drawable.loading).into(songViewHolder.songPhoto);
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
}
