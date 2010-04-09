package org.pillarone.riskanalytics.core.initialization;

/**
 * An interface which enables extensions for starting and stopping non-embedded
 * databases in the standalone mode.
 */
public interface IExternalDatabaseSupport {

    void startDatabase();

    void stopDatabase();
}
