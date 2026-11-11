package com.fongmi.android.tv.bean;

import android.text.TextUtils;

import com.fongmi.android.tv.App;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * 网盘配置源（Depot）数据模型。
 * <p>
 * 当配置 JSON 根节点包含 {@code urls} 数组时，将其解析为 Depot 列表，
 * 取第一个 URL 作为实际配置地址加载，实现"网盘配置聚合"能力。
 */
public class Depot {

    /** 网盘 urls 列表为空时的错误信息 */
    public static final String ERROR_EMPTY = "Depot urls is empty";
    /** 网盘 urls 字段缺失或格式错误时的错误信息 */
    public static final String ERROR_INVALID = "Depot urls is invalid";

    @SerializedName("url")
    private String url;
    @SerializedName("name")
    private String name;

    public static List<Depot> arrayFrom(String str) {
        if (TextUtils.isEmpty(str)) return Collections.emptyList();
        Type listType = TypeToken.getParameterized(List.class, Depot.class).getType();
        List<Depot> items = App.gson().fromJson(str, listType);
        return items == null ? Collections.emptyList() : items;
    }

    public String getUrl() {
        return TextUtils.isEmpty(url) ? "" : url;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? getUrl() : name;
    }
}
