package com.droidapps.anniversarycollage.ui.fragment;

import android.app.Activity;
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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;
import android.widget.ListView;

import com.clockbyte.admobadapter.expressads.AdmobExpressAdapterWrapper;
import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.adapter.GalleryAlbumAdapter;
import com.droidapps.anniversarycollage.config.ALog;
import com.droidapps.anniversarycollage.gphotos.Util;
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

    OnApplyFilterListener onApplyFilterListener;

    @Override
    public void onPause() {
        // Save ListView mListViewState @ onPause

        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.onApplyFilterListener = (OnApplyFilterListener)activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_gphotos_filter, container, false);
        GridView gridView=view.findViewById(R.id.checkboxgroup);

        String[] categories = Util.CATEGORIES;
//        gridView.setItemChecked(0,true);

        ArrayAdapter adapter = new ArrayAdapter(getContext(),
                android.R.layout.simple_list_item_multiple_choice, categories);

        gridView.setAdapter(adapter);
        final AutoCompleteTextView dayView = ((AutoCompleteTextView)view.findViewById(R.id.daytext));
        final AutoCompleteTextView monthView = ((AutoCompleteTextView)view.findViewById(R.id.monthtext));
        final AutoCompleteTextView yearView = ((AutoCompleteTextView)view.findViewById(R.id.yeartext));

        for (int i = 0; i < categories.length; i++) {

            gridView.setSelection(i);
            gridView.setItemChecked(i,true);
        }


        setTitle(R.string.gphotos_filter);
        view.findViewById(R.id.applyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = dayView.getText().toString().length()>0?Integer.parseInt(dayView.getText().toString()):0;
                int month = monthView.getText().toString().length()>0?Integer.parseInt(monthView.getText().toString()):0;
                int year = yearView.getText().toString().length()>0?Integer.parseInt(yearView.getText().toString()):0;
               onApplyFilterListener.filterApplied(day,month,year,Util.CATEGORIES);

            }
        });

        return view;
    }




    public interface OnApplyFilterListener {
        void filterApplied(int day,int month,int year, String[] categories);
    }
}
