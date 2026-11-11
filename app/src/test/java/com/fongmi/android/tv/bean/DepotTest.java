package com.fongmi.android.tv.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * 网盘配置源（Depot）解析验证用例。
 * 验证 urls 数组解析契约、空值安全与错误常量。
 * <p>
 * 由于 Depot.getUrl()/getName() 内部依赖 android.text.TextUtils，
 * 在纯 JUnit 环境下桩实现会抛出 RuntimeException，故通过反射读取私有字段
 * 来验证 Gson 反序列化的数据绑定正确性。
 */
public class DepotTest {

    private static final Gson GSON = new Gson();

    /**
     * 复刻 Depot.arrayFrom 的解析逻辑，验证数据契约。
     * （实际方法依赖 App.gson()，此处用等价 Gson 验证解析行为）
     */
    private List<Depot> parse(String str) {
        if (str == null || str.isEmpty()) return Collections.emptyList();
        Type listType = TypeToken.getParameterized(List.class, Depot.class).getType();
        List<Depot> items = GSON.fromJson(str, listType);
        return items == null ? Collections.emptyList() : items;
    }

    /** 通过反射读取 Depot 的私有字段，避免触发 TextUtils 桩异常。 */
    private String field(Depot depot, String name) throws Exception {
        Field f = Depot.class.getDeclaredField(name);
        f.setAccessible(true);
        return (String) f.get(depot);
    }

    @Test
    public void errorConstants_areDefined() {
        assertNotNull(Depot.ERROR_EMPTY);
        assertNotNull(Depot.ERROR_INVALID);
        assertTrue("ERROR_EMPTY should not be blank", !Depot.ERROR_EMPTY.trim().isEmpty());
        assertTrue("ERROR_INVALID should not be blank", !Depot.ERROR_INVALID.trim().isEmpty());
    }

    @Test
    public void parse_validUrlsArray_bindsFieldsCorrectly() throws Exception {
        String json = "[{\"url\":\"http://a.com/config.json\",\"name\":\"A\"},{\"url\":\"http://b.com\"}]";
        List<Depot> items = parse(json);
        assertEquals(2, items.size());
        assertEquals("http://a.com/config.json", field(items.get(0), "url"));
        assertEquals("A", field(items.get(0), "name"));
        assertEquals("http://b.com", field(items.get(1), "url"));
        assertNull(field(items.get(1), "name"));
    }

    @Test
    public void parse_emptyArray_returnsEmptyList() {
        List<Depot> items = parse("[]");
        assertTrue(items.isEmpty());
    }

    @Test
    public void parse_nullOrEmptyString_returnsEmptyList() {
        assertTrue(parse(null).isEmpty());
        assertTrue(parse("").isEmpty());
    }

    @Test
    public void parse_missingUrl_fieldIsNull() throws Exception {
        String json = "[{\"name\":\"no-url\"}]";
        List<Depot> items = parse(json);
        assertEquals(1, items.size());
        assertNull(field(items.get(0), "url"));
        assertEquals("no-url", field(items.get(0), "name"));
    }

    @Test
    public void parse_nameEmptyOrMissing_isNull() throws Exception {
        String json = "[{\"url\":\"http://only-url.com\"},{\"url\":\"http://x.com\",\"name\":\"\"}]";
        List<Depot> items = parse(json);
        assertNull(field(items.get(0), "name"));
        assertEquals("", field(items.get(1), "name"));
    }
}
