package org.pillarone.riskanalytics.core.parameterization.global;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@Retention(value = RetentionPolicy.RUNTIME)
public @interface Global {

    String identifier();

}