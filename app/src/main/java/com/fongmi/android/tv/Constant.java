package com.fongmi.android.tv;

import java.util.concurrent.TimeUnit;

public class Constant {

    public static final long INTERVAL_SEEK = TimeUnit.SECONDS.toMillis(10);
    public static final long INTERVAL_HIDE = TimeUnit.SECONDS.toMillis(5);
    public static final long TIMEOUT_VOD = TimeUnit.SECONDS.toMillis(30);
    public static final long TIMEOUT_LIVE = TimeUnit.SECONDS.toMillis(30);
    public static final long TIMEOUT_EPG = TimeUnit.SECONDS.toMillis(5);
    public static final long TIMEOUT_XML = TimeUnit.SECONDS.toMillis(15);
    public static final long TIMEOUT_PLAY = TimeUnit.SECONDS.toMillis(15);
    public static final long TIMEOUT_SYNC = TimeUnit.SECONDS.toMillis(2);
    public static final long TIMEOUT_SEARCH = TimeUnit.SECONDS.toMillis(30);
    public static final long TIMEOUT_PARSE_DEF = TimeUnit.SECONDS.toMillis(15);
    public static final long TIMEOUT_PARSE_WEB = TimeUnit.SECONDS.toMillis(15);
    public static final long TIMEOUT_PARSE_LIVE = TimeUnit.SECONDS.toMillis(10);
    public static final long HISTORY_TIME = TimeUnit.DAYS.toMillis(60);

    // 网盘扫码 / 配置推送相关常量
    public static final int PUSH_TAB_SEARCH = 1;
    public static final int PUSH_TAB_PUSH = 2;
    public static final int PUSH_TAB_DANMAKU = 3;
    public static final int PUSH_TAB_SETTING = 4;
    public static final int PUSH_TAB_LOCAL = 5;

    public static final int QR_SIZE_CONFIG = 200;
    public static final int QR_MARGIN_CONFIG = 0;
    public static final int QR_SIZE_PUSH = 250;
    public static final int QR_MARGIN_PUSH = 1;

    public static final long PUSH_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(2);

    public static long getOpEdLimit(long duration) {
        if (duration < TimeUnit.MINUTES.toMillis(15)) return TimeUnit.MINUTES.toMillis(3);
        if (duration < TimeUnit.MINUTES.toMillis(30)) return TimeUnit.MINUTES.toMillis(6);
        return TimeUnit.MINUTES.toMillis(10);
    }
}
