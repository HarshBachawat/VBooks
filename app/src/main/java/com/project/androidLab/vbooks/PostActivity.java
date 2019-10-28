package com.project.androidLab.vbooks;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
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


import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class PostActivity extends AppCompatActivity  {
    // imports
    Context mContext;
    private ImageButton imageBtn;
    private static final int GALLERY_REQUEST_CODE = 2;
    private Uri uri = null;
    private EditText textTitle;
    private EditText textDesc;
    private Button postBtn;
    private StorageReference storage;
    private FirebaseDatabase database;
    private DatabaseReference databaseRef;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseUsers;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        // initializing objects
        postBtn = (Button) findViewById(R.id.postBtn);
        textDesc = (EditText) findViewById(R.id.textDesc);
        textTitle = (EditText) findViewById(R.id.textTitle);

        storage = FirebaseStorage.getInstance().getReference();
        databaseRef = database.getInstance().getReference().child("Books");
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        imageBtn = (ImageButton) findViewById(R.id.imageBtn);
        //picking image from gallery
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int flag = 0;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    if (!hasPermissions(permissions)) {
                        requestPermissions(permissions, 1);
                    } else {
                        flag = 1;
                    }
                } else {
                    flag = 1;

                }
                if (flag == 1) {
                    final File root = new File(Environment.getExternalStorageDirectory() + File.separator + "MyDir" + File.separator);
                    boolean isDirectoryCreated = root.exists();
                    if (!isDirectoryCreated) {
                        isDirectoryCreated = root.mkdirs();
                    }
                    final String fname = getUniqueImageFilename();
                    final File sdImageMainDirectory = new File(root, fname);
                    uri = Uri.fromFile(sdImageMainDirectory);

                    final List<Intent> cameraIntents = new ArrayList<Intent>();
                    final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    final PackageManager packageManager = getPackageManager();
                    final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
                    for (ResolveInfo res : listCam) {
                        final String packageName = res.activityInfo.packageName;
                        final Intent intent = new Intent(captureIntent);
                        intent.setComponent(new ComponentName(packageName, res.activityInfo.name));
                        intent.setPackage(packageName);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        cameraIntents.add(intent);
                    }

                    final Intent galleryIntent = new Intent();
                    galleryIntent.setType("image/*");
                    galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                    Intent chooser = Intent.createChooser(galleryIntent, "Complete action using...");
                    chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
                    startActivityForResult(chooser, GALLERY_REQUEST_CODE);
                }
            }
        });
        // posting to Firebase
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(PostActivity.this, "Adding Book...", Toast.LENGTH_LONG).show();
                final String PostTitle = textTitle.getText().toString().trim();
                final String PostDesc = textDesc.getText().toString().trim();
                // do a check for empty fields
                if (!TextUtils.isEmpty(PostDesc) && !TextUtils.isEmpty(PostTitle)) {
                    StorageReference filepath = storage.child("post_images").child(uri.getLastPathSegment());
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            @SuppressWarnings("VisibleForTests")
                            //getting the post image download url
                            final Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            Toast.makeText(getApplicationContext(), "Succesfully Uploaded", Toast.LENGTH_SHORT).show();
                            final DatabaseReference newPost = databaseRef.push();
                            //adding post contents to database reference
                            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    newPost.child("title").setValue(PostTitle);
                                    newPost.child("desc").setValue(PostDesc);
                                    newPost.child("imageUrl").setValue(downloadUrl.toString());
                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                    newPost.child("username").setValue(dataSnapshot.child("name").getValue())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {

                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(PostActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                   // Toast.makeText(mContext,databaseError.toString(),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });

                }
            }
        });

    }

    public boolean hasPermissions( String... permissions) {
        if (permissions != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public String getUniqueImageFilename() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "img_"+timeStamp+".jpg";
    }

    @Override
    // image from gallery result
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
//            uri = data.getData();
//            imageBtn.setImageURI(uri);
//            Bitmap photo = (Bitmap) data.getExtras().get("data");
//            imageBtn.setImageBitmap(photo);
            final boolean isCamera;
            if (data == null || data.getData() == null) {
                isCamera = true;
            }
            else {
                isCamera = MediaStore.ACTION_IMAGE_CAPTURE.equals(data.getAction());
                uri = data.getData();
            }
            imageBtn.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageBtn.setImageURI(uri);
        }
    }

}
