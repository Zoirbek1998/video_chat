package dev.future.videochat.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import dev.future.videochat.databinding.ActivityConnectingBinding;
import dev.future.videochat.utils.Utils;

public class ConnectingActivity extends AppCompatActivity {

    ActivityConnectingBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    boolean isOkay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConnectingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        String profile = getIntent().getStringExtra(Utils.PROFILE);

        Glide.with(this).load(profile).into(binding.profileImage);

        String userName = auth.getUid();

        database.getReference().child(Utils.USERS)
                .orderByChild(Utils.STATUS)
                .equalTo(0).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.getChildrenCount() > 0) {
                            isOkay = true;
                            // Room Available
                            for (DataSnapshot childSnap : snapshot.getChildren()) {
                                database.getReference()
                                        .child(Utils.USERS)
                                        .child(childSnap.getKey())
                                        .child(Utils.INCOMING)
                                        .setValue(userName);
                                database.getReference()
                                        .child(Utils.USERS)
                                        .child(childSnap.getKey())
                                        .child(Utils.STATUS)
                                        .setValue(1);
                                
                                        Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                        String incoming = childSnap.child(Utils.INCOMING).getValue(String.class);
                                        String createBy = childSnap.child(Utils.CREATEBY).getValue(String.class);
                                        boolean isAvamble = childSnap.child(Utils.ISAVAILABLE).getValue(Boolean.class);
                                     
                                        intent.putExtra(Utils.USERNAME, userName);
                                        intent.putExtra(Utils.INCOMING, incoming);
                                        intent.putExtra(Utils.CREATEBY, createBy);
                                        intent.putExtra(Utils.ISAVAILABLE, isAvamble);
                                        startActivity(intent);
                            }

                        } else {
                            // Not Available

                            HashMap<String, Object> room = new HashMap<>();
                            room.put(Utils.INCOMING, userName);
                            room.put(Utils.CREATEBY, userName);
                            room.put(Utils.ISAVAILABLE, true);
                            room.put(Utils.STATUS, 0);

                            database.getReference()
                                    .child(Utils.USERS)
                                    .child(userName).setValue(room)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            database.getReference()
                                                    .child(Utils.USERS)
                                                    .child(userName)
                                                    .addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.child(Utils.STATUS).exists()) {
                                                                if (snapshot.child(Utils.STATUS).getValue(Integer.class) == 1) {
                                                                    if (isOkay)
                                                                    return;

                                                                    isOkay = true;

                                                                    Intent intent = new Intent(ConnectingActivity.this, CallActivity.class);
                                                                    String incoming = snapshot.child(Utils.INCOMING).getValue(String.class);
                                                                    String createBy = snapshot.child(Utils.CREATEBY).getValue(String.class);
                                                                    boolean isAvamble = snapshot.child(Utils.ISAVAILABLE).getValue(Boolean.class);
                                                                   
                                                                    intent.putExtra(Utils.USERNAME, userName);
                                                                    intent.putExtra(Utils.INCOMING, incoming);
                                                                    intent.putExtra(Utils.CREATEBY, createBy);
                                                                    intent.putExtra(Utils.ISAVAILABLE, isAvamble);
                                                                    startActivity(intent);
                                                                    finish();

                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {

                                                        }
                                                    });


                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}