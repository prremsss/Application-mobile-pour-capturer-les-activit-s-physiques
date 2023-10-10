package com.example.miloudi_oualid_projet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class MapFragment extends SupportMapFragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    int i =0;
    double avgspeedtmp = 0;
    private Location mLastLocation;
    GoogleMap mGoogleMap;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Marker marker;
    Marker marker2;
    private Polyline polyline;

    private Boolean flag=true;

    private List<LatLng> polylinePoints;

    @Override
    public void onResume() {
        super.onResume();

        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {

        if (mGoogleMap == null) {
            getMapAsync(this);
        }
    }
    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) + ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACTIVITY_RECOGNITION)!= PackageManager.PERMISSION_GRANTED) {


                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                        Manifest.permission.ACTIVITY_RECOGNITION)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Grant those Permissions");
                    builder.setMessage("Location, Activity recognition");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACTIVITY_RECOGNITION},
                                    MY_PERMISSIONS_REQUEST_LOCATION );


                        }
                    });
                    builder.setNegativeButton("Cancel",null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();

                }
                else {
                    ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACTIVITY_RECOGNITION},
                            MY_PERMISSIONS_REQUEST_LOCATION );

                }
            }
            else {
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            }

        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        polylinePoints = new ArrayList<>();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        double speed = 0;
        i=i+1;

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        if (this.mLastLocation != null)
            speed = Math.sqrt(
                    Math.pow(location.getLongitude() - mLastLocation.getLongitude(), 2)
                            + Math.pow(location.getLatitude() - mLastLocation.getLatitude(), 2)
            ) / ((location.getTime() - this.mLastLocation.getTime())/1000);
        if (location.hasSpeed())
            speed = location.getSpeed();
        this.mLastLocation = location;

        avgspeedtmp=speed+avgspeedtmp;
        ActivityFragment.speed.setText("Vitesse : " + new DecimalFormat("#.##").format(speed) + "m/s" + " ---  "+ new DecimalFormat("#.##").format(speed*3.6) + "km/h");
        ActivityFragment.averagespeed.setText("Vitesse moyenne : " + new DecimalFormat("#.##").format(avgspeedtmp/i) + "m/s" + " --- "+ new DecimalFormat("#.##").format(avgspeedtmp/i*3.6) + "km/h");

        Geocoder geocoder = new Geocoder(getActivity());
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(latitude, longitude, 1);
            String result = addresses.get(0).getLocality() + ":";
            result += addresses.get(0).getCountryName();
            LatLng latLng = new LatLng(latitude, longitude);


            if (flag) {
                marker2 = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(result).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }


            if (marker != null) {
                marker.remove();
                marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(result).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                mGoogleMap.setMaxZoomPreference(100);
            } else {
                marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(result).icon(BitmapDescriptorFactory.defaultMarker(120.0f)));
                mGoogleMap.setMaxZoomPreference(100);
            }
            polylinePoints.add(latLng);
            if (polyline != null) {
                polyline.setPoints(polylinePoints);
            } else {
                polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(polylinePoints).color(Color.BLACK).jointType(JointType.DEFAULT).width(9.0f));
            }
            flag = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}