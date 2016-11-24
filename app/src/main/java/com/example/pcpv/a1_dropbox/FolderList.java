package com.example.pcpv.a1_dropbox;

/**
 * Created by PCPV on 11/11/2015.
 */
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;


import java.io.IOException;
import java.util.ArrayList;
import android.view.View.OnClickListener;
import android.widget.Toast;
public class FolderList extends DialogFragment{
    //declare variables
    private DropboxAPI<?> mApi;
    private final Context context;
    private DropboxAPI.Entry[] entry = null;
    private boolean isRoot = true;
    private ListView lsView;
    private GridView grdView;
    private FolderList2 fl;
    private FolderGrid fg;
    private String choosenFile;
    private String path = "/";
    ArrayList<String> str = new ArrayList<>();
    private ImageView list;
    private ImageView grid;
    private ImageView back;
    private ImageView upload;
    private TextView logout;
    private TextView title;
    private MainActivity mainAc;

    private TextView isEmpty; //the text view show whenever there is no files in directory
    private String status = " list"; //default showing is list view
    //Constructor for folder list
    public FolderList(Context context, DropboxAPI<?> api,MainActivity mainAc) {
        this.context = context;
        mApi = api;
        this.mainAc = mainAc;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //inflate layout from xml file
        View v = inflater.inflate(R.layout.view_folder,container,false);
        //retrieve element in xml file by ID
        lsView = (ListView) v.findViewById(R.id.listView);
        grdView = (GridView) v.findViewById(R.id.gridView);
        list = (ImageView) v.findViewById(R.id.list);
        grid = (ImageView) v.findViewById(R.id.grid);
        back = (ImageView) v.findViewById(R.id.back);
        upload = (ImageView) v.findViewById(R.id.upload);
        isEmpty = (TextView) v.findViewById(R.id.nothing);
        title = (TextView) v.findViewById(R.id.title);
        logout = (TextView) v.findViewById(R.id.logout);
        //set isEmpty and grid view defaultly is Gone ( not showing up)
        setView();
        lsView.setVisibility(View.VISIBLE);
        isEmpty.setVisibility(View.GONE);
        grdView.setVisibility(View.GONE);
        //functions for those icons and each entries in the list view or grid view ( list, grid,back ,upload, logout)
            //List Icon
            list.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    status ="list";
                    if(entry.length == 0) { //entry (files ) is 0
                        //the case that folder doesnt have files => showing empty
                        isEmpty.setVisibility(View.VISIBLE);
                        lsView.setVisibility(View.GONE);
                        grdView.setVisibility(View.GONE);}
                    else {
                        //if it has entries => default list view will be prioritized
                        lsView.setVisibility(View.VISIBLE);
                        isEmpty.setVisibility(View.GONE);
                        grdView.setVisibility(View.GONE);}

                }
            });
            //Grid Icon
            grid.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                status ="grid";
                if(entry.length == 0) { //entry (files ) is 0
                    //the case that folder doesnt have files => showing empty
                    isEmpty.setVisibility(View.VISIBLE);
                    lsView.setVisibility(View.GONE);
                    grdView.setVisibility(View.GONE);}
                else {
                    //if it has entries => default list view will be prioritized
                    grdView.setVisibility(View.VISIBLE);
                    isEmpty.setVisibility(View.GONE);
                    lsView.setVisibility(View.GONE);}

            }
        });
            //Upload icon
            upload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                enableUpload();
            }
        });
            //Log out button
            logout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainAc.logOut(); //log out from the linked dropbox account
                    FolderList.this.dismiss();
                }
            });
            //Back Icon
            back.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = str.remove(str.size()-1);
                    path = path.substring(0,path.lastIndexOf(s));
                    entry = null;
                    //so we check if the arraylist str is empty that means like root directory, cannot go back anymore
                    if (str.isEmpty()) {
                        isRoot = true;
                        title.setText("Home");
                    }
                    setView();


                }
            });
            //Each individual entry in the list
            lsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                    if (entry[index] != null) {
                        choosenFile = entry[index].fileName();
                        if (entry[index].isDir) { //if entry is a directory
                            isRoot = false;
                            str.add(choosenFile);
                            //Handling the name of file or directory too long => damage the layout
                            if (choosenFile.length() > 12) {
                                String showUp = choosenFile.substring(0, 9) + "...";
                                title.setText(showUp);
                            } else {
                                title.setText(choosenFile);
                            }
                            entry = null;
                            path = path + choosenFile + "/";
                            setView(); //set up view again
                        } else {
                            if (entry[index].fileName().length() > 4) {
                                String ext = entry[index].fileName().substring(entry[index].fileName().length() - 4, entry[index].fileName().length());
                                if (ext.equalsIgnoreCase(".txt")) {
                                    try {
                                        enableEdit(entry[index]);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    showToast("Format is not supported. Only supporting edit text files !");
                                }

                            } else {
                                showToast("File does not have extension !");
                            }
                        }
                    }
                }
            });
            //Each individual entry in the list
            grdView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int index, long id) {
                if (entry[index] != null) {
                    choosenFile = entry[index].fileName();
                    if (entry[index].isDir) { //if entry is a directory
                        isRoot = false;
                        str.add(choosenFile);
                        //Handling the name of file or directory too long => damage the layout
                        if (choosenFile.length() > 12) {
                            String showUp = choosenFile.substring(0, 9) + "...";
                            title.setText(showUp);
                        } else {
                            title.setText(choosenFile);
                        }
                        entry = null;
                        path = path + choosenFile + "/";
                        setView(); //set up view again
                    } else {
                        if (entry[index].fileName().length() > 4) {
                            String ext = entry[index].fileName().substring(entry[index].fileName().length() - 4, entry[index].fileName().length());
                            if (ext.equalsIgnoreCase(".txt")) {
                                try {
                                    enableEdit(entry[index]);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showToast("Format is not supported. Only supporting all images types and text files !");
                            }

                        } else {
                            showToast("File does not have extension !");
                        }
                    }
                }
            }
        });
        return v;
    }
    //functionns to load the entries from the dropbox

    protected void loadFolder(String p) {
        try {
            int i = 0;
            DropboxAPI.Entry entries = mApi.metadata(p, 1000 , null , true, null); //retrieve entries from dropbox
            entry = new DropboxAPI.Entry[(entries.contents.size())]; //create an array based on the size of dropbox entries content size
            for (DropboxAPI.Entry e: entries.contents) {
                entry[i] = e;
                i++;
            }
            if(!isRoot) {back.setVisibility(View.VISIBLE);}
            else{back.setVisibility(View.GONE);}
        }
        catch(DropboxException e) {e.printStackTrace();}
    }
    //Set up view if something change
    public void setView() {
        loadFolder(path);
        fl = new FolderList2(context,mApi,entry,path,FolderList.this);
        lsView.setAdapter(fl);
        fg = new FolderGrid(context,mApi,entry,path,FolderList.this);
        grdView.setAdapter(fg);

        if (entry.length == 0) {
            isEmpty.setVisibility(View.VISIBLE);
            lsView.setVisibility(View.GONE);
            grdView.setVisibility(View.GONE);
        }
        else if (status.equals("list")) {
            lsView.setVisibility(View.VISIBLE);
            grdView.setVisibility(View.GONE);
            isEmpty.setVisibility(View.GONE);}
        else {
            grdView.setVisibility(View.VISIBLE);
            lsView.setVisibility(View.GONE);
            isEmpty.setVisibility(View.GONE);}


        }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //just going to remove the title of the dialog
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
    //Dialog Fragments
    public void enableUpload() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null); //clear the previous fragment to show the new one
        //create new dialog fragment and display
        DialogFragment df = new Upload(context, mApi, path, FolderList.this);
        df.show(ft, "dialog");
    }
    public void enableRename(FolderList f, int index, DropboxAPI.Entry[] e) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null); //clear the previous fragment to show the new one
        //create new dialog fragment and display
        DialogFragment df = new Rename(context,mApi,e,index,f);
        df.show(ft, "dialog");
    }
    public void enableMove(String curPth, String fname) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        DialogFragment df = new Move(context,mApi,curPth,fname,FolderList.this);
        df.show(ft,"dialog");
    }
    public void enableEdit(DropboxAPI.Entry ent) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.addToBackStack(null);
        DialogFragment df = new editFile(context,mApi,ent);
        df.show(ft,"dialog");
    }
    //Function to show toast
    private void showToast(String msg) {
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }

}
