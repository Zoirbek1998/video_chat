package dev.future.videochat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;

import dev.future.videochat.R;

public class WelcomeActivity extends AppCompatActivity {

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null){
            getToNextActivity();
        }

        findViewById(R.id.getStrat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             getToNextActivity();
            }
        });
    }

    private void getToNextActivity() {
        startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
        finish();
    }
}