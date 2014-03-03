package org.pillarone.riskanalytics.core.search

import grails.util.Holders
import models.core.CoreModel
import org.junit.Before
import org.junit.Test
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.pillarone.riskanalytics.core.modellingitem.CacheItem
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.simulation.item.Parameterization

import static org.junit.Assert.*

class CacheItemSearchServiceTests {

    CacheItemSearchService modellingItemSearchService

    @Before
    void setUp() {
        modellingItemSearchService = Holders.grailsApplication.mainContext.getBean(CacheItemSearchService)
        FileImportService.importModelsIfNeeded(['Core', 'Application'])
        modellingItemSearchService.refresh()
    }

    @Test
    void testService() {
        final List<CacheItem> results = modellingItemSearchService.search([new AllFieldsFilter(query: "Parameters")])

        assertEquals(3, results.size())

        Parameterization parameterization = new Parameterization("MyParameters", CoreModel)
        ParameterizationDAO.withNewSession {
            parameterization.save()
        }

        results = modellingItemSearchService.search([new AllFieldsFilter(query: "Parameters")])

        assertEquals(4, results.size())

        assertNotNull(results.find { it.id == parameterization.id })

        ParameterizationDAO.withNewSession {
            parameterization.delete()
        }

        results = modellingItemSearchService.search([new AllFieldsFilter(query: "Parameters")])

        assertEquals(3, results.size())

        assertNull(results.find { it.id == parameterization.id })

    }

    @Test
    void testRenameParametrization() {

        Parameterization parameterization = new Parameterization("MyParameters", CoreModel)
        ParameterizationDAO.withNewSession {
            parameterization.save()
        }
        List<ModellingItem> results = modellingItemSearchService.search([new AllFieldsFilter(query: "MyParameters")])
        assertEquals(1, results.size())

        ParameterizationDAO.withNewSession {
            parameterization.rename("RenamedParameters")
        }
        results = modellingItemSearchService.search([new AllFieldsFilter(query: "MyParameters")])
        assertEquals(0, results.size())
        results = modellingItemSearchService.search([new AllFieldsFilter(query: "RenamedParameters")])
        assertEquals(1, results.size())
    }
}
