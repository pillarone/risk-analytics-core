package org.pillarone.riskanalytics.core.output;

/**
 * Data class which transports a single result from a collecting mode strategy to a output strategy.
 * This class is a 'copy' of SingleValueResult, but it performs better because it is no domain class
 * and it does not have any references to domain classes (id only).
 *
 * Using domain classes instead would generate hibernate overhead.
 */
public class SingleValueResultPOJO {

    private SimulationRun simulationRun;
    private int period;
    private int iteration;
    private long pathId;
    private long collectorId;
    private long fieldId;
    private int valueIndex;
    private Double value;

    public long getCollectorId() {
        return collectorId;
    }

    public void setCollectorId(long collectorId) {
        this.collectorId = collectorId;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public long getPathId() {
        return pathId;
    }

    public void setPathId(long pathId) {
        this.pathId = pathId;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public SimulationRun getSimulationRun() {
        return simulationRun;
    }

    public void setSimulationRun(SimulationRun simulationRun) {
        this.simulationRun = simulationRun;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }
}
