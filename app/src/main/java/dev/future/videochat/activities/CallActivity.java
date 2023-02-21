package dev.future.videochat.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

import dev.future.videochat.R;
import dev.future.videochat.databinding.ActivityCallBinding;
import dev.future.videochat.models.InterfaceJava;
import dev.future.videochat.models.User;
import dev.future.videochat.utils.Utils;

public class CallActivity extends AppCompatActivity {

    ActivityCallBinding binding;
    String uniqueId = "";
    FirebaseAuth auth;
    String userName = "";
    String friendsUsername = "";

    boolean isPeerConnected = false;

    DatabaseReference firebaseRef;

    boolean isAudio = true;
    boolean isVideo = true;
    String createBy;

    boolean pageExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firebaseRef = FirebaseDatabase.getInstance().getReference().child(Utils.USERS);
        userName = getIntent().getStringExtra(Utils.USERNAME);
        String incomming = getIntent().getStringExtra(Utils.INCOMING);
        createBy = getIntent().getStringExtra(Utils.CREATEBY);

//        friendsUsername = "";
//
//        if (incomming.equalsIgnoreCase(friendsUsername))
        friendsUsername = incomming;

        setupWebView();

        binding.micBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAudio = !isAudio;
                callJavaScriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                if (isAudio) {
                    binding.micBtn.setImageResource(R.drawable.btn_unmute_normal);
                } else {
                    binding.micBtn.setImageResource(R.drawable.btn_mute_normal);
                }
            }
        });

        binding.videoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isVideo = !isVideo;
                callJavaScriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
                if (isAudio) {
                    binding.videoBtn.setImageResource(R.drawable.btn_video_normal);
                } else {
                    binding.videoBtn.setImageResource(R.drawable.btn_video_muted);
                }
            }
        });

        binding.endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    void setupWebView() {
        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }

    private void initializePeer() {
        uniqueId = getUniqueId();
        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        if (createBy.equalsIgnoreCase(userName)) {

            if (pageExit)
                return;

            firebaseRef.child(userName).child(Utils.CONNID).setValue(uniqueId);
            firebaseRef.child(userName).child(Utils.ISAVAILABLE).setValue(true);

            binding.loadingGroup.setVisibility(View.GONE);
            binding.controls.setVisibility(View.VISIBLE);

            FirebaseDatabase.getInstance().getReference().child(Utils.PROFILES)
                    .child(friendsUsername)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);

                            Glide.with(CallActivity.this).load(user.getProfile()).into(binding.profile);
                            binding.name.setText(user.getName());
                            binding.city.setText(user.getCity());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createBy;
                    FirebaseDatabase.getInstance().getReference()
                            .child(Utils.USERS)
                            .child(friendsUsername)
                            .child(Utils.CONNID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                }
            }, 2000);
        }
    }

    public void onPeerConnected() {
        isPeerConnected = true;
    }

    private void sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, R.string.internet, Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();
    }

    void listenConnId() {
        firebaseRef.child(friendsUsername).child(Utils.CONNID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.getValue() == null)
                    return;

                binding.loadingGroup.setVisibility(View.GONE);
                binding.controls.setVisibility(View.VISIBLE);
                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\"" + connId + "\")");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    void callJavaScriptFunction(String function) {
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(function, null);
            }
        });
    }

    String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        pageExit = true;

        firebaseRef.child(createBy).setValue(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pageExit = true;

        firebaseRef.child(createBy).setValue(null);
        finish();
    }
}