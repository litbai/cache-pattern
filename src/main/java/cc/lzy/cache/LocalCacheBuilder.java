package cc.lzy.cache;

import cc.lzy.cache.loader.IdentityLoader;
import cc.lzy.cache.loader.LocalCacheLoader;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.tools.javac.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存加载器
 *
 * @author litbai
 * @version 18/12/25
 */
public class LocalCacheBuilder implements LocalCacheManager{

    private ListeningExecutorService refreshPool;

    private ConcurrentMap<String, LoadingCache<String, Optional<?>>> cacheMap = new ConcurrentHashMap<>(16);

    private final IdentityLoader identityLoader = new IdentityLoader();

    public static final String CACHE_NAME_IDENTITY = "IDENTITY";

    private final Logger logger = LoggerFactory.getLogger(LocalCacheBuilder.class);

    public LocalCacheBuilder() {
        init();
    }

    public void init() {
        refreshPool = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(5));
        buildCaches();
    }

    private void buildCaches() {
        addCache(CACHE_NAME_IDENTITY, buildIdentityCache());
    }

    private LoadingCache<String, Optional<?>> buildIdentityCache() {
        return buildCache(1000, 10, 2, identityLoader);
    }

    // 构建缓存
    private LoadingCache<String, Optional<?>> buildCache(int maxSize, int expire, int refresh, LocalCacheLoader loader) {
        return CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expire, TimeUnit.SECONDS)
                .refreshAfterWrite(refresh, TimeUnit.SECONDS)
                .recordStats()
                .build(new CacheLoader<String, Optional<?>>() {

                    @Override
                    public Optional<?> load(String key) throws Exception {
                        return loader.load(key);
                    }

                    @Override
                    public ListenableFuture<Optional<?>> reload(String key, Optional<?> oldValue) throws Exception {
                        return refreshPool.submit(() -> loader.load(key));
                    }
                });
    }


    @Override
    public void addCache(String cacheName, LoadingCache<String, Optional<?>> cache) {
        Assert.checkNonNull(cacheName);
        Assert.checkNonNull(cache);
        cacheMap.put(cacheName, cache);
    }

    @Override
    public LoadingCache<String, Optional<?>> getCache(String cacheName) {
        return cacheMap.get(cacheName);
    }

    @Override
    public <T> T get(String cacheName, String key, Class<T> clazz) {
        try {
            LoadingCache<String, Optional<?>> cache = cacheMap.get(cacheName);
            if (cache == null) {
                throw new IllegalArgumentException("没有与[" + cacheName + "]对应的cache");
            }
            Optional<?> op = cache.getUnchecked(key);
            if (!op.isPresent()) {
                return null;
            }
            return clazz.cast(op.get());
        } catch (Exception e1) {
            logger.error("获取本地缓存异常, cacheName: " + cacheName + ", key:  " +  key, e1);
        }
        if (List.class.isAssignableFrom(clazz)) {
            return clazz.cast(Collections.emptyList());
        }
        if (Map.class.isAssignableFrom(clazz)) {
            return clazz.cast(Collections.emptyMap());
        }
        return null;
    }

    @Override
    public void put(String cacheName, String key, Optional<?> value) {
        Assert.checkNonNull(key);
        Assert.checkNonNull(value);
        cacheMap.get(cacheName).put(key, value);
    }

    @Override
    public int size() {
        return cacheMap.size();
    }

    public Map<String, LoadingCache<String, Optional<?>>> getCaches() {
        return cacheMap;
    }
}
