package cc.lzy.cache.loader;

import java.util.Optional;

/**
 * 本地缓存加载器
 *
 * @author litbai
 * @version 18/12/25
 */
public interface LocalCacheLoader {
    Optional<?> load(String key);
}
