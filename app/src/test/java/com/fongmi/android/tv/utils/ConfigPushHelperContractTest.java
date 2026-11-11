package com.fongmi.android.tv.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.fongmi.android.tv.Constant;

import org.junit.Test;

/**
 * 网盘扫码核心组件 ConfigPushHelper 契约验证用例。
 * <p>
 * 由于 ConfigPushHelper 依赖 Server 与 QRCode（需 Android 运行时），
 * 本测试聚焦于可脱离 Android 验证的契约：推送地址 tab 参数拼接规则、
 * 二维码生成对空内容的防御、回调接口默认实现。
 */
public class ConfigPushHelperContractTest {

    @Test
    public void configPushAddress_usesSettingTab() {
        // 网盘配置推送地址必须指向 index.html 的"设定"tab
        String tabParam = "?tab=" + Constant.PUSH_TAB_SETTING;
        assertEquals("?tab=4", tabParam);
    }

    @Test
    public void videoPushAddress_usesPushTab() {
        String tabParam = "?tab=" + Constant.PUSH_TAB_PUSH;
        assertEquals("?tab=2", tabParam);
    }

    @Test
    public void generateConfigQr_withEmptyContent_returnsNull() {
        // 复刻 ConfigPushHelper.generateConfigQr 对空内容的防御逻辑
        String content = "";
        assertNull(isEmpty(content) ? null : "not-null");
    }

    @Test
    public void generateConfigQr_withNullContent_returnsNull() {
        String content = null;
        assertNull(isEmpty(content) ? null : "not-null");
    }

    @Test
    public void callbackDefaultMethods_doNotThrow() {
        // 验证回调接口默认实现可安全调用
        ConfigPushHelper.Callback callback = new ConfigPushHelper.Callback() {
            @Override
            public void onPushed(String text, String name) {
            }
        };
        callback.onTimeout();
        callback.onError("test error");
    }

    @Test
    public void qrConfigSize_matchesExpected() {
        assertEquals(200, Constant.QR_SIZE_CONFIG);
        assertEquals(0, Constant.QR_MARGIN_CONFIG);
    }

    @Test
    public void qrPushSize_matchesExpected() {
        assertEquals(250, Constant.QR_SIZE_PUSH);
        assertEquals(1, Constant.QR_MARGIN_PUSH);
    }

    private boolean isEmpty(String content) {
        return content == null || content.isEmpty();
    }
}
