package com.example.vojjalateja.symphonia;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class downloads extends Fragment{

    private List<FirstList> flist;
    File file;
    Context context;
    MediaMetadataRetriever metadataRetriever;
    File list[];
    private List<byte []>art;
    public downloads() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        context=getActivity();
        flist = new ArrayList<>();
        art=new ArrayList<>();
        file =new File(Environment.getExternalStorageDirectory()+"/symphonia_downloads");
        if (!file.exists()) {
            file.mkdirs();
        }
        metadataRetriever=new MediaMetadataRetriever();
        list= file.listFiles();
        for( int i=0;i<list.length; i++)
        {
            try
            {
                FirstList flis=new FirstList();
                flis.SongLink=list[i].getPath();
                metadataRetriever.setDataSource(flis.SongLink);
                art.add(metadataRetriever.getEmbeddedPicture());
                flis.Name=list[i].getName().substring(0,list[i].getName().length()-4);
                flist.add(flis);
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        MainActivity.numberoffiles=list.length;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.recyclerviewlayout2, container, false);
        context=getActivity();
        RVAdapter3 adapter = new RVAdapter3(flist);
        RecyclerView rv = (RecyclerView)view.findViewById(R.id.rv2);
        StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(2,1);
        rv.setLayoutManager(sglm);
        rv.setAdapter(adapter);
        return view;
    }
    private class RVAdapter3 extends RecyclerView.Adapter<RVAdapter3.SongViewHolder>{

        List<FirstList> songs;
        RVAdapter3(List<FirstList> songs){
            this.songs=songs;
        }
        public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener {
            CardView cv;
            TextView Name;
            ImageView songPhoto;
            SongViewHolder(View itemView) {
                super(itemView);
                cv = (CardView)itemView.findViewById(R.id.cv3);
                Name = (TextView)itemView.findViewById(R.id.name);
                songPhoto = (ImageView)itemView.findViewById(R.id.song_photo);
                itemView.setOnClickListener(this);
                itemView.setOnLongClickListener(this);
            }
            @Override
            public void onClick(View v) {
                int position=getAdapterPosition();
                Intent intent=new Intent(context,MusicPlayer.class);
                intent.putExtra("position",position);
                startActivity(intent);
            }

            @Override
            public boolean onLongClick(View v) {
                CharSequence type[] = new CharSequence[]{"Play","Rename","Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Choose a Format");
                builder.setItems(type, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            int position=getAdapterPosition();
                            Intent intent=new Intent(context,MusicPlayer.class);
                            intent.putExtra("position", position);
                            startActivity(intent);
                        }
                        else if(which==1){
                            AlertDialog.Builder alert = new AlertDialog.Builder(context);
                            alert.setTitle("Rename file to??");
                            final EditText input = new EditText(context);
                            alert.setView(input);
                            input.requestFocus();
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String srt = input.getEditableText().toString();
                                    File f = new File(list[getAdapterPosition()].getAbsolutePath());
                                    File fff = new File(Environment.getExternalStorageDirectory() + "/symphonia_downloads", srt + ".mp3");
                                    boolean fffff = f.renameTo(fff);
                                    Intent intent=new Intent();
                                    intent.setAction("com.vojjalateja.deleteaction");
                                    getActivity().sendBroadcast(intent);
                                }
                            });
                            alert.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alertDialog = alert.create();
                            alertDialog.show();
                        }
                        else if(which==2) {
                            File ff = new File(list[getAdapterPosition()].getAbsolutePath());
                            if(ff.delete())
                            {
                                Intent intent = new Intent();
                                intent.setAction("com.vojjalateja.deleteaction");
                                getActivity().sendBroadcast(intent);
                            }
                        }
                    }
                });
                builder.show();
                return true;
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
            byte[] ar=art.get(i);
            songViewHolder.Name.setText(song.Name);
            if(null != art) {
                Bitmap bm = BitmapFactory.decodeByteArray(ar, 0, ar.length);
                songViewHolder.songPhoto.setImageBitmap(bm);
            }
            else{
                songViewHolder.songPhoto.setImageDrawable(getResources().getDrawable(R.drawable.musicsearch));
            }
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
