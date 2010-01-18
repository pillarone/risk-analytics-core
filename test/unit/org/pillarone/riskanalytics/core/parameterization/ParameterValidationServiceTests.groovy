package org.pillarone.riskanalytics.core.parameterization

class ParameterValidationServiceTests extends GroovyTestCase {

    ParameterValidationService service

    protected void setUp() {
        super.setUp();
        service = new ParameterValidationService()
    }

    void testNoValidatorsRegistered() {
        assertNull service.validate(new Object())
    }

    void testOneSingletonValidatorRegistered() { 
        def singleton = new Object()
        service.register(singleton) { ["errorkey", 1, 2] }
        def errors = service.validate(singleton)
        assertEquals 1, errors.size()
        assert errors[0].toString().contains('errorkey') 
    }

    void testSingletonRegisteredTwoValidators() {
        def singleton = new Object()
        service.register(singleton) { ["errorkey", 1, 2] }
        service.register(singleton) { ["otherkey", 1, 2] }
        assertEquals 2, service.validate(singleton).size()
    }

    void testSingletonRegisteredTwoNonFailingValidators() {
        def singleton = new Object()
        service.register(singleton) { true }
        service.register(singleton) { null }
        assertNull service.validate(singleton)
    }

    void testClassValidatorRegistered() {
        service.register(Object) { ["errorkey", 1, 2] }
        assertEquals 1, service.validate(new Object()).size()
    }

    void testEqualValidatorRegistered() {
        String one = "x" * 256
        String two = "x" * 256
        assert ! one.is(two)
        assert one == two
        service.register(one) { ["errorkey", 1, 2] }
        assertEquals 1, service.validate(two).size()
    }

    void testIsCaseValidatorRegistered() {
        def key = 0..10
        def obj = 5
        assert key.isCase(obj)
        service.register(key) { ["errorkey", 1, 2] }
        assertEquals 1, service.validate(obj).size()
    }



}