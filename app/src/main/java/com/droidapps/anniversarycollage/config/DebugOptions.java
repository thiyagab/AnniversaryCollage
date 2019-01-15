package com.droidapps.anniversarycollage.config;

import com.droidapps.anniversarycollage.BuildConfig;
import com.droidapps.anniversarycollage.ui.PhotoCollageApp;

/**
 * Config all debug options in application. In release version, all value must
 * set to FALSE
 */
public class DebugOptions {
    public static final boolean ENABLE_LOG = BuildConfig.DEBUG;

    public static final boolean ENABLE_DEBUG = BuildConfig.DEBUG;

    public static final boolean ENABLE_FOR_DEV = false;

    public static boolean isProVersion() {
        return dauroi.photoeditor.config.DebugOptions.isProVersion(PhotoCollageApp.getAppContext());
    }
}
