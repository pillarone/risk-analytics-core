package org.pillarone.riskanalytics.core.output

class PostSimulationCalculation {

    public static final String MEAN = 'mean'
    public static final String MIN = 'min'
    public static final String MAX = 'max'
    public static final String STDEV = 'stdev'
    //todo(jwa): change later to PERCENTILE_LOSS, VAR_LOSS, TVAR_LOSS
    public static final String PERCENTILE = 'percentile'
    public static final String VAR = 'var'
    public static final String TVAR = 'tvar'
    public static final String PERCENTILE_PROFIT = 'percentileProfitFunction'
    public static final String VAR_PROFIT = 'varProfitFunction'
    public static final String TVAR_PROFIT = 'tvarProfitFunction'
    public static final String PDF = 'pdf'
    public static final String IS_STOCHASTIC = 'is stochastic'

    private
    static
    final List VALUES = [MEAN, STDEV, PERCENTILE, VAR, TVAR, PERCENTILE_PROFIT, VAR_PROFIT, TVAR_PROFIT, PDF, IS_STOCHASTIC]

    SimulationRun run
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

    @Override
    String toString() {
        return "$keyFigure ${keyFigureParameter != null ? keyFigureParameter : ''} $path, $field $collector P$period: $result"
    }


}