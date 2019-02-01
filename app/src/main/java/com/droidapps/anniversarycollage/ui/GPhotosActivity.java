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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        initBottomPhotoSelectionView();

    }

    private void initBottomPhotoSelectionView() {
        //TODO 4
        /*
        Copy the bottom photo selection list from 'SelectPhotoActivity', so that this view would be three sections
        1. Top date,month selection and search button
        2. Middle list of cards to show the years and number of photos in each year
        3. Bottom selection photo list, which will be persistent, when user click on a card and it ll show list of all images, in this new screen also,
        this bottom list will be shown to show the list of selected pics
        4. User can do multiple selections in single year or come back and select different year and pick multiple photos across years
        5. All the selected pics will be shown in this bottom list, and at the end of the list there will be a button called create
        6. On Click on create, the selected pics will be taken to next CollageActivity via collageButtonClicked
         */
    }

    private void collageButtonClicked(String[] selectedPicUris){
        //TODO 5
        /*
        1. Open Frame Detail activity with the selectedPicUris
        2. Check the BaseTemplateDetailActivity where we can pass the imageuris
        3. Based on the number of pics the array length of selectedPicUris, the mImageInTemplateCount in 'BaseTemplateDetailActivity'
            can be set
        4. The above option will show a fixed defined set of frames, we also need to see how can we do freeform collage
        5. The freeform collage can be made using "PhotoCollageActivity
        6. So on click of create itself, it will ask an option, either to do freeform collage (then direct to step 5) or based on template ( direct to step 3)

         */
    }

    private void initFilterView() {
        final Spinner dayView = ((Spinner)findViewById(R.id.daytext));
        final Spinner monthView = ((Spinner)findViewById(R.id.monthtext));


        setTitle(R.string.gphotos_filter);
        final int currentYear= Calendar.getInstance().get(Calendar.YEAR);
        final String years[]= new String[30];
        years[0]="All Years";
        for (int i=0;i<30;i++){
            years[i]=String.valueOf(currentYear-i);
        }

        applybutton =(Button)findViewById(R.id.applyButton);
        applybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = dayView.getSelectedItemPosition();
                int month = monthView.getSelectedItemPosition();
//                int year = yearView.getSelectedItemPosition()!=0? (currentYear-yearView.getSelectedItemPosition()-1):0;
//                filterApplied(day,month,0,Util.CATEGORIES);
                applybutton.setText("Searching...");
                startFetchingForAllYears(day,month);

            }
        });

        initListView();
    }

    private void initListView() {
        //TODO 1 The listview will be a list of cards
        // Each card will show the year and number of photos in that year
        // Create proper view and adapter and set it here
    }


    Map<Integer,SearchResponse> yearWiseSearchResults = new HashMap<>();

    private void startFetchingForAllYears(int day, int month) {
        //TODO 2
        /*Call this method,
        filterApplied(day,month,2019,Util.CATEGORIES);
        for every year in a for loop, starting form this year to 10 years before e.g. 2019-2009
        We can try calling thse requests in a thread, and as the requests in progress, show a progress in each card
        on successfull respone update the map 'yearwisesearchresults', then
        in the list view as initiated in the above method 'initListView', the list would aleady be initiated with the map data, so it should be updated
        as and when we receive responses
        From each response, set the number alone in each card, on click show all the pics in a new view ( new view design discussed later)
        */
    }

    private void onYearListItemSelected(int year){
        SearchResponse searchResponse=yearWiseSearchResults.get(year);
        List<MediaItem> photos=searchResponse.mediaItems;


        //TODO 3
        /*
        Pass the list of photos to select photo  , "GalleryAlbumImageFrament" will do the job
         */
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
