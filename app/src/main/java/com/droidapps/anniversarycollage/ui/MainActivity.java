package com.droidapps.anniversarycollage.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.config.ALog;
import com.droidapps.anniversarycollage.config.DebugOptions;
import com.droidapps.anniversarycollage.receiver.PackageInstallReceiver;
import com.droidapps.anniversarycollage.ui.fragment.BaseFragment;
import com.droidapps.anniversarycollage.ui.fragment.CreatedCollageFragment;
import com.droidapps.anniversarycollage.ui.fragment.MainPhotoFragment;
import com.droidapps.anniversarycollage.ui.fragment.StoreFragment;
import com.droidapps.anniversarycollage.utils.BigDAdsHelper;
import com.droidapps.anniversarycollage.utils.DialogUtils;
import com.droidapps.anniversarycollage.utils.ResultContainer;
import com.google.android.gms.ads.internal.gmsg.HttpClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;



import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import dauroi.photoeditor.api.response.StoreItem;
import dauroi.photoeditor.database.DatabaseManager;
import dauroi.photoeditor.utils.StoreUtils;

public class MainActivity extends AdsFragmentActivity {
    public static final String RATE_APP_PREF_NAME = "rateAppPref";
    public static final String RATED_APP_KEY = "ratedApp";
    public static final String OPEN_APP_COUNT_KEY = "openAppCount";

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerlayout;
    private String mTitle;
    private ViewGroup mAdLayout;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!DatabaseManager.getInstance(this).isDbFileExisted()) {
            DatabaseManager.getInstance(this).createDb();
        } else {
            boolean isOpen = DatabaseManager.getInstance(this).openDb();
            ALog.d("MainActivity", "onCreate, database isOpen=" + isOpen);
        }

        if (savedInstanceState == null) {
            PackageInstallReceiver.clickedApp = null;
            PackageInstallReceiver.reportedMap.clear();
            BigDAdsHelper.clearInstalledApp();
        }


        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.home);
        }

        mTitle = getString(R.string.home);
        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getString("mTitle");
        }

        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, // host activity
                mDrawerlayout, // drawerlayout object
                toolbar, // toolbar
                R.string.navigation_drawer_open, // open drawer description required!
                R.string.navigation_drawer_close) { // closed drawer description

            // called once the drawer has closed.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to
            }

            // called when the drawer is now open.
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.syncState();
        // To disable the icon for the drawer, change this to false
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerlayout.setDrawerListener(mDrawerToggle);

        mAdLayout = (ViewGroup) findViewById(R.id.adsLayout);

        findViewById(R.id.homeView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerlayout.closeDrawer(GravityCompat.START);
                mTitle = getString(R.string.home);
                getSupportActionBar().setTitle(mTitle);
                if (getAdsHelper() != null)
                    getAdsHelper().addAdsBannerView(mAdLayout);
                onHomeItemMenuClickListener();
            }
        });

        findViewById(R.id.rateAppView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerlayout.closeDrawer(GravityCompat.START);
                onRateAppButtonClick();
            }
        });

        findViewById(R.id.storeView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerlayout.closeDrawer(GravityCompat.START);
                mAdLayout.removeAllViews();
                mTitle = getString(R.string.store);
                getSupportActionBar().setTitle(mTitle);
                onStoreItemMenuClickListener();
            }
        });

        findViewById(R.id.albumView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerlayout.closeDrawer(GravityCompat.START);
                mTitle = getString(R.string.album);
                getSupportActionBar().setTitle(mTitle);
                if (getAdsHelper() != null)
                    getAdsHelper().addAdsBannerView(mAdLayout);
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, new CreatedCollageFragment(), "CreatedCollageFragment")
                        .commit();
            }
        });

        findViewById(R.id.photoEditorView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerlayout.closeDrawer(GravityCompat.START);
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("market://details?id=dauroi.photoeditor"));
                    startActivity(i);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(MainActivity.this, getString(R.string.app_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        final View removeAdsView = findViewById(R.id.removeAdsView);
        removeAdsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerlayout.closeDrawer(GravityCompat.START);
                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("market://details?id=com.codetho.photocollagepro"));
                    startActivity(i);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Toast.makeText(MainActivity.this, getString(R.string.app_not_found), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (DebugOptions.isProVersion()) {
            removeAdsView.setVisibility(View.GONE);
        }

        if (getAdsHelper() != null)
            getAdsHelper().addAdsBannerView(mAdLayout);
        //set view
        if (savedInstanceState == null) {
            ResultContainer.getInstance().clearAll();
            getFragmentManager().beginTransaction()
                    .replace(R.id.frame_container, new MainPhotoFragment(), "MainPhotoFragment")
                    .commit();
        }
        //Redownload all unsuccessful items
        try {
            StoreUtils.redownloadItems();
        } catch (Exception ex) {
            ex.printStackTrace();
//            FirebaseCrash.report(ex);
        }
        //Handle pushed notification
        if (getIntent().getExtras() != null) {
            String itemType = getIntent().getExtras().getString("type");
            if (itemType != null && itemType.length() > 0) {
                itemType = itemType.trim();
                if (itemType.equalsIgnoreCase("update")) {
                    try {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse("market://details?id=" + getPackageName()));
                        startActivity(i);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Toast.makeText(MainActivity.this, getString(R.string.app_not_found), Toast.LENGTH_SHORT).show();
                    }
                } else if (itemType.equalsIgnoreCase("ad")) {
                    ALog.d("MainActivity", "show ad");
                } else {
                    try {
                        StoreFragment fragment = new StoreFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(StoreItem.EXTRA_ITEM_TYPE_KEY, itemType);
                        fragment.setArguments(bundle);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.frame_container, fragment, "StoreFragment")
                                .commit();
                        mLoadedData = true;
                        if (getAdsHelper() != null)
                            getAdsHelper().showInterstitialAds();
                    } catch (Exception ex) {
                        ex.printStackTrace();
//                        FirebaseCrash.report(ex);
                    }
                }
            }
        }
        initializeGooglePhotos();
        // [END handle_data_extras]
    }


    static final int RC_SIGN_IN=3333;
    public void initializeGooglePhotos(){


        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope("https://www.googleapis.com/auth/photoslibrary.readonly"))
//                    .requestIdToken("413558917554-2a13qf70rqkcm2e131f6ca4jivan0ded.apps.googleusercontent.com")
                    .requestServerAuthCode("413558917554-2a13qf70rqkcm2e131f6ca4jivan0ded.apps.googleusercontent.com")
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

            startActivityForResult( mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);


//            GoogleCredential credential=GoogleCredential.fromStream(getAssets().open("anniversarycollage.json"));
//
//            credential.refreshToken();
//            System.out.println("Credentials: "+credential.getAccessToken());
//            PhotosLibrarySettings settings =
//                    PhotosLibrarySettings.newBuilder()
//                            .setCredentialsProvider(FixedCredentialsProvider.create(GoogleCredentials.fromStream(getAssets().open("anniversarycollage.json"))))
//                            .build();
//            PhotosLibraryClient.initialize(settings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            System.out.println("Server auth code: "+account.getServerAuthCode());
            System.out.println("Server id token: "+account.getIdToken());
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    String accessToken=requestAccessToken(account);
                    System.out.println("AccessToken: "+accessToken);
                    Date expiryTime=new Date(System.currentTimeMillis()+3600*1000);
                    final URL url;
                    try {
                        url = new URL(
                                "https://photoslibrary.googleapis.com/v1/albums");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setUseCaches(false);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(3000);
                    conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Authorization", "Bearer "+accessToken);


                        conn.connect();

                        if (conn.getResponseCode() != 200) {
                            throw new RuntimeException("Failed : HTTP error code : "
                                    + conn.getResponseCode());
                        }

                        String assembledOutput = "";

                        BufferedReader responseBuffer = new BufferedReader(new InputStreamReader(
                                (conn.getInputStream())));

                        String output;
                        System.out.println("Output from Server:\n");
                        while ((output = responseBuffer.readLine()) != null) {
                            System.out.println(output);
                            assembledOutput = assembledOutput + output;
                        }

                        conn.disconnect();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();

            // Signed in successfully, show authenticated UI.
//            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
//            updateUI(null);
            e.printStackTrace();
        }
    }


    private String mAccessToken;
    private long mTokenExpired;
    private String requestAccessToken(GoogleSignInAccount googleAccount) {
        if (mAccessToken != null && SystemClock.elapsedRealtime() < mTokenExpired) return mAccessToken;
        mTokenExpired = 0;
        mAccessToken = null;

        HttpURLConnection conn = null;
        OutputStream os = null;
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            final URL url = new URL("https://www.googleapis.com/oauth2/v4/token");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(3000);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            final StringBuilder b = new StringBuilder();
            b.append("code=").append(googleAccount.getServerAuthCode()).append('&')
                    .append("client_id=").append(getString(R.string.default_web_client_id)).append('&')
                    .append("client_secret=").append(getString(R.string.client_secret)).append('&')
                    .append("redirect_uri=").append("").append('&')
                    .append("grant_type=").append("authorization_code");

            final byte[] postData = b.toString().getBytes("UTF-8");

            os = conn.getOutputStream();
            os.write(postData);

            final int responseCode = conn.getResponseCode();
            if (200 <= responseCode && responseCode <= 299) {
                is = conn.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);
            } else {
                Log.d("Error:", conn.getResponseMessage());
                return null;
            }

            b.setLength(0);
            String output;
            while ((output = br.readLine()) != null) {
                b.append(output);
            }

            final JSONObject jsonResponse = new JSONObject(b.toString());
            mAccessToken = jsonResponse.getString("access_token");
            mTokenExpired = SystemClock.elapsedRealtime() + jsonResponse.getLong("expires_in") * 1000;
            System.out.println("Expired: "+jsonResponse.getLong("expires_in"));
            return mAccessToken;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException e) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mTitle", mTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            getSupportActionBar().setTitle(mTitle);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onHomeItemMenuClickListener() {
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new MainPhotoFragment(), "MainPhotoFragment")
                .commit();
    }

    public void onRateAppButtonClick() {
        try {
            final SharedPreferences preferences = getSharedPreferences(RATE_APP_PREF_NAME, Context.MODE_PRIVATE);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("market://details?id=" + getPackageName()));
            startActivity(i);
            // save result
            preferences.edit().putBoolean(RATED_APP_KEY, true).commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(MainActivity.this, getString(R.string.app_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    public void onStoreItemMenuClickListener() {
        getFragmentManager().beginTransaction()
                .replace(R.id.frame_container, new StoreFragment(), "StoreFragment")
                .commit();
    }

    public void rateApp(final boolean finish) {
        final SharedPreferences preferences = getSharedPreferences(RATE_APP_PREF_NAME, Context.MODE_PRIVATE);
        boolean rated = preferences.getBoolean(RATED_APP_KEY, false);
        int count = preferences.getInt(OPEN_APP_COUNT_KEY, 0) + 1;
        ALog.d("NetworkUtils.rateApp", "rated=" + rated + ", count=" + count);
        preferences.edit().putInt(OPEN_APP_COUNT_KEY, count).commit();
        if (!rated && (count % 5 == 2)) {
            DialogUtils.showConfirmDialog(this, R.string.rate_app, R.string.photo_editor_rate_app,
                    new DialogUtils.ConfirmDialogOnClickListener() {

                        @Override
                        public void onOKButtonOnClick() {
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse("market://details?id=" + getPackageName()));
                                startActivity(i);
                                // save result
                                preferences.edit().putBoolean(RATED_APP_KEY, true).commit();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            if (finish) {
                                finish();
                            }
                        }

                        @Override
                        public void onCancelButtonOnClick() {
                            if (finish) {
                                finish();
                            }
                        }
                    });
        } else if (finish) {
            finish();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            BaseFragment fragment = (BaseFragment) getVisibleFragment();
            if (fragment instanceof MainPhotoFragment) {
                rateApp(true);
            } else {
                try {
                    mTitle = getString(R.string.home);
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(mTitle);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (getAdsHelper() != null)
                    getAdsHelper().addAdsBannerView(mAdLayout);
                getFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, new MainPhotoFragment(), "MainPhotoFragment")
                        .commit();
            }
        }
    }

    public Fragment getVisibleFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        return fragmentManager.findFragmentById(R.id.frame_container);
    }
}
