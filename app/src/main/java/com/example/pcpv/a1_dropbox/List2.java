package com.example.pcpv.a1_dropbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;

/**
 * Created by PCPV on 23/11/2015.
 */
public class List2 extends ArrayAdapter {
    private Context context;
    private DropboxAPI<?> mApi;
    private DropboxAPI.Entry[] e;

    public List2(Context context, DropboxAPI<?> mApi, DropboxAPI.Entry[] e) {
        super(context, R.layout.create_folder, e);
        this.context = context;
        this.mApi = mApi;
        this.e = e;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list2, parent, false);
        TextView text = (TextView) rowView.findViewById(R.id.firstLine);
        ImageView image = (ImageView) rowView.findViewById(R.id.iconn);
        if (e[position] != null) {
            if(e[position].fileName().length() > 26) {
                text.setText(e[position].fileName().substring(0,13) + "..." + e[position].fileName().substring(e[position].fileName().length()-6,e[position].fileName().length()));
            }
            else {
                text.setText(e[position].fileName());

            }
        }
        else{
            text.setText("Back");
        }
        if(e[position] == null) {
            image.setImageResource(R.drawable.directory_up);

        }
        else
        {
            image.setImageResource(R.drawable.directory);
        }
        return rowView;
    }


}
