package org.pillarone.riskanalytics.core.user

import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.plugins.springsecurity.SpringSecurityService


class UserManagement {

    public static Person getCurrentUser() {
        SpringSecurityService securityService = getSpringSecurityService()
        def userName = securityService.getPrincipal()
        return userName != null ? Person.findByUsername(userName) : null
    }

    static SpringSecurityService getSpringSecurityService() {
        SpringSecurityService securityService = ApplicationHolder.application.mainContext.getBean("springSecurityService")
        return securityService
    }
}
