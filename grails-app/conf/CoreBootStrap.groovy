import grails.util.Environment
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.fileimport.FileImportService
import org.springframework.transaction.TransactionStatus
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.user.Authority
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserSettings
import org.grails.plugins.springsecurity.service.AuthenticateService
import org.pillarone.riskanalytics.core.user.UserManagement
import org.pillarone.riskanalytics.core.output.CollectorMapping
import org.pillarone.riskanalytics.core.output.batch.AbstractBulkInsert
import org.pillarone.riskanalytics.core.output.SingleValueCollectingModeStrategy
import org.pillarone.riskanalytics.core.output.AggregatedCollectingModeStrategy

class CoreBootStrap {

    AuthenticateService authenticateService

    def init = {servletContext ->

        authenticateService = UserManagement.getAuthenticateService()

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
                adminGroup.description = "admin group"
                adminGroup.authority = "ROLE_ADMIN"
                adminGroup.save()

                Authority userGroup = new Authority()
                userGroup.description = "admin group"
                userGroup.authority = "ROLE_USER"
                userGroup.save()

                Person admin = new Person()
                admin.username = "admin"
                admin.userRealName = "admin"
                admin.passwd = authenticateService.encodePassword("admin")
                admin.enabled = true
                admin.email = "admin@pillarone.org"
                admin.settings = new UserSettings(language: "en")
                admin.addToAuthorities(adminGroup)
                admin.save()

                Person actuary = new Person()
                actuary.username = "actuary"
                actuary.userRealName = "actuary"
                actuary.passwd = authenticateService.encodePassword("actuary")
                actuary.enabled = true
                actuary.email = "actuary@pillarone.org"
                actuary.settings = new UserSettings(language: "en")
                actuary.addToAuthorities(userGroup)
                actuary.save()

                Person actuaryDE = new Person()
                actuaryDE.username = "aktuar"
                actuaryDE.userRealName = "aktuar"
                actuaryDE.passwd = authenticateService.encodePassword("aktuar")
                actuaryDE.enabled = true
                actuaryDE.email = "actuary@pillarone.org"
                actuaryDE.settings = new UserSettings(language: "de")
                actuaryDE.addToAuthorities(userGroup)
                actuaryDE.save()

                Person actuaryFR = new Person()
                actuaryFR.username = "actuaire"
                actuaryFR.userRealName = "actuaire"
                actuaryFR.passwd = authenticateService.encodePassword("actuaire")
                actuaryFR.enabled = true
                actuaryFR.email = "actuary@pillarone.org"
                actuaryFR.settings = new UserSettings(language: "fr")
                actuaryFR.addToAuthorities(userGroup)
                actuaryFR.save()
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
