package com.example.filebrowser;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

class ImageListAdapter extends ArrayAdapter<ListAdapter> {
    private Context mContext;
    private int mResource;

    public ImageListAdapter(Context mContext, int resource, ArrayList<ListAdapter> images) {
        super(mContext, resource, images);
        this.mContext = mContext;
        mResource = resource;
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String fileName = getItem(position).getFileName();
        String image = getItem(position).getImage();

        ListAdapter imageObj = new ListAdapter(fileName, image);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView nameText = convertView.findViewById(R.id.textView);
        ImageView imageView = convertView.findViewById(R.id.imageView);

        nameText.setText(fileName);
        imageView.setImageBitmap(BitmapFactory.decodeFile(image));

        return convertView;
    }
}
