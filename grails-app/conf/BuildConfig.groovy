grails.plugin.repos.discovery.pillarone = "https://svn.intuitive-collaboration.com/GrailsPlugins/"
grails.plugin.repos.distribution.pillarone = "https://svn.intuitive-collaboration.com/GrailsPlugins/"

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
