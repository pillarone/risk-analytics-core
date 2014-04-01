package org.pillarone.riskanalytics.core.user

class Person {

    String username
    String password
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    UserSettings settings

    static constraints = {
        username blank: false, unique: true
        password blank: false
    }

    static mapping = {
        password column: '`password`'
    }

    Set<Authority> getAuthorities() {
        withTransaction {
            PersonAuthority.findAllByPerson(this).authority as Set
        }
    }
}
