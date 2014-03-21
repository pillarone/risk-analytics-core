package org.pillarone.riskanalytics.core.util

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import grails.util.Holders

@TestMixin(GrailsUnitTestMixin)
class RegistryInitializationSupportTests {

    def config

    void setUp() {
        config = Holders.config
        RegistryInitializationSupport.instance = null
    }

    void tearDown() {
        Holders.config = config
        RegistryInitializationSupport.instance = null
    }

    void testDefaultInit() {
        def instance = RegistryInitializationSupport.instance
        assert instance.packages == ['org.pillarone'] as Set<String>
    }

    void testInitByConfig() {
        Holders.config.autoRegistrationBasePackages = 'com.blabla.howareyou'
        RegistryInitializationSupport instance = RegistryInitializationSupport.instance
        assert instance.packages == ['org.pillarone', 'com.blabla.howareyou'] as Set<String>
    }

    void testInitBySystemProperty() {
        Holders.config = null
        System.setProperty('autoRegistrationBasePackages', 'com.blablub')
        RegistryInitializationSupport instance = RegistryInitializationSupport.instance
        assert instance.packages == ['org.pillarone', 'com.blablub'] as Set<String>
    }
}
