package com.fongmi.android.tv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * 网盘扫码 / 配置推送相关常量验证用例。
 * 确保所有硬编码参数已迁移至 Constant 并符合预期。
 */
public class ConstantTest {

    @Test
    public void pushTabConstants_matchHtmlTabOrder() {
        // index.html 中 tab 顺序：1=搜尋 2=推送 3=彈幕 4=設定 5=本地
        assertEquals(1, Constant.PUSH_TAB_SEARCH);
        assertEquals(2, Constant.PUSH_TAB_PUSH);
        assertEquals(3, Constant.PUSH_TAB_DANMAKU);
        assertEquals(4, Constant.PUSH_TAB_SETTING);
        assertEquals(5, Constant.PUSH_TAB_LOCAL);
    }

    @Test
    public void qrSizeConstants_arePositive() {
        assertTrue("QR_SIZE_CONFIG should be positive", Constant.QR_SIZE_CONFIG > 0);
        assertTrue("QR_SIZE_PUSH should be positive", Constant.QR_SIZE_PUSH > 0);
        assertTrue("QR_MARGIN_CONFIG should be >= 0", Constant.QR_MARGIN_CONFIG >= 0);
        assertTrue("QR_MARGIN_PUSH should be >= 0", Constant.QR_MARGIN_PUSH >= 0);
    }

    @Test
    public void pushTimeout_isTwoMinutes() {
        assertEquals(TimeUnit.MINUTES.toMillis(2), Constant.PUSH_TIMEOUT_MS);
    }

    @Test
    public void configPushTab_isSettingTab() {
        // 网盘配置推送必须指向 index.html 的"設定"tab(4)
        assertEquals(4, Constant.PUSH_TAB_SETTING);
    }
}
