package com.example.vojjalateja.symphonia;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

public class SongsManager {
    private ArrayList<FirstList> flist;
    File file;
    public ArrayList<FirstList> getPlaylist(){
        flist = new ArrayList<>();
        file =new File(Environment.getExternalStorageDirectory()+"/symphonia_downloads");
        if (!file.exists()) {
            file.mkdirs();
        }
        File list[] = file.listFiles();
        for( int i=0;i<list.length; i++)
        {
            FirstList flis=new FirstList();
            flis.SongLink=list[i].getPath();
            flis.Name=list[i].getName().substring(0,list[i].getName().length()-4);
            flist.add(flis);
        }
        return flist;
    }
}
