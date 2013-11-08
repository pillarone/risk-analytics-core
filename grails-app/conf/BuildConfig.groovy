//Use a custom plugins dir, because different branches use different plugin versions
grails.project.plugins.dir = "../local-plugins/RiskAnalyticsCore-master"

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    inherits "global" // inherit Grails' default dependencies
    log "warn"

    repositories {
        grailsHome()
        grailsCentral()
        mavenCentral()

        //even though this plugin does not need anything from this repo, it has to be added for the deploy script to check existing plugins
        mavenRepo "https://repository.intuitive-collaboration.com/nexus/content/repositories/pillarone-public/"
        mavenRepo "http://repo.spring.io/milestone/" //needed for spring-security-core 2.0-rc2 plugin
//        mavenRepo "http://www.gridgainsystems.com/maven2/" //TODO: repository offline..
    }

    plugins {
        runtime ":background-thread:1.3"
        runtime ":hibernate:3.6.10.3"
        runtime ":joda-time:0.5"
        runtime ":release:3.0.1", {
            exclude "groovy"
        }
        runtime ":quartz:0.4.2"
        runtime ":spring-security-core:2.0-RC2"
        runtime ":tomcat:7.0.42"

        test ":code-coverage:1.2.6"
        runtime ":database-migration:1.3.6"
    }

    dependencies {
        runtime 'net.sf.jasperreports:jasperreports:4.0.1', {
            excludes "xml-apis", "commons-collections"
        }
        compile('org.apache.poi:poi:3.8')
        compile('org.apache.poi:poi-ooxml:3.8') {
            exclude "xmlbeans"
        }
        compile("org.apache.xmlbeans:xmlbeans:2.3.0-without-w3c")

        //gridgain & deps
//        compile("org.gridgain:gridgain:3.6.0c") {
//            transitive = false
//        }
        runtime("javax.mail:mail:1.4.4")
        runtime("net.sf.jtidy:jtidy:r938")
//        runtime("net.sf.cron4j:cron4j:2.2.5")
        runtime("com.sun.grizzly:grizzly-utils:1.9.43")
        runtime("jboss:jboss-serialization:4.2.2.GA")
        runtime("trove:trove:1.0.2")
        runtime("org.fusesource.jansi:jansi:1.2.1")
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
