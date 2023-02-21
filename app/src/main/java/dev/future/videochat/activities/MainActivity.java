package dev.future.videochat.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaopiz.kprogresshud.KProgressHUD;

import dev.future.videochat.R;
import dev.future.videochat.databinding.ActivityMainBinding;
import dev.future.videochat.models.User;
import dev.future.videochat.utils.Utils;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    long coins = 0;
    String[] permission = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private int requestCode = 1;
    User user;
    KProgressHUD progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        progress = KProgressHUD.create(this);
        progress.setDimAmount(0.5f);
        progress.show();

        FirebaseUser currentUser = auth.getCurrentUser();


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });


        database.getReference().child(Utils.PROFILES)
                .child(currentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progress.dismiss();
                         user = snapshot.getValue(User.class);
                        coins = user.getCoins();
                        binding.coins.setText(getString(R.string.have) + coins);
                        Glide.with(MainActivity.this).load(user.getProfile())
                                .into(binding.profileImage);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
        binding.findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionGranted()) {
                    if (coins > 50) {
                        coins = coins-50;
                        database.getReference().child(Utils.PROFILES)
                                .child(currentUser.getUid())
                                .child(Utils.COINS)
                                .setValue(coins);

                        Intent intent = new Intent(MainActivity.this, ConnectingActivity.class);
                        intent.putExtra(Utils.PROFILE,user.getProfile());
                        startActivity(intent);
//                    Toast.makeText(MainActivity.this, R.string.call_finding, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, R.string.insufficient, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    askPermission();
                }
            }
        });

        binding.rewardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,RevardActivity.class));
            }
        });

    }

    void askPermission() {
        ActivityCompat.requestPermissions(this, permission, requestCode);
    }

    private boolean isPermissionGranted() {
        for (String permission : permission) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }
}