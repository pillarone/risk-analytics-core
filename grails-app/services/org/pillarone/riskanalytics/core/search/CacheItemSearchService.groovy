package org.pillarone.riskanalytics.core.search

import com.google.common.collect.ImmutableList
import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.BatchRun
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.modellingitem.*
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import java.util.concurrent.CopyOnWriteArraySet

class CacheItemSearchService {

    private final Set<ICacheItemEventListener> listeners = new CopyOnWriteArraySet<ICacheItemEventListener>()

    private final static Log LOG = LogFactory.getLog(CacheItemSearchService)
    private final
    static boolean PROFILE_CACHE_FILTERING = System.getProperty("disableProfileCacheFiltering", "true").equalsIgnoreCase("false")

    static transactional = false

    CacheItemHibernateListener cacheItemListener

    private List<CacheItem> cache
    private CacheItemListener listener

    @PostConstruct
    void init() {
        cache = []
        createInitialIndex()
        listener = new SearchCacheItemListener()
        cacheItemListener.addCacheItemListener(listener)
    }

    @PreDestroy
    void cleanUp() {
        cacheItemListener.removeCacheItemListener(listener)
        listener = null
        cache = null
    }

    void addItemEventListener(ICacheItemEventListener listener) {
        listeners.add(listener)
    }

    void removeItemEventListener(ICacheItemEventListener listener) {
        listeners.remove(listener)
    }

    private void fireItemEvent(CacheItemEvent event) {
        listeners.each { it.onEvent(event) }
    }

    protected synchronized void createInitialIndex() {
        LOG.info("start creating initial index.")

        long t;
        if (PROFILE_CACHE_FILTERING) {
            LOG.info("-DdisableProfileCacheFiltering not set, will time search service")
            t = System.currentTimeMillis()
        } else {
            LOG.info("-DdisableProfileCacheFiltering=true, will not time search service")
        }

        ParameterizationDAO.withTransaction {
            for (ParameterizationDAO dao in ParameterizationDAO.list()) {
                cache.add(CacheItemMapper.getModellingItem(dao))
            }
            for (ResultConfigurationDAO dao in ResultConfigurationDAO.list()) {
                cache.add(CacheItemMapper.getModellingItem(dao))
            }

            for (SimulationRun dao in SimulationRun.list().findAll { !it.toBeDeleted }) {
                cache.add(CacheItemMapper.getModellingItem(dao))
            }
            for (ResourceDAO dao in ResourceDAO.list()) {
                cache.add(CacheItemMapper.getModellingItem(dao))
            }
            for (BatchRun dao in BatchRun.list()) {
                cache.add(CacheItemMapper.getModellingItem(dao))
            }
        }
        LOG.info("end creating initial index.")
        if (PROFILE_CACHE_FILTERING) {
            t = System.currentTimeMillis() - t
            LOG.info("Timed " + t + " ms: creating initial index");
        }
    }

    synchronized void refresh() {
        cache.clear()
        createInitialIndex()
    }

    synchronized List<CacheItem> search(List<ISearchFilter> filters) {

        long t
        long start

        if (PROFILE_CACHE_FILTERING) {
            start = System.currentTimeMillis()
        }

        List<CacheItem> results = []
        List<CacheItem> cacheCopy = new ArrayList<CacheItem>(cache)

        if (PROFILE_CACHE_FILTERING) {
            t = System.currentTimeMillis()
            LOG.info("Timed " + (t - start) + " ms: copying cache")
        }

        cacheCopy.each { CacheItem item ->
            if (filters.every { it.accept(item) }) {
                results << item
            }
        }
        if (PROFILE_CACHE_FILTERING) {
            long now = System.currentTimeMillis()
            LOG.info("Timed " + (now - t) + " ms: filtered copy")
            t = now
        }

        if (PROFILE_CACHE_FILTERING) {
            long now = System.currentTimeMillis()
            LOG.info("Timed " + (now - t) + " ms: collecting. Total: " + (now - start) / 1000 + " sec.");
        }

        return results;
    }

    private synchronized void addModellingItemToIndex(CacheItem item) {
        cache.add(item)
        fireItemEvent(new CacheItemEvent(item: item, eventType: CacheItemEvent.EventType.ADDED))
    }

    private synchronized void removeModellingItemFromIndex(CacheItem item) {
        cache.remove(item)
        fireItemEvent(new CacheItemEvent(item: item, eventType: CacheItemEvent.EventType.REMOVED))
    }

    private synchronized void updateModellingItemInIndex(CacheItem item) {
        int indexOf = cache.indexOf(item)
        if (indexOf == -1) {
            log.warn("could not find item $item in cache. Probably it was inserted outside this application. Please do not ever do this!!. For now will add it to the cache.")
            addModellingItemToIndex(item)
        } else {
            cache[indexOf] = item
        }
        internalUpdateModellingItemInIndex(item)
        fireItemEvent(new CacheItemEvent(item: item, eventType: CacheItemEvent.EventType.UPDATED))
    }

    private synchronized void internalUpdateModellingItemInIndex(CacheItem item) {

    }

    private synchronized void internalUpdateModellingItemInIndex(ParameterizationCacheItem item) {
        cache.findAll {
            it instanceof SimulationCacheItem && it.parameterization == item
        }.each { SimulationCacheItem simulationCacheItem ->
            simulationCacheItem.parameterization = item
        }
        cache.findAll {
            (it instanceof BatchCacheItem) && it.parameterizations.contains(item)
        }.each { BatchCacheItem batchCacheItem ->
            internalUpdateModellingItemInIndex(batchCacheItem)
        }
    }

    private synchronized void internalUpdateModellingItemInIndex(ResultConfigurationCacheItem item) {
        cache.findAll {
            it instanceof SimulationCacheItem && it.resultConfiguration == item
        }.each { SimulationCacheItem simulationCacheItem ->
            simulationCacheItem.resultConfiguration = item
        }
    }

    private synchronized void internalUpdateModellingItemInIndex(SimulationCacheItem item) {
        if (item.batch) {
            int index = cache.indexOf(item.batch)
            if (index != -1) {
                item.batch = cache[index] as BatchCacheItem
            }
        }
    }

    private synchronized void internalUpdateModellingItemInIndex(BatchCacheItem item) {
        cache.findAll {
            (it instanceof SimulationCacheItem) && it.batch && it.batch.id == item.id
        }.each { SimulationCacheItem simulationCacheItem ->
            simulationCacheItem.batch = item
        }
        List<ParameterizationCacheItem> collect = item.parameterizations.collect { ParameterizationCacheItem parameterizationCacheItem ->
            ParameterizationCacheItem existent = cache.find {
                it == parameterizationCacheItem
            } as ParameterizationCacheItem
            existent ?: parameterizationCacheItem
        }
        item.parameterizations = ImmutableList.copyOf(collect)
    }

    public static CacheItemSearchService getInstance() {
        return Holders.grailsApplication.mainContext.getBean(CacheItemSearchService)
    }

    private class SearchCacheItemListener implements CacheItemListener {

        void itemAdded(CacheItem item) {
            addModellingItemToIndex(item)
        }

        void itemDeleted(CacheItem item) {
            removeModellingItemFromIndex(item)
        }

        void itemChanged(CacheItem item) {
            updateModellingItemInIndex(item)
        }
    }
}
