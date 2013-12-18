import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners
import org.gridgain.grid.GridConfigurationAdapter
import org.gridgain.grid.GridSpringBean
import org.gridgain.grid.spi.collision.fifoqueue.GridFifoQueueCollisionSpi
import org.gridgain.grid.spi.failover.never.GridNeverFailoverSpi
import org.joda.time.DateTimeZone
import org.pillarone.riskanalytics.core.FileConstants
import org.pillarone.riskanalytics.core.example.migration.TestConstrainedTable
import org.pillarone.riskanalytics.core.example.parameter.ExampleResourceConstraints
import org.pillarone.riskanalytics.core.modellingitem.ModellingItemHibernateListener
import org.pillarone.riskanalytics.core.log.TraceLogManager
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.AggregatedWithSingleAvailableCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.CollectingModeFactory
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.aggregation.PacketAggregatorRegistry
import org.pillarone.riskanalytics.core.output.aggregation.SumAggregator
import org.pillarone.riskanalytics.core.output.aggregation.SumAggregatorSingleValuePacket
import org.pillarone.riskanalytics.core.output.batch.calculations.GenericBulkInsert as GenericCalculationBulkInsert
import org.pillarone.riskanalytics.core.output.batch.results.GenericBulkInsert as GenericResultBulkInsert
import org.pillarone.riskanalytics.core.packets.Packet
import org.pillarone.riskanalytics.core.packets.SingleValuePacket
import org.pillarone.riskanalytics.core.parameterization.ConstraintsFactory
import org.pillarone.riskanalytics.core.parameterization.SimpleConstraint
import org.pillarone.riskanalytics.core.remoting.IResultService
import org.pillarone.riskanalytics.core.remoting.ITransactionService
import org.pillarone.riskanalytics.core.remoting.impl.ResultService
import org.pillarone.riskanalytics.core.simulation.engine.MappingCache
import org.pillarone.riskanalytics.core.util.GrailsConfigValidator
import org.springframework.remoting.rmi.RmiProxyFactoryBean
import org.springframework.remoting.rmi.RmiServiceExporter
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

class RiskAnalyticsCoreGrailsPlugin {
    // the plugin version
    def version = "1.8.9"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [
            "backgroundThread": "1.3",
            "jodaTime": "0.5",
            "quartz": "0.4.2",
            "springSecurityCore": "1.2.7.3",
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
        ConfigObject config = ConfigurationHolder.config

        traceLogManager(TraceLogManager)

        String url = "rmi://localhost:1099/TransactionService"
        if (config.containsKey("transactionServiceUrl")) {
            url = config.transactionServiceUrl
        }
        transactionService(RmiProxyFactoryBean) {
            serviceInterface = ITransactionService
            serviceUrl = url
            refreshStubOnConnectFailure = true
            lookupStubOnStartup = false
        }

        int port = 1099
        if (config.containsKey("resultServiceRegistryPort")) {
            port = config.resultServiceRegistryPort
        }
        resultServiceExporter(RmiServiceExporter) {
            serviceName = "ResultService"
            serviceInterface = IResultService
            registryPort = port
            service = ref("resultService")
        }

        Properties attributes = new Properties()
        attributes.put("*", "PROPAGATION_REQUIRED,readOnly")

        resultService(TransactionProxyFactoryBean) {
            transactionManager = ref("transactionManager")
            target = ref("resultServiceBean")
            transactionAttributes = attributes
        }

        resultServiceBean(ResultService) { }

        "grid.cfg"(GridConfigurationAdapter) {
            gridName = "pillarone"

            String gridgainHomeDefault = FileConstants.GRIDGAIN_HOME
            String ggHome = System.getProperty("GRIDGAIN_HOME")
            if (ggHome != null) {
                gridgainHomeDefault = new File(ggHome).absolutePath
            }
            gridGainHome = gridgainHomeDefault
            collisionSpi = ref("collisionSpi")
            failoverSpi = ref("failoverSpi")
            marshaller = ref("marshaller")
            networkTimeout = 30000

        }
        marshaller(org.gridgain.grid.marshaller.optimized.GridOptimizedMarshaller) { }
        failoverSpi(GridNeverFailoverSpi)
        collisionSpi(GridFifoQueueCollisionSpi) {
            parallelJobsNumber = ConfigurationHolder.config.containsKey("numberOfParallelJobsPerNode") ?
                ConfigurationHolder.config."numberOfParallelJobsPerNode" : 100
        }
        grid(GridSpringBean) {
            configuration = ref('grid.cfg')
        }

        modellingItemListener(ModellingItemHibernateListener)

        hibernateEventListeners(HibernateEventListeners) {
            listenerMap = ['post-insert': modellingItemListener,
                    'post-update': modellingItemListener,
                    'post-delete': modellingItemListener]
        }

        mappingCache(MappingCache) {}
    }

    def doWithDynamicMethods = {ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = {applicationContext ->

        /** Setting the default time zone to UTC avoids problems in multi user context with different time zones
         *  and switches off daylight saving capabilities and possible related problems.                */
        DateTimeZone.setDefault(DateTimeZone.UTC)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

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
        CollectingModeFactory.registerStrategy(new AggregatedWithSingleAvailableCollectingModeStrategy())

        ConstraintsFactory.registerConstraint(new SimpleConstraint())
        ConstraintsFactory.registerConstraint(new TestConstrainedTable())
        ConstraintsFactory.registerConstraint(new ExampleResourceConstraints())

        PacketAggregatorRegistry.registerAggregator(Packet, new SumAggregator())
        PacketAggregatorRegistry.registerAggregator(SingleValuePacket, new SumAggregatorSingleValuePacket())
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
