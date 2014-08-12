package org.pillarone.riskanalytics.core.upload

class UploadValidationError {
    static enum REASON {
        NO_PROFILE,
        WRONG_RANDOM_SEED,
        WRONG_NUMBER_OF_ITERATION,
        WRONG_TEMPLATE,
        WRONG_PARAMETER_COUNT,
        WRONG_PARAMETER;
    }

    String path
    REASON reason
    String simulationParameterValue
    String profileParameterValue
}
