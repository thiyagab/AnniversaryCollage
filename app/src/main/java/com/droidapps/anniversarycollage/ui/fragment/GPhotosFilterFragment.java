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
import android.widget.Spinner;

import com.clockbyte.admobadapter.expressads.AdmobExpressAdapterWrapper;
import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.adapter.GalleryAlbumAdapter;
import com.droidapps.anniversarycollage.config.ALog;
import com.droidapps.anniversarycollage.gphotos.Util;
import com.droidapps.anniversarycollage.model.GalleryAlbum;
import com.droidapps.anniversarycollage.ui.BaseFragmentActivity;
import com.droidapps.anniversarycollage.utils.AdsHelper;

import java.util.ArrayList;
import java.util.Calendar;
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
        final Spinner dayView = ((Spinner)view.findViewById(R.id.daytext));
        final Spinner monthView = ((Spinner)view.findViewById(R.id.monthtext));
        final Spinner yearView = ((Spinner)view.findViewById(R.id.yeartext));


        setTitle(R.string.gphotos_filter);
       final int currentYear=Calendar.getInstance().get(Calendar.YEAR);
        final String years[]= new String[30];
        years[0]="All Years";
        for (int i=0;i<30;i++){
            years[i]=String.valueOf(currentYear-i);
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, years );
        yearView.setAdapter(spinnerArrayAdapter);
        view.findViewById(R.id.applyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int day = dayView.getSelectedItemPosition();
                int month = monthView.getSelectedItemPosition();
                int year = yearView.getSelectedItemPosition()!=0? (currentYear-yearView.getSelectedItemPosition()-1):0;
               onApplyFilterListener.filterApplied(day,month,year,Util.CATEGORIES);

            }
        });

        return view;
    }




    public interface OnApplyFilterListener {
        void filterApplied(int day,int month,int year, String[] categories);
    }
}
