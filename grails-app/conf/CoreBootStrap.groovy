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

class CoreBootStrap {

    AuthenticateService authenticateService

    def init = {servletContext ->

        authenticateService = UserManagement.getAuthenticateService()

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
