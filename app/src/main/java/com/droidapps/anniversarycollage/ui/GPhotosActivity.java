package com.droidapps.anniversarycollage.ui;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.gphotos.Util;
import com.droidapps.anniversarycollage.gphotos.model.MediaItem;
import com.droidapps.anniversarycollage.gphotos.model.SearchResponse;
import com.droidapps.anniversarycollage.ui.fragment.GPhotosFilterFragment;
import com.droidapps.anniversarycollage.ui.fragment.GalleryAlbumImageFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import dauroi.photoeditor.utils.FileUtils;
import dauroi.photoeditor.utils.PhotoUtils;

public class GPhotosActivity extends BaseFragmentActivity implements GalleryAlbumImageFragment.OnSelectImageListener, GPhotosFilterFragment.OnApplyFilterListener, Util.SearchCallback {

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
        Fragment fragment = fm.findFragmentByTag("myfragment");
        if (fragment == null) {
            FragmentTransaction ft = fm.beginTransaction();
            fragment =new GPhotosFilterFragment();
            ft.replace(android.R.id.content,fragment,"myFragmentTag");
            ft.commit();
        }

    }

    @Override
    public void onSelectImage(final String imageUrl) {
        AsyncTask asyncTask = new AsyncTask<String,Object,String>() {


            @Override
            protected String doInBackground(String... objects) {
                String tempSavePath = FileUtils.TEMP_FOLDER.concat("/"+System.currentTimeMillis()+".jpg");
                try {
                    FileUtils.downloadFile(imageUrl, tempSavePath);
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
        ArrayList<String> images=extractImageUrls(response);
        Bundle data = new Bundle();
        data.putStringArrayList(GalleryAlbumImageFragment.ALBUM_IMAGE_EXTRA, images);
        data.putString(GalleryAlbumImageFragment.ALBUM_NAME_EXTRA, "Google photos");
        GalleryAlbumImageFragment imageFragment = new GalleryAlbumImageFragment();
        imageFragment.setArguments(data);
        getFragmentManager().beginTransaction().replace(android.R.id.content,imageFragment,"albumFragment").commit();
        Intent intent = new Intent(this,GPhotosActivity.class);
//        intent.putExtras(data);
//        startActivity(intent);


    }

    private ArrayList<String> extractImageUrls(SearchResponse response) {
        if(response!=null && response.mediaItems!=null){
            ArrayList<String> imageUrls = new ArrayList<>(response.mediaItems.size());
            for (MediaItem mediaItem: response.mediaItems){
                //TODO add =w256-h256 in url
                imageUrls.add(mediaItem.baseUrl);
            }
            return imageUrls;
        }
        return null;
    }
    @Override
    public void onError(String errror) {

    }
}
