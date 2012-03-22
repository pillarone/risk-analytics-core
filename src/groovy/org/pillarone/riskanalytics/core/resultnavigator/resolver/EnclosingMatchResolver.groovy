package org.pillarone.riskanalytics.core.resultnavigator.resolver

import java.util.regex.Pattern
import org.pillarone.riskanalytics.core.resultnavigator.ICategoryResolver
import org.pillarone.riskanalytics.core.resultnavigator.OutputElement

/**
 * Is resolvable if a String of non-zero length is embraced by any pair of strings (prefix,suffix)
 * where both, prefix and suffix can be specified by a list of possibilities.
 * If a given path is identified as resolvable, String embraced by the (prefix,suffix)-pair is returned.
 * @author martin.melchior
 */
class EnclosingMatchResolver implements ICategoryResolver {

    static final String NAME = "enclosedBy"
    static final String EXCEPTION_MSG = "The enclosedBy resolver should be initialized with a List of prefixes and a list of suffix."

    List<String> prefix = []
    List<String> suffix = []
    String refCategory
    Pattern pattern

    EnclosingMatchResolver(String prefix, String suffix) {
        this(prefix,suffix,OutputElement.PATH)
    }

    EnclosingMatchResolver(List<String> prefix, List<String> suffix) {
        this(prefix,suffix, OutputElement.PATH)
    }

    EnclosingMatchResolver(String prefix, String suffix, String refCategory) {
        this.refCategory = refCategory
        initialize([prefix], [suffix])
    }

    EnclosingMatchResolver(List<String> prefix, List<String> suffix, String refCategory) {
        this.refCategory = refCategory
        initialize(prefix, suffix)
    }

    String getName() {
        return NAME
    }

    void addChildResolver(ICategoryResolver resolver) {
    }

    void initialize(List<String> prefix, List<String> suffix) {
        this.prefix = prefix
        this.suffix = suffix

        String pref = prefix[0]
        String str = "($pref"
        for (int i = 1; i < prefix.size(); i++) {
            pref = prefix[i]
            str += "|$pref"
        }
        String suff = suffix[0]
        str += ")(\\w*)($suff"
        for (int i = 1; i < suffix.size(); i++) {
            suff = suffix[i]
            str += "|$suff"
        }
        str += ")"
        pattern = ~str
    }

    boolean isResolvable(OutputElement element) {
        if (element.getCategoryValue(refCategory)==null) return false
        java.util.regex.Matcher matcher = element.getCategoryValue(refCategory) =~ pattern
        return matcher.size()>0
    }

    String getResolvedValue(OutputElement element) {
        if (element.getCategoryValue(refCategory)==null) return null
        java.util.regex.Matcher matcher = element.getCategoryValue(refCategory) =~ pattern
        return matcher.size()>0 ? matcher[0][2] : null
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
        buffer.append(matcher[0][1])
        buffer.append("\${${category}}")
        buffer.append(matcher[0][3])
        buffer.append(element.templatePath[(index+size)..-1])
        element.templatePath = buffer.toString()
        return true
    }
}
