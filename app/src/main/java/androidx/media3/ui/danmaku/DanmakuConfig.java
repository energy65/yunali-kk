package androidx.media3.ui.danmaku;

public class DanmakuConfig {
    public static final int STYLE_NONE = 0;
    public static final int STYLE_SHADOW = 1;
    public static final int STYLE_STROKE = 2;
    public static final int STYLE_PROJECTION = 3;
    public static final int COLOR_MODE_DEFAULT = 0;
    public static final int COLOR_MODE_COLORFUL = 1;
    public static final int COLOR_MODE_GRADIENT = 2;
    public static final DanmakuConfig DEFAULT = new DanmakuConfig();
    public float textScale = 1f;
    public float transparency = 1f;
    public boolean textBold = false;
    public int styleMode = STYLE_STROKE;
    public float shadowTransparency = 1f;
    public float strokeWidthMultiplier = 1f;
    public float projectionOffsetXMultiplier = 0f;
    public float projectionOffsetYMultiplier = 0f;
    public float projectionTransparency = 1f;
    public int colorMode = COLOR_MODE_DEFAULT;
    public long durationMs = 0;
    public long fixedDurationMs = 0;
    public long timeOffsetMs = 0;
    public int maxOnScreen = 0;
    public float scrollAreaRatio = 0f;
    public float scrollGapRatio = 0f;
    public float lineSpacing = 0f;
    public int maxScrollLines = 0;
    public int maxTopLines = 0;
    public int maxBottomLines = 0;
    public boolean showScroll = true;
    public boolean showTop = true;
    public boolean showBottom = true;
    public boolean showReverse = true;
    public boolean showPositioned = true;
    public boolean showSubtitle = true;
    public boolean showSpecial = true;
    public static Builder builder() { return new Builder(); }
    public boolean isEnabled() { return false; }
    public float getSize() { return 1f; }
    public float getSpeed() { return 1f; }
    public float getOpacity() { return 1f; }
    public static class Builder {
        public Builder setEnabled(boolean v) { return this; }
        public Builder setSize(float v) { return this; }
        public Builder setSpeed(float v) { return this; }
        public Builder setOpacity(float v) { return this; }
        public Builder setTextScale(float v) { return this; }
        public Builder setTransparency(float v) { return this; }
        public Builder setTextBold(boolean v) { return this; }
        public Builder setStyleMode(int v) { return this; }
        public Builder setShadowTransparency(float v) { return this; }
        public Builder setStrokeWidthMultiplier(float v) { return this; }
        public Builder setProjectionOffsetXMultiplier(float v) { return this; }
        public Builder setProjectionOffsetYMultiplier(float v) { return this; }
        public Builder setProjectionTransparency(float v) { return this; }
        public Builder setColorMode(int v) { return this; }
        public Builder setDurationMs(long v) { return this; }
        public Builder setFixedDurationMs(long v) { return this; }
        public Builder setTimeOffsetMs(long v) { return this; }
        public Builder setTextSize(float v) { return this; }
        public Builder setTextColor(int v) { return this; }
        public Builder setOutlineColor(int v) { return this; }
        public Builder setOutlineWidth(float v) { return this; }
        public Builder setShadowColor(int v) { return this; }
        public Builder setShadowRadius(float v) { return this; }
        public Builder setShowPositioned(boolean v) { return this; }
        public Builder setShowSubtitle(boolean v) { return this; }
        public Builder setShowSpecial(boolean v) { return this; }
        public Builder setMaxOnScreen(int v) { return this; }
        public Builder setScrollAreaRatio(float v) { return this; }
        public Builder setScrollGapRatio(float v) { return this; }
        public Builder setLineSpacing(float v) { return this; }
        public Builder setMaxScrollLines(int v) { return this; }
        public Builder setMaxTopLines(int v) { return this; }
        public Builder setMaxBottomLines(int v) { return this; }
        public Builder setShowScroll(boolean v) { return this; }
        public Builder setShowTop(boolean v) { return this; }
        public Builder setShowBottom(boolean v) { return this; }
        public Builder setShowReverse(boolean v) { return this; }
        public DanmakuConfig build() { return new DanmakuConfig(); }
    }
}