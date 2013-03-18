package org.pillarone.riskanalytics.core.components

import models.core.CoreModel
import org.pillarone.riskanalytics.core.model.Model

/**
 * @author stefan.kunz (at) intuitive-collaboration (dot) com
 */
class ComponentUtilsTests extends GroovyTestCase {

    void testGetNormalizedPath() {
        assertEquals "Universe > Milky Way > Solar System > Earth > Europe > Switzerland",
            ComponentUtils.getNormalizedPath("Universe:subMilkyWay:subSolarSystem:subEarth:subEurope:parmSwitzerland", " > ")
        assertEquals "Heinecken -> Brewery -> Ale", ComponentUtils.getNormalizedPath("Heinecken:subBrewery:outAle", " -> ")
        assertEquals "heinecken > Brewery > Ale", ComponentUtils.getNormalizedPath(":heinecken:subBrewery:outAle", " > ")
    }

    void testGetNormalizedName() {
        assertEquals 'Claims Generator', ComponentUtils.getNormalizedName('subClaimsGenerator')
    }

    void testRemoveNamingConventions() {
        assertEquals 'ClaimsGenerator', ComponentUtils.removeNamingConventions('subClaimsGenerator')
        assertEquals 'Portion', ComponentUtils.removeNamingConventions('parmPortion')
        assertEquals 'Ultimate', ComponentUtils.removeNamingConventions('outUltimate')
        assertEquals 'Ultimate', ComponentUtils.removeNamingConventions('globalUltimate')
        assertEquals 'Ultimate', ComponentUtils.removeNamingConventions('runtimeUltimate')
        assertEquals 'Ultimate', ComponentUtils.removeNamingConventions('Ultimate')
        assertNull ComponentUtils.removeNamingConventions(null)
    }

    void testGetComponentNormalizedName() {
        assertEquals "subEurope", ComponentUtils.getComponentNormalizedName("Universe:subMilkyWay:subSolarSystem:subEarth:subEurope")
        assertEquals "subSolarSystem", ComponentUtils.getComponentNormalizedName("Universe:subMilkyWay:subSolarSystem")
    }

    void testRemoveModelFromPath(){
        Model model = new CoreModel()
        assertEquals("Universe:subMilkyWay:subSolarSystem:subEarth:subEurope", ComponentUtils.removeModelFromPath("Core:Universe:subMilkyWay:subSolarSystem:subEarth:subEurope", model))
        assertEquals("Universe:subMilkyWay:subSolarSystem:subEarth:subEurope", ComponentUtils.removeModelFromPath("Universe:subMilkyWay:subSolarSystem:subEarth:subEurope", model))

    }

}
