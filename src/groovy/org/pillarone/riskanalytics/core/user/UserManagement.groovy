package org.pillarone.riskanalytics.core.user

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.grails.plugins.springsecurity.service.AuthenticateService


class UserManagement {

    public static final String USER_ROLE = "ROLE_USER"
    public static final String REVIEWER_ROLE = "ROLE_REVIEWER"
    public static final String ADMIN_ROLE = "ROLE_ADMIN"


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
