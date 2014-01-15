package org.pillarone.riskanalytics.core.util

import org.springframework.core.type.filter.AssignableTypeFilter


class RegistryInitializationSupport {

    private static Set<String> packages = new HashSet()

    private static boolean initializing = true

    static {
        packages << "org.pillarone"
    }

    public static void addBasePackage(String name) {
        if (initializing) {
            packages << name
        } else {
            throw new IllegalStateException("Cannot add base package ${name} after initialization is finished")
        }
    }

    public static <E> List<Class<E>> findClasses(Class<E> assignableFrom) {
        initializing = false

        ClassPathScanner scanner = new ClassPathScanner()
        scanner.addIncludeFilter(new AssignableTypeFilter(assignableFrom))

        List<Class> result = []

        for(String packageName in packages) {
            result.addAll(scanner.findCandidateComponents(packageName).collect { Thread.currentThread().contextClassLoader.loadClass(it.beanClassName) })
        }

        return result.findAll { !it.isAnnotationPresent(Manual) }
    }
}
