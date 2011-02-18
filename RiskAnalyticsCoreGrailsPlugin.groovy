import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.pillarone.riskanalytics.core.util.GrailsConfigValidator
import org.pillarone.riskanalytics.core.output.batch.results.GenericBulkInsert as GenericResultBulkInsert
import org.pillarone.riskanalytics.core.output.batch.calculations.GenericBulkInsert as GenericCalculationBulkInsert
import org.pillarone.riskanalytics.core.parameterization.SimpleConstraint
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.CollectingModeFactory
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy

class RiskAnalyticsCoreGrailsPlugin {
    // the plugin version
    def version = "1.2.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.4 > *"
    // the other plugins this plugin depends on
    def dependsOn = [
            "backgroundThread": "1.3",
            "jodaTime": "0.5",
            "quartz": "0.4.1",
            "springSecurityCore": "1.0.1",
            "mavenPublisher": "0.7.5"
    ]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    def author = "Intuitive Collaboration AG"
    def authorEmail = "info@pillarone.org"
    def title = "RiskAnalytics core"
    def description = '''\\
Persistence & Simulation engine.
'''

    def documentation = "http://www.pillarone.org"

    def groupId = "org.pillarone"

    def doWithWebDescriptor = {xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        // TODO Implement runtime spring config (optional)
    }

    def doWithDynamicMethods = {ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = {applicationContext ->
        //Checks at startup if certain config options required for the core are set and sets defaults otherwise
        ConfigObject grailsConfig = ConfigurationHolder.config
        def standardCalculatorOutput = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]

        GrailsConfigValidator.validateConfig(grailsConfig, [
                "resultBulkInsert": GenericResultBulkInsert,
                "calculationBulkInsert": GenericCalculationBulkInsert,
                "keyFiguresToCalculate": standardCalculatorOutput
        ])

        CollectingModeFactory.registerStrategy(new SingleValueCollectingModeStrategy())
        CollectingModeFactory.registerStrategy(new AggregatedCollectingModeStrategy())

        ConstraintsFactory.registerConstraint(new SimpleConstraint())
    }

    def onChange = {event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = {event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }
}
