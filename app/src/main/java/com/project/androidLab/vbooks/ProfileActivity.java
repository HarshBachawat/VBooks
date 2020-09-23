package com.project.androidLab.vbooks;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener {

    Context mContext;
    private EditText profUserName;
    private EditText profPhone;
    private ImageButton imageButton;
    private final static int GALLERY_REQ = 1;
    private Button doneBtn;
    private Uri mImageUri = null;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private StorageReference mStorageRef;
    private MapView mapView;
    private GoogleMap gmap;
    private LocationManager locationManager;
    private static final String MAP_VIEW_BUNDLE_KEY = BuildConfig.GOOGLE_MAP_KEY;
    private Marker loc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        profUserName = (EditText)findViewById(R.id.profUserName);
        profPhone = (EditText) findViewById(R.id.profPhone);
        imageButton = (ImageButton)findViewById(R.id.imagebutton);
        doneBtn = (Button)findViewById(R.id.doneBtn);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQ);
            }
        });


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = profUserName.getText().toString().trim();
                final String phone = profPhone.getText().toString().trim();
                final String userID = mAuth.getCurrentUser().getUid();
                if (!TextUtils.isEmpty(name) && mImageUri != null && !TextUtils.isEmpty(phone)){

                    StorageReference filepath = mStorageRef.child(mImageUri.getLastPathSegment());
                    filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests")
                            String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                            final double lat = loc.getPosition().latitude;
                            final double lng = loc.getPosition().longitude;
                            mDatabaseUsers.child(userID).child("name").setValue(name);
                            mDatabaseUsers.child(userID).child("image").setValue(downloadUrl);
                            mDatabaseUsers.child(userID).child("lat").setValue(lat);
                            mDatabaseUsers.child(userID).child("lng").setValue(lng);
                            mDatabaseUsers.child(userID).child("phone").setValue(phone);
                            Toast.makeText(ProfileActivity.this, "Profile Details Saved", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(ProfileActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });

                }
                else{
                    Toast.makeText(ProfileActivity.this,"Please Enter All the Details",Toast.LENGTH_SHORT).show();
                }
            }
        });
        mContext = this;
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION},2);
            isLocationEnabled();
            return;

        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                100,
                10, locationListenerGPS);
        isLocationEnabled();
    }

    LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String msg = "New Latitude: " + latitude + "New Longitude: " + longitude;
                LatLng latLng = new LatLng(latitude, longitude);
                loc = gmap.addMarker(new MarkerOptions().position(latLng).draggable(true).visible(true));
                moveToCurrentLocation(latLng);
            }
            else
                Toast.makeText(mContext, "Location Unavailable", Toast.LENGTH_SHORT).show();
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

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    private void moveToCurrentLocation(LatLng currentLocation)
    {
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        gmap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        gmap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


    }

    protected void onResume(){
        super.onResume();
    }

    private void isLocationEnabled() {

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }
        else{
            Toast.makeText(mContext, "Location is Enabled.. Hurray!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
                return;
            }
            gmap = googleMap;
            loc = gmap.addMarker(new MarkerOptions().position(new LatLng(19.02,72.86)).visible(false));
            gmap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        loc = marker;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQ && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
            .start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK){
                mImageUri = result.getUri();
                imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
                imageButton.setImageURI(mImageUri);
            }else {
                if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Exception err = result.getError();
                }
            }
        }
    }

}
