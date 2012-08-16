//Use a custom plugins dir, because different branches use different plugin versions
grails.project.plugins.dir = "../local-plugins/RiskAnalyticsCore-2.1.0"

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
        mavenCentral()

        //even though this plugin does not need anything from this repo, it has to be added for the deploy script to check existing plugins
        mavenRepo "https://repository.intuitive-collaboration.com/nexus/content/repositories/pillarone-public/"
    }

    plugins {
        compile(":background-thread:1.3")
        compile(":hibernate:2.1.0")
        compile(":joda-time:0.5")
        provided(":release:2.0.3") { excludes "groovy" }
        compile(":quartz:0.4.2")
        compile(":spring-security-core:1.2.7.3")
        provided(":tomcat:2.1.0")

        test(":code-coverage:1.2.4")
    }

    dependencies {
        runtime "colt:colt:1.2.0"
        runtime "commons-cli:commons-cli:1.2"
        runtime "com.google.collections:google-collections:1.0"
        runtime "org.apache.poi:poi:3.7"
        runtime "ca.umontreal.iro:ssj:2.4"
        runtime "optimization:optimization:1.0"
        runtime 'net.sf.jasperreports:jasperreports:4.0.1', {
            exclude "xml-apis"
        }
    }
}

grails.project.dependency.distribution = {
    String password = ""
    String user = ""
    String scpUrl = ""
    try {
        Properties properties = new Properties()
        properties.load(new File("${userHome}/deployInfo.properties").newInputStream())

        user = properties.get("user")
        password = properties.get("password")
        scpUrl = properties.get("url")
    } catch (Throwable t) {
    }
    remoteRepository(id: "pillarone", url: scpUrl) {
        authentication username: user, password: password
    }
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
