package org.pillarone.riskanalytics.core.resultnavigator.resolver

import org.pillarone.riskanalytics.core.resultnavigator.ICategoryResolver
import org.pillarone.riskanalytics.core.resultnavigator.OutputElement

/**
 * Applies the logical OR to all resolvers set as children.
 * As resolved value the first resolved value when iterating through the child resolvers
 * is returned. In case there is no unique common resolved value null is returned.
 * @author martin.melchior
 */
class OrResolver implements ICategoryResolver {
    static final String NAME = "or"
    List<ICategoryResolver> children

    OrResolver() {
        this.children = []
    }

    OrResolver(List<ICategoryResolver> children) {
        this.children = children
    }

    String getName() {
        return NAME
    }

    void addChildResolver(ICategoryResolver resolver) {
        children << resolver
    }

    boolean isResolvable(OutputElement element) {
        for (ICategoryResolver child : children) {
            if(child.isResolvable(element)) {
                return true
            }
        }
        return false
    }

    String getResolvedValue(OutputElement element) {
        for (ICategoryResolver child : children) {
            if(child.isResolvable(element)) {
                return child.getResolvedValue(element)
            }
        }
        return null
    }

    boolean createTemplatePath(OutputElement element, String category) {
        if (!isResolvable(element)) return
        for (ICategoryResolver resolver in children) {
            if (resolver.createTemplatePath(element, category)) return true
        }
        return false
    }
}
