package org.pillarone.riskanalytics.core.user

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.grails.plugins.springsecurity.service.AuthenticateService


class UserManagement {

    public static Person getCurrentUser() {
        AuthenticateService authenticateService = getAuthenticateService()
        def id = authenticateService.userDomain()?.id
        return id != null ? Person.get(id) : null
    }

    static AuthenticateService getAuthenticateService() {
        AuthenticateService authenticateService = ApplicationHolder.application.mainContext.getBean("authenticateService")
        return authenticateService
    }
}
