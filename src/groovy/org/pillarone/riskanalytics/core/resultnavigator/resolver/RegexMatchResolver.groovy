package org.pillarone.riskanalytics.core.resultnavigator.resolver

import java.util.regex.Pattern
import org.pillarone.riskanalytics.core.resultnavigator.ICategoryResolver
import org.pillarone.riskanalytics.core.resultnavigator.OutputElement

/**
 * Is resolvable if substring of the given path is matched by a regex
 * If a given path is identified as resolvable, the string found in the <code>n</code>-th group
 * where <code>n</code> can be defined by the through the parameter groupDefiningMemberName. -
 * See how to use regular expressions and java.util.regex.Pattern for further details.
 *
 * @author martin.melchior
 */
class RegexMatchResolver implements ICategoryResolver {
    static final String NAME = "matchRegex"
    static final String EXCEPTION_MSG = """The regex resolver should be initialized with a regex \n
                            and an integer that denotes the index of the group that defines the value."""

    Pattern pattern
    String refCategory = OutputElement.PATH
    int groupDefiningMemberName

    RegexMatchResolver(String regex, int groupDefiningMemberName) {
        this(regex, groupDefiningMemberName, OutputElement.PATH)
    }

    RegexMatchResolver(String regex, int groupDefiningMemberName, String refCategory) {
        pattern = ~regex
        this.refCategory = refCategory
        this.groupDefiningMemberName = groupDefiningMemberName
    }

    String getName() {
        return NAME
    }

    void addChildResolver(ICategoryResolver resolver) {
    }

    boolean isResolvable(OutputElement element) {
        if (element.getCategoryValue(refCategory)==null) return false
        java.util.regex.Matcher matcher = element.getCategoryValue(refCategory) =~ pattern
        return matcher.size()>0
    }

    String getResolvedValue(OutputElement element) {
        if (element.getCategoryValue(refCategory)==null) return null
        java.util.regex.Matcher matcher = element.getCategoryValue(refCategory) =~ pattern
        return matcher.size()>0 ? matcher[0][groupDefiningMemberName] : null
    }

    boolean createTemplatePath(OutputElement element, String category) {
        if (refCategory != OutputElement.PATH) return false
        if (element.templatePath==null) {
            element.templatePath=new String(element.getPath())
        }
        java.util.regex.Matcher matcher = element.getTemplatePath() =~ pattern
        if (matcher.size()==0) return false
        String originalString = matcher[0][0]
        int size = originalString.length()
        int index = element.templatePath.indexOf(originalString)
        StringBuffer buffer = new StringBuffer()
        buffer.append(element.templatePath[0..(index-1)])
        for (int i = 1; i < groupDefiningMemberName; i++) {
            buffer.append(matcher[0][i])
        }
        buffer.append("\${${category}}")
        for (int i = groupDefiningMemberName; i <= matcher.groupCount(); i++) {
            buffer.append(matcher[0][i])
        }
        buffer.append(element.templatePath[(index+size)..-1])
        element.templatePath = buffer.toString()
        return true
    }
}
