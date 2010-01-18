/**
 * Gant script to drop and re-create the database instance in order to
 * securely drop all tables.
 * Call it like 'grails -Dgrails.env=mysql cleanDB'.
 * @author Dierk Koenig
 */

import org.springframework.jdbc.datasource.DriverManagerDataSource
import groovy.sql.Sql

databaseName = 'p1rat'

grailsHome = Ant.project.properties."environment.GRAILS_HOME"

includeTargets << grailsScript("Init")

target('default': "init db") {
    getClass().classLoader.rootLoader?.addURL(new File(classesDirPath).toURL())
    def ds = getDataSource()
    def db = new Sql(ds)
    def initFile = new File("src/java/${grailsEnv}.sql")
    if (!initFile.exists()) {
        println "init script not found: $initFile.path"
        return
    }
    def statements = initFile.text.split(";")
    statements.collect {String s -> s.replaceAll("DATABASE_NAME", databaseName) }.each {
        execute(db, it)
    }
}

def execute(db, command) {
    def cmd = command.toString()
    println "executing: $cmd"
    def result = db.execute(cmd)
    if (result)
        println "db returned with: $result"
    else
        println "ok"
}

def getDataSource() {
    File dsFile = new File("${basedir}/grails-app/conf/DataSource.groovy")
    def dsConfig = null
    if (dsFile.exists()) {
        dsConfig = new ConfigSlurper(grailsEnv).parse(dsFile.text)
    }

    def ds = new DriverManagerDataSource()
    ds.username = dsConfig?.dataSource?.username
    ds.password = dsConfig?.dataSource?.password
    def url = dsConfig?.dataSource?.url
    databaseName = url.tokenize('/').last()
    ds.url = url - "/$databaseName"
    def driver = dsConfig?.dataSource?.driverClassName
    ds.driverClassName = driver

    println "user:$ds.username , pw:$ds.password , url: $ds.url , driver: $driver"
    return ds
}