package com.fongmi.android.tv.ui.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.SweepGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class ColorPickerView extends View {

    private Paint huePaint;
    private Paint indicatorPaint;
    private Paint ringPaint;
    private Paint previewPaint;
    private Bitmap hueBitmap;
    private int centerX, centerY, radius;
    private float hue = 0f;
    private float saturation = 1f;
    private int listenerColor = -1;
    private OnColorChangeListener listener;

    public interface OnColorChangeListener {
        void onColorChanged(int color);
    }

    public ColorPickerView(Context context) {
        this(context, null);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        huePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        huePaint.setStyle(Paint.Style.FILL);
        indicatorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(Color.WHITE);
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setColor(Color.WHITE);
        ringPaint.setStrokeWidth(4f);
        previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        invalidate();
    }

    public int getColor() {
        float[] hsv = new float[]{hue, saturation, 1f};
        return Color.HSVToColor(hsv);
    }

    public float getHue() {
        return hue;
    }

    public float getSaturation() {
        return saturation;
    }

    public void setBrightness(float brightness) {
        // handled externally, brightness slider
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2;
        centerY = h / 2;
        radius = Math.min(w, h) / 2 - 10;
        createHueBitmap();
    }

    private void createHueBitmap() {
        if (radius <= 0) return;
        if (hueBitmap != null && !hueBitmap.isRecycled()) hueBitmap.recycle();
        hueBitmap = Bitmap.createBitmap(radius * 2, radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(hueBitmap);
        int[] colors = new int[7];
        for (int i = 0; i < 7; i++) colors[i] = Color.HSVToColor(new float[]{i * 60f, saturation, 1f});
        SweepGradient sweep = new SweepGradient(radius, radius, colors, null);
        RadialGradient radial = new RadialGradient(radius, radius, radius, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP);
        huePaint.setShader(sweep);
        canvas.drawCircle(radius, radius, radius, huePaint);
        huePaint.setShader(null);
        Paint whitePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whitePaint.setShader(radial);
        whitePaint.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_ATOP));
        canvas.drawCircle(radius, radius, radius, whitePaint);
        whitePaint.setXfermode(null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hueBitmap == null) return;
        canvas.drawBitmap(hueBitmap, centerX - radius, centerY - radius, null);
        // indicator
        double angle = Math.toRadians(hue);
        float indicatorX = centerX + (float) (Math.cos(angle) * radius * saturation);
        float indicatorY = centerY + (float) (Math.sin(angle) * radius * saturation);
        canvas.drawCircle(indicatorX, indicatorY, 12f, indicatorPaint);
        canvas.drawCircle(indicatorX, indicatorY, 12f, ringPaint);
        // inner filled dot showing selected color
        previewPaint.setColor(getColor());
        canvas.drawCircle(indicatorX, indicatorY, 6f, previewPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - centerX;
                float dy = event.getY() - centerY;
                double dist = Math.sqrt(dx * dx + dy * dy);
                // clamp distance
                if (dist > radius) {
                    dx = (float) (dx * radius / dist);
                    dy = (float) (dy * radius / dist);
                    dist = radius;
                }
                saturation = radius > 0 ? (float) dist / radius : 1f;
                float angleDeg = (float) (Math.toDegrees(Math.atan2(dy, dx)));
                if (angleDeg < 0) angleDeg += 360f;
                hue = angleDeg;
                // update hue bitmap if saturation changed significantly
                float[] hsv = new float[]{hue, saturation, 1f};
                int color = Color.HSVToColor(hsv);
                if (color != listenerColor) {
                    listenerColor = color;
                    if (listener != null) listener.onColorChanged(color);
                }
                invalidate();
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }
}
