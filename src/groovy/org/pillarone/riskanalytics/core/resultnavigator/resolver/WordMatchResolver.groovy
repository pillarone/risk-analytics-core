package org.pillarone.riskanalytics.core.resultnavigator.resolver

import java.util.regex.Pattern
import org.pillarone.riskanalytics.core.resultnavigator.ICategoryResolver
import org.pillarone.riskanalytics.core.resultnavigator.OutputElement

/**
 * Returns <code>isResolved = true</code> if one of the words specified in the 'toMatch'-list is found
 * in the path of the output element. As resolved value it returns the first matched word found.
 *
 * @author martin.melchior
 */
class WordMatchResolver implements ICategoryResolver {
    static final String NAME = "matching"
    static final String EXCEPTION_MSG = "The matching word resolver should be initialized with a List of words to match."

    /**
     * the resolver may refer to A reference category can be set (which is by default the path)
     */
    String refCategory = OutputElement.PATH
    List<String> toMatch = []

    private List<Pattern> patterns

    WordMatchResolver(List<String> toMatch) {
        this(toMatch, OutputElement.PATH)
    }

    WordMatchResolver(List<String> toMatch, String refCategory) {
        this.refCategory = refCategory
        initialize(toMatch)
    }

    void initialize(List<String> toMatch) {
        this.toMatch = toMatch
        patterns = []
        for (String s : toMatch) {
            patterns.add(~s)
        }
    }

    String getName() {
        return NAME
    }

    void addChildResolver(ICategoryResolver resolver) {
    }

    boolean isResolvable(OutputElement element) {
        if (element.getCategoryValue(refCategory)==null) return false
        boolean isFound = false
        for (Pattern pattern : patterns) {
            java.util.regex.Matcher matcher = element.getCategoryValue(refCategory) =~ pattern
            if (matcher.size()>0) {
                if (isFound) {
                    return false
                }
                isFound = true
            }
        }
        return isFound
    }

    String getResolvedValue(OutputElement element) {
        if (element.getCategoryValue(refCategory)==null) return null
        boolean isFound = false
        String value = null
        for (int i = 0; i < patterns.size(); i++) {
            java.util.regex.Matcher matcher = element.getCategoryValue(refCategory) =~ patterns[i]
            if (matcher.size()>0) {
                if (isFound) {
                    return null
                }
                isFound = true
                value = toMatch[i]
            }
        }
        return value
    }

    boolean createTemplatePath(OutputElement element, String category) {
        try {
            if (refCategory != OutputElement.PATH) return false
            if (element.templatePath==null) {
                element.templatePath=new String(element.getPath())
            }
            String originalString = getResolvedValue(element)
            int size = originalString.length()
            int index = element.templatePath.indexOf(originalString)
            StringBuffer buffer = new StringBuffer()
            buffer.append(element.templatePath[0..(index-1)])
            buffer.append("\${${category}}")
            if (index+size<element.templatePath.size()) {
                buffer.append(element.templatePath[(index+size)..-1])
            }
            element.templatePath = buffer.toString()
            return true
        } catch (Exception ex) {
            return false
        }
    }
}
