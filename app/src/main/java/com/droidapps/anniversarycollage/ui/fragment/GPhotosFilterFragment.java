package com.droidapps.anniversarycollage.ui.fragment;

import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.clockbyte.admobadapter.expressads.AdmobExpressAdapterWrapper;
import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.adapter.GalleryAlbumAdapter;
import com.droidapps.anniversarycollage.config.ALog;
import com.droidapps.anniversarycollage.model.GalleryAlbum;
import com.droidapps.anniversarycollage.ui.BaseFragmentActivity;
import com.droidapps.anniversarycollage.utils.AdsHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import dauroi.photoeditor.receiver.NetworkStateReceiver;
import dauroi.photoeditor.utils.DateTimeUtils;

/**
 * Created by ThiyagaB on 1/26/2019.
 */
public class GPhotosFilterFragment extends BaseFragment  {


    @Override
    public void onPause() {
        // Save ListView mListViewState @ onPause

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();


    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery_album, container, false);


        setTitle(R.string.gphotos_filter);

        return view;
    }


}
