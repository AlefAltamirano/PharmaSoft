package pe.edu.upeu.PharmaBackckend.config;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.*;
import org.springframework.cache.interceptor.*;
import org.springframework.context.annotation.Configuration;
import org.slf4j.LoggerFactory;
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {
 public CacheErrorHandler errorHandler() {
  return new SimpleCacheErrorHandler() {
   private void warn(Cache c) { LoggerFactory.getLogger(CacheConfig.class).warn("Cache {} no disponible; se usa Oracle", c.getName()); }
   public void handleCacheGetError(RuntimeException e, Cache c, Object k) { warn(c); }
   public void handleCachePutError(RuntimeException e, Cache c, Object k, Object v) { warn(c); }
   public void handleCacheEvictError(RuntimeException e, Cache c, Object k) { warn(c); }
   public void handleCacheClearError(RuntimeException e, Cache c) { warn(c); }
  };
 }
}
