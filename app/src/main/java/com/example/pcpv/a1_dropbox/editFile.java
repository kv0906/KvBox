package com.example.pcpv.a1_dropbox;

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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Created by PCPV on 25/11/2015.
 */
public class editFile extends DialogFragment {
    private DropboxAPI<?> mApi;
    private DropboxAPI.DropboxFileInfo fileInfo;
    private Context context;
    private DropboxAPI.Entry e;
    private TextView title;
    private EditText editText;
    private Button closeBtn;
    private Button saveBtn;
    private File f;
    private FileOutputStream fos;
    private String fileTitle;

    public editFile(Context context, DropboxAPI<?> mApi, DropboxAPI.Entry e) {
        this.context = context;
        this.mApi = mApi;
        this.e = e;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.editfile, container, false);
        title = (TextView) v.findViewById(R.id.title);
        editText = (EditText) v.findViewById(R.id.edit_text);
        closeBtn = (Button) v.findViewById(R.id.close);
        saveBtn = (Button) v.findViewById(R.id.save);
        title.setText(fileTitle);
        fileTitle = e.fileName();
        title.setText(fileTitle);
        String path = "/sdcard/Download/" + fileTitle;
        f = new File(path);
        try {
            fos = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            DropboxAPI.DropboxFileInfo fileInfo = mApi.getFile(e.path, null, fos, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader("/sdcard/Download/" + fileTitle));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line + '\n');
                text.toString();
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        editText.setText(text);
        f.delete();
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new save().execute();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editFile.this.dismiss();
            }
        });
        return v;
    }
    public class save extends AsyncTask<Void,Long,Boolean> {
        private boolean result;
        private ProgressDialog dialog;
        public save() {
            dialog = new ProgressDialog(context);
            editFile.this.dismiss();
            dialog.show();
            dialog.setContentView(R.layout.processing);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                String outPth = "/sdcard/Download/" + "edit.txt";
                File newFile = new File(outPth);
                FileOutputStream nFos = new FileOutputStream(newFile);
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(nFos));
                String content = editText.getText().toString();
                String[] array = content.split("\n");
                for(int i=0;i<array.length;i++){
                    out.write(array[i] + "\n");
                }
                out.close();
                outPth = "/sdcard/Download/" + "edit.txt";
                newFile = new File(outPth);
                FileInputStream fis = new FileInputStream(newFile);
                DropboxAPI.Entry entry = mApi.putFileOverwrite(e.path, fis, newFile.length(), null);
                result = true;
                out.close();
                newFile.delete();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }
        protected void onProgressUpdate(Long... progress) {
            int percent = (int)(100.0*(double)progress[0]/100 + 0.5);
            dialog.setProgress(percent);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            if (result) {
                showToast("Saved!");
            } else {
                showToast("Cannot save the modification !");
            }
        }

        private void showToast(String msg) {
            Toast t = Toast.makeText(context, msg, Toast.LENGTH_LONG);
            t.show();
        }
    }
}
