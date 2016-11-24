package com.example.pcpv.a1_dropbox;

/**
 * Created by PCPV on 18/11/2015.
 */

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;


public class Rename extends DialogFragment {
    private DropboxAPI.Entry[] e;
    private DropboxAPI<?> mApi;
    private Context context;
    private int index;
    private FolderList folderList;
    public Rename(Context context, DropboxAPI<?> api,DropboxAPI.Entry[]e, int index, FolderList fl) {
        this.context = context;
        this.e = e;
        this.index = index;
        mApi = api;
        folderList = fl;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.rename, container, false);
        final EditText name = (EditText) v.findViewById(R.id.newName);
        Button submit = (Button) v.findViewById(R.id.rename);
        Button cancel = (Button) v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Rename.this.dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = name.getText().toString();
                if (newName.contains("/") || newName.contains("?") || newName.contains("\\") || newName.contains("*")) {
                    showToast("File or folder name can't contain special characters!");
                } else {
                    new RenameAction(newName).execute();
                }
            }
        });
        return v;
    }
    public class RenameAction extends AsyncTask<Void,Long,Boolean> {
        private boolean result;
        private String newName;
        private final ProgressDialog dialog;
        public RenameAction(String name) {
            newName = name;
            dialog = new ProgressDialog(context);
            dialog.setMessage("Renaming...");
            Rename.this.dismiss();
            dialog.show();
            dialog.setContentView(R.layout.processing);

        }
        @Override
        protected Boolean doInBackground(Void... params) {
            String currentPath = e[index].path;
            String parentPath = e[index].parentPath();
            String change = newName;
            parentPath = parentPath + "" + change;
            DropboxAPI.Entry RenamedFile;
            try {
                RenamedFile = mApi.move(currentPath, "/"+ change);
                DropboxAPI.Entry MoveRenameFile = mApi.move(RenamedFile.path,parentPath);
                result = true;
            } catch (DropboxException e1) {
                e1.printStackTrace();
                result = false;
            }

            return result;
        }
        @Override
        protected void onProgressUpdate(Long... progress) {
            int percent = (int)(100.0*(double)progress[0]/100 + 0.5);
            dialog.setProgress(percent);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (result) {
                showToast("Renamed!");
                folderList.setView();
            } else
                showToast("Cannot rename !");
            }
        }

        private void showToast(String msg) {
            Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            t.show();
        }
    }




