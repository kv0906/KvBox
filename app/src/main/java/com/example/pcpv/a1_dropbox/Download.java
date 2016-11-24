package com.example.pcpv.a1_dropbox;
/**
 * Created by PCPV on 18/11/2015.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;


public class Download extends AsyncTask<Void,Long,Boolean>{
    private DropboxAPI<?> mApi;
    private Context mContext;
    private FileOutputStream mFos;
    private final ProgressDialog mDialog;
    private Long fLength;
    private boolean mCanceled;
    private String errorMsg;
    private String fileName;
    private String downloadPth;

    public Download(Context context,DropboxAPI<?>api , String f ,String path) {
        this.mApi = api;
        mContext = context.getApplicationContext();
        fileName = f;
        downloadPth = path + f;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Downloading File...");
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCanceled = true;
                errorMsg = "Download canceled";
                if (mFos != null) {
                    try {
                        mFos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mDialog.show();
    }
    @Override
    protected Boolean doInBackground(Void... params) {
        //Setting up download directly do folder Download of SD card
        String fpath = "/sdcard/Download/" + fileName;
        File file = new File(fpath);
        boolean result = false;
        if (mCanceled) {
            return false;
        }
        try {mFos = new FileOutputStream(file);}
        catch (FileNotFoundException e1) {e1.printStackTrace();}
        try {
            if(mCanceled) {
                return false;
            }
            DropboxAPI.DropboxFileInfo fileInfo = mApi.getFile(downloadPth,null,mFos,null);
            Entry temp = mApi.metadata(downloadPth,1000,null,true,null);
            fLength = temp.bytes;
            result = true;

        }
        catch (DropboxException e) {
            file.delete();
            result = false;
        }
        return result;
    }
    @Override
    protected void onProgressUpdate(Long... progress) {
        int percent = (int)(100.0*(double)progress[0]/fLength + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            showToast("Downloaded successfully !");

        } else {
            showToast(errorMsg);
        }
    }

    private void showToast(String msg) {
        Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        toast.show();
    }
















































}
