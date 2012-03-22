package org.pillarone.riskanalytics.core.resultnavigator.resolver

import org.pillarone.riskanalytics.core.resultnavigator.ICategoryResolver
import org.pillarone.riskanalytics.core.resultnavigator.OutputElement

/**
 * With this resolver you can specify an element to be resolved provided that
 * it contains a non-null entry for the specified category.
 * It returns the value found for that category as resolved value.
 * @author martin.melchior
 */
class SynonymToCategoryResolver implements ICategoryResolver {
    static final String NAME = "synonymousTo"
    static final String EXCEPTION_MSG = "The synonymousTo resolver should be initialized with the name of the category this category is synonymous with."

    String category

    SynonymToCategoryResolver(String category) {
        this.category = category
    }

    String getName() {
        return NAME
    }

    void addChildResolver(ICategoryResolver resolver) {
    }

    boolean isResolvable(OutputElement element) {
        return element.getCategoryValue(category) != null
    }

    String getResolvedValue(OutputElement element) {
        return element.getCategoryValue(category)
    }

    boolean createTemplatePath(OutputElement element, String category) {
        return false
    }
}
