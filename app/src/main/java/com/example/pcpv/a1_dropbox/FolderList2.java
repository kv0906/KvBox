package com.example.pcpv.a1_dropbox;

/**
 * Created by PCPV on 12/11/2015.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;

import com.dropbox.client2.DropboxAPI.*;
import com.dropbox.client2.exception.DropboxException;

import android.view.View.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
public class FolderList2 extends ArrayAdapter {
    //declare variables
    private final int USER_OPTION = 100;
    private final static String IMAGE_FILE_NAME = "dbroulette.png";
    private DropboxAPI<?> mApi;
    private Context context;
    private Entry[] ent;
    private String path2;
    private FolderList f;

    //constructor for folderlist2
    public FolderList2(Context context, DropboxAPI<?> api, Entry[] e, String path, FolderList folderlist) {
        super(context, R.layout.create_folder, e);
        this.context = context;
        mApi = api;
        ent = e;
        path2 = path;
        f = folderlist;

    }

    public View getView(final int index, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.create_folder, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        final ImageView arrow_down = (ImageView) rowView.findViewById(R.id.arrow_down);

        if (ent[index] != null) {
            //prevent name too long
            if (ent[index].fileName().length() > 26) {
                textView.setText(ent[index].fileName().substring(0, 12) + "..." + ent[index].fileName().substring(ent[index].fileName().length() - 6, ent[index].fileName().length()));
            } else {
                textView.setText(ent[index].fileName());
            }
        }
        //check the entry is Directory or file
        if (ent[index].isDir) {
            imageView.setImageResource(R.drawable.directory);
        } else {
            imageView.setImageResource(R.drawable.file);
        }
        //add function for Arrow down icon ( for more options in menu)
        arrow_down.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.equals(arrow_down))
                    onCreateDialog(USER_OPTION, ent[index], index);
            }
        });
        //Handle images as entries( files)
        if (ent[index].fileName().length() > 4) {
            String ext = ent[index].fileName().substring(ent[index].fileName().length() - 4, ent[index].fileName().length());
            String ext2 = ent[index].fileName().substring(ent[index].fileName().length() - 5, ent[index].fileName().length());
            if (ext.equals(".png") || ext.equals(".jpg") || ext2.equals(".jpeg")) {
                imageView.setImageResource(R.drawable.image);
                //new loadThumbnail(imageView, ent[index]).execute();
            }
        }
        return rowView;
    }


    public Dialog onCreateDialog(int id, Entry e, int position) {
        final Entry entry = e;
        Dialog dialog = null;
        final int post = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String[] actions = {"Download", "Delete", "Rename", "Move"};
        if (e.isDir) {
            String[] actionDir = {"Delete", "Move", "Rename"};
            actions = actionDir;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, actions);
        switch (id) {
            case USER_OPTION:
                final String[] finalAction = actions;
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        if (finalAction[index].equals("Delete")) {
                            try {
                                if (entry.isDir)
                                    mApi.delete(entry.path + "/");
                                else
                                    mApi.delete(entry.path);
                                f.setView();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else if (finalAction[index].equals("Download")) {
                            Download download = new Download(context, mApi,entry.fileName(),path2);
                            download.execute();
                        }
                        else if (finalAction[index].equals("Rename")) {
                            f.enableRename(f,post,ent);
                        }
                        else {
                            f.enableMove(entry.path,entry.fileName());
                        }
                    }
                });
                break;

        }
        dialog = builder.show();
        return dialog;


    }
}