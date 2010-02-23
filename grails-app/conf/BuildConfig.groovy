//Use a custom project work dir, because different branches use different plugin versions
grails.project.work.dir = "${grailsSettings.grailsWorkDir}/projects/${appName}-0.5.x"

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
