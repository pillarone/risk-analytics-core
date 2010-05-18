package org.pillarone.riskanalytics.core.user

/**
 * Authority domain class.
 */
class Authority {

	static hasMany = [people: Person]

	/** description */
	String description
	/** ROLE String */
	String authority

	static constraints = {
		authority(blank: false, unique: true)
		description()
	}
}
