package org.pillarone.riskanalytics.core.parameterization

import org.pillarone.riskanalytics.core.parameterization.validation.AbstractParameterValidationService
import org.pillarone.riskanalytics.core.parameterization.validation.ParameterValidationError

class ParameterValidationServiceTests extends GroovyTestCase {

    AbstractParameterValidationService service

    protected void setUp() {
        super.setUp();
        service = new TestValidationService()
    }

    void testNoValidatorsRegistered() {
        assertEquals 0, service.validate(new Object()).size()
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
        assertEquals 0, service.validate(singleton).size()
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

class TestValidationError extends ParameterValidationError {

    def TestValidationError(message, arguments) {
        super(message, arguments);
    }

    String getLocalizedMessage(Locale locale) {
        return msg;
    }

}

class TestValidationService extends AbstractParameterValidationService {

    ParameterValidationError createErrorObject(String msg, List args) {
        return new TestValidationError(msg, args);
    }

}