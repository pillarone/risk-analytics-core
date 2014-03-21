package org.pillarone.riskanalytics.core.util

import grails.util.Holders
import groovy.util.logging.Log
import org.springframework.core.type.filter.AssignableTypeFilter

@Singleton(lazy = true)
@Log
class RegistryInitializationSupport {
    public static final String AUTO_REGISTRATION_BASE_PACKAGES = 'autoRegistrationBasePackages'
    public static final String DEFAULT_PACKAGE = 'org.pillarone'
    private final Set<String> packages = new HashSet()

    private RegistryInitializationSupport() {
        packages << DEFAULT_PACKAGE
        try {
            log.info("trying to get additionDal packages from config")
            //this will fail on external grid nodes and it will also fail during deserializing objects which call the findClasses method in static init blocks. There cannot be a Holder.config at this point.
            if (Holders.config.containsKey(AUTO_REGISTRATION_BASE_PACKAGES)) {
                packages.addAll([Holders.config."$AUTO_REGISTRATION_BASE_PACKAGES"].flatten() as Collection<String>)
            }
        } catch (Throwable ignored) {
            log.warning("could not get additional packages from config. Trying from system property 'autoRegistrationBasePackages'.")
            def property = System.getProperty(AUTO_REGISTRATION_BASE_PACKAGES, null)
            if (property) {
                packages.addAll(property.split(','))
            } else {
                log.warning("could not get additional packages from system property 'autoRegistrationBasePackages'.")
            }
        }
        log.info("packages which are registered: $packages")
    }

    public <E> List<Class<E>> findClasses(Class<E> assignableFrom) {
        ClassPathScanner scanner = new ClassPathScanner()
        scanner.addIncludeFilter(new AssignableTypeFilter(assignableFrom))
        List<Class> result = []
        for (String packageName in packages) {
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
