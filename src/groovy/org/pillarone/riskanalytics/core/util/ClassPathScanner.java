package org.pillarone.riskanalytics.core.util;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ClassPathScanner extends ClassPathScanningCandidateComponentProvider {

    public ClassPathScanner() {
        super(false);
    }

    @Override
    protected boolean isCandidateComponent(MetadataReader metadataReader) throws IOException {
        try {
            return super.isCandidateComponent(metadataReader);
        } catch (FileNotFoundException e) {
            return false;
        }
    }
}
