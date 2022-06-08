package com.example.lab1_jenish_c0850919_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lab1_jenish_c0850919_android.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SeekBar.OnSeekBarChangeListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;




    //declaring some variables
    private static final int REQUEST_CODE = 1;
    private Marker homeMarker;
    private Marker destMarker;

    Polyline line;
    Polygon shape;
    private static final int POLYGON_SIDES = 4;
    List<Marker> markers = new ArrayList();
    List<LatLng> latLngList = new ArrayList<>();

    // location with location manager and listener
    LocationManager locationManager;
    LocationListener locationListener;



    //declaring variable for drag
    double end_latitude, end_longitude;
    double latitude, longitude;



    //declaring default values for color and seekbar
    int colorRed = 0, colorGreen = 0, colorBlue = 0;


    TextView displayDist;


    SeekBar redSB;
    SeekBar greenSB;
    SeekBar blueSB;
    SeekBar polygonSB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


       //binding seekbar
        redSB = findViewById(R.id.redSeekBar);
        greenSB = findViewById(R.id.greenSeekBar);
        blueSB = findViewById(R.id.blueSeekBar);
        polygonSB = findViewById(R.id.polygonSeekBar);


        //binding distance lable
        displayDist = findViewById(R.id.displayDist);

        //adding seekbarChnaging listener


        redSB.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) this);
        greenSB.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) this);
        blueSB.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) this);
        polygonSB.setOnSeekBarChangeListener((SeekBar.OnSeekBarChangeListener) this);






    }



    @Override
    public void onMapReady(@NonNull  GoogleMap googleMap)
    {
        mMap = googleMap;


        mMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                setHomeMarker(location);
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





        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);


        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();



        // apply tap gesture
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng)
            {


                //giving title to the markers
                MarkerOptions options = new MarkerOptions().position(latLng)
                        .title("A").draggable(true);

                markers.add(mMap.addMarker(options));
                if (markers.size() == POLYGON_SIDES)
                {

                    latLngList.add(latLng);
                    drawShape();


                    Log.d("Add",shape.toString());
                    double count = 0.0;
                    for (int a = 0; a < latLngList.size();a++)
                    {
                        if(a == latLngList.size() - 1)
                        {
                            count += calDistance(latLngList.get(a), latLngList.get(0));

                        }
                        else
                        {
                            count += calDistance(latLngList.get(a), latLngList.get(a+1));
                        }
                    }
                    Integer totalInInt = Math.toIntExact(Math.round(count));

                    displayDist.setText("Total Distance is :- " + totalInInt + " km");


                }


            }

        });



        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(@NonNull Polyline polyline) {
                Log.d("line clicked", polyline.getPoints().toString());
            }
        });


        googleMap.setOnPolygonClickListener(new GoogleMap.OnPolygonClickListener() {
            @Override
            public void onPolygonClick(@NonNull Polygon shape)
            {
                Log.d("Add",shape.toString());
                double count = 0.0;
                for (int a = 0; a < latLngList.size();a++)
                {
                    if(a == latLngList.size() - 1)
                    {
                        count += calDistance(latLngList.get(a), latLngList.get(0));

                    }
                    else
                    {
                        count += calDistance(latLngList.get(a), latLngList.get(a+1));
                    }
                }
                Integer totalInInt = Math.toIntExact(Math.round(count));

               if (displayDist.getVisibility() == TextView.INVISIBLE)
                {
                    displayDist.setVisibility(TextView.VISIBLE);
                    displayDist.setText("Total Distance is :- " + totalInInt + " km");
                } else {
                    displayDist.setVisibility(TextView.INVISIBLE);
                    displayDist.setText("");
                }


            }
        });


        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener()
        {
            @Override
            public void onMapLongClick(LatLng latLng) {

                removeAll();

                }

        });

    }

    void drawShape()
    {
       PolygonOptions options = new PolygonOptions()
                .fillColor(0x3500FF00)
                .strokeColor(Color.RED)
                .strokeWidth(7);

        for (int i = 0; i < POLYGON_SIDES; i++)
        {
            options.add(markers.get(i).getPosition());
        }

        shape = mMap.addPolygon(options);


    }

    void removeAll(){

        if(shape !=null) shape.remove();
        for(Marker marker : markers) marker.remove();
        markers.clear();
        latLngList.clear();
    }


    //implementing find distance function

    double calDistance(LatLng startPoint, LatLng endPoin) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = startPoint.latitude;
        double lat2 = endPoin.latitude;
        double lon1 = startPoint.longitude;
        double lon2 = endPoin.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        return Radius * c;
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);

    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setHomeMarker(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        homeMarker = mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 5));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
    {

        //for picking up random color for filing up polygon
        final int minColorValue = 0;
        final int maxColorValue = 255;
        final int randomColorPicker = new Random().nextInt((maxColorValue - minColorValue) + 1) + minColorValue;
        float[] hsvColor = {randomColorPicker, 0,randomColorPicker};
        hsvColor[1] = 360f * progress / progress;



        switch (seekBar.getId()) {
            case R.id.redSeekBar:
                colorRed = progress;
                break;
            case R.id.greenSeekBar:
                colorGreen = progress;
                break;
            case R.id.blueSeekBar:
                colorBlue = progress;
                break;

            case R.id.polygonSeekBar:
                if (shape != null)
                    shape.setFillColor(Color.HSVToColor(hsvColor));
                break;
        }
        if (shape != null)

            shape.setStrokeColor(Color.rgb(colorRed, colorGreen, colorBlue));


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker)
    {
        marker.setDraggable(true);

        return true;
    }

    @Override
    public void onMarkerDrag(@NonNull Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(@NonNull Marker marker)
    {
        end_latitude = marker.getPosition().latitude;
        end_longitude = marker.getPosition().longitude;
    }

    @Override
    public void onMarkerDragStart(@NonNull Marker marker) {

    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if(shape!= null)
            {
                shape.remove();
                markers.clear();
                latLngList.clear();

            }
    }

}
