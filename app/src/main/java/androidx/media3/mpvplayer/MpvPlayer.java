package androidx.media3.mpvplayer;

import android.content.Context;
import androidx.media3.common.Player;

public abstract class MpvPlayer implements Player {
    public static boolean isAvailable() { return false; }
    public static class Builder {
        public Builder(Context context) {}
        public Builder setDecode(int v) { return this; }
        public Builder setConfig(MpvPlayerConfig v) { return this; }
        public MpvPlayer build() { return null; }
    }
    public void setSubtitleOptions(MpvPlayerConfig v) {}
    public void release() {}
    public void setMediaItem(androidx.media3.common.MediaItem v) {}
    public void setMediaItem(androidx.media3.common.MediaItem v, long p) {}
    public void prepare() {}
    public void play() {}
    public void stop() {}
    public long getDuration() { return 0; }
    public long getCurrentPosition() { return 0; }
    public int getPlaybackState() { return STATE_IDLE; }
    public androidx.media3.common.MediaItem getCurrentMediaItem() { return null; }
    public void setVideoSurface(android.view.Surface v) {}
    public void addListener(Listener v) {}
    public void removeListener(Listener v) {}
    public void seekTo(long p) {}
    public float getPlaybackSpeed() { return 1f; }
    public void setPlaybackSpeed(float v) {}
    public void setRepeatMode(int v) {}
    public int getRepeatMode() { return REPEAT_MODE_OFF; }
    public void setShuffleModeEnabled(boolean v) {}
    public boolean isShuffleModeEnabled() { return false; }
    public androidx.media3.common.Tracks getCurrentTracks() { return null; }
    public void setTrackSelectionParameters(androidx.media3.common.TrackSelectionParameters v) {}
    public androidx.media3.common.TrackSelectionParameters getTrackSelectionParameters() { return null; }
    public boolean isPlaying() { return false; }
    public void pause() {}
    public void setVolume(float v) {}
    public float getVolume() { return 1f; }
    public void setAudioSessionId(int v) {}
    public int getAudioSessionId() { return 0; }
    public boolean isCommandAvailable(Command v) { return false; }
    public void setAudioAttributes(androidx.media3.common.AudioAttributes v) {}
    public void setAudioAttributes(androidx.media3.common.AudioAttributes v, boolean h) {}
    public androidx.media3.common.AudioAttributes getAudioAttributes() { return null; }
    public void setWakeMode(int v) {}
    public int getWakeMode() { return 0; }
    public void setVideoScalingMode(int v) {}
    public int getVideoScalingMode() { return 0; }
    public androidx.media3.common.VideoSize getVideoSize() { return null; }
    public long getBufferedPosition() { return 0; }
    public void seekToNextMediaItem() {}
    public void seekToPreviousMediaItem() {}
    public void seekTo(int i, long p) {}
    public void addMediaItem(androidx.media3.common.MediaItem v) {}
    public void addMediaItem(int i, androidx.media3.common.MediaItem v) {}
    public void removeMediaItem(int i) {}
    public void removeMediaItems(int s, int e) {}
    public void moveMediaItem(int c, int n) {}
    public void replaceMediaItem(int i, androidx.media3.common.MediaItem v) {}
    public void setMediaItems(java.util.List<androidx.media3.common.MediaItem> v) {}
    public void setMediaItems(java.util.List<androidx.media3.common.MediaItem> v, int i, long p) {}
    public int getMediaItemCount() { return 0; }
    public androidx.media3.common.MediaItem getMediaItemAt(int i) { return null; }
    public int getCurrentMediaItemIndex() { return 0; }
    public void setPlayWhenReady(boolean v) {}
    public boolean getPlayWhenReady() { return false; }
    public void reset() {}
    public void clearVideoSurface() {}
    public void addSubtitle(androidx.media3.common.MediaItem.SubtitleConfiguration v) {}
    public void setDeviceMuted(boolean v) {}
    public void setDeviceMuted(boolean v, int i) {}
    public java.util.List<androidx.media3.common.MediaChapter> getCurrentMediaChapters() { return java.util.Collections.emptyList(); }
    public java.util.List<androidx.media3.common.MediaEdition> getCurrentMediaEditions() { return java.util.Collections.emptyList(); }
    public void selectChapter(androidx.media3.common.MediaChapter v) {}
    public void selectEdition(androidx.media3.common.MediaEdition v) {}
    public void setDecode(int v) {}
    public long getAudioOffsetMs() { return 0; }
    public void setAudioOffsetMs(long v) {}
    public long getSubtitleOffsetMs() { return 0; }
    public void setSubtitleOffsetMs(long v) {}
    public void decreaseDeviceVolume() {}
    public void increaseDeviceVolume() {}
    public void decreaseDeviceVolume(int v) {}
    public void increaseDeviceVolume(int v) {}
    public long getTextOffsetMs() { return 0; }
    public void setTextOffsetMs(long v) {}
    public void setDeviceVolume(int v1, int v2) {}
    public void setDeviceVolume(int v) {}
    public boolean isDeviceMuted() { return false; }
    public int getDeviceVolume() { return 0; }
}