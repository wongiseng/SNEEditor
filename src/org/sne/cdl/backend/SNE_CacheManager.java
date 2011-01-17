package org.sne.cdl.backend;

import java.util.Collections;
import java.util.logging.Logger;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;

/**
 * Singleton cache manager 
 * @author wibisono
 *
 */
public final class SNE_CacheManager {
	private static CacheFactory cacheFactory;
	private static Cache cache = null;

	public static final Logger log = Logger.getLogger(SNE_CacheManager.class.getName());

	public static Cache getCache() {
		if (cache != null)
			return cache;
		
		try {
			cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
		} catch (CacheException e) {
			e.printStackTrace();			
		}
		
		return cache;
	}
	
	
}
