package org.pillarone.riskanalytics.core.resultnavigator

import groovy.text.GStringTemplateEngine
import groovy.text.Template

/**
 * The wild card path consists of a template path with suitable variable sub-sections
 * referred to as wild cards. These sub-sections are identified by '${category}'
 * which are interpreted by the GStringTemplateEngine.
 * The available wild cards (categories) for which a value can be set in the template
 * are hold in the list
 *
 * @author martin.melchior
 */
class WildCardPath {

    /**
     * The string that is used as template.
     */
    String templatePath

    /**
     * The list of strings that occur as modifiable wild cards in the path.
     */
    List<String> pathWildCards

    // map category <-> wildcard values
    private Map<String, List<String>> wildCardsMap
    private Template template

    /**
     * Initialize the object with a given templatePath and a list of strings that should be considered as
     * wild cards.
     * @param spec
     * @param pathWildCards
     */
    void initialize(String templatePath, List<String> pathWildCards) {
        this.templatePath = templatePath
        GStringTemplateEngine engine = new GStringTemplateEngine()
        this.template = engine.createTemplate(templatePath)
        this.pathWildCards = pathWildCards
        this.wildCardsMap = [:]
        for (String wildCard : pathWildCards) {
            wildCardsMap[wildCard] = []
        }
    }

    /**
     * Method to return all the wild cards available for this class.
     * It differs from the wild cards occurring in the template path by
     * wild cards that are not associated with the templatePath but rather
     * by synonyms to the field.
     *
     * @return all the keys included in the map.
     */
    List<String> getAllWildCards() {
        return wildCardsMap.keySet().asList()
    }

    /**
     * Method to return the possible values for a given wild card element defined.
     * @param category
     * @return
     */
    List<String> getWildCardValues(String category) {
        return wildCardsMap[category]
    }

    /**
     * Method to set the possible values for a given wild card element defined for this wild card path.
     * @param category
     * @return
     */
    void addWildCardValue(String category, String value) {
        if (!wildCardsMap.containsKey(category)) {
            wildCardsMap[category] = []
        }
        if (!wildCardsMap[category].contains(value)) {
            this.wildCardsMap[category].add(value)
        }
    }

    /**
     * Method to set the possible values for a given wild card element defined for this wild card path.
     * @param category
     * @return
     */
    void addPathWildCardValue(String category, String value) {
        if (pathWildCards.indexOf(category)<0) {
            pathWildCards.add(category)
        }
        addWildCardValue(category, value)
    }

    /**
     * Compose from the wild card path a specific path by entering the given values
     * into the wild card elements included in this wild card path.
     * @param wildCardValues
     * @return
     */
    String getSpecificPath(Map<String,String> wildCardValues) {
        Map map = [:]
        for (String wc  : pathWildCards) {
            if (wildCardValues.containsKey(wc)) {
                map[wc] = wildCardValues[wc]
            }
        }
        return template.make(map)
    }
}
