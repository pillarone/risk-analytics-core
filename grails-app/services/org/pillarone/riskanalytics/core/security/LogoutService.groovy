package org.pillarone.riskanalytics.core.security

import org.codehaus.groovy.grails.web.util.WebUtils
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.LogoutHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class LogoutService {

    List<LogoutHandler> logoutHandlers
    LogoutSuccessHandler logoutSuccessHandler

    void logout(boolean redirect = false) {
        Authentication authentication = authentication
        internalLogout(authentication)
        if (redirect) {
            logoutSuccessHandler.onLogoutSuccess(request, response, authentication)
        }

    }

    private List<LogoutHandler> internalLogout(Authentication authentication) {
        logoutHandlers.each { LogoutHandler handler ->
            handler.logout(request, response, authentication)
        }
    }

    private HttpServletRequest getRequest() {
        WebUtils.retrieveGrailsWebRequest().currentRequest
    }

    private HttpServletResponse getResponse() {
        WebUtils.retrieveGrailsWebRequest().currentResponse
    }

    private Authentication getAuthentication() {
        SecurityContextHolder.context.authentication
    }
}
