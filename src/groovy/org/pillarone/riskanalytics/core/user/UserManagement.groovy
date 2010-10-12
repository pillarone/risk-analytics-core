package org.pillarone.riskanalytics.core.user

import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.plugins.springsecurity.SpringSecurityService
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser


class UserManagement {

    public static final String USER_ROLE = "ROLE_USER"
    public static final String REVIEWER_ROLE = "ROLE_REVIEWER"
    public static final String ADMIN_ROLE = "ROLE_ADMIN"


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
