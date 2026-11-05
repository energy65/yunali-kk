package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.ActivityThemeSettingBinding;
import com.fongmi.android.tv.databinding.DialogColorPickerBinding;
import com.fongmi.android.tv.databinding.DialogManageConfigBinding;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.ColorPickerView;
import com.fongmi.android.tv.ui.custom.CustomWallView;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ThemeSettingActivity extends BaseActivity {

    private static final int[] THEME_COLORS = {-1, 0, 0xFF6750A4, 0xFF3949AB, 0xFF1E88E5, 0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFFB8C00, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF6D4C41};

    private ActivityThemeSettingBinding mBinding;
    private DialogManageConfigBinding dialogBinding;
    private AlertDialog dialog;
    private List<TextView> wallChecks = new ArrayList<>();
    private List<TextView> themeChecks = new ArrayList<>();

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, ThemeSettingActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityThemeSettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        mBinding.back.requestFocus();
        buildWallGrid();
        buildThemeGrid();
        updateWallDesc();
    }

    @Override
    protected void initEvent() {
        mBinding.back.setOnClickListener(v -> finish());
        mBinding.wallRefresh.setOnClickListener(v -> refreshWall());
        mBinding.wallCustom.setOnClickListener(v -> showCustomDialog());
        mBinding.customTheme.setOnClickListener(v -> showColorPicker());
    }

    private int getColumns() {
        int width = getResources().getConfiguration().screenWidthDp;
        if (width >= 720) return 8;
        if (width >= 480) return 6;
        return 4;
    }

    private void buildWallGrid() {
        mBinding.gridWall.removeAllViews();
        mBinding.gridWall.setColumnCount(getColumns());
        wallChecks = new ArrayList<>();
        int size = ResUtil.dp2px(getColumns() >= 8 ? 64 : 52);
        int gap = ResUtil.dp2px(8);
        int selected = Setting.getWall();
        for (int i = 1; i < CustomWallView.WALL_COLORS.length; i++) {
            FrameLayout frame = new FrameLayout(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(0, 0, gap, gap);
            frame.setLayoutParams(params);
            frame.setBackgroundResource(R.drawable.selector_manage_item);
            frame.setClickable(true);
            frame.setFocusable(true);
            frame.setFocusableInTouchMode(true);
            ImageView image = new ImageView(this);
            image.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int resId = getResources().getIdentifier("wallpaper_" + i, "drawable", getPackageName());
            if (resId != 0) image.setImageResource(resId);
            else image.setBackgroundColor(CustomWallView.WALL_COLORS[i]);
            frame.addView(image);
            TextView check = createCheck();
            frame.addView(check);
            final int index = i;
            frame.setOnClickListener(v -> selectWall(index));
            mBinding.gridWall.addView(frame);
            wallChecks.add(check);
        }
        updateWallChecks(selected);
    }

    private void updateWallChecks(int selected) {
        for (int i = 0; i < wallChecks.size(); i++) wallChecks.get(i).setVisibility(selected == i + 1 ? View.VISIBLE : View.GONE);
    }

    private void selectWall(int index) {
        Setting.putWall(index);
        Setting.putWallType(0);
        ConfigEvent.wall();
        updateWallChecks(index);
        updateWallDesc();
    }

    private void buildThemeGrid() {
        mBinding.gridTheme.removeAllViews();
        mBinding.gridTheme.setColumnCount(getColumns());
        themeChecks = new ArrayList<>();
        int size = ResUtil.dp2px(getColumns() >= 8 ? 56 : 46);
        int gap = ResUtil.dp2px(10);
        int selected = Setting.getThemeColor();
        for (int color : THEME_COLORS) {
            FrameLayout frame = new FrameLayout(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(0, 0, gap, gap);
            frame.setLayoutParams(params);
            frame.setBackgroundResource(R.drawable.selector_manage_item);
            frame.setClickable(true);
            frame.setFocusable(true);
            frame.setFocusableInTouchMode(true);
            View swatch = new View(this);
            int margin = ResUtil.dp2px(3);
            FrameLayout.LayoutParams swatchParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            swatchParams.setMargins(margin, margin, margin, margin);
            swatch.setLayoutParams(swatchParams);
            swatch.setBackground(roundRect(color, color == selected));
            frame.addView(swatch);
            TextView check = createCheck();
            if (color != selected) check.setVisibility(View.GONE);
            frame.addView(check);
            final int value = color;
            frame.setOnClickListener(v -> selectTheme(value));
            mBinding.gridTheme.addView(frame);
            themeChecks.add(check);
        }
    }

    private void selectTheme(int color) {
        Setting.putThemeColor(color);
        RefreshEvent.theme();
        buildThemeGrid();
    }

    private GradientDrawable roundRect(int color, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(ResUtil.dp2px(10));
        drawable.setColor(displayColor(color));
        drawable.setStroke(ResUtil.dp2px(selected ? 3 : 1), ContextCompat.getColor(this, selected ? R.color.white : R.color.white_20));
        return drawable;
    }

    private int displayColor(int color) {
        if (color == -1) return 0xFF616161;
        if (color == 0) return 0xFF90A4AE;
        return color;
    }

    private TextView createCheck() {
        TextView check = new TextView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ResUtil.dp2px(16), ResUtil.dp2px(16));
        params.gravity = Gravity.END | Gravity.TOP;
        params.setMargins(0, ResUtil.dp2px(5), ResUtil.dp2px(5), 0);
        check.setLayoutParams(params);
        check.setGravity(Gravity.CENTER);
        check.setBackgroundResource(R.drawable.bg_manage_tag);
        check.setText("✓");
        check.setTextColor(ContextCompat.getColor(this, R.color.white));
        check.setTextSize(11);
        return check;
    }

    private void updateWallDesc() {
        String desc;
        if (Setting.getWall() == 0) desc = TextUtils.isEmpty(WallConfig.getDesc()) ? getString(R.string.wall_custom) : WallConfig.getDesc();
        else desc = getString(R.string.wall_built_in) + " " + Setting.getWall();
        mBinding.wallDesc.setText(getString(R.string.config_current, desc));
    }

    private void refreshWall() {
        if (TextUtils.isEmpty(WallConfig.getUrl())) {
            Notify.show(R.string.wall_need_source);
            return;
        }
        Setting.putWall(0);
        Setting.putWallType(0);
        WallConfig.get().load(getCallback());
    }

    private void showCustomDialog() {
        dialogBinding = DialogManageConfigBinding.inflate(getLayoutInflater());
        dialogBinding.nameLayout.setVisibility(View.GONE);
        dialogBinding.url.setText(WallConfig.getUrl());
        dialogBinding.choose.setOnClickListener(v -> PermissionUtil.requestFile(this, allGranted -> FileChooser.from(launcher).show()));
        dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.wall_custom)
                .setMessage(R.string.wall_custom_tip)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.dialog_positive, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> onSaveCustom()));
        dialog.show();
    }

    private void onSaveCustom() {
        String url = dialogBinding.url.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.config_url_empty);
            return;
        }
        Setting.putWall(0);
        Setting.putWallType(0);
        Config config = Config.find(url, 2);
        config.update();
        dismissDialog();
        WallConfig.load(config, getCallback());
    }

    private Callback getCallback() {
        return new Callback() {
            @Override
            public void start() {
                Notify.progress(getActivity());
            }

            @Override
            public void success() {
                Notify.dismiss();
                buildWallGrid();
                updateWallDesc();
            }

            @Override
            public void error(String msg) {
                Notify.dismiss();
                Notify.show(msg);
            }
        };
    }

    private void dismissDialog() {
        if (dialog != null) dialog.dismiss();
        dialog = null;
        dialogBinding = null;
    }

    private void showColorPicker() {
        int initialColor = Setting.getThemeColor();
        if (initialColor < 0) initialColor = 0xFF6750A4;
        final int[] currentColor = {initialColor};
        DialogColorPickerBinding pBinding = DialogColorPickerBinding.inflate(getLayoutInflater());
        pBinding.picker.setColor(initialColor);
        pBinding.initialColor.setBackgroundColor(initialColor);
        pBinding.currentColor.setBackgroundColor(currentColor[0]);
        pBinding.brightness.setProgress(100);
        pBinding.brightnessLabel.setText(getString(R.string.theme_brightness, 100));

        pBinding.picker.setOnColorChangeListener(color -> {
            currentColor[0] = color;
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            int brightness = (int) (hsv[2] * 100);
            pBinding.brightness.setProgress(brightness);
            pBinding.brightnessLabel.setText(getString(R.string.theme_brightness, brightness));
            pBinding.currentColor.setBackgroundColor(color);
        });

        pBinding.brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pBinding.brightnessLabel.setText(getString(R.string.theme_brightness, progress));
                float[] hsv = new float[3];
                Color.colorToHSV(pBinding.picker.getColor(), hsv);
                hsv[2] = progress / 100f;
                currentColor[0] = Color.HSVToColor(hsv);
                pBinding.currentColor.setBackgroundColor(currentColor[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.theme_custom)
                .setView(pBinding.getRoot())
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.dialog_positive, (d, w) -> {
                    Setting.putThemeColor(currentColor[0]);
                    RefreshEvent.theme();
                    buildThemeGrid();
                })
                .show();
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        String path = FileChooser.getPathFromUri(result.getData().getData());
        if (TextUtils.isEmpty(path) || dialogBinding == null) return;
        dialogBinding.url.setText("file:/" + path.replace(Path.rootPath(), ""));
    });
}
