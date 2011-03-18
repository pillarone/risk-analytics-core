package org.pillarone.riskanalytics.core.output;

/**
 * @author jessika.walter (at) intuitive-collaboration (dot) com
 */
public enum QuantilePerspective {
    PROFIT, LOSS;

    public String getVarAsString() {
        switch (this) {
            case PROFIT: return PostSimulationCalculation.VAR_PROFIT;
            case LOSS: return PostSimulationCalculation.VAR;
            default: throw new IllegalArgumentException("distribution has to be profit or loss");
        }
    }

    public String getTvarAsString() {
        switch (this) {
            case PROFIT: return PostSimulationCalculation.TVAR_PROFIT;
            case LOSS: return PostSimulationCalculation.TVAR;
            default: throw new IllegalArgumentException("distribution has to be profit or loss");
        }
    }

    public String getPercentileAsString() {
        switch (this) {
            case PROFIT: return PostSimulationCalculation.PERCENTILE_PROFIT;
            case LOSS: return PostSimulationCalculation.PERCENTILE;
            default: throw new IllegalArgumentException("distribution has to be profit or loss");
        }
    }
}