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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.WallConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.ActivityThemeSettingBinding;
import com.fongmi.android.tv.databinding.DialogColorPickerBinding;
import com.fongmi.android.tv.databinding.DialogConfigPushBinding;
import com.fongmi.android.tv.databinding.DialogManageConfigBinding;
import com.fongmi.android.tv.event.ConfigEvent;
import com.fongmi.android.tv.event.RefreshEvent;
import com.fongmi.android.tv.event.ServerEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.setting.Setting;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.ui.custom.ColorPickerView;
import com.fongmi.android.tv.ui.custom.CustomWallView;
import com.fongmi.android.tv.utils.ConfigPushHelper;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ThemeSettingActivity extends BaseActivity {

    private static final int[] THEME_COLORS = {-1, 0, 0xFF6750A4, 0xFF3949AB, 0xFF1E88E5, 0xFF00ACC1, 0xFF00897B, 0xFF43A047, 0xFF7CB342, 0xFFFB8C00, 0xFFE53935, 0xFFD81B60, 0xFF8E24AA, 0xFF6D4C41};

    private ActivityThemeSettingBinding mBinding;
    private DialogManageConfigBinding dialogBinding;
    private DialogConfigPushBinding pushBinding;
    private AlertDialog dialog;
    private boolean isTv;
    private List<TextView> wallChecks = new ArrayList<>();
    private List<TextView> themeChecks = new ArrayList<>();
    private List<View> wallCells = new ArrayList<>();
    private List<View> themeCells = new ArrayList<>();
    private int focusThemeIndex = -1;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, ThemeSettingActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityThemeSettingBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        isTv = TextUtils.equals("leanback", BuildConfig.FLAVOR_mode);
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
        mBinding.wallColor.setOnClickListener(v -> showWallColorPicker());
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
        wallChecks = new ArrayList<>();
        int columns = getColumns();
        int size = ResUtil.dp2px(columns >= 8 ? 64 : 52);
        int gap = ResUtil.dp2px(8);
        int selected = Setting.getWall();
        List<View> cells = new ArrayList<>();
        for (int i = 1; i < CustomWallView.WALL_COLORS.length; i++) {
            FrameLayout frame = new FrameLayout(this);
            frame.setId(View.generateViewId());
            setCellFocusable(frame);
            ImageView image = new ImageView(this);
            image.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            int resId = getResources().getIdentifier("wallpaper_" + i, "drawable", getPackageName());
            if (resId != 0) image.setImageResource(resId);
            else image.setBackgroundColor(CustomWallView.WALL_COLORS[i]);
            frame.addView(image);
            frame.addView(createFocusOverlay());
            TextView check = createCheck();
            frame.addView(check);
            final int index = i;
            frame.setOnClickListener(v -> selectWall(index));
            cells.add(frame);
            wallChecks.add(check);
        }
        layoutGrid(mBinding.gridWall, cells, columns, size, gap, R.id.back, new int[]{R.id.wallRefresh, R.id.wallCustom, R.id.wallColor});
        wallCells = cells;
        updateWallChecks(selected);
        wireAnchors();
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
        themeChecks = new ArrayList<>();
        int columns = getColumns();
        int size = ResUtil.dp2px(columns >= 8 ? 56 : 46);
        int gap = ResUtil.dp2px(10);
        int selected = Setting.getThemeColor();
        List<View> cells = new ArrayList<>();
        for (int color : THEME_COLORS) {
            FrameLayout frame = new FrameLayout(this);
            frame.setId(View.generateViewId());
            setCellFocusable(frame);
            View swatch = new View(this);
            int margin = ResUtil.dp2px(3);
            FrameLayout.LayoutParams swatchParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            swatchParams.setMargins(margin, margin, margin, margin);
            swatch.setLayoutParams(swatchParams);
            swatch.setBackground(roundRect(color, color == selected));
            frame.addView(swatch);
            frame.addView(createFocusOverlay());
            TextView check = createCheck();
            if (color != selected) check.setVisibility(View.GONE);
            frame.addView(check);
            final int value = color;
            frame.setOnClickListener(v -> selectTheme(value));
            cells.add(frame);
            themeChecks.add(check);
        }
        layoutGrid(mBinding.gridTheme, cells, columns, size, gap, R.id.wallColor, new int[]{R.id.customTheme});
        themeCells = cells;
        if (focusThemeIndex >= 0 && focusThemeIndex < cells.size()) {
            View target = cells.get(focusThemeIndex);
            mBinding.gridTheme.post(target::requestFocus);
        }
        focusThemeIndex = -1;
        wireAnchors();
    }

    private void setCellFocusable(View view) {
        view.setClickable(true);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
    }

    /**
     * 焦点高亮必须盖在图片之上，否则会被铺满的缩略图遮住，遥控器移动焦点时看不到任何变化。
     */
    private View createFocusOverlay() {
        View overlay = new View(this);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setBackgroundResource(R.drawable.selector_manage_item);
        overlay.setDuplicateParentStateEnabled(true);
        overlay.setClickable(false);
        overlay.setFocusable(false);
        return overlay;
    }

    private void wireAnchors() {
        int columns = getColumns();
        if (!wallCells.isEmpty()) {
            mBinding.back.setNextFocusDownId(wallCells.get(0).getId());
            wireUp(mBinding.wallRefresh, wallCells, columns, 0);
            wireUp(mBinding.wallCustom, wallCells, columns, 1);
            wireUp(mBinding.wallColor, wallCells, columns, 2);
        }
        if (!themeCells.isEmpty()) {
            mBinding.customTheme.setNextFocusUpId(themeCells.get(themeCells.size() - 1).getId());
            wireDown(mBinding.wallRefresh, themeCells, columns, 0);
            wireDown(mBinding.wallCustom, themeCells, columns, 1);
            wireDown(mBinding.wallColor, themeCells, columns, 2);
        }
    }

    private void wireUp(View anchor, List<View> cells, int columns, int col) {
        int lastRowStart = ((cells.size() - 1) / columns) * columns;
        anchor.setNextFocusUpId(cells.get(Math.min(lastRowStart + col, cells.size() - 1)).getId());
    }

    private void wireDown(View anchor, List<View> cells, int columns, int col) {
        anchor.setNextFocusDownId(cells.get(Math.min(col, cells.size() - 1)).getId());
    }

    /**
     * 以横向行布局排列单元格，并显式指定遥控器上下左右焦点跳转，避免 GridLayout 无法用遥控器导航。
     */
    private void layoutGrid(LinearLayout container, List<View> cells, int columns, int size, int gap, int topAnchor, int[] bottomAnchors) {
        int count = cells.size();
        int lastRowStart = columns > 0 ? ((count - 1) / columns) * columns : 0;
        LinearLayout row = null;
        for (int i = 0; i < count; i++) {
            int col = i % columns;
            if (col == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                container.addView(row);
            }
            View cell = cells.get(i);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 0, gap, gap);
            cell.setLayoutParams(params);
            row.addView(cell);
        }
        for (int i = 0; i < count; i++) {
            int col = i % columns;
            View cell = cells.get(i);
            if (col > 0) cell.setNextFocusLeftId(cells.get(i - 1).getId());
            if (col + 1 < columns && i + 1 < count) cell.setNextFocusRightId(cells.get(i + 1).getId());
            cell.setNextFocusUpId(i - columns >= 0 ? cells.get(i - columns).getId() : topAnchor);
            if (i + columns < count) {
                cell.setNextFocusDownId(cells.get(i + columns).getId());
            } else if (i >= lastRowStart && bottomAnchors.length > 0) {
                cell.setNextFocusDownId(bottomAnchors[Math.min(col, bottomAnchors.length - 1)]);
            }
        }
    }

    private void selectTheme(int color) {
        Setting.putThemeColor(color);
        RefreshEvent.theme();
        focusThemeIndex = indexOfTheme(color);
        buildThemeGrid();
    }

    private int indexOfTheme(int color) {
        for (int i = 0; i < THEME_COLORS.length; i++) if (THEME_COLORS[i] == color) return i;
        return -1;
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
        if (Setting.getWallType() == 3) desc = getString(R.string.wall_custom_color);
        else if (Setting.getWall() == 0) desc = TextUtils.isEmpty(WallConfig.getDesc()) ? getString(R.string.wall_custom) : WallConfig.getDesc();
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
        refreshDynamicColor();
        dialogBinding = DialogManageConfigBinding.inflate(getLayoutInflater());
        dialogBinding.nameLayout.setVisibility(View.GONE);
        dialogBinding.url.setText(WallConfig.getUrl());
        dialogBinding.choose.setOnClickListener(v -> PermissionUtil.requestFile(this, allGranted -> FileChooser.from(launcher).show()));
        dialogBinding.scan.setOnClickListener(v -> {
            String url = dialogBinding.url.getText().toString().trim();
            dismissDialog();
            showPushDialog(url);
        });
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

    private void showPushDialog(String initialUrl) {
        refreshDynamicColor();
        pushBinding = DialogConfigPushBinding.inflate(getLayoutInflater());
        pushBinding.name.setVisibility(View.GONE);
        pushBinding.url.setText(initialUrl);
        pushBinding.received.setText(TextUtils.isEmpty(initialUrl) ? getString(R.string.push_received) : initialUrl);
        pushBinding.choose.setOnClickListener(v -> PermissionUtil.requestFile(this, allGranted -> FileChooser.from(launcher).show()));
        ConfigPushHelper.ensureServerStarted();
        pushBinding.code.setImageBitmap(ConfigPushHelper.generateConfigQr(ConfigPushHelper.getConfigPushAddress()));
        pushBinding.info.setText(ResUtil.getString(R.string.push_info, ConfigPushHelper.getServerAddress()).replace("\uff0c", "\n"));
        dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.push_link)
                .setView(pushBinding.getRoot())
                .setPositiveButton(R.string.dialog_positive, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> onSaveCustom()));
        dialog.show();
    }

    private void onSaveCustom() {
        if (pushBinding != null) {
            String url = pushBinding.url.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                Notify.show(R.string.config_url_empty);
                return;
            }
            Setting.putWall(0);
            Setting.putWallType(0);
            dismissDialog();
            Config target = Config.find(url, 2);
            target.update();
            WallConfig.load(target, getCallback());
            return;
        }
        String url = dialogBinding.url.getText().toString().trim();
        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.config_url_empty);
            return;
        }
        Setting.putWall(0);
        Setting.putWallType(0);
        dismissDialog();
        Config target = Config.find(url, 2);
        target.update();
        WallConfig.load(target, getCallback());
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
        pushBinding = null;
    }

    private void showColorPicker() {
        int color = Setting.getThemeColor();
        showColorPicker(color <= 0 ? 0xFF6750A4 : color, newColor -> {
            Setting.putThemeColor(newColor);
            RefreshEvent.theme();
            buildThemeGrid();
        });
    }

    private void showWallColorPicker() {
        showColorPicker(Setting.getWallColorCustom(), newColor -> {
            Setting.putWallColorCustom(newColor);
            Setting.putWall(0);
            Setting.putWallType(3);
            ConfigEvent.wall();
            buildWallGrid();
            updateWallDesc();
        });
    }

    private void showColorPicker(int initialColor, OnColorPicked listener) {
        refreshDynamicColor();
        final int[] currentColor = {initialColor};
        DialogColorPickerBinding pBinding = DialogColorPickerBinding.inflate(getLayoutInflater());
        float[] hsv = new float[3];
        Color.colorToHSV(initialColor, hsv);
        int brightness = Math.max(1, Math.round(hsv[2] * 100));
        pBinding.picker.setColor(initialColor);
        applySwatch(pBinding.initialColor, initialColor);
        applySwatch(pBinding.currentColor, initialColor);
        updateBrightnessTrack(pBinding.brightness, initialColor);
        pBinding.brightness.setProgress(brightness);
        pBinding.brightnessLabel.setText(getString(R.string.theme_brightness, brightness));

        pBinding.picker.setOnColorChangeListener(color -> {
            updateBrightnessTrack(pBinding.brightness, color);
            currentColor[0] = composeColor(color, pBinding.brightness.getProgress());
            applySwatch(pBinding.currentColor, currentColor[0]);
        });

        pBinding.brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pBinding.brightnessLabel.setText(getString(R.string.theme_brightness, progress));
                currentColor[0] = composeColor(pBinding.picker.getColor(), progress);
                applySwatch(pBinding.currentColor, currentColor[0]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        AlertDialog colorDialog = new MaterialAlertDialogBuilder(this)
                .setView(pBinding.getRoot())
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.dialog_positive, null)
                .create();
        Runnable apply = () -> {
            listener.onPicked(currentColor[0]);
            colorDialog.dismiss();
        };
        // 遥控器在选择器上按确定键，直接应用当前颜色并关闭弹窗
        pBinding.picker.setOnClickListener(v -> apply.run());
        colorDialog.setOnShowListener(d -> colorDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> apply.run()));
        colorDialog.show();
    }

    private interface OnColorPicked {
        void onPicked(int color);
    }

    private int composeColor(int pickerColor, int progress) {
        float[] hsv = new float[3];
        Color.colorToHSV(pickerColor, hsv);
        hsv[2] = Math.max(1, Math.min(100, progress)) / 100f;
        return Color.HSVToColor(hsv);
    }

    private void updateBrightnessTrack(SeekBar seekBar, int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] = 1f;
        GradientDrawable track = new GradientDrawable();
        track.setShape(GradientDrawable.RECTANGLE);
        track.setCornerRadius(ResUtil.dp2px(5));
        track.setColor(Color.HSVToColor(hsv));
        track.setSize(getResources().getDisplayMetrics().widthPixels, ResUtil.dp2px(10));
        seekBar.setProgressDrawable(track);
    }

    private void applySwatch(View view, int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(ResUtil.dp2px(8));
        drawable.setColor(color);
        drawable.setStroke(ResUtil.dp2px(1), ContextCompat.getColor(this, R.color.white_20));
        view.setBackground(drawable);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerEvent(ServerEvent event) {
        if (event.type() != ServerEvent.Type.SETTING) return;
        if (pushBinding != null && !TextUtils.isEmpty(event.text())) {
            pushBinding.url.setText(event.text());
            pushBinding.received.setText(event.text());
        } else if (dialogBinding != null && !TextUtils.isEmpty(event.text())) {
            dialogBinding.url.setText(event.text());
        }
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null || result.getData().getData() == null) return;
        String path = FileChooser.getPathFromUri(result.getData().getData());
        if (TextUtils.isEmpty(path)) return;
        String url = "file:/" + path.replace(Path.rootPath(), "");
        if (pushBinding != null) {
            pushBinding.url.setText(url);
            pushBinding.received.setText(url);
        } else if (dialogBinding != null) {
            dialogBinding.url.setText(url);
        }
    });
}
