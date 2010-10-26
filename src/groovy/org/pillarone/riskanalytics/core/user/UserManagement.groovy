package org.pillarone.riskanalytics.core.user

import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.plugins.springsecurity.SpringSecurityService
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser


class UserManagement {

    public static Person getCurrentUser() {
        SpringSecurityService securityService = getSpringSecurityService()
        GrailsUser user = securityService.getPrincipal()
        return user != null ? Person.get(user.id) : null
    }

    static SpringSecurityService getSpringSecurityService() {
        SpringSecurityService securityService = ApplicationHolder.application.mainContext.getBean("springSecurityService")
        return securityService
    }
}
