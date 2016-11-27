package com.example.android.locationbutton;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    GoogleMap mMap;
    private static final double
            SEATTLE_LAT = 47.60621,
            SEATTLE_LNG =-122.33207,
            SYDNEY_LAT = -33.867487,
            SYDNEY_LNG = 151.20699,
            NEWYORK_LAT = 40.714353,
            NEWYORK_LNG = -74.005973;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private GoogleApiClient mLocationClient;
    private LocationListener mListener; //do pacote gms
    private Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (servicesOK()) {
            setContentView(R.layout.activity_map);

            if (initMap()) {
                gotoLocation(SEATTLE_LAT, SEATTLE_LNG, 15);

                // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
                // See https://g.co/AppIndexing/AndroidStudio for more information.
                mLocationClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .addApi(AppIndex.API).build();
                mLocationClient.connect();
            } else {
                Toast.makeText(this, "Map not connected!", Toast.LENGTH_SHORT).show();
            }
        } else {
            setContentView(R.layout.activity_main);
        }
    }

    public boolean servicesOK(){
        int isAvaliable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(isAvaliable == ConnectionResult.SUCCESS){
            return true;
        } else if(GooglePlayServicesUtil.isUserRecoverableError(isAvaliable)){
            Dialog dialog =
                    GooglePlayServicesUtil.getErrorDialog(isAvaliable,this,ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Can't connect to mapping service",Toast.LENGTH_SHORT).show();
        }
        return false;
    }


    private boolean initMap(){
        if (mMap == null){
            SupportMapFragment mapFragment =
                    (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mMap = mapFragment.getMap();

            if(mMap != null){
                mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        View v = getLayoutInflater().inflate(R.layout.info_window, null);
                        TextView tvLocality = (TextView) v.findViewById(R.id.tvLocality);
                        TextView tvLat = (TextView) v.findViewById(R.id.tvLat);
                        TextView tvLng = (TextView) v.findViewById(R.id.tvLng);
                        TextView tvSnippet = (TextView) v.findViewById(R.id.tvSnippet);

                        LatLng latLng = marker.getPosition();
                        tvLocality.setText(marker.getTitle());
                        tvLat.setText("Latitude: "+latLng.latitude);
                        tvLng.setText("Longitude: "+latLng.longitude);
                        tvSnippet.setText(marker.getSnippet());
                        return v;
                    }
                });

                mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        Geocoder gc = new Geocoder(MainActivity.this);
                        List<Address> list = null;
                        try {
                            list = gc.getFromLocation(latLng.latitude, latLng.longitude,1);
                        } catch (IOException e){
                            e.printStackTrace();
                            return;
                        }
                        Address add = list.get(0);
                        MainActivity.this.addMarker(add,latLng.latitude,latLng.longitude);
                    }
                });

                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        String msg = marker.getTitle() +", "+
                                marker.getPosition().latitude+", "+
                                marker.getPosition().longitude+")";
                        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
            }

        }
        return(mMap != null);
    }

    private void gotoLocation(double lat, double lng, float zoom){
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    public void geoLocate(View v) throws IOException {

        hideSoftKeyboard(v);

        TextView tv = (TextView) findViewById(R.id.editText1);
        String searchString = tv.getText().toString();

        Geocoder gc = new Geocoder(this);
        List<Address> list = gc.getFromLocationName(searchString, 1); //segundo argumento é maximo de resultados que eu quero receber

        if (list.size() > 0) {
            Address add = list.get(0);
            String locality = add.getLocality();
            Toast.makeText(this, "Found: " + locality, Toast.LENGTH_SHORT).show();

            double lat = add.getLatitude();
            double lng = add.getLongitude();
            gotoLocation(lat, lng, 15);

            if(marker != null){
                marker.remove();
            }
            addMarker(add, lat, lng);
        }

    }
    private void addMarker(Address add, double lat, double lng){
        MarkerOptions options = new MarkerOptions()
                .title(add.getLocality())
                .position(new LatLng(lat,lng))
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_marker));
        String country = add.getCountryName();
        if(country.length()>0){
            options.snippet(country);
        }
        marker = mMap.addMarker(options);

    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm =
                (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public  void showCurrentLocation(MenuItem item){
        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);
        if(currentLocation == null){
            Toast.makeText(this, "Culdn't connect!",Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                    latLng, 15
            );
            mMap.animateCamera(update);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //Add menu handling code
        switch (id) {
            case R.id.mapTypeNone:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(this, "Ready to map!",Toast.LENGTH_SHORT).show();
        /*
        mListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Toast.makeText(MainActivity.this
                    , "Location changed: "+ location.getLatitude()+", "+location.getLongitude()
                        ,Toast.LENGTH_SHORT).show();
                gotoLocation(location.getLatitude(),location.getLongitude(),15);
            }
        };

        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);
        request.setFastestInterval(1000);
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mLocationClient,request,mListener
        );*/

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mLocationClient,mListener
        );
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        mLocationClient.connect();
        AppIndex.AppIndexApi.start(mLocationClient, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(mLocationClient, getIndexApiAction());
        mLocationClient.disconnect();
    }
}
