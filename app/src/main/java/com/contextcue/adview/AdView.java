package com.contextcue.adview;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static android.support.v4.content.ContextCompat.startActivity;

public class AdView extends AppCompatImageView implements View.OnClickListener {

    private static final String AD_TAG = "contextcue";
    private RequestQueue queue;
    private String redirectURI;
    private View.OnClickListener clickListener;
    private Context context;

    private String slotId;
    private String siteId;
    private String slotWidth;
    private String slotHeight;

    public AdView(Context context) {
        this(context, null, 0);
    }

    public AdView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if(attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.AdView);
            int count = typedArray.getIndexCount();
            try {
                for (int i = 0; i < count; i++) {
                    int attr = typedArray.getIndex(i);
                    switch (attr) {
                        case R.styleable.AdView_slotId : slotId = typedArray.getString(attr);
                            break;
                        case R.styleable.AdView_siteId : siteId = typedArray.getString(attr);
                            break;
                        case R.styleable.AdView_slotWidth : slotWidth = typedArray.getString(attr);
                            break;
                        case R.styleable.AdView_slotHeight : slotHeight = typedArray.getString(attr);
                            break;
                    }
                }
            }
            finally {
                typedArray.recycle();
            }
        }
        queue = Volley.newRequestQueue(context);
        this.context = context;
        loadAd();
        setOnClickListener(this);
    }

    public void loadAd() {
        String userAgent = System.getProperty("http.agent");
        String timeZone = TimeZone.getDefault().getID();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        String timeNow = format.format(new Date());
        Calendar calendar = Calendar.getInstance();
        String dayOfWeek = Integer.toString(calendar.get(Calendar.DAY_OF_WEEK) - 1);

        // String host = "http://10.0.2.2:3000";
        String host = "https://api.contextcue.com";
        String url = host + "/ad-fetch/serve?q={\"slots\": [{\"id\": \"" + slotId + "\"," +
                "\"w\": " + slotWidth + "," +
                "\"h\": " + slotHeight + "}]," +
                "\"ua\": \"" + userAgent + "\"," +
                "\"tz\": \"" + timeZone + "\"," +
                "\"site\": \"" + siteId + "\"," +
                " \"time\":\"" + timeNow + "\"," +
                "\"dow\":\"" + dayOfWeek + "\"}";

        Log.d(AD_TAG, "Loading ad fetch url: " + url);

        final ImageView adImage = this;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(AD_TAG, "Loading ad response: " + response);

                            String adURI = response.getJSONObject("data").getJSONArray("slots").getJSONObject(0).getString("adURI");
                            new DownloadImageTask(adImage).execute(adURI);
                            redirectURI = response.getJSONObject("data").getJSONArray("slots").getJSONObject(0).getString("redirectURI");
                            Log.d(AD_TAG, "Loading ad uri: " + adURI);
                            Log.d(AD_TAG, "Loading ad redirect uri: " + redirectURI);

                        } catch (JSONException e) {
                            Log.d(AD_TAG, "json error: " + e);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(AD_TAG, "Loading ad error: " + error);
                    }
                }
                );

        queue.add(jsonObjectRequest);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (l == this) {
            super.setOnClickListener(l);
        } else {
            clickListener = l;
        }
    }

    @Override
    public void onClick(View v) {
        String url = redirectURI;
        // url = url.replaceFirst("localhost", "10.0.2.2");
        Log.d(AD_TAG, "adclick redirect: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(AD_TAG, "response is: " + response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(AD_TAG, "volley error");

                final int status = error.networkResponse.statusCode;
                // Handle 30x
                if(HttpURLConnection.HTTP_MOVED_PERM == status || status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    String location = error.networkResponse.headers.get("Location");
                    if (!location.startsWith("http://") && !location.startsWith("https://")) {
                        location = "http://" + location;
                    }

                    Log.d(AD_TAG, "redirect to: " + location);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
                    startActivity(context, browserIntent, null);
                } else {
                    Log.d(AD_TAG, "redirect: failed, " + error.toString() + " " + error.networkResponse.statusCode);
                }
            }
        });

        queue.add(stringRequest);

        if (clickListener != null) {
            clickListener.onClick(this);
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap bm = null;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inTargetDensity = Math.round(getResources().getDisplayMetrics().density);
                options.inDensity = Math.round(getResources().getDisplayMetrics().density);
                options.inScaled = true;
                InputStream in = new java.net.URL(urldisplay).openStream();
                bm = BitmapFactory.decodeStream(in, null, options);
            } catch (Exception e) {
                Log.e(AD_TAG, "error getting ad image: " + e.getMessage());
                e.printStackTrace();
            }
            return bm;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

}
