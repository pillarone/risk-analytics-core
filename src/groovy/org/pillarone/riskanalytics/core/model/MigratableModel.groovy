package org.pillarone.riskanalytics.core.model

import org.pillarone.riskanalytics.core.simulation.item.VersionNumber


public interface MigratableModel {

    VersionNumber getVersion()

}