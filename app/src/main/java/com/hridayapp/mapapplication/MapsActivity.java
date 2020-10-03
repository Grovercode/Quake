package com.hridayapp.mapapplication;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.sip.SipAudioCall;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.hridayapp.mapapplication.Model.EarthQuake;
import com.hridayapp.mapapplication.UI.CustomInfoWindow;
import com.hridayapp.mapapplication.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnInfoWindowClickListener , GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        queue = Volley.newRequestQueue(this);

        getEarthQuakes();

    }
    

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);



        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
               // Log.d("Location: ", location.toString());

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            }

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    //getting all earthquakes
    public void getEarthQuakes(){

        final EarthQuake earthQuake = new EarthQuake();


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray features = response.getJSONArray("features");

                            for(int i = 0; i<Constants.LIMIT; i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");

                                //geometry object
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");

                                //get coordinates array
                                JSONArray coordinates = geometry.getJSONArray("coordinates");

                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

                                Log.d("Quale:", lon + ", " + lat);

                                earthQuake.setPlace(properties.getString("place"));
                                earthQuake.setType(properties.getString("type"));
                                earthQuake.setTime(properties.getLong("time"));
                                earthQuake.setMagnitude(properties.getDouble("mag"));
                                earthQuake.setDetailLink(properties.getString("detail"));

                                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                           String formattedDate = dateFormat.format(new Date(Long.valueOf(properties.getLong("time"))).getTime());

                           MarkerOptions markerOptions = new MarkerOptions();

                           markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                           markerOptions.title(earthQuake.getPlace());
                           markerOptions.position(new LatLng(lat,lon));
                           markerOptions.snippet("Magnitude: " + earthQuake.getMagnitude() + "\n"
                           + "Date: " + formattedDate);

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(earthQuake.getDetailLink());


                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon),1));

                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);


        }

    @Override
    public void onInfoWindowClick(Marker marker) {

        /*
        getQuakeDetails(marker.getTag().toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                Constants.url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                String detailsUrl = "";

                try {
                    JSONArray features = response.getJSONArray("features");
                    for(int i = 0; i<Constants.LIMIT; i++) {
                        JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
                        JSONObject Urldets = properties.getJSONObject("url");

                        detailsUrl = Urldets.toString();

                        getQuakeDetails(detailsUrl);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);

         */
    }

    private void getQuakeDetails(String url) {

        /*
        dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        View view = getLayoutInflater().inflate(R.layout.popup, null);

        Button dismissButton = (Button) view.findViewById(R.id.dismissPop);
        Button dismissButtonTop = (Button) view.findViewById(R.id.dismissPopTop);
        TextView popList = (TextView) view.findViewById(R.id.popList);
        WebView htmlPop = (WebView) view.findViewById(R.id.htmlWebView);


        htmlPop.loadUrl(url);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.popup, null);

                        Button dismissButton = (Button) view.findViewById(R.id.dismissPop);
                        Button dismissButtonTop = (Button) view.findViewById(R.id.dismissPopTop);
                        TextView popList = (TextView) view.findViewById(R.id.popList);
                        WebView htmlPop = (WebView) view.findViewById(R.id.htmlWebView);

                        StringBuilder stringBuilder = new StringBuilder();


                        try {
                            JSONArray cities = response.getJSONArray("cities");

                            for(int i=0;i<cities.length();i++)
                            {
                                JSONObject citiesObj = cities.getJSONObject(i);
                                stringBuilder.append("City" + citiesObj.getString("name")+"\n"
                                        +"Distance: " + citiesObj.getString("distance")
                                        +"\n" + "Population: " + citiesObj.getString("population"));

                                stringBuilder.append("\n\n");

                            }



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);

         */
    }

    private void goToUrl(String s) {
        Uri uriUrl = Uri.parse(s);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
