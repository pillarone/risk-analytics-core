security {

    // see DefaultSecurityConfig.groovy for all settable/overridable properties

    active = true

    loginUserDomainClass = "org.pillarone.riskanalytics.core.user.Person"
    authorityDomainClass = "org.pillarone.riskanalytics.core.user.Authority"

    useRequestMapDomainClass = false

    requestMapString = """
		CONVERT_URL_TO_LOWERCASE_BEFORE_COMPARISON
		PATTERN_TYPE_APACHE_ANT

		/login/**=IS_AUTHENTICATED_ANONYMOUSLY
		/css/**=IS_AUTHENTICATED_ANONYMOUSLY
		/js/**=IS_AUTHENTICATED_ANONYMOUSLY
		/images/**=IS_AUTHENTICATED_ANONYMOUSLY
		/person/**=IS_AUTHENTICATED_ANONYMOUSLY
		/authority/**=ROLE_ADMIN
		/**=IS_AUTHENTICATED_REMEMBERED
	"""
}
