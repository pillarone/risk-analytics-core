import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.springframework.transaction.TransactionStatus
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Authority
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserSettings
import org.pillarone.riskanalytics.core.user.UserManagement
import grails.plugins.springsecurity.SpringSecurityService
import org.pillarone.riskanalytics.core.user.PersonAuthority
import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.SimulationRun

class CoreBootStrap {

    SpringSecurityService authenticateService

    def init = {servletContext ->

        authenticateService = UserManagement.getSpringSecurityService()

        //All mappings must be persistent before a simulation is started
        CollectorMapping.withTransaction { status ->
            if (CollectorMapping.count() == 0) {
                new CollectorMapping(collectorName: SingleValueCollectingModeStrategy.IDENTIFIER).save()
                new CollectorMapping(collectorName: AggregatedCollectingModeStrategy.IDENTIFIER).save()
            }
        }

        Authority.withTransaction { status ->
            if (Authority.count() == 0) {

                Authority adminGroup = new Authority()
                adminGroup.authority = "ROLE_ADMIN"
                adminGroup.save()

                Authority userGroup = new Authority()
                userGroup.authority = "ROLE_USER"
                userGroup.save()

                Person admin = new Person()
                admin.username = "admin"
                admin.password = authenticateService.encodePassword("admin")
                admin.enabled = true
                admin.settings = new UserSettings(language: "en")
                admin.save()
                PersonAuthority.create(admin, adminGroup)

                Person actuary = new Person()
                actuary.username = "actuary"
                actuary.password = authenticateService.encodePassword("actuary")
                actuary.enabled = true
                actuary.settings = new UserSettings(language: "en")
                actuary.save()
                PersonAuthority.create(actuary, userGroup)

                Person actuaryDE = new Person()
                actuaryDE.username = "aktuar"
                actuaryDE.password = authenticateService.encodePassword("aktuar")
                actuaryDE.enabled = true
                actuaryDE.settings = new UserSettings(language: "de")
                actuaryDE.save()
                PersonAuthority.create(actuaryDE, userGroup)

                Person actuaryFR = new Person()
                actuaryFR.username = "actuaire"
                actuaryFR.password = authenticateService.encodePassword("actuaire")
                actuaryFR.enabled = true
                actuaryFR.settings = new UserSettings(language: "fr")
                actuaryFR.save()
                PersonAuthority.create(actuaryFR, userGroup)

            }
        }
        //delete all unfinished simulations
        SimulationRun.withTransaction {status ->
            def unfinishedSimulations = SimulationRun.findAllByEndTime(null)
            unfinishedSimulations.each {SimulationRun simulationRun ->
                try {
                    simulationRun.delete()
                } catch (Exception ex) {
                    ex.printStackTrace()
                }
            }
        }

        if (Environment.current == Environment.TEST) {
            return
        }

        //File import must be executed after the registration of all Constraints and CollectingModeStrategies
        //-> registration in the plugin descriptors / fileimport in bootstrap
        def modelFilter = ApplicationHolder.application.config?.models

        List models = null
        if (modelFilter) {
            models = modelFilter.collect {it - "Model"}
        }
        if (!Boolean.getBoolean("skipImport")) {
            ParameterizationDAO.withTransaction {TransactionStatus status ->
                FileImportService.importModelsIfNeeded(models)
            }
        }
    }

    def destroy = {
    }
}
