package org.pillarone.riskanalytics.core.util

import grails.util.Holders
import groovy.util.logging.Log
import org.springframework.beans.BeansException
import org.springframework.core.type.filter.AssignableTypeFilter

@Singleton(lazy = true)
@Log
class RegistryInitializationSupport {
    private final Set<String> PACKAGES = new HashSet()

    private RegistryInitializationSupport() {
        try {
            PackageProvider packageProvider = Holders.grailsApplication.mainContext.getBean('packageProvider', PackageProvider)
            PACKAGES.addAll(packageProvider.packages)
        } catch (BeansException ignored) {
            log.warning('could not get PackageProvider from mainContext')
            PACKAGES << 'org.pillarone'
        }
    }

    public <E> List<Class<E>> findClasses(Class<E> assignableFrom) {
        ClassPathScanner scanner = new ClassPathScanner()
        scanner.addIncludeFilter(new AssignableTypeFilter(assignableFrom))
        List<Class> result = []
        for (String packageName in PACKAGES) {
            result.addAll(scanner.findCandidateComponents(packageName).collect {
                Thread.currentThread().contextClassLoader.loadClass(it.beanClassName)
            })
        }
        return result.findAll { !it.isAnnotationPresent(Manual) }
    }
}

class PackageProvider {
    Set<String> packages = new HashSet<String>()
}
