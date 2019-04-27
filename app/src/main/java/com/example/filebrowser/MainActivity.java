package com.example.filebrowser;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends ListActivity {

    private static final int PICKFILE_RESULT_CODE = 10;
    private String path;
    private TextRecognizer textRecognizer;
    private EditText editText;
    private ImageListAdapter adapter;
    private ArrayList<ListAdapter> items;
    private ListView listView;
    private File dcim;
    private File whatsApp;
    private String searchText;
    private ProgressBar progressBar;
    private SearchTask st;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = findViewById(R.id.textBox);
        textRecognizer = new TextRecognizer.Builder(this).build();
        items = new ArrayList<>();
        listView = getListView();
        listView.setVisibility(View.INVISIBLE);
        adapter = new ImageListAdapter(this, R.layout.custom_list_view, items);
        listView.setAdapter(adapter);

        st = new SearchTask();

        /*StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file = new File(items.get(position).getImage());
                //Uri path = Uri.parse("file://" + items.get(position).getImage()) ;
                //Uri path = Uri.fromFile(file);
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider", file);
                Log.d("IMAGE", "IMAGE INTENT: " + photoURI);
                Intent openImage = new Intent();
                openImage.setAction(Intent.ACTION_QUICK_VIEW);
                openImage.setDataAndType(photoURI, "image/*");
                openImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(openImage);
            }
        });

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        boolean grantedAll = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        if (!grantedAll)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }


    }



    public void browse2(View view) {
        //String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        //File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if(editText.getText().length() < 2) {
            Toast.makeText(this, "Enter more letters", Toast.LENGTH_LONG).show();
            return;
        }
        items.clear();
        adapter.notifyDataSetChanged();
        progressBar.setVisibility(View.VISIBLE);
        dcim = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath());

        int ind = dcim.getAbsolutePath().lastIndexOf("DCIM");
        path = dcim.getAbsolutePath().substring(0, ind);
        whatsApp = new File(path + "WhatsApp/Media/");
        searchText = editText.getText().toString().toLowerCase().trim();

        if (st.getStatus() == AsyncTask.Status.RUNNING) {
            Log.d("IMAGE", "SEARCH ALREADY RUNNING");
            return;
        } else {
            Log.d("IMAGE", "SEARCH STARTED EXECUTING");
            st = new SearchTask();
            st.execute(searchText);
        }
    }


    void traverse2(File file, String searchText){
        if(file.isDirectory()){
            //Log.d("IMAGE", "IS DIR TRAVERSE");
            File files[] = file.listFiles();
            for(int i = 0; i < files.length; i++){
                traverse2(files[i], searchText);
            }
        }
        else {
            //Log.d("IMAGE", "IS FILE TRAVERSE");
            if (isImageFile(file.getAbsolutePath()) && search(file, searchText)) {
                items.add(new ListAdapter(file.getName(), file.getAbsolutePath()));
                //adapter.notifyDataSetChanged();
                Log.d("IMAGE", "SEARCH FOUND");
            }
        }
    }
    public static boolean isImageFile(String path) {
        String mimeType = URLConnection.guessContentTypeFromName(path);
        return mimeType != null && mimeType.startsWith("image");
    }

    boolean search(File file, String text) {

        if(!textRecognizer.isOperational()) {
            new AlertDialog.Builder(this)
                    .setMessage("Text recognizer could not be set up on your device :(")
                    .show();
            return false;
        }
        Bitmap bitmap;
        Frame frame;
        Log.d("IMAGE", "Image path being scanned: " + file.getPath());
        try {
            bitmap = BitmapFactory.decodeFile(file.getPath()); // image to bitmap
            frame = new Frame.Builder().setBitmap(bitmap).build();
        }catch (Exception e){
            return false;
        }

        SparseArray<TextBlock> textBlocks = textRecognizer.detect(frame);
        for (int i = 0; i < textBlocks.size(); ++i) {
            TextBlock item = textBlocks.valueAt(i);

            if (item != null && item.getValue() != null) {
                // check string
                if(item.getValue().toLowerCase().contains(text)){
                    return true;
                }
            }
        }
        return false;
    }

    public class SearchTask extends AsyncTask<String, String, String>{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressBar.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }

        @Override
        protected String doInBackground(String... strings) {
            traverse2(whatsApp, strings[0]);
            traverse2(dcim, strings[0]);
            return null;
        }
    }
}



