package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.simulation.item.Simulation

class UploadConfiguration {

    UploadConfiguration(Simulation simulation) {
        this.simulation = simulation
    }

    final Simulation simulation
}
