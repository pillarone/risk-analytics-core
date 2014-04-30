package org.pillarone.riskanalytics.core.search
import com.google.common.collect.ImmutableList
import org.apache.commons.lang.StringUtils
import org.pillarone.riskanalytics.core.modellingitem.ParameterizationCacheItem
import org.pillarone.riskanalytics.core.modellingitem.ResourceCacheItem
import org.pillarone.riskanalytics.core.modellingitem.ResultConfigurationCacheItem
import org.pillarone.riskanalytics.core.modellingitem.SimulationCacheItem
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.user.Person

class AllFieldsFilterTests extends GroovyTestCase {

    void testSearchByOwner() {
        ResourceCacheItem resource = new ResourceCacheItem(1l, 'testName', null, null, null, null, new Person(username: 'user'), null, null, false, null)
        assert new AllFieldsFilter(query: 'use').accept(resource)
        assert new AllFieldsFilter(query: 'user').accept(resource)
        // Must not find 'user' in name of resource, nor 'testName' in username
        assert !new AllFieldsFilter(query: 'name:user').accept(resource)
        assert !new AllFieldsFilter(query: 'owner:testName').accept(resource)
    }


    void testSearchParameterizations() {
        ParameterizationCacheItem parameterization = new ParameterizationCacheItem(1l, null, 'testName', null, null, null, null, null, null, false, null, null)
        verifyOnlyNameSpecificOrGenericFiltersMatch('test', parameterization)

        assert !new AllFieldsFilter(query: 'not found').accept(parameterization)
        parameterization = new ParameterizationCacheItem(1l, null, 'some other name', null, null, null, null, null, ImmutableList.copyOf([new Tag(name: 'testName'), new Tag(name: 'secondTag')]), false, null, null)
        assert new AllFieldsFilter(query: 'testName').accept(parameterization)
        assert new AllFieldsFilter(query: 'tag:testName').accept(parameterization)
        assert new AllFieldsFilter(query: 'tag:unknown OR name:other').accept(parameterization)
        assert !new AllFieldsFilter(query: 'tag:unknown AND name:other').accept(parameterization)
        assert !new AllFieldsFilter(query: 'unknown AND other').accept(parameterization)
    }


    void testSearchSimulations() {
        SimulationCacheItem simulation = new SimulationCacheItem(1l, 'testName', null, null, null, null, null, null, null, null, null, null, 0, null, 0)
        verifyOnlyNameSpecificOrGenericFiltersMatch('test', simulation)

        assert !new AllFieldsFilter(query: 'not found').accept(simulation)
        assert !new AllFieldsFilter(query: 'name:not found').accept(simulation)


        simulation = new SimulationCacheItem(1l, 'some other name', null, null, ImmutableList.copyOf([new Tag(name: 'testName'), new Tag(name: 'secondTag')]), null, null, null, null, null, null, null, 0, null, 0)
        assert new AllFieldsFilter(query: 'testName').accept(simulation)        //should match tag
        assert !new AllFieldsFilter(query: 'name:testName').accept(simulation)   //name-specific; should not match tag
        assert new AllFieldsFilter(query: 'tag:testName').accept(simulation)    //should match tag
        assert !new AllFieldsFilter(query: 'tag:other').accept(simulation)       //tag-specific; should not match name

        ParameterizationCacheItem parameterization = new ParameterizationCacheItem(1l, null, 'PARAM_NAME', null, null, null, null, null, null, false, null, null)
        simulation = new SimulationCacheItem(1l, 'some other name', parameterization, null, ImmutableList.copyOf([new Tag(name: 'testName'), new Tag(name: 'secondTag')]), null, null, null, null, null, null, null, 0, null, 0)
        assert new AllFieldsFilter(query: 'PARAM_NAME').accept(simulation)     //should match on pn
        assert new AllFieldsFilter(query: 'name:param_').accept(simulation)    //should match on pn

        ResultConfigurationCacheItem resultConfiguration = new ResultConfigurationCacheItem(1l, 'TEMPLATE_NAME', null, null, null, null, null, null)
        simulation = new SimulationCacheItem(1l, 'some other name', parameterization, resultConfiguration, ImmutableList.copyOf([new Tag(name: 'testName'), new Tag(name: 'secondTag')]), null, null, null, null, null, null, null, 0, null, 0)
        assert new AllFieldsFilter(query: 'TEMPLATE_NAME').accept(simulation)  //should match tmpl name
        assert new AllFieldsFilter(query: 'name:template').accept(simulation)  //should match on tmpl

        simulation = new SimulationCacheItem(1l, 'some other name', parameterization, resultConfiguration, ImmutableList.copyOf([new Tag(name: 'paramTestName')]), null, null, null, null, null, null, null, 0, null, 0)
        assert new AllFieldsFilter(query: 'paramTestName').accept(simulation)  //should not match pn's tags
        assert new AllFieldsFilter(query: 'tag:paramtest').accept(simulation)  //should not match pn's tags

        parameterization = new ParameterizationCacheItem(1l, null, 'PARAM_NAME', null, null, null, null, null, null, false, null, 12345l)
        simulation = new SimulationCacheItem(1l, 'some other name', parameterization, resultConfiguration, ImmutableList.copyOf([new Tag(name: 'paramTestName')]), null, null, null, null, null, null, null, 0, null,54321)
        assert !new AllFieldsFilter(query: 't:12345').accept(simulation)        //should not match pn's tags
        assert new AllFieldsFilter(query: 'd:12345').accept(simulation)        //should match pn's deal id
        assert new AllFieldsFilter(query: '12345').accept(simulation)          //should match pn's deal id
        assert !new AllFieldsFilter(query: 'd:2345').accept(simulation)         //but not on partial deal id
        assert !new AllFieldsFilter(query: '2345').accept(simulation)           //but not on partial deal id
        assert new AllFieldsFilter(query: 'seed=54321').accept(simulation)      //should match exact randomSeed value
        assert !new AllFieldsFilter(query: 'seed:4321').accept(simulation)      //but not on partial value
    }

    void testSearchResources() {
        ResourceCacheItem resource = new ResourceCacheItem(1l, 'testName', null, null, null, null, null, null, null, false, null)
        verifyOnlyNameSpecificOrGenericFiltersMatch('TEST', resource)

        assert !new AllFieldsFilter(query: 'not found').accept(resource)
        assert !new AllFieldsFilter(query: 'tag:found').accept(resource)
        assert !new AllFieldsFilter(query: 'state:found').accept(resource)
        assert !new AllFieldsFilter(query: 'dealid:found').accept(resource)
        assert !new AllFieldsFilter(query: 'owner:found').accept(resource)

        resource = new ResourceCacheItem(1l, 'some other name', null, null, null, null, null, null, ImmutableList.copyOf([new Tag(name: 'testName'), new Tag(name: 'secondTag')]), false, null)
        assert new AllFieldsFilter(query: 'testName').accept(resource)
        assert new AllFieldsFilter(query: 'tag:testName').accept(resource)
        assert !new AllFieldsFilter(query: 'name:testName').accept(resource)
        assert new AllFieldsFilter(query: 'name:testName OR TAG:TESTNAME').accept(resource)
        assert new AllFieldsFilter(query: 'name:other AND tag:second').accept(resource)
    }

    void testSearchWithMultipleValues() {
        ResourceCacheItem resource1 = new ResourceCacheItem(1l, 'firstName', null, new VersionNumber('1'), null, null, null, null, null, false, null)
        ResourceCacheItem resource2 = new ResourceCacheItem(1l, 'secondName', null, new VersionNumber('2'), null, null, null, null, null, false, null)
        ResourceCacheItem resource3 = new ResourceCacheItem(1l, 'firstName', null, new VersionNumber('3'), null, null, null, null, null, false, null)
        AllFieldsFilter filter = new AllFieldsFilter(query: 'firstName v1 OR secondName OR thirdName')
        assert filter.accept(resource1)
        assert filter.accept(resource2)
        assert !filter.accept(resource3)

        assert new AllFieldsFilter(query: 'name:first AND name:v1').accept(resource1)
        assert !new AllFieldsFilter(query: 'name:first AND name:v2').accept(resource1)
        assert new AllFieldsFilter(query: 'name:first OR name:v2').accept(resource1)

    }
    // Supply sim, pn or resource with given name-fragment
    // Checks that only a generic filters or a name-specific one will match fragment via the name
    private static void verifyOnlyNameSpecificOrGenericFiltersMatch(String nameFragment, def modellingItem) {

        //Checks are pointless unless item name contains supplied name fragment
        assert StringUtils.containsIgnoreCase(modellingItem.name, nameFragment)

        //Generic filter should match by name
        assert (new AllFieldsFilter(query: nameFragment)).accept(modellingItem)
        //Name-specific filter should match by name
        assert (new AllFieldsFilter(query: 'name:' + nameFragment)).accept(modellingItem)
        assert (new AllFieldsFilter(query: 'n:' + nameFragment)).accept(modellingItem)
        //Other-column-specific filters should not match by name
        assert !(new AllFieldsFilter(query: 'dealid:' + nameFragment)).accept(modellingItem)
        assert !(new AllFieldsFilter(query: 'd:' + nameFragment)).accept(modellingItem)
        assert !(new AllFieldsFilter(query: 'owner:' + nameFragment)).accept(modellingItem)
        assert !(new AllFieldsFilter(query: 'o:' + nameFragment)).accept(modellingItem)
        assert !(new AllFieldsFilter(query: 'state:' + nameFragment)).accept(modellingItem)
        assert !(new AllFieldsFilter(query: 's:' + nameFragment)).accept(modellingItem)
        assert !(new AllFieldsFilter(query: 'tag:' + nameFragment)).accept(modellingItem)
        assert !(new AllFieldsFilter(query: 't:' + nameFragment)).accept(modellingItem)
    }
}
