package com.droidapps.anniversarycollage.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.adapter.GalleryAlbumImageAdapter;

import java.util.ArrayList;

/**
 * Created by vanhu_000 on 3/26/2016.
 */
public class GalleryAlbumImageFragment extends BaseFragment {
    private TextView emptyView;

    public interface OnSelectImageListener {
        void onSelectImage(String image);
    }

    public static final String ALBUM_IMAGE_EXTRA = "albumImage";
    public static final String ALBUM_NAME_EXTRA = "albumName";

    private GridView mGridView;
    private ArrayList<String> mImages;
    private OnSelectImageListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof OnSelectImageListener) {
            mListener = (OnSelectImageListener) getActivity();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_gallery_photo, container, false);
        mGridView = (GridView) view.findViewById(R.id.gridView);
//        String albumName = getString(R.string.album_image);
        if (getArguments() != null) {
            updateImages(getArguments());
        }
         emptyView = view.findViewById(R.id.emptyText);

        return view;
    }

    public void updateImages(Bundle data){
        mImages = data.getStringArrayList(ALBUM_IMAGE_EXTRA);
        String albumName = data.getString(ALBUM_NAME_EXTRA);
        if(albumName!=null && !albumName.isEmpty())
            setTitle(albumName);
        if (mImages != null) {
            GalleryAlbumImageAdapter adapter = new GalleryAlbumImageAdapter(getActivity(), mImages);
            mGridView.setAdapter(adapter);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(mListener != null){
                        mListener.onSelectImage(mImages.get(position));
                    }
                }
            });
        }
        updateViews();
    }

    private void updateViews() {
        if(mImages==null || mImages.isEmpty()){
            mGridView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }else{
            mGridView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}
