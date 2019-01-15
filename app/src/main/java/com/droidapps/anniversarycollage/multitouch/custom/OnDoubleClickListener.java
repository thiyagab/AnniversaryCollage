package com.droidapps.anniversarycollage.multitouch.custom;

import com.droidapps.anniversarycollage.multitouch.controller.MultiTouchEntity;

public interface OnDoubleClickListener {
	public void onPhotoViewDoubleClick(PhotoView view, MultiTouchEntity entity);
	public void onBackgroundDoubleClick();
}
