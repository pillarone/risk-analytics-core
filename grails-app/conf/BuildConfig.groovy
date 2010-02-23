//Use a custom plugins dir, because different branches use different plugin versions
grails.project.plugins.dir = "../local-plugins/RiskAnalyticsCore-master"

grails.plugin.repos.discovery.pillarone = "https://readplugins:readplugins@svn.intuitive-collaboration.com/GrailsPlugins/"

grails.plugin.repos.resolveOrder = ['pillarone', 'default', 'core']

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
