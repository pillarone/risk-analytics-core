package org.pillarone.riskanalytics.core.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentCategory {

    String[] categories();

}
