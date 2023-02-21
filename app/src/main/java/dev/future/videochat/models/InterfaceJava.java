package dev.future.videochat.models;

import android.telecom.Call;
import android.webkit.JavascriptInterface;

import dev.future.videochat.activities.CallActivity;

public class InterfaceJava {

    CallActivity callActivity;

    public InterfaceJava(CallActivity callActivity){
        this.callActivity= callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
            callActivity.onPeerConnected();
    }
}
