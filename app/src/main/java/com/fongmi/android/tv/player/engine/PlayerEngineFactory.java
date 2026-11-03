package com.fongmi.android.tv.player.engine;

import androidx.media3.common.Player;

import com.fongmi.android.tv.player.exo.ExoPlayerEngine;
import com.fongmi.android.tv.player.media.PlaySpec;
import com.fongmi.android.tv.player.mpv.MpvPlayerEngine;
import com.fongmi.android.tv.setting.PlayerSetting;

public final class PlayerEngineFactory {

    private static PlayerEngine.Type desiredType() {
        return PlayerSetting.getEngine() == PlayerSetting.ENGINE_MPV && MpvPlayerEngine.isAvailable() ? PlayerEngine.Type.MPV : PlayerEngine.Type.EXO;
    }

    public static PlayerEngine create(int decode, Player.Listener listener) {
        return desiredType() == PlayerEngine.Type.MPV ? new MpvPlayerEngine(decode, listener) : new ExoPlayerEngine(decode, listener);
    }

    public static PlayerEngine create(int decode, PlaySpec spec, Player.Listener listener) {
        return create(decode, listener);
    }

    public static boolean matches(PlayerEngine engine, PlaySpec spec) {
        return engine != null && engine.getType() == desiredType();
    }
}
