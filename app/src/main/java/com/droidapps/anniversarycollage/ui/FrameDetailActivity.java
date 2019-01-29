package com.droidapps.anniversarycollage.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.config.Constant;
import com.droidapps.anniversarycollage.frame.FrameImageView;
import com.droidapps.anniversarycollage.frame.FramePhotoLayout;
import com.droidapps.anniversarycollage.gphotos.Util;
import com.droidapps.anniversarycollage.gphotos.model.MediaItem;
import com.droidapps.anniversarycollage.gphotos.model.SearchResponse;
import com.droidapps.anniversarycollage.model.GalleryAlbum;
import com.droidapps.anniversarycollage.model.TemplateItem;
import com.droidapps.anniversarycollage.ui.fragment.GalleryAlbumImageFragment;
import com.droidapps.anniversarycollage.utils.ImageUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

import dauroi.photoeditor.PhotoEditorApp;
import dauroi.photoeditor.colorpicker.ColorPickerDialog;
import dauroi.photoeditor.utils.FileUtils;
import dauroi.photoeditor.utils.ImageDecoder;

/**
 * Created by admin on 4/28/2016.
 */
public class FrameDetailActivity extends BaseTemplateDetailActivity implements FramePhotoLayout.OnQuickActionClickListener,
        ColorPickerDialog.OnColorChangedListener ,Util.SignInCallback{
    private static final int REQUEST_SELECT_PHOTO = 99;
    private static final float MAX_SPACE = ImageUtils.pxFromDp(PhotoEditorApp.getAppContext(), 30);
    private static final float MAX_CORNER = ImageUtils.pxFromDp(PhotoEditorApp.getAppContext(), 60);
    private static final float DEFAULT_SPACE = ImageUtils.pxFromDp(PhotoEditorApp.getAppContext(), 2);
    private static final float MAX_SPACE_PROGRESS = 300.0f;
    private static final float MAX_CORNER_PROGRESS = 200.0f;

    private FrameImageView mSelectedFrameImageView;
    private FramePhotoLayout mFramePhotoLayout;
    private ViewGroup mSpaceLayout;
    private SeekBar mSpaceBar;
    private SeekBar mCornerBar;
    private float mSpace = DEFAULT_SPACE;
    private float mCorner = 0;
    //Background
    private int mBackgroundColor = Color.WHITE;
    private Bitmap mBackgroundImage;
    private Uri mBackgroundUri = null;
    private ColorPickerDialog mColorPickerDialog;
    //Saved instance state
    private Bundle mSavedInstanceState;
    private TextView mTipText;

    @Override
    protected boolean isShowingAllTemplates() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //restore old params
        if (savedInstanceState != null) {
            mSpace = savedInstanceState.getFloat("mSpace");
            mCorner = savedInstanceState.getFloat("mCorner");
            mBackgroundColor = savedInstanceState.getInt("mBackgroundColor");
            mBackgroundUri = savedInstanceState.getParcelable("mBackgroundUri");
            mSavedInstanceState = savedInstanceState;
            if (mBackgroundUri != null)
                mBackgroundImage = ImageDecoder.decodeUriToBitmap(this, mBackgroundUri);
        }
        //add ads view
//        addAdsView(R.id.adsLayout);
        //inflate widgets
//        mAddImageDialog.findViewById(R.id.dividerTextView).setVisibility(View.VISIBLE);
//        mAddImageDialog.findViewById(R.id.alterBackgroundView).setVisibility(View.VISIBLE);
//        mAddImageDialog.findViewById(R.id.dividerBackgroundPhotoView).setVisibility(View.VISIBLE);
//        mAddImageDialog.findViewById(R.id.alterBackgroundColorView).setVisibility(View.VISIBLE);
        mSpaceLayout = (ViewGroup) findViewById(R.id.spaceLayout);
        mSpaceBar = (SeekBar) findViewById(R.id.spaceBar);
        mSpaceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSpace = MAX_SPACE * seekBar.getProgress() / MAX_SPACE_PROGRESS;
                if (mFramePhotoLayout != null)
                    mFramePhotoLayout.setSpace(mSpace, mCorner);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mCornerBar = (SeekBar) findViewById(R.id.cornerBar);
        mCornerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCorner = MAX_CORNER * seekBar.getProgress() / MAX_CORNER_PROGRESS;
                if (mFramePhotoLayout != null)
                    mFramePhotoLayout.setSpace(mSpace, mCorner);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mCornerBar.setVisibility(View.GONE);
        // show guide on first time
        boolean show = mPreferences.getBoolean(Constant.SHOW_GUIDE_CREATE_FRAME_KEY, true);
        if (show) {
            clickInfoView();
            mPreferences.edit().putBoolean(Constant.SHOW_GUIDE_CREATE_FRAME_KEY, false)
                    .commit();
        }
        requestPermissions(REQUEST);
        mTipText=findViewById(R.id.tipText);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("mSpace", mSpace);
        outState.putFloat("mCornerBar", mCorner);
        outState.putInt("mBackgroundColor", mBackgroundColor);
        outState.putParcelable("mBackgroundUri", mBackgroundUri);
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout.saveInstanceState(outState);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        return result;
    }

    @Override
    public void onBackgroundColorButtonClick() {
        if (mColorPickerDialog == null) {
            mColorPickerDialog = new ColorPickerDialog(this, mBackgroundColor);
            mColorPickerDialog.setOnColorChangedListener(this);
        }

        mColorPickerDialog.setOldColor(mBackgroundColor);
        if (!mColorPickerDialog.isShowing()) {
            mColorPickerDialog.show();
        }
    }

    public void  onGooglePhotosClick(){
        if(Util.isLoggedIn(FrameDetailActivity.this)){
            onSignInComplete();
        }else{
            Util.initializeGooglePhotos(this);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_frame_detail;
    }

    @Override
    public Bitmap createOutputImage() throws OutOfMemoryError {
        try {
            Bitmap template = mFramePhotoLayout.createImage();
            Bitmap result = Bitmap.createBitmap(template.getWidth(), template.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
                canvas.drawBitmap(mBackgroundImage, new Rect(0, 0, mBackgroundImage.getWidth(), mBackgroundImage.getHeight()),
                        new Rect(0, 0, result.getWidth(), result.getHeight()), paint);
            } else {
                canvas.drawColor(mBackgroundColor);
            }

            canvas.drawBitmap(template, 0, 0, paint);
            template.recycle();
            template = null;
            Bitmap stickers = mPhotoView.getImage(mOutputScale);
            canvas.drawBitmap(stickers, 0, 0, paint);
            stickers.recycle();
            stickers = null;
            System.gc();
            return result;
        } catch (OutOfMemoryError error) {
            throw error;
        }
    }

    @Override
    protected void buildLayout(TemplateItem item) {
        mFramePhotoLayout = new FramePhotoLayout(this, item.getPhotoItemList());
        mFramePhotoLayout.setQuickActionClickListener(this);
        if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
            if (Build.VERSION.SDK_INT >= 16)
                mContainerLayout.setBackground(new BitmapDrawable(getResources(), mBackgroundImage));
            else
                mContainerLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), mBackgroundImage));
        } else {
            mContainerLayout.setBackgroundColor(mBackgroundColor);
        }

        int viewWidth = mContainerLayout.getWidth();
        int viewHeight = mContainerLayout.getHeight();
        if (mLayoutRatio == RATIO_SQUARE) {
            if (viewWidth > viewHeight) {
                viewWidth = viewHeight;
            } else {
                viewHeight = viewWidth;
            }
        } else if (mLayoutRatio == RATIO_GOLDEN) {
            final double goldenRatio = 1.61803398875;
            if (viewWidth <= viewHeight) {
                if (viewWidth * goldenRatio >= viewHeight) {
                    viewWidth = (int) (viewHeight / goldenRatio);
                } else {
                    viewHeight = (int) (viewWidth * goldenRatio);
                }
            } else if (viewHeight <= viewWidth) {
                if (viewHeight * goldenRatio >= viewWidth) {
                    viewHeight = (int) (viewWidth / goldenRatio);
                } else {
                    viewWidth = (int) (viewHeight * goldenRatio);
                }
            }
        }
        mOutputScale = ImageUtils.calculateOutputScaleFactor(viewWidth, viewHeight);
        mFramePhotoLayout.build(viewWidth, viewHeight, mOutputScale, mSpace, mCorner);
        if (mSavedInstanceState != null) {
            mFramePhotoLayout.restoreInstanceState(mSavedInstanceState);
            mSavedInstanceState = null;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(viewWidth, viewHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mContainerLayout.removeAllViews();
        mContainerLayout.addView(mFramePhotoLayout, params);
        //add sticker view
        mContainerLayout.removeView(mPhotoView);
        mContainerLayout.addView(mPhotoView, params);
        //reset space and corner seek bars
        mSpaceBar.setProgress((int) (MAX_SPACE_PROGRESS * mSpace / MAX_SPACE));
        mCornerBar.setProgress((int) (MAX_CORNER_PROGRESS * mCorner / MAX_CORNER));
    }

    @Override
    public void onEditActionClick(FrameImageView v) {
        mSelectedFrameImageView = v;
        if (v.getImage() != null && v.getPhotoItem().imagePath != null && v.getPhotoItem().imagePath.length() > 0) {
            Uri uri = Uri.fromFile(new File(v.getPhotoItem().imagePath));
            requestEditingImage(uri);
        }
    }
    private static final int REQUEST = 112;
    private static final int REQUEST_PHOTO = 113;
    public void requestPermissions(int requestCode){
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(this, PERMISSIONS)) {
                requestPermissions(PERMISSIONS, requestCode );
            } else {
                if(requestCode==REQUEST_PHOTO){
                    requestPhoto();
                }
            }
        } else {
            if(requestCode==REQUEST_PHOTO){
                requestPhoto();
            }
        }

    }

    private  boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //do here
                } else {
                    Toast.makeText(this, "The app cannot create collage without storage permissions", Toast.LENGTH_LONG).show();
                }
            }
            break;
            case REQUEST_PHOTO:{
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPhoto();
                } else {
                    Toast.makeText(this, "The app cannot create collage without permissions", Toast.LENGTH_LONG).show();
                }
            }
            break;

        }
    }

    @Override
    public void onChangeActionClick(FrameImageView v) {
        mSelectedFrameImageView = v;
        requestPermissions(REQUEST_PHOTO);
//        requestPhoto();
//        Intent data = new Intent(this, SelectPhotoActivity.class);
//        data.putExtra(SelectPhotoActivity.EXTRA_IMAGE_COUNT, 1);
//        startActivityForResult(data, REQUEST_SELECT_PHOTO);
    }

    public void onTextActionClick(FrameImageView v){
        mTipText.setText("Tip: Double tap on Text for more options or use two fingers to zoom and rotate");
        onTextButtonClick();
    }

    @Override
    protected void resultEditImage(Uri uri) {
        if (mSelectedFrameImageView != null) {
            mSelectedFrameImageView.setImagePath(FileUtils.getPath(this, uri));
        }
    }

    @Override
    protected void resultFromPhotoEditor(Uri image) {
        mTipText.setText("Tip: Use two fingers to zoom/crop and rotate");
        if (mSelectedFrameImageView != null) {
            mSelectedFrameImageView.setImagePath(FileUtils.getPath(this, image));
        }
    }

    private void recycleBackgroundImage() {
        if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
            mBackgroundImage.recycle();
            mBackgroundImage = null;
            System.gc();
        }
    }

    @Override
    protected void resultBackground(Uri uri) {
        recycleBackgroundImage();
        mBackgroundUri = uri;
        mBackgroundImage = ImageDecoder.decodeUriToBitmap(this, uri);
        if (Build.VERSION.SDK_INT >= 16)
            mContainerLayout.setBackground(new BitmapDrawable(getResources(), mBackgroundImage));
        else
            mContainerLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), mBackgroundImage));
    }

    @Override
    public void onColorChanged(int color) {
        recycleBackgroundImage();
        mBackgroundColor = color;
        mContainerLayout.setBackgroundColor(mBackgroundColor);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {
            ArrayList<String> mSelectedImages = data.getStringArrayListExtra(SelectPhotoActivity.EXTRA_SELECTED_IMAGES);
            if (mSelectedFrameImageView != null && mSelectedImages != null && !mSelectedImages.isEmpty()) {
                mSelectedFrameImageView.setImagePath(mSelectedImages.get(0));
            }
        }else if(requestCode == Util.RC_SIGN_IN){
            Util.handleActivityResult(requestCode,FrameDetailActivity.this,data);
        }else if(data!=null && requestCode==GPHOTOS_REQUEST_CODE){
            String photoPath=data.getStringExtra("TEMP_PHOTO_PATH");
            photoPath = "file://"+photoPath;
            if(photoPath!=null){
                resultFromPhotoEditor(Uri.parse(photoPath));
            }

        }

        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void finish() {
        recycleBackgroundImage();
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout.recycleImages();
        }
        super.finish();
    }


   static final  int GPHOTOS_REQUEST_CODE=1111;

    @Override
    public void onSignInComplete() {
        Intent intent = new Intent(this,GPhotosActivity.class);
        startActivityForResult(intent,GPHOTOS_REQUEST_CODE);
    }

    @Override
    public void onError(String errror) {

    }
}
