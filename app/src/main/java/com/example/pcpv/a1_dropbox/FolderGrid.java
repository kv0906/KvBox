package com.example.pcpv.a1_dropbox;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;

import java.io.FileOutputStream;

/**
 * Created by PCPV on 24/11/2015.
 */
public class FolderGrid extends ArrayAdapter {
        private final Context context;
        private final DropboxAPI<?> mApi;
        private final DropboxAPI.Entry[] e;
        private String mPath;
        private FolderList f;
        public FolderGrid(Context context, DropboxAPI<?> api, DropboxAPI.Entry[] e, String path, FolderList f) {
            super(context,R.layout.view_folder,e);
            this.context = context;
            this.e = e;
            mApi = api;
            mPath = path;
            this.f = f;
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.grid_view, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.title);
            final ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            final ImageView download = (ImageView) rowView.findViewById(R.id.download);
            final ImageView delete = (ImageView) rowView.findViewById(R.id.delete);
            final ImageView move = (ImageView) rowView.findViewById(R.id.move);
            final ImageView rename = (ImageView) rowView.findViewById(R.id.rename);

            final LinearLayout downloadLinear = (LinearLayout) rowView.findViewById(R.id.downloadLayout);

            if (e[position]!=null) {
                if(e[position].fileName().length()>26){
                    String name = e[position].fileName();
                    textView.setText(name.substring(0,13) + "..." + name.substring(name.length()-6,name.length()));
                }
                else {
                    textView.setText(e[position].fileName());
                }
                if (e[position].isDir) {
                    download.setVisibility(View.GONE);
                    downloadLinear.setVisibility(View.GONE);
                }
            }
            // Change the icon for folders and files
            if(e[position]==null){
                imageView.setImageResource(R.drawable.directory_up);
            }
            else if(e[position].isDir){
                imageView.setImageResource(R.drawable.directory);
            }
            else{
                imageView.setImageResource(R.drawable.file);
            }
            //If the entry is an image file then load the thumbnail
            if (e[position].fileName().length()>4) {
                String extension = e[position].fileName().substring(e[position].fileName().length() - 4, e[position].fileName().length());
                if (extension.equalsIgnoreCase(".jpg") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase(".png")) {
                    imageView.setImageResource(R.drawable.image);
                }
            }
            download.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (v.equals(download)) {
                        Download d = new Download(context,mApi,e[position].fileName(),mPath);
                        d.execute();
                    }
                }
            });
            delete.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    try {
                        if (e[position].isDir)
                            mApi.delete(e[position].path + "/");
                        else
                            mApi.delete(e[position].path);
                        f.setView();

                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            move.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    f.enableMove(e[position].path, e[position].fileName());
                    f.setView();
                }
            });
            rename.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    f.enableRename(f,position,e);
                }
            });
            return rowView;
        }
                }




