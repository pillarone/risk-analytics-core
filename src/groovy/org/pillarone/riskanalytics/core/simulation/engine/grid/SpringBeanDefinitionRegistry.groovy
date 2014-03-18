package org.pillarone.riskanalytics.core.simulation.engine.grid

import grails.util.Holders
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.support.AbstractApplicationContext

class SpringBeanDefinitionRegistry {

    private final static Set<String> BEAN_DEFINITION_NAMES = new HashSet<String>()

    public static void registerBean(String name) {
        BEAN_DEFINITION_NAMES << name
    }

    public static Map<String, BeanDefinition> getRequiredBeanDefinitions() {
        Map result = [:]
        BeanFactory beanFactory = (Holders.grailsApplication.mainContext as AbstractApplicationContext).beanFactory
        for (String name in BEAN_DEFINITION_NAMES) {
            result.put(name, beanFactory.getBeanDefinition(name))
        }
        return result
    }
}
