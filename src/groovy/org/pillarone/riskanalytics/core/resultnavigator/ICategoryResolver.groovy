package org.pillarone.riskanalytics.core.resultnavigator

/**
 * Interface used for identifying model paths to match certain conditions and, if there is a match,
 * assigning a suitable value.
 * Often, the identification is implemented in form of matching suitable words or regex occurring
 * in the path. In this case, it allows to replace the matching part by a suitable wild-card in the form
 * ${category}.
 *
 * @author martin.melchior
 */
public interface ICategoryResolver {
    /**
     * Returns true if the condition is fulfilled so that the category for which this resolver is used
     * can be assigned.
     *
     * @param element
     * @return
     */
    boolean isResolvable(OutputElement element)

    /**
     * Return the resolved value, e.g. the matching part.
     * @param element
     * @return
     */
    String getResolvedValue(OutputElement element)

    /**
     * Replaces the matching part by a suitable wild card of the form ${category}.
     * In this way a template path can be constructed (possibly, by subsequent application
     * of the the same method from different ICategoryResolver object.
     * This kind of template path can then be efficiently evaluated by inserting different values
     * for the categories in the form of a GString.
     * @param element
     * @param category
     * @return
     */
    boolean createTemplatePath(OutputElement element, String category)

    /**
     * Returns the name of the resolver implementation. This is primarily used
     * in the CategoryResolverFactory and in combination with the MapCategoriesBuilder.
     * @return
     */
    String getName()

    /**
     * Some category resolvers can depend on one or many other category resolvers (like OrResolver, AndResolver).
     * This method can be used to inject such other resolvers.
     * @param resolver
     */
    void addChildResolver(ICategoryResolver resolver)
}

