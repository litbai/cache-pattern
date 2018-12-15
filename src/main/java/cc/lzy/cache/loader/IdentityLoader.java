package cc.lzy.cache.loader;

import java.util.Optional;

/**
 * key-value 相同的Loader
 *
 * @author litbai
 * @version 18/12/15
 */
public class IdentityLoader implements LocalCacheLoader {
    @Override
    public Optional<?> load(String key) {
        String value = key;
        return Optional.of(value);
    }
}
