package com.example.testad;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private static final String AD_TAG = "contextcue";
    private RequestQueue queue;
    private String redirectURI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);
        loadAd();
    }

    public void adClick(View view) {
        final TextView textView = findViewById(R.id.textintro);
        //String url ="http://10.0.2.2:3000/adclick?s=ecb92814-46a7-433c-8dbf-3c95feed5379&i=38905820-2806-4d3c-8944-762659f4d2e6";
        String url = redirectURI;
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        textView.setText("Response is: "+ response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(AD_TAG, "volley error");

                final int status = error.networkResponse.statusCode;
                // Handle 30x
                if(HttpURLConnection.HTTP_MOVED_PERM == status || status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    textView.setText("redirect" + error.toString());
                    String location = error.networkResponse.headers.get("Location");

                    if (!location.startsWith("http://") && !location.startsWith("https://")) {
                        location = "http://" + location;
                    }

                    Log.d(AD_TAG, "Location: " + location);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
                    startActivity(browserIntent);
                } else {
                    textView.setText("That didn't work!" + error.toString() + " " + error.networkResponse.statusCode);
                }
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void loadAd() {

        //set on component
        String slotId = "ehxBZUfTT";
        String siteId = "ecb92814-46a7-433c-8dbf-3c95feed5379";
        String slotWidth = "300";
        String slotHeight = "250";

        String userAgent = System.getProperty("http.agent");
        String timeZone = TimeZone.getDefault().getID();
        SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.US);
        String timeNow = format.format(new Date());
        Calendar calendar = Calendar.getInstance();
        String dayOfWeek = Integer.toString(calendar.get(Calendar.DAY_OF_WEEK));

        String url = "http://10.0.2.2:3000/ad-fetch/serve?q={\"slots\": [{\"id\": \"" + slotId + "\"," +
                    "\"w\": " + slotWidth + "," +
                    "\"h\": " + slotHeight + "}]," +
                "\"ua\": \"" + userAgent + "\"," +
                "\"tz\": \"" + timeZone + "\"," +
                "\"site\": \"" + siteId + "\"," +
                " \"time\":\"" + timeNow + "\"," +
                "\"dow\":\"" + dayOfWeek + "\"}";

        Log.d(AD_TAG, "Loading ad fetch url: " + url);

        final ImageView adImage = findViewById(R.id.imageView);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String adURI = response.getJSONArray("slots").getJSONObject(0).getString("adURI");
                            adImage.setImageURI(Uri.parse(adURI));
                            redirectURI = response.getJSONArray("slots").getJSONObject(0).getString("redirectURI");

                        } catch (JSONException e) {
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

}
