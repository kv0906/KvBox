package com.example.pcpv.a1_dropbox;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.view.View;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;

/**
 * Created by PCPV on 23/11/2015.
 */
public class Move extends DialogFragment {
    private DropboxAPI<?> mApi;
    private Context context;
    private ListView fileList;
    private List2 list2;
    private FolderList f;
    private DropboxAPI.Entry[] en = null;
    private boolean isRoot = true;
    private String selectedFile;
    private String path = "/";
    private String previousPath;
    private String fname;
    ArrayList<String> arrayStr = new ArrayList<>();

    public Move(Context context, DropboxAPI mApi, String previousPath, String fname, FolderList f) {
        this.context = context;
        this.mApi = mApi;
        this.previousPath = previousPath;
        this.fname = fname;
        this.f = f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.move, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        fileList = (ListView) v.findViewById(R.id.listView);
        Button ok = (Button) v.findViewById(R.id.ok);
        Button cancel = (Button) v.findViewById(R.id.cancel);
        final EditText showPath = (EditText) v.findViewById(R.id.link_path);
        setView();
        //handle event
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (en[position] != null) {
                    selectedFile = en[position].fileName();
                    if (en[position].isDir) {
                        isRoot = true;
                        arrayStr.add(selectedFile);
                        en = null;
                        path = path + selectedFile + "/";
                        showPath.setText(path);
                        setView();
                    }
                } else {
                    String s = arrayStr.remove(arrayStr.size() - 1);
                    path = path.substring(0, path.lastIndexOf(s));
                    en = null;
                    if (arrayStr.isEmpty()) {
                        isRoot = true;
                    }
                    setView();
                }
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newPath = showPath.getText().toString();
                newPath = newPath + "/" + fname;
                new MoveDo(previousPath, newPath).execute();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Move.this.dismiss();
            }
        });
        return v;
    }

    public void loadFolder(String p) {
        try {
            int i = 0;
            DropboxAPI.Entry entries = mApi.metadata(p, 1000, null, true, null);
            for (DropboxAPI.Entry e : entries.contents) {
                if (e.isDir)
                    i++;
            }
            en = new DropboxAPI.Entry[i];
            i = 0;
            for (DropboxAPI.Entry e : entries.contents) {
                if (e.isDir) {
                    en[i] = e;
                    i++;
                }
            }

            if (isRoot) {
                DropboxAPI.Entry[] temp = new DropboxAPI.Entry[en.length + 1];
                for (int j = 0; j < en.length; j++) {
                    temp[j + 1] = en[j];
                }
                en = temp;
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }
    }

    public void setView() {
        loadFolder(path);
        list2 = new List2(context, mApi, en);
        fileList.setAdapter(list2);
    }
    public class MoveDo extends AsyncTask<Void,Long,Boolean> {
        private String curPth;
        private String newPth;
        private boolean result;
        private ProgressDialog dialog;
        public MoveDo(String curPth,String newPth) {
            this.curPth = curPth;
            this.newPth = newPth;
            dialog = new ProgressDialog(context);
            Move.this.dismiss();
            dialog.show();
            dialog.setContentView(R.layout.processing);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                mApi.move(curPth,newPth);
                result = true;
            } catch (DropboxException e) {
                e.printStackTrace();
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
                showToast("Moved!");
                f.setView();
            } else {
                showToast("Cannot move !");
            }
        }

        private void showToast(String msg) {
            Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            t.show();
        }



    }
}
