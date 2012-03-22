package org.pillarone.riskanalytics.core.resultnavigator

import org.pillarone.riskanalytics.core.output.SimulationRun

/**
 * Bean-like class that contains the information on how to retrieve a single value result item
 * from a simulation run. In addition, category information can be added in the form of assigning
 * keyword for suitable categories, such as 'MTPL' for 'lob' or 'Storm' for 'peril', etc.
 * These keywords per category are hold in a map with the category as key and the 'keyword' as value.
 * The output path, output field and output collector are also included in this map,
 * but are also available as fields.
 *
 * @author martin.melchior
 */
class OutputElement {
    static final String PATH = "Path"
    static final String FIELD = "Field"
    static final String COLLECTOR = "Collector"
    static final String PERIOD = "period"
    static final String STATISTICS = "statistics"
    static final String STATISTICS_PARAMETER = "parameter"

    SimulationRun run

    String path
    String field
    String collector

    String templatePath
    Map<String,String> categoryMap = [:]  // may also contain elements with null value
    List<String> wildCards
    WildCardPath wildCardPath

    /**
     * Allows to set a value ('keyword') for a given category.
     * @param category
     * @param value
     */
    void addCategoryValue(String category, String value) {
        categoryMap[category] = value
    }

    /**
     * Returns the category value ('keyword') for a given category.
     * @param category
     * @return
     */
    Object getCategoryValue(String category) {
        return categoryMap[category]
    }

    public boolean equals(Object o){
        if (o instanceof OutputElement) {
            OutputElement el = (OutputElement) o
            return el.path.equals(path) && el.field.equals(field) && el.collector.equals(collector) \
                    && (el.categoryMap==null ? categoryMap==null : el.categoryMap.equals(categoryMap))
        }
        return false
    }

    public int hashCode() {
        return path.hashCode()+field.hashCode()+collector.hashCode()
    }

}
