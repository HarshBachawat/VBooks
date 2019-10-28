package com.project.androidLab.vbooks;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class SinglePostActivity extends AppCompatActivity {

    private ImageView singelImage;
    private TextView singleTitle, singleDesc;
    String post_key = null;
    String post_uid;
    private DatabaseReference mDatabase;
    private Button deleteBtn;
    private Button ownerDetails;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        singelImage = (ImageView)findViewById(R.id.profileImage);
        singleTitle = (TextView)findViewById(R.id.singleTitle);
        singleDesc = (TextView)findViewById(R.id.singleDesc);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Books");
        post_key = getIntent().getExtras().getString("PostID");
        deleteBtn = (Button)findViewById(R.id.deleteBtn);
        ownerDetails = (Button)findViewById(R.id.ownerDetails);
        mAuth = FirebaseAuth.getInstance();
        deleteBtn.setVisibility(View.INVISIBLE);
        ownerDetails.setVisibility(View.INVISIBLE);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDatabase.child(post_key).removeValue();
                SinglePostActivity.super.onBackPressed();
            }
        });

        mDatabase.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String post_title = (String) dataSnapshot.child("title").getValue();
                String post_desc = (String) dataSnapshot.child("desc").getValue();
                String post_image = (String) dataSnapshot.child("imageUrl").getValue();
                post_uid = (String) dataSnapshot.child("uid").getValue();

                singleTitle.setText(post_title);
                singleDesc.setText(post_desc);
                Picasso.with(SinglePostActivity.this).load(post_image).into(singelImage);
                ownerDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent ownerDetail = new Intent(SinglePostActivity.this,OwnerDetailsActivity.class);
                        ownerDetail.putExtra("UserID",post_uid);
                        startActivity(ownerDetail);
                    }
                });
                if (mAuth.getCurrentUser().getUid().equals(post_uid)){

                    deleteBtn.setVisibility(View.VISIBLE);
                    ownerDetails.setVisibility(View.GONE);
                }
                else {
                    ownerDetails.setVisibility(View.VISIBLE);
                    deleteBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


}
