import org.apache.ivy.plugins.resolver.URLResolver

//Use a custom plugins dir, because different branches use different plugin versions
grails.project.plugins.dir = "../local-plugins/RiskAnalyticsCore-v1.2"

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
    }

    def myResolver = new URLResolver()
    myResolver.addArtifactPattern "https://build.intuitive-collaboration.com/plugins/[artifact]/grails-[artifact]-[revision].[ext]"

    resolver myResolver
}

coverage {
    exclusions = [
            'models/**',
            '**/*Test*',
            '**/com/energizedwork/grails/plugins/jodatime/**',
            '**/grails/util/**',
            '**/org/codehaus/**',
            '**/org/grails/**',
            '**GrailsPlugin**',
            '**TagLib**'
    ]

}
