package com.droidapps.anniversarycollage.gphotos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.droidapps.anniversarycollage.R;
import com.droidapps.anniversarycollage.gphotos.model.DateFilter;
import com.droidapps.anniversarycollage.gphotos.model.Filters;
import com.droidapps.anniversarycollage.gphotos.model.SearchRequest;
import com.droidapps.anniversarycollage.gphotos.model.SearchResponse;
import com.droidapps.anniversarycollage.ui.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import dauroi.photoeditor.utils.GsonUtils;

public class Util {

    public static final int RC_SIGN_IN=3333;
    static final String URL_SEARCH="https://photoslibrary.googleapis.com/v1/mediaItems:search";
    static final String CLIENT_ID="413558917554-2a13qf70rqkcm2e131f6ca4jivan0ded.apps.googleusercontent.com";

    public static void initializeGooglePhotos(Activity activity){


        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestScopes(new Scope("https://www.googleapis.com/auth/photoslibrary.readonly"))
//                    .requestIdToken("413558917554-2a13qf70rqkcm2e131f6ca4jivan0ded.apps.googleusercontent.com")
                    .requestServerAuthCode(CLIENT_ID)
                    .requestEmail()
                    .build();

           GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

            activity.startActivityForResult( mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);


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

    public static void handleActivityResult(int requestCode, Activity activity, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task,activity);
        }
    }


    private static void handleSignInResult(Task<GoogleSignInAccount> completedTask, final Activity activity) {
        try {
            final GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            System.out.println("Server auth code: "+account.getServerAuthCode());
            System.out.println("Server id token: "+account.getIdToken());
            AsyncTask task = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {
                    String accessToken=requestAccessToken(account,activity);
                    System.out.println("AccessToken: "+accessToken);
                    Date expiryTime=new Date(System.currentTimeMillis()+3600*1000);
                    fetchAlbums(accessToken,28,10,2018,URL_SEARCH,activity);
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

    static String APP_NAME="collage";
    public static String getAccessToken(Activity activity){
        if(mAccessToken!=null) return mAccessToken;
        return activity.getSharedPreferences(APP_NAME,Context.MODE_PRIVATE).getString("accesstoken",null);

    }

    public static long isTokenExpired(Activity activity){
       if(mTokenExpired==0) {
          mTokenExpired= activity.getSharedPreferences(APP_NAME, Context.MODE_PRIVATE).getLong("expiry", 0);
       }
       return mTokenExpired;
    }

    public static void setAccessToken(Activity activity,String accessToken, long expiry){
        mAccessToken=accessToken;
        mTokenExpired = SystemClock.elapsedRealtime() + expiry * 1000;
        activity.getSharedPreferences("collage",Context.MODE_PRIVATE).edit()
                .putString("accesstoken",accessToken)
                .putLong("expiry",mTokenExpired)
                .commit();

    }

    static final int PAGE_SIZE=100;
    public static void fetchAlbums(final String accessToken, int day, int month, int year,String url, Activity activity){
        JSONObject requestObject = constructDateFilterRequest(day,month,year);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.POST,url,
                requestObject, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                System.out.println("Response: "+response);
               SearchResponse searchResponse= new Gson().fromJson(response.toString(), SearchResponse.class);
               if (searchResponse.mediaItems!=null && searchResponse.mediaItems.size()>0)
                    System.out.println("Search Response: "+searchResponse.mediaItems.get(0));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error: "+error);
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders()  {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }
        };
        Volley.newRequestQueue(activity).add(req);
    }

    private static JSONObject constructDateFilterRequest(long day, long month, long year){

        SearchRequest searchRequest = new SearchRequest();
        Filters filters =new Filters();
        DateFilter dateFilter = new DateFilter();
        filters.dateFilter = dateFilter;
        dateFilter.dates = new ArrayList<>();
        com.droidapps.anniversarycollage.gphotos.model.Date date = new com.droidapps.anniversarycollage.gphotos.model.Date();
        date.day=day;
        date.month=month;
        date.year=year;
        dateFilter.dates.add(date);
        searchRequest.pageSize=(long)PAGE_SIZE;
        JSONObject requestObject =null;
        try {
           requestObject=new JSONObject(new Gson().toJson(searchRequest));


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return requestObject;
    }

    public static void fetchAlbums(final String accessToken, String url,Activity activity){
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET,url,
                null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                System.out.println("Response: "+response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Error: "+error);
            }
        }) {

            /**
             * Passing some request headers
             */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                //headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer "+accessToken);
                return headers;
            }
        };
        Volley.newRequestQueue(activity).add(req);
    }


    private static String mAccessToken;
    private static long mTokenExpired;
    private static String requestAccessToken(GoogleSignInAccount googleAccount,Activity activity) {
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
                    .append("client_id=").append(activity.getString(R.string.default_web_client_id)).append('&')
                    .append("client_secret=").append(activity.getString(R.string.client_secret)).append('&')
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
            setAccessToken(activity,mAccessToken,jsonResponse.getLong("expires_in"));
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
}
