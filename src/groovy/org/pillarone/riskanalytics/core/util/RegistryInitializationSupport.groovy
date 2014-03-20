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
        PackageProvider packageProvider
        try {
            packageProvider = Holders.grailsApplication?.mainContext?.getBean('packageProvider', PackageProvider)
        } catch (BeansException ignored) {
            log.warning('could not get PackageProvider from mainContext. Try to configure packages directly from config.')
        }
        if (packageProvider) {
            //We configure the package via a spring bean, because we have to do it on external grid nodes. There we have no config object.
            PACKAGES.addAll(packageProvider.packages)
        } else {
            //This is the fallback if we do not have a packageProvider in the applicationContext, e.g. in unit tests.
            PACKAGES.addAll([Holders.config.autoRegistrationBasePackages, 'org.pillarone'].flatten() - null as Set<String>)
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
