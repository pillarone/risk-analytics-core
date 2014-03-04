package org.pillarone.riskanalytics.core.search

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.modellingitem.*
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun

import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

class CacheItemSearchService {

    private final static Log LOG = LogFactory.getLog(CacheItemSearchService)
    private final
    static boolean PROFILE_CACHE_FILTERING = System.getProperty("disableProfileCacheFiltering", "false").equalsIgnoreCase("false")

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
        cache = null
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

    private synchronized void addModellingItemToIndex(CacheItem modellingItem) {
        cache.add(modellingItem)
    }

    private synchronized void removeModellingItemFromIndex(CacheItem modellingItem) {
        cache.remove(modellingItem)
    }

    private synchronized void updateModellingItemInIndex(CacheItem item) {
        cache[cache.indexOf(item)] = item
        internalUpdateModellingItemInIndex(item)
    }

    private synchronized void internalUpdateModellingItemInIndex(CacheItem item) {

    }

    private synchronized void internalUpdateModellingItemInIndex(ParameterizationCacheItem item) {
        List<SimulationCacheItem> allSimulations = cache.findAll {
            it instanceof SimulationCacheItem
        } as List<SimulationCacheItem>
        for (SimulationCacheItem simulation in allSimulations) {
            if (simulation.parameterization.equals(item)) {
                simulation.parameterization = item
            }
        }
    }

    private synchronized void internalUpdateModellingItemInIndex(ResultConfigurationCacheItem item) {
        List<SimulationCacheItem> allSimulations = cache.findAll {
            it instanceof SimulationCacheItem
        } as List<SimulationCacheItem>
        for (SimulationCacheItem simulation in allSimulations) {
            if (simulation.resultConfiguration.equals(item)) {
                simulation.resultConfiguration = item
            }
        }
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
