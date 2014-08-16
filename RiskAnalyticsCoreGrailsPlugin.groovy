import org.codehaus.groovy.grails.orm.hibernate.HibernateEventListeners
import org.gridgain.grid.GridConfigurationAdapter
import org.gridgain.grid.GridSpringBean
import org.gridgain.grid.marshaller.optimized.GridOptimizedMarshaller
import org.gridgain.grid.spi.collision.fifoqueue.GridFifoQueueCollisionSpi
import org.gridgain.grid.spi.failover.never.GridNeverFailoverSpi
import org.joda.time.DateTimeZone
import org.pillarone.riskanalytics.core.FileConstants
import org.pillarone.riskanalytics.core.log.TraceLogManager
import org.pillarone.riskanalytics.core.modellingitem.CacheItemHibernateListener
import org.pillarone.riskanalytics.core.output.batch.calculations.GenericBulkInsert as GenericCalculationBulkInsert
import org.pillarone.riskanalytics.core.output.batch.results.GenericBulkInsert as GenericResultBulkInsert
import org.pillarone.riskanalytics.core.remoting.IResultService
import org.pillarone.riskanalytics.core.remoting.ITransactionService
import org.pillarone.riskanalytics.core.remoting.impl.ResultService
import org.pillarone.riskanalytics.core.simulation.engine.MappingCache
import org.pillarone.riskanalytics.core.upload.DefaultUploadService
import org.pillarone.riskanalytics.core.upload.DefaultUploadStrategy
import org.pillarone.riskanalytics.core.util.GrailsConfigValidator
import org.springframework.remoting.rmi.RmiProxyFactoryBean
import org.springframework.remoting.rmi.RmiServiceExporter
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean

class RiskAnalyticsCoreGrailsPlugin {
    // the plugin version
    def version = "1.9-2800-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.3.2 > *"
    // the other plugins this plugin depends on
    def dependsOn = [
            "backgroundThread": "1.3",
            "springSecurityCore": "2.0-RC2",
            "release": "3.0.1"
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

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
        uploadStrategy(DefaultUploadStrategy) {
            backgroundService = ref('backgroundService')
            uploadService = ref('uploadService')
        }
        //TODO overwrite it in the resource.groovy of AllianzART to use the rmi service
        uploadService(DefaultUploadService)

        ConfigObject config = application.config

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

        resultServiceBean(ResultService) {}

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
        marshaller(GridOptimizedMarshaller) {}
        failoverSpi(GridNeverFailoverSpi)
        collisionSpi(GridFifoQueueCollisionSpi) {
            parallelJobsNumber = config.containsKey("numberOfParallelJobsPerNode") ?
                config."numberOfParallelJobsPerNode" : 100
        }
        grid(GridSpringBean) {
            configuration = ref('grid.cfg')
        }

        cacheItemListener(CacheItemHibernateListener)

        hibernateEventListeners(HibernateEventListeners) {
            listenerMap = ['post-commit-insert': cacheItemListener,
                    'post-commit-update': cacheItemListener,
                    'post-commit-delete': cacheItemListener]
        }

        mappingCache(MappingCache) {}
    }

    def doWithDynamicMethods = { ctx -> }

    def doWithApplicationContext = { applicationContext ->

        /** Setting the default time zone to UTC avoids problems in multi user context with different time zones
         *  and switches off daylight saving capabilities and possible related problems.                */
        DateTimeZone.setDefault(DateTimeZone.UTC)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        //Checks at startup if certain config options required for the core are set and sets defaults otherwise
        def standardCalculatorOutput = [
                'stdev': true,
                'percentile': [0.0, 10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0],
                'var': [99, 99.5],
                'tvar': [99, 99.5],
                'pdf': 200
        ]

        GrailsConfigValidator.validateConfig(application.config, [
                "resultBulkInsert": GenericResultBulkInsert,
                "calculationBulkInsert": GenericCalculationBulkInsert,
                "keyFiguresToCalculate": standardCalculatorOutput
        ])

    }

    def onChange = { event -> }

    def onConfigChange = { event -> }
}
