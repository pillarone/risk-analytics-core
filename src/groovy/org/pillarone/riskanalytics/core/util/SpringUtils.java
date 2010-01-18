package org.pillarone.riskanalytics.core.util;

import org.codehaus.groovy.grails.web.context.ServletContextHolder;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.springframework.context.ApplicationContext;

public class SpringUtils {

    /**
     * Get a spring bean by name.
     * Only works if a servlet context is available.
     * @param name the instance name, e.g. "myService"
     * @return the bean instance
     * @throws NullPointerException if context is not available
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(final String name) {
        ApplicationContext ctx =
                (ApplicationContext) ServletContextHolder.getServletContext().getAttribute(
                        GrailsApplicationAttributes.APPLICATION_CONTEXT);
        return (T) ctx.getBean(name);
    }
}
