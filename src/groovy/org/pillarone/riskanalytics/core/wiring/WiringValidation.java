package org.pillarone.riskanalytics.core.wiring;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WiringValidation {

    int[] connections();

    int[] packets();
}
