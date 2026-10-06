package com.fongmi.android.tv.ui.custom;

import android.content.Context;
import android.util.AttributeSet;

import java.net.URI;

import okhttp3.OkHttpClient;

public class PlayerView extends androidx.media3.ui.PlayerView {

    public PlayerView(Context context) {
        super(context);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void sendDanmaku(String text) {
    }

    public void setDanmakuConfig(Object config) {
    }

    public void setDanmakuEnabled(boolean enabled) {
    }

    public void setDanmakuSource(URI uri) {
    }

    public void setDanmakuOkHttpClient(OkHttpClient client) {
    }

    public boolean isDebugViewVisible() {
        return false;
    }

    public void toggleDebugView() {
    }

    public void hideDebugView() {
    }

    public void setRender(int render) {
    }
}