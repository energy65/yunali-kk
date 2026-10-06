package androidx.media3.exoplayer.source.preload;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PriorityTaskManager;
import androidx.media3.exoplayer.ExoPlayer;

public class DiskPreloadManager {
    public static class Builder {
        public Builder(androidx.media3.datasource.cache.Cache c, androidx.media3.datasource.DataSource.Factory f, androidx.media3.exoplayer.RenderersFactory r) {}
        public Builder setPriorityTaskManager(PriorityTaskManager v) { return this; }
        public DiskPreloadManager build() { return new DiskPreloadManager(); }
    }
    public void start(ExoPlayer p, MediaItem m, Options o) {}
    public void release() {}
    public static class Options {
        public static Builder builder() { return new Builder(); }
        public static class Builder {
            public Builder setDurationMs(long v) { return this; }
            public Builder setMaxThreads(int v) { return this; }
            public Options build() { return new Options(); }
        }
    }
}