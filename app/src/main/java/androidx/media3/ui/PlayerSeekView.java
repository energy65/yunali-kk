package androidx.media3.ui;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.util.Util;

import java.util.Formatter;
import java.util.Locale;

public class PlayerSeekView extends LinearLayout implements Player.Listener {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder formatBuilder = new StringBuilder();
    private final Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
    private DefaultTimeBar timeBar;
    private TextView positionText;
    private TextView durationText;
    private Player player;
    private boolean isScrubbing;

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateProgress();
            handler.postDelayed(this, 1000);
        }
    };

    public PlayerSeekView(Context context) {
        super(context);
        init(context);
    }

    public PlayerSeekView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PlayerSeekView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        positionText = new TextView(context);
        positionText.setTextColor(Color.WHITE);
        positionText.setTextSize(13);
        positionText.setId(R.id.exo_position);
        addView(positionText, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));

        timeBar = new DefaultTimeBar(context, null);
        timeBar.setId(R.id.exo_progress);
        LayoutParams barParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        barParams.setMargins(12, 0, 12, 0);
        addView(timeBar, barParams);

        timeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(@NonNull TimeBar timeBar, long position) {
                isScrubbing = true;
            }

            @Override
            public void onScrubMove(@NonNull TimeBar timeBar, long position) {
                isScrubbing = true;
            }

            @Override
            public void onScrubStop(@NonNull TimeBar timeBar, long position, boolean canceled) {
                isScrubbing = false;
            }
        });

        durationText = new TextView(context);
        durationText.setTextColor(Color.WHITE);
        durationText.setTextSize(13);
        durationText.setId(R.id.exo_duration);
        addView(durationText, new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0));
    }

    public void setPlayer(@Nullable Player player) {
        if (this.player != null) {
            this.player.removeListener(this);
            stopUpdate();
        }
        this.player = player;
        if (player != null) {
            player.addListener(this);
            startUpdate();
        } else {
            positionText.setText("00:00");
            durationText.setText("00:00");
            timeBar.setDuration(0);
            timeBar.setPosition(0);
        }
    }

    public TimeBar getTimeBar() {
        return timeBar;
    }

    private void startUpdate() {
        handler.post(updateRunnable);
    }

    private void stopUpdate() {
        handler.removeCallbacks(updateRunnable);
    }

    private void updateProgress() {
        if (player == null) return;
        long position = player.getCurrentPosition();
        long duration = player.getDuration();
        positionText.setText(Util.getStringForTime(formatBuilder, formatter, position));
        if (duration != C.TIME_UNSET) {
            durationText.setText(Util.getStringForTime(formatBuilder, formatter, duration));
            timeBar.setDuration(duration);
        } else {
            durationText.setText("00:00");
            timeBar.setDuration(0);
        }
        if (!isScrubbing) {
            timeBar.setPosition(position);
        }
        long bufferedPosition = player.getBufferedPosition();
        timeBar.setBufferedPosition(bufferedPosition);
    }

    @Override
    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
        if (events.containsAny(Player.EVENT_PLAYBACK_STATE_CHANGED, Player.EVENT_IS_PLAYING_CHANGED, Player.EVENT_POSITION_DISCONTINUITY, Player.EVENT_TIMELINE_CHANGED)) {
            updateProgress();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (player != null) startUpdate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopUpdate();
    }
}
