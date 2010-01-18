grails.plugin.repos.discovery.pillarone = "https://svn.intuitive-collaboration.com/GrailsPlugins/"
user.svn.username.discovery.pillarone = ""
user.svn.password.discovery.pillarone = ""

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
