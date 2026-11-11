package com.fongmi.android.tv.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;

import com.fongmi.android.tv.Constant;
import com.fongmi.android.tv.server.Server;

/**
 * 网盘扫码 / 配置推送核心组件。
 * <p>
 * 封装配置推送流程中的二维码生成、本地服务端地址拼接、服务启动与异常处理，
 * 供 {@code ConfigManageActivity}、{@code ConfigDialog}、{@code PushActivity} 等
 * 需要展示"扫码推送配置"二维码的页面复用，避免硬编码 tab 索引、二维码尺寸等参数。
 * <p>
 * 核心能力：
 * <ul>
 *     <li>二维码生成：基于 zxing 生成圆角点阵二维码</li>
 *     <li>服务端地址：按命名 tab 常量生成推送页面地址</li>
 *     <li>服务启动：启动本地 NanoHTTPD 服务并返回是否成功</li>
 *     <li>结果回调：通过 {@link Callback} 通知推送结果（成功/超时/失败）</li>
 * </ul>
 */
public final class ConfigPushHelper {

    private ConfigPushHelper() {
    }

    /**
     * 生成配置推送二维码。
     *
     * @param content 二维码内容（通常为服务端推送地址）
     * @return 二维码 Bitmap，生成失败返回 null
     */
    public static Bitmap generateConfigQr(String content) {
        if (TextUtils.isEmpty(content)) return null;
        return QRCode.getBitmap(content, Constant.QR_SIZE_CONFIG, Constant.QR_MARGIN_CONFIG);
    }

    /**
     * 生成通用推送二维码（PushActivity 使用）。
     */
    public static Bitmap generatePushQr(String content) {
        if (TextUtils.isEmpty(content)) return null;
        return QRCode.getBitmap(content, Constant.QR_SIZE_PUSH, Constant.QR_MARGIN_PUSH);
    }

    /**
     * 获取配置推送页面地址（对应 index.html 的"设定"tab）。
     */
    public static String getConfigPushAddress() {
        return Server.get().getAddress(Constant.PUSH_TAB_SETTING);
    }

    /**
     * 获取视频推送页面地址（对应 index.html 的"推送"tab）。
     */
    public static String getVideoPushAddress() {
        return Server.get().getAddress(Constant.PUSH_TAB_PUSH);
    }

    /**
     * 获取服务端基础地址（不含 tab 参数）。
     */
    public static String getServerAddress() {
        return Server.get().getAddress();
    }

    /**
     * 启动本地推送服务。
     *
     * @return true 表示服务已成功启动或已在运行，false 表示启动失败
     */
    public static boolean ensureServerStarted() {
        return Server.get().start();
    }

    /**
     * 推送结果回调。
     */
    public interface Callback {
        /**
         * 收到推送的配置内容。
         *
         * @param text 配置 URL
         * @param name 配置名称（可能为空）
         */
        void onPushed(String text, String name);

        /**
         * 等待推送超时。
         */
        default void onTimeout() {
        }

        /**
         * 服务启动失败等异常情况。
         */
        default void onError(String message) {
        }
    }
}
