package org.pillarone.riskanalytics.core.util

import org.springframework.core.type.filter.AssignableTypeFilter


class RegistryInitializationSupport {

    private static Set<String> packages = new HashSet()

    static {
        packages << "org.pillarone"
    }

    public static void addBasePackage(String name) {
        packages << name
    }

    public static <E> List<Class<E>> findClasses(Class<E> assignableFrom) {
        ClassPathScanner scanner = new ClassPathScanner()
        scanner.addIncludeFilter(new AssignableTypeFilter(assignableFrom))

        List<Class> result = []

        for(String packageName in packages) {
            result.addAll(scanner.findCandidateComponents(packageName).collect { Thread.currentThread().contextClassLoader.loadClass(it.beanClassName) })
        }

        return result.findAll { !it.isAnnotationPresent(Manual) }
    }
}
