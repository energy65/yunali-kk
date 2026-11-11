package com.fongmi.android.tv.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.BuildConfig;
import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.LiveConfig;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.ActivityConfigManageBinding;
import com.fongmi.android.tv.databinding.DialogConfigPushBinding;
import com.fongmi.android.tv.databinding.DialogManageConfigBinding;
import com.fongmi.android.tv.databinding.ItemConfigManageBinding;
import com.fongmi.android.tv.db.AppDatabase;
import com.fongmi.android.tv.event.ServerEvent;
import com.fongmi.android.tv.impl.Callback;
import com.fongmi.android.tv.ui.base.BaseActivity;
import com.fongmi.android.tv.utils.ConfigPushHelper;
import com.fongmi.android.tv.utils.FileChooser;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.PermissionUtil;
import com.fongmi.android.tv.utils.ResUtil;
import com.github.catvod.utils.Path;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class ConfigManageActivity extends BaseActivity {

    private ActivityConfigManageBinding mBinding;
    private DialogManageConfigBinding dialogBinding;
    private DialogConfigPushBinding pushBinding;
    private AlertDialog dialog;
    private Config editing;
    private int type = 0;
    private boolean isTv;
    private final Handler pushHandler = new Handler(Looper.getMainLooper());
    private final Runnable pushTimeoutRunnable = this::onPushTimeout;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, ConfigManageActivity.class));
    }

    @Override
    protected ViewBinding getBinding() {
        return mBinding = ActivityConfigManageBinding.inflate(getLayoutInflater());
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        isTv = TextUtils.equals("leanback", BuildConfig.FLAVOR_mode);
        mBinding.back.requestFocus();
        selectTab(0);
    }

    @Override
    protected void initEvent() {
        mBinding.back.setOnClickListener(v -> finish());
        mBinding.tabVod.setOnClickListener(v -> selectTab(0));
        mBinding.tabLive.setOnClickListener(v -> selectTab(1));
        mBinding.add.setOnClickListener(v -> showEditDialog(null));
        mBinding.follow.setOnClickListener(v -> followVod());
    }

    private void selectTab(int type) {
        this.type = type;
        mBinding.tabVod.setBackgroundResource(type == 0 ? R.drawable.shape_manage_tab_active : R.drawable.shape_manage_tab_normal);
        mBinding.tabVod.setTextColor(ContextCompat.getColor(this, type == 0 ? R.color.white : R.color.white_70));
        mBinding.tabLive.setBackgroundResource(type == 1 ? R.drawable.shape_manage_tab_active : R.drawable.shape_manage_tab_normal);
        mBinding.tabLive.setTextColor(ContextCompat.getColor(this, type == 1 ? R.color.white : R.color.white_70));
        mBinding.follow.setVisibility(type == 1 ? View.VISIBLE : View.GONE);
        refreshList();
    }

    private void refreshList() {
        List<Config> items = Config.getAll(type);
        String current = type == 0 ? VodConfig.getDesc() : isFollow() ? getString(R.string.config_follow_on) : LiveConfig.getDesc();
        mBinding.current.setText(getString(R.string.config_current, TextUtils.isEmpty(current) ? getString(R.string.config_none) : current));
        mBinding.follow.setText(isFollow() ? R.string.config_follow_on : R.string.config_follow);
        mBinding.list.removeAllViews();
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.config_empty);
            empty.setTextColor(ContextCompat.getColor(this, R.color.white_70));
            empty.setTextSize(15);
            empty.setPadding(0, ResUtil.dp2px(20), 0, 0);
            mBinding.list.addView(empty);
            return;
        }
        for (Config config : items) addItem(config, TextUtils.equals(config.getUrl(), getActiveUrl()));
    }

    private void addItem(Config config, boolean active) {
        ItemConfigManageBinding item = ItemConfigManageBinding.inflate(getLayoutInflater(), mBinding.list, false);
        item.tag.setVisibility(active ? View.VISIBLE : View.GONE);
        item.name.setText(TextUtils.isEmpty(config.getName()) ? config.getUrl() : config.getName());
        item.url.setText(config.getUrl());
        item.getRoot().setOnClickListener(v -> enable(config));
        item.getRoot().setOnLongClickListener(v -> {
            showMenu(config);
            return true;
        });
        item.more.setOnClickListener(v -> showMenu(config));
        mBinding.list.addView(item.getRoot());
    }

    private void showMenu(Config config) {
        String[] actions = {getString(R.string.config_edit), getString(R.string.config_delete)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(config.getDesc())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) showEditDialog(config);
                    else confirmDelete(config);
                })
                .show();
    }

    private void confirmDelete(Config config) {
        if (TextUtils.equals(config.getUrl(), getActiveUrl())) {
            Notify.show(R.string.config_delete_active);
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.config_delete_title)
                .setMessage(getString(R.string.config_delete_message, config.getDesc()))
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.config_delete, (dialog, which) -> {
                    config.delete();
                    refreshList();
                })
                .show();
    }

    private void enable(Config config) {
        if (TextUtils.equals(config.getUrl(), getActiveUrl())) return;
        String desc = config.getDesc();
        Config target = Config.find(config.getUrl(), type);
        target.update();
        load(target, desc);
    }

    private void followVod() {
        String url = VodConfig.getUrl();
        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.config_follow_vod_tip);
            return;
        }
        if (isFollow()) return;
        Config target = Config.find(url, 1);
        target.update();
        load(target, getString(R.string.config_follow_on));
    }

    private void load(Config config, String desc) {
        Callback callback = new Callback() {
            @Override
            public void start() {
                Notify.progress(getActivity());
            }

            @Override
            public void success() {
                Notify.dismiss();
                refreshList();
                Notify.show(getString(R.string.config_switched, desc));
            }

            @Override
            public void error(String msg) {
                Notify.dismiss();
                Notify.show(msg);
            }
        };
        if (type == 0) VodConfig.load(config, callback);
        else LiveConfig.load(config, callback);
    }

    private void showEditDialog(Config config) {
        if (isTv) {
            showPushDialog(config);
            return;
        }
        editing = config;
        dialogBinding = DialogManageConfigBinding.inflate(getLayoutInflater());
        dialogBinding.nameLayout.setVisibility(View.VISIBLE);
        dialogBinding.name.setText(config == null ? "" : config.getName());
        dialogBinding.url.setText(config == null ? "" : config.getUrl());
        dialogBinding.choose.setOnClickListener(v -> onChoose());
        dialogBinding.scan.setOnClickListener(v -> toPushDialog());
        dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(config == null ? R.string.config_add : R.string.config_edit)
                .setView(dialogBinding.getRoot())
                .setNegativeButton(R.string.dialog_negative, null)
                .setPositiveButton(R.string.dialog_positive, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> onSave()));
        dialog.show();
    }

    private void showPushDialog(Config config) {
        showPushDialog(config, null, null);
    }

    private void showPushDialog(Config config, String name, String url) {
        editing = config;
        pushBinding = DialogConfigPushBinding.inflate(getLayoutInflater());
        String nameText = name != null ? name : config == null ? "" : config.getName();
        String urlText = url != null ? url : config == null ? "" : config.getUrl();
        pushBinding.name.setText(nameText);
        pushBinding.url.setText(urlText);
        pushBinding.received.setText(TextUtils.isEmpty(urlText) ? getString(R.string.push_received) : urlText);
        pushBinding.choose.setOnClickListener(v -> onChoose());
        if (!ConfigPushHelper.ensureServerStarted()) {
            Notify.show(R.string.push_server_error);
        }
        String address = ConfigPushHelper.getConfigPushAddress();
        pushBinding.code.setImageBitmap(ConfigPushHelper.generateConfigQr(address));
        pushBinding.info.setText(ResUtil.getString(R.string.push_info, ConfigPushHelper.getServerAddress()).replace("\uff0c", "\n"));
        refreshDynamicColor();
        dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.push_link)
                .setView(pushBinding.getRoot())
                .setPositiveButton(R.string.dialog_positive, null)
                .create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> onPushSave()));
        dialog.show();
        pushHandler.removeCallbacks(pushTimeoutRunnable);
        pushHandler.postDelayed(pushTimeoutRunnable, Constant.PUSH_TIMEOUT_MS);
    }

    private void onPushTimeout() {
        if (pushBinding == null) return;
        Notify.show(R.string.push_timeout);
    }

    private void toPushDialog() {
        String name = dialogBinding.name.getText().toString().trim();
        String url = dialogBinding.url.getText().toString().trim();
        if (dialog != null) dialog.dismiss();
        dialog = null;
        dialogBinding = null;
        showPushDialog(editing, name, url);
    }

    private void onPushSave() {
        String name = pushBinding.name.getText().toString().trim();
        String url = pushBinding.url.getText().toString().trim();
        applyConfig(name, url);
    }

    private void onSave() {
        String name = dialogBinding.name.getText().toString().trim();
        String url = dialogBinding.url.getText().toString().trim();
        applyConfig(name, url);
    }

    private void applyConfig(String name, String url) {
        if (TextUtils.isEmpty(url)) {
            Notify.show(R.string.config_url_empty);
            return;
        }
        Config exist = AppDatabase.get().getConfigDao().find(url, type);
        if (exist != null && (editing == null || exist.getId() != editing.getId())) {
            Notify.show(R.string.config_exists);
            return;
        }
        if (editing == null) {
            Config target = name.isEmpty() ? Config.find(url, type) : Config.find(url, name, type);
            target.update();
            dismissDialog();
            load(target, target.getDesc());
        } else {
            String oldUrl = editing.getUrl();
            boolean active = TextUtils.equals(oldUrl, getActiveUrl());
            Config target = Config.find(oldUrl, type).name(name);
            if (active) target.url(url).update();
            else target.url(url).save();
            dismissDialog();
            if (active) {
                Config next = Config.find(url, type);
                load(next, next.getDesc());
            } else {
                refreshList();
            }
        }
    }

    private void onChoose() {
        PermissionUtil.requestFile(this, allGranted -> FileChooser.from(launcher).show());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onServerEvent(ServerEvent event) {
        if (event.type() != ServerEvent.Type.SETTING) return;
        pushHandler.removeCallbacks(pushTimeoutRunnable);
        if (pushBinding != null) {
            if (!TextUtils.isEmpty(event.name())) pushBinding.name.setText(event.name());
            if (!TextUtils.isEmpty(event.text())) {
                pushBinding.url.setText(event.text());
                pushBinding.received.setText(event.text());
            }
            return;
        }
        if (dialogBinding == null) return;
        if (!TextUtils.isEmpty(event.name())) dialogBinding.name.setText(event.name());
        if (!TextUtils.isEmpty(event.text())) dialogBinding.url.setText(event.text());
    }

    private void dismissDialog() {
        pushHandler.removeCallbacks(pushTimeoutRunnable);
        if (dialog != null) dialog.dismiss();
        dialog = null;
        dialogBinding = null;
        pushBinding = null;
        editing = null;
    }

    private String getActiveUrl() {
        return type == 0 ? VodConfig.getUrl() : LiveConfig.getUrl();
    }

    private boolean isFollow() {
        String url = LiveConfig.getUrl();
        return !TextUtils.isEmpty(url) && TextUtils.equals(url, VodConfig.getUrl());
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
