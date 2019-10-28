package com.project.androidLab.vbooks;

import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class OwnerDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ImageView profileImage;
    private MapView mapView;
    private double lat;
    private double lng;
    private GoogleMap gmap;
    private static final String MAP_VIEW_BUNDLE_KEY = "AIzaSyD3PnRRrmeaG-LiWgM9YoHVNyWdvl4hxvY";
    private TextView profUserName,profPhone;
    private  String userId;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_details);

        profileImage = (ImageView) findViewById(R.id.profileImage);
        profUserName = (TextView)findViewById(R.id.profUserName);
        profPhone = (TextView) findViewById(R.id.profPhone);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        userId = getIntent().getExtras().getString("UserID");
        mAuth = FirebaseAuth.getInstance();

        mDatabase.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = (String) dataSnapshot.child("Username").getValue();
                String profile_pic = (String) dataSnapshot.child("image").getValue();
                if (dataSnapshot.child("lat").exists()){
                    lat = (double) dataSnapshot.child("lat").getValue();
                    lng = (double) dataSnapshot.child("lng").getValue();
                    gmap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)));
                    moveToCurrentLocation(new LatLng(lat,lng));
                }
                else
                    mapView.setVisibility(View.GONE);
                if (dataSnapshot.child("phone").exists()){
                    String phone = (String) dataSnapshot.child("phone").getValue();
                    profPhone.setText("Phone Number: "+phone);
                }
                else
                    profPhone.setVisibility(View.GONE);
                profUserName.setText("UserName: "+name);
                Picasso.with(OwnerDetailsActivity.this).load(profile_pic).into(profileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        gmap = googleMap;
    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        gmap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        gmap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }
}
