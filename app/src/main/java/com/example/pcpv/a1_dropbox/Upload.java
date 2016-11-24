package com.example.pcpv.a1_dropbox;

/**
 * Created by PCPV on 17/11/2015.
 */
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.dropbox.client2.DropboxAPI;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class Upload extends DialogFragment {
    private DropboxAPI<?> mApi;
    private Context context;
    private ListView uploadList;
    private File path = new File(Environment.getExternalStorageDirectory() + "");
    private Item[] fileList;
    private Button cancel;
    private FolderList folderList;
    private boolean cancelUp;
    private boolean isRoot = true;
    private String chosenFile;
    private String mPath;
    ListAdapter adapter;
    ArrayList<String> arrayStr = new ArrayList<>();

    //constructor
    public Upload(Context context, DropboxAPI<?> api, String path, FolderList fl) {
        this.context = context;
        mApi = api;
        this.mPath = path;
        this.folderList = fl;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.upload, container, false);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        uploadList = (ListView) v.findViewById(R.id.listView);
        cancel = (Button) v.findViewById(R.id.cancel);
        //call func loadFile to load the number of file in the list to display
        loadFiles();
        uploadList.setAdapter(adapter);
        uploadList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenFile = fileList[position].file;
                File f = new File(path + "/" + chosenFile);
                if (f.isDirectory()) {
                    isRoot = false;
                    //add directory to list
                    arrayStr.add(chosenFile);
                    fileList = null;
                    path = new File(f + "");
                    setView();
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Upload.this.dismiss();
            }
        });
        return v;
    }

    public void loadFiles() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e("Path", "Cannot write to the sd card !");
        }
        //validate the path if exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File f = new File(dir, filename);
                    return (f.isFile() || f.isDirectory()) && !f.isHidden();


                }
            };
            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            //loop through
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.file);
                //convert it to path
                File f = new File(path, fList[i]);
                if (f.isDirectory()) {
                    fileList[i].icon = R.drawable.directory;
                    Log.d("DIRECTORY", fileList[i].file);
                } else {
                    Log.d("FILE", fileList[i].file);
                }
            }
            adapter = new ArrayAdapter<Item>(context, R.layout.list2, fileList) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View rowView = inflater.inflate(R.layout.list3, parent, false);
                    TextView text = (TextView) rowView.findViewById(R.id.firstLine);
                    ImageView imageView = (ImageView) rowView.findViewById(R.id.iconn);
                    ImageView upload = (ImageView) rowView.findViewById(R.id.up);

                    if (fileList[position] != null) {
                        if (fileList[position].file.length() > 25) {
                            String ext = fileList[position].file.substring(fileList[position].file.length() - 5,fileList[position].file.length());
                            String filename = fileList[position].file.substring(0,12) + "..." + ext;
                            text.setText(filename);
                        }
                        else {
                            text.setText(fileList[position].file);
                        }
                    }

                    imageView.setImageResource(fileList[position].icon);
                    if(fileList[position].icon == R.drawable.directory_up ||fileList[position].icon == R.drawable.directory ) {
                        upload.setVisibility(View.GONE);
                    }
                    else {upload.setVisibility(View.VISIBLE);}
                    //set function for upload button
                    upload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File f = new File(path, fileList[position].file);
                            String uplaodFile = mPath + fileList[position].file;
                            new UploadAction(f , uplaodFile).execute();
                        }
                    });
                    return rowView;
                }
            };

        }

    }
    public class Item {
        public String file;
        public int icon;
        public Item(String file, Integer icon) {
            this.file =file;
            this.icon = icon;

        }
        public String toString() {
            return file;
        }
    }
    public void setView() {
        loadFiles();
        uploadList.setAdapter(adapter);

    }
    public class UploadAction extends AsyncTask<Void , Long , Boolean> {
        private String uploadPth;
        private boolean result;
        private File f;
        private FileInputStream fis;
        private final ProgressDialog dialog;
        public UploadAction(File f, String p) {
            uploadPth = p;
            this.f = f;
            //create progress dialog
            dialog = new ProgressDialog(context);
            //when progressDialog exceute => temporary dismiss upload view
            Upload.this.dismiss();
            dialog.show();
            dialog.setContentView(R.layout.processing);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                fis = new FileInputStream(this.f);
                DropboxAPI.Entry entry = mApi.putFile(uploadPth,fis,f.length(),null,null);
                result = true;
            }
            catch (Exception e) {
                e.printStackTrace();
                result = false;
            }
            return result;
        }
        //progress bar percentage
        public void onProgressUpdate(Long... progress) {
            int per = (int) (100.0 * (double)progress[0]/100 + 0.5);
            dialog.setProgress(per);
        }
        //handle Toast showing up
        public void onPostExecute(Boolean result) {
            dialog.dismiss();
            if(result) {
                showToast("Uploaded successfully !");
                folderList.setView();
            }
            else {
                showToast("Something occurs in the process.Try again !");
            }

        }
        public void showToast(String text) {
            Toast toast = Toast.makeText(context,text,Toast.LENGTH_LONG);
            toast.show();
        }

    }
}
