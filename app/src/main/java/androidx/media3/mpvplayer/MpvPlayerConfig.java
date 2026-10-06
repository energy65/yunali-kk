package androidx.media3.mpvplayer;

import android.content.Context;
import java.io.File;

public class MpvPlayerConfig {
    public static final String VIDEO_OUTPUT_GPU_NEXT = "gpu-next";
    public static class Builder {
        public Builder setDefaultUserAgent(String v) { return this; }
        public Builder setHlsHttpPersistent(boolean v) { return this; }
        public Builder addConfigDirectory(File v) { return this; }
        public Builder addAndroidFontConfig(File c, File d) { return this; }
        public Builder addAndroidDefaults(String d, File c) { return this; }
        public Builder addTlsCaFileFromAsset(Context c, String a, File o) { return this; }
        public Builder addPostInitStringOption(String k, String v) { return this; }
        public Builder addPreInitStringOption(String k, String v) { return this; }
        public Builder addDiskCacheOptions(File c, int t, int s) { return this; }
        public Builder addAndroidSubtitleOptions(Context c, boolean cap, double pos, double scale) { return this; }
        public MpvPlayerConfig build() { return new MpvPlayerConfig(); }
    }
}