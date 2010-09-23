package org.pillarone.riskanalytics.core.user

import org.codehaus.groovy.grails.plugins.springsecurity.AuthorizeTools

/**
 * User domain class.
 */
class Person {
	static transients = ['pass']
	static hasMany = [authorities: Authority]
	static belongsTo = Authority

	/** Username */
	String username
	/** User Real Name*/
	String userRealName
	/** MD5 Password */
	String passwd
	/** enabled */
	boolean enabled

	String email
	boolean emailShow

	/** description */
	String description = ''

	/** plain password to create a MD5 password */
	String pass = '[secret]'

    UserSettings settings

	static constraints = {
		username(blank: false, unique: true)
		userRealName(blank: false)
		passwd(blank: false)
		enabled()
        settings()
	}

    public Set<String> roles() {
        return AuthorizeTools.authoritiesToRoles(authorities)
    }
}
