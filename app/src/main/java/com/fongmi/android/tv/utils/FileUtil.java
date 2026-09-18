package com.fongmi.android.tv.utils;

import android.webkit.CookieManager;

import com.bumptech.glide.Glide;
import com.fongmi.android.tv.App;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.impl.Callback;
import com.github.catvod.utils.Path;

import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.StatFs;
import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtil {

    public static File getWall(int index) {
        return Path.files("wallpaper_" + index);
    }

    public static File getWallCache() {
        return Path.files("wallpaper_cache");
    }

    public static void openFile(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(getShareUri(file), FileUtil.getMimeType(file.getName()));
        App.get().startActivity(intent);
    }

    public static void gzipCompress(File target) {
        byte[] buffer = new byte[16384];
        try (FileInputStream is = new FileInputStream(target); GZIPOutputStream os = new GZIPOutputStream(new FileOutputStream(target.getAbsolutePath() + ".gz"))) {
            int read;
            while ((read = is.read(buffer)) > 0) os.write(buffer, 0, read);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Path.clear(target);
        }
    }

    public static void gzipDecompress(File target, File path) {
        byte[] buffer = new byte[16384];
        try (GZIPInputStream is = new GZIPInputStream(new BufferedInputStream(new FileInputStream(target))); BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(path))) {
            int read;
            while ((read = is.read(buffer)) != -1) os.write(buffer, 0, read);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zipDecompress(File target, File path) {
        try (ZipFile zip = new ZipFile(target)) {
            Enumeration<?> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File out = new File(path, entry.getName());
                if (entry.isDirectory()) out.mkdirs();
                else Path.copy(zip.getInputStream(entry), out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void clearCache(Callback callback) {
        Task.execute(() -> {
            // 1. 内部缓存目录（含 exo、mpv、js、py、jar、epg、jpa、thunder、Glide 磁盘缓存等）
            Path.clear(Path.cache());
            // 2. 外部缓存目录
            File externalCache = App.get().getExternalCacheDir();
            if (externalCache != null) Path.clear(externalCache);
            // 3. 壁纸缓存（在 files 目录下）
            Path.clear(getWallCache());
            // 4. Glide 磁盘缓存（显式调用更安全，避免文件句柄冲突）
            try {
                Glide.get(App.get()).clearDiskCache();
            } catch (Throwable ignored) {
            }
            // 5. WebView 缓存（位于内部缓存目录下的 webviewCache 子目录，已被 Path.clear(Path.cache()) 清理）
            // 6. WebView Cookie（存储在 app_webview 目录，需通过 CookieManager 清理）
            try {
                CookieManager.getInstance().removeAllCookies(null);
                CookieManager.getInstance().flush();
            } catch (Throwable ignored) {
            }
            App.post(callback::success);
        });
    }

    public static void getCacheSize(Callback callback) {
        Task.execute(() -> {
            long size = 0;
            // 内部缓存目录
            size += getDirectorySize(Path.cache());
            // 外部缓存目录
            File externalCache = App.get().getExternalCacheDir();
            if (externalCache != null) size += getDirectorySize(externalCache);
            // 壁纸缓存
            size += getDirectorySize(getWallCache());
            String usage = byteCountToDisplaySize(size);
            App.post(() -> callback.success(usage));
        });
    }

    public static long getDirectorySize(File dir) {
        long size = 0;
        if (dir == null) return 0;
        if (dir.isDirectory()) for (File file : Path.list(dir)) size += getDirectorySize(file);
        else size = dir.length();
        return size;
    }

    public static long getAvailableStorageSpace(File file) {
        try {
            StatFs stat = new StatFs(file.getAbsolutePath());
            return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        } catch (Exception e) {
            return 0;
        }
    }

    public static Uri getShareUri(String path) {
        return getShareUri(new File(path.replace("file://", "")));
    }

    public static Uri getShareUri(File file) {
        return FileProvider.getUriForFile(App.get(), App.get().getPackageName() + ".provider", file);
    }

    private static String getMimeType(String fileName) {
        String mimeType = URLConnection.guessContentTypeFromName(fileName);
        return TextUtils.isEmpty(mimeType) ? "*/*" : mimeType;
    }

    public static String byteCountToDisplaySize(long size) {
        if (size <= 0) return ResUtil.getString(R.string.none);
        String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
