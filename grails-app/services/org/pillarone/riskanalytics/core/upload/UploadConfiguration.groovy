package org.pillarone.riskanalytics.core.upload

import org.pillarone.riskanalytics.core.simulation.item.Simulation

class UploadConfiguration {

    final String username
    final String destination
    final boolean allowOverwrite
    final Simulation simulation

    UploadConfiguration(Simulation simulation, boolean allowOverwrite, String destination, String username) {
        this.simulation = simulation
        this.allowOverwrite = allowOverwrite
        this.destination = destination
        this.username = username
    }


}
