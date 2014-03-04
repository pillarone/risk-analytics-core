package org.pillarone.riskanalytics.core.search

import grails.util.Holders
import models.core.CoreModel
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

import static org.junit.Assert.*

class CacheItemSearchServiceTests {

    CacheItemSearchService cacheItemSearchService

    @Before
    void setUp() {
        cacheItemSearchService = Holders.grailsApplication.mainContext.getBean(CacheItemSearchService)
        FileImportService.importModelsIfNeeded(['Core', 'Application'])
        cacheItemSearchService.refresh()
    }

    @Test
    void testService() {
        List<CacheItem> results = cacheItemSearchService.search([new AllFieldsFilter(query: "Parameters")])

        assertEquals(3, results.size())

        Parameterization parametrization = new Parameterization("MyParameters", CoreModel)
        ParameterizationDAO.withNewSession {
            parametrization.save()
        }

        results = cacheItemSearchService.search([new AllFieldsFilter(query: "Parameters")])

        assertEquals(4, results.size())

        assertNotNull(results.find { it.id == parametrization.id })

        ParameterizationDAO.withNewSession {
            parametrization.delete()
        }

        results = cacheItemSearchService.search([new AllFieldsFilter(query: "Parameters")])

        assertEquals(3, results.size())

        assertNull(results.find { it.id == parametrization.id })

    }

    @Test
    void testRenameParametrization() {
        Parameterization parametrization = new Parameterization("MyParameters", CoreModel)
        ParameterizationDAO.withNewSession {
            parametrization.save()
        }
        List<CacheItem> results = cacheItemSearchService.search([new AllFieldsFilter(query: "MyParameters")])
        assertEquals(1, results.size())
        ParameterizationDAO.withNewSession {
            parametrization.rename("RenamedParameters")
        }
        results = cacheItemSearchService.search([new AllFieldsFilter(query: "MyParameters")])
        assertEquals(0, results.size())
        results = cacheItemSearchService.search([new AllFieldsFilter(query: "RenamedParameters")])
        assertEquals(1, results.size())
    }
}
