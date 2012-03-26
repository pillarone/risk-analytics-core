package org.pillarone.riskanalytics.core.user

import grails.plugins.springsecurity.SpringSecurityService
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser

class UserManagement {

    public static final String USER_ROLE = "ROLE_USER"
    public static final String REVIEWER_ROLE = "ROLE_REVIEWER"
    public static final String ADMIN_ROLE = "ROLE_ADMIN"
    static boolean _hasTriedFindingFixUserNameFromSystemProperty = false
    static Person _possiblyFixUser


    public static Person getCurrentUser() {
        SpringSecurityService securityService = getSpringSecurityService()
        GrailsUser user = securityService.getPrincipal()
        return user != null ? Person.get(user.id) : tryFindingFixUserNameFromSystemProperty()
    }

    private static Person tryFindingFixUserNameFromSystemProperty() {
        if (_hasTriedFindingFixUserNameFromSystemProperty) {
            return _possiblyFixUser
        }
        Person.withTransaction {e ->
            String runAsUserName = System.getProperty("runAsUserName");
            _possiblyFixUser = runAsUserName != null ? Person.findByUsername(runAsUserName) : null
            if (_possiblyFixUser) {
                // loading the settings seems needed here for later retrieval...?
                _possiblyFixUser.settings
            }
            _hasTriedFindingFixUserNameFromSystemProperty = true
            return _possiblyFixUser
        }
    }
    
    static SpringSecurityService getSpringSecurityService() {
        SpringSecurityService securityService = ApplicationHolder.application.mainContext.getBean("springSecurityService")
        return securityService
    }
}
