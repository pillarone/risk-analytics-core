package org.pillarone.riskanalytics.core.output

class PostSimulationCalculation {

    public static final String MEAN = 'mean'
    public static final String STDEV = 'stdev'
    public static final String PERCENTILE = 'percentile'
    public static final String VAR = 'var'
    public static final String TVAR = 'tvar'
    public static final String PDF = 'pdf'
    public static final String IS_STOCHASTIC = 'is stochastic'

    private static List VALUES = [MEAN, STDEV, PERCENTILE, VAR, TVAR,PDF, IS_STOCHASTIC]

    static belongsTo = [run: SimulationRun]

    String keyFigure
    BigDecimal keyFigureParameter
    PathMapping path
    FieldMapping field
    CollectorMapping collector
    Integer period
    Double result

    static constraints = {
        keyFigure(inList: VALUES)
        keyFigureParameter(nullable: true)
    }
}