package cc.lzy.cache;

/**
 * 本地缓存Manager
 *
 * @author litbai
 * @version 18/12/15
 */

import com.google.common.cache.LoadingCache;

import java.util.Optional;

/**
 * 本地缓存(Guava Cache)管理器, 只允许String类型的key
 *
 * @author chengyu
 * @version 17/10/24
 */
public interface LocalCacheManager {

    void addCache(String cacheName, LoadingCache<String, Optional<?>> cache);

    LoadingCache<String, Optional<?>> getCache(String cacheName);

    <T> T get(String cacheName, String key, Class<T> clazz);

    void put(String cacheName, String key, Optional<?> value);

    int size();
}
