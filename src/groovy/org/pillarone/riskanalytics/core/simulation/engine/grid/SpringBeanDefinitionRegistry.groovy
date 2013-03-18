package org.pillarone.riskanalytics.core.simulation.engine.grid

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.BeanFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.ApplicationContext


class SpringBeanDefinitionRegistry {

    private static Set<String> beanDefinitionNames = new HashSet<String>()

    public static void registerBean(String name) {
        beanDefinitionNames << name
    }

    public static Map<String, BeanDefinition> getRequiredBeanDefinitions() {
        Map result = [:]

        BeanFactory beanFactory = ApplicationHolder.application.mainContext.beanFactory
        for (String name in beanDefinitionNames) {
            result.put(name, beanFactory.getBeanDefinition(name))
        }

        return result
    }

}
