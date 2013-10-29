package org.pillarone.riskanalytics.core.simulation.item

import org.junit.Test
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.example.model.EmptyModel
import org.pillarone.riskanalytics.core.example.parameter.ExampleEnum
import org.pillarone.riskanalytics.core.example.parameter.ExampleParameterObjectClassifier
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.CollectingModeFactory
import org.pillarone.riskanalytics.core.output.PacketCollector
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.parameterization.SimpleMultiDimensionalParameter
import org.pillarone.riskanalytics.core.parameterization.TableMultiDimensionalParameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory

import static org.junit.Assert.*

class ItemComparatorTests {

    @Test
    void testCompareTemplates() {
        ResultConfiguration a = new ResultConfiguration("a")
        ResultConfiguration b = new ResultConfiguration("b")
        assertTrue ItemComparator.contentEquals(a, b)

        a.collectors.add(new PacketCollector(path: "path", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)))
        assertFalse ItemComparator.contentEquals(a, b)

        b.collectors.add(new PacketCollector(path: "path", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)))
        assertTrue ItemComparator.contentEquals(a, b)

        a.collectors.add(new PacketCollector(path: "path2", mode: CollectingModeFactory.getStrategy(AggregatedCollectingModeStrategy.IDENTIFIER)))
        b.collectors.add(new PacketCollector(path: "path2", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)))
        assertFalse ItemComparator.contentEquals(a, b)

        a.collectors.clear()
        b.collectors.clear()

        a.collectors.add(new PacketCollector(path: "path", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)))
        b.collectors.add(new PacketCollector(path: "path2", mode: CollectingModeFactory.getStrategy(SingleValueCollectingModeStrategy.IDENTIFIER)))
        assertFalse ItemComparator.contentEquals(a, b)
    }

    @Test
    void testSimplePositiveCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testSimplePositiveCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, 1.1))
        item.addParameter(ParameterHolderFactory.getHolder('path2', 0, 2))
        item.addParameter(ParameterHolderFactory.getHolder('path3', 0, 'text'))
        item.addParameter(ParameterHolderFactory.getHolder('path4', 0, ExampleEnum.FIRST_VALUE))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testSimplePositiveCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, 1.1))
        item2.addParameter(ParameterHolderFactory.getHolder('path2', 0, 2))
        item2.addParameter(ParameterHolderFactory.getHolder('path3', 0, 'text'))
        item2.addParameter(ParameterHolderFactory.getHolder('path4', 0, ExampleEnum.FIRST_VALUE))

        assertTrue ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testSimplePositiveWithMultiplePeriodsCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testSimplePositiveWithMultiplePeriodsCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, 1.1))
        item.addParameter(ParameterHolderFactory.getHolder('path2', 0, 2))
        item.addParameter(ParameterHolderFactory.getHolder('path', 1, 2.2))
        item.addParameter(ParameterHolderFactory.getHolder('path2', 1, 3))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testSimplePositiveWithMultiplePeriodsCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, 1.1))
        item2.addParameter(ParameterHolderFactory.getHolder('path2', 0, 2))
        item2.addParameter(ParameterHolderFactory.getHolder('path', 1, 2.2))
        item2.addParameter(ParameterHolderFactory.getHolder('path2', 1, 3))

        assertTrue ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithDifferentCountCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testSimpleNegativeCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, 1.1))
        item.addParameter(ParameterHolderFactory.getHolder('path2', 0, 2))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testSimpleNegativeCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, 1.1))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithSameCountButDifferentTypeCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithSameCountButDifferentTypeCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, 2))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithSameCountButDifferentTypeCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, 1.1))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithSameCountButDifferentPathCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithSameCountButDifferentPathCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, 2))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithSameCountButDifferentPathCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path2', 0, 2))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testPositiveWithParameterObjectCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testPositiveWithParameterObjectCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0d, "p2": 1d])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testPositiveWithParameterObjectCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0d, "p2": 1d])))

        assertTrue ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithDifferentParameterObjectTypeCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithDifferentParameterObjectTypeCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0d, "p2": 1d])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithDifferentParameterObjectTypeCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, ExampleParameterObjectClassifier.TYPE2.getParameterObject(["p1": 0d, "p2": 1d, "p3": 2d])))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithDifferentParameterObjectParametersCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithDifferentParameterObjectParametersCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0d, "p2": 1d])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithDifferentParameterObjectParametersCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, ExampleParameterObjectClassifier.TYPE1.getParameterObject(["p1": 0d, "p2": 2d])))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testPositiveWithMultiDimensionalParametersCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testPositiveWithMultiDimensionalParametersCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([[1, 2], [3, 4]], ['title1', 'title2'])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testPositiveWithMultiDimensionalParametersCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([[1, 2], [3, 4]], ['title1', 'title2'])))

        assertTrue ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithMultiDimensionalWithDifferentTypesCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithMultiDimensionalWithDifferentTypesCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, new SimpleMultiDimensionalParameter([[1, 2], [3, 4]])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithMultiDimensionalWithDifferentTypesCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([[1, 2], [3, 4]], ['title1', 'title2'])))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithMultiDimensionalWithDifferentStructureCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithMultiDimensionalWithDifferentStructureCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([1, 2, 3, 4], ['title1', 'title2'])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithMultiDimensionalWithDifferentStructureCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([[1, 2], [3, 4]], ['title1', 'title2'])))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithMultiDimensionalWithDifferentValuesCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithMultiDimensionalWithDifferentValuesCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([1, 2, 3, 4], ['title1', 'title2'])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithMultiDimensionalWithDifferentValuesCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([1, 2, 3, 5], ['title1', 'title2'])))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testNegativeWithMultiDimensionalWithDifferentTitlesCompareParameterizations() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testNegativeWithMultiDimensionalWithDifferentTitlesCompareParameterizations'
        dao.modelClassName = 'modelClass'
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([1, 2, 3, 4], ['title1', 'title2'])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testNegativeWithMultiDimensionalWithDifferentTitlesCompareParameterizations'
        dao2.modelClassName = 'modelClass'
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, new TableMultiDimensionalParameter([1, 2, 3, 4], ['title1', 'title3'])))

        assertFalse ItemComparator.contentEquals(item, item2)
    }

    @Test
    void testPositivePMO768() {
        ParameterizationDAO dao = new ParameterizationDAO()
        dao.name = 'testPositivePMO768'
        dao.modelClassName = EmptyModel.name
        dao.itemVersion = '1'
        dao.periodCount = 1

        Parameterization item = new Parameterization(dao.name)
        item.addParameter(ParameterHolderFactory.getHolder('path', 0, new SimpleMultiDimensionalParameter([1])))

        ParameterizationDAO dao2 = new ParameterizationDAO()
        dao2.name = 'testPositivePMO768'
        dao2.modelClassName = EmptyModel.name
        dao2.itemVersion = '2'
        dao2.periodCount = 1

        Parameterization item2 = new Parameterization(dao2.name)
        item2.versionNumber = new VersionNumber(dao2.itemVersion)
        item2.addParameter(ParameterHolderFactory.getHolder('path', 0, new SimpleMultiDimensionalParameter([[1]])))

        assertTrue ItemComparator.contentEquals(item, item2)
    }

}
