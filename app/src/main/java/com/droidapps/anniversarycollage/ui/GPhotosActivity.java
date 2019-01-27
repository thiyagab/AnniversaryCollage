package com.droidapps.anniversarycollage.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.gphotos.Util;
import com.droidapps.anniversarycollage.gphotos.model.MediaItem;
import com.droidapps.anniversarycollage.gphotos.model.SearchResponse;
import com.droidapps.anniversarycollage.ui.fragment.GPhotosFilterFragment;
import com.droidapps.anniversarycollage.ui.fragment.GalleryAlbumImageFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import dauroi.photoeditor.utils.FileUtils;
import dauroi.photoeditor.utils.PhotoUtils;

public class GPhotosActivity extends BaseFragmentActivity implements GalleryAlbumImageFragment.OnSelectImageListener, GPhotosFilterFragment.OnApplyFilterListener, Util.SearchCallback {

    private Button applybutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gphotos);
//       GalleryAlbumImageFragment galleryAlbumImageFragment= (GalleryAlbumImageFragment) getFragmentManager().findFragmentById(R.id.gphotosFragment);
//       galleryAlbumImageFragment.updateImages(getIntent().getExtras());
       setTitle("Google Photos");
       initView();
    }


    public void initView(){
        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentByTag("imagefragment");
        if (fragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            fragment =new GalleryAlbumImageFragment();
            ft.replace(R.id.contentView,fragment,"imagefragment");
            ft.commit();
        }

        initFilterView();

    }

    private void initFilterView() {
        final Spinner dayView = ((Spinner)findViewById(R.id.daytext));
        final Spinner monthView = ((Spinner)findViewById(R.id.monthtext));
        final Spinner yearView = ((Spinner)findViewById(R.id.yeartext));


        setTitle(R.string.gphotos_filter);
        final int currentYear= Calendar.getInstance().get(Calendar.YEAR);
        final String years[]= new String[30];
        years[0]="All Years";
        for (int i=0;i<30;i++){
            years[i]=String.valueOf(currentYear-i);
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, years );
        yearView.setAdapter(spinnerArrayAdapter);
        applybutton =(Button)findViewById(R.id.applyButton);
        applybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = dayView.getSelectedItemPosition();
                int month = monthView.getSelectedItemPosition();
                int year = yearView.getSelectedItemPosition()!=0? (currentYear-yearView.getSelectedItemPosition()-1):0;
                filterApplied(day,month,year,Util.CATEGORIES);
                applybutton.setText("Searching...");

            }
        });
    }

    @Override
    public void onSelectImage(final String imageUrl) {
        AsyncTask asyncTask = new AsyncTask<String,Object,String>() {


            @Override
            protected String doInBackground(String... objects) {
                String tempSavePath = FileUtils.TEMP_FOLDER.concat("/"+System.currentTimeMillis()+".jpg");
                try {
                    String fullSizeUrl =imageUrl.substring(0,imageUrl.lastIndexOf("="));
                    FileUtils.downloadFile(fullSizeUrl, tempSavePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return tempSavePath;
            }

            @Override
            protected void onPostExecute(String filePath) {


                if(filePath!=null){
                    Intent intent = new Intent();
                    intent.putExtra("TEMP_PHOTO_PATH",filePath);
                    setResult(0,intent);
                    finish();
                }
            }
        }.execute();

    }

    @Override
    public void filterApplied(int day, int month, int year, String[] categories) {
        Util.searchPhotos(day,month,year,this);

    }

    @Override
    public void onSearchResults(SearchResponse response) {
        applybutton.setText(R.string.search_gphotos);
        ArrayList<String> images=extractImageUrls(response);
        final Bundle data = new Bundle();
        data.putStringArrayList(GalleryAlbumImageFragment.ALBUM_IMAGE_EXTRA, images);
        data.putString(GalleryAlbumImageFragment.ALBUM_NAME_EXTRA, "Google photos");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = getFragmentManager();
                GalleryAlbumImageFragment imageFragment = (GalleryAlbumImageFragment)fm.findFragmentByTag("imagefragment");
                imageFragment.updateImages(data);
            }
        });



    }

    private ArrayList<String> extractImageUrls(SearchResponse response) {
        if(response!=null && response.mediaItems!=null){
            ArrayList<String> imageUrls = new ArrayList<>(response.mediaItems.size());
            for (MediaItem mediaItem: response.mediaItems){
                //TODO add =w256-h256 in url
                imageUrls.add(mediaItem.baseUrl+"=w256-h256");
            }
            return imageUrls;
        }
        return null;
    }
    @Override
    public void onError(String errror) {
        applybutton.setText(R.string.search_gphotos);
    }
}
