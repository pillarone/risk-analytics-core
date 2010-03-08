import org.pillarone.riskanalytics.core.output.batch.SQLServerBulkInsert
import org.pillarone.riskanalytics.core.output.batch.MysqlBulkInsert
import org.pillarone.riskanalytics.core.output.batch.DerbyBulkInsert

environments {

    batchInsert = null
    keyFiguresToCalculate = null

    development {
        log4j = {
            info 'org.pillarone.riskanalytics.core'
        }
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }
    test {
        log4j = {
            info 'org.pillarone.riskanalytics.core'
        }
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }
    sqlserver {
        models = ["FiniteReModel"]
        batchInsert = SQLServerBulkInsert
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
        ]
        log4j = {
            info 'org.pillarone.riskanalytics.core.output',
                    'org.pillarone.riskanalytics.core.components',
                    'org.pillarone.riskanalytics.core.simulation',
                    'org.pillarone.riskanalytics.core.fileimport',
                    'org.pillarone.riskanalytics.core.parameterization',
                    'org.pillarone.riskanalytics.core.jobs.JobScheduler',
                    'org.pillarone.riskanalytics.core.simulation.engine',
                    'org.pillarone.riskanalytics.core.jobs.BatchRunner'
        }
    }
    mysql {
        batchInsert = MysqlBulkInsert
        log4j = {
            appenders {
                console name: 'stdout', layout: pattern(conversionPattern: '[%d] %-5p %c{1} %m%n')
                file name: 'file', file: 'RiskAnalytics.log', layout: pattern(conversionPattern: '[%d] %-5p %c{1} %m%n')
            }
            root {
                error 'stdout', 'file'
                additivity = false
            }
            info('org.pillarone.riskanalytics.core.output',
                    'org.pillarone.riskanalytics.core.components',
                    'org.pillarone.riskanalytics.core.simulation',
                    'org.pillarone.riskanalytics.core.fileimport',
                    'org.pillarone.riskanalytics.core.parameterization',
                    'org.pillarone.riskanalytics.core.jobs.JobScheduler',
                    'org.pillarone.riskanalytics.core.simulation.engine',
                    'org.pillarone.riskanalytics.core.jobs.BatchRunner')
        }
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }


    production {
        batchInsert = MysqlBulkInsert
        userLogin = true
        maxIterations = 10000
        models = ["CapitalEagleModel", "DependencyModel", "DynamicCapitalEagleModel", "MultiLineReinsuranceModel", "TwoLobDependencyModel", "PodraModel"]
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }

    standalone {
        batchInsert = DerbyBulkInsert
        maxIterations = 10000
        models = ["CapitalEagleModel", "DependencyModel", "DynamicCapitalEagleModel", "MultiLineReinsuranceModel", "TwoLobDependencyModel", "PodraModel"]
        keyFiguresToCalculate = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]
    }
}
