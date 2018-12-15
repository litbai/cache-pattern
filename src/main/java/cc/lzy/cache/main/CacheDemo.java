package cc.lzy.cache.main;

import cc.lzy.cache.LocalCacheBuilder;
import cc.lzy.cache.LocalCacheManager;

/**
 * 启动类
 *
 * @author litbai
 * @version 18/12/15
 */
public class CacheDemo {
    private static final LocalCacheManager manager = new LocalCacheBuilder();

    public static void main(String[] args) {
        String key = "cache-demo";
        String value = manager.get(LocalCacheBuilder.CACHE_NAME_IDENTITY, key, String.class);
        System.out.println(value);
    }
}
