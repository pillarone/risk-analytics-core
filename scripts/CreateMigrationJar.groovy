import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil
import org.springframework.context.ApplicationContext

includeTargets << grailsScript('_GrailsBootstrap')

target(main: "Creates a jar file of the current model class files for migration") {
    depends(parseArguments, packageApp)

    String[] arguments = args.split()

    String modelClassName = arguments[0]
    String[] packagesToInclude = arguments[1].split(":")

    //workaround for GRAILS-7367
    ant.copy(toDir: classesDir, file: "./web-app/WEB-INF/applicationContext.xml", verbose: true)
    ApplicationContext ctx = GrailsUtil.bootstrapGrailsFromClassPath();
    GrailsApplication app = (GrailsApplication) ctx.getBean(GrailsApplication.APPLICATION_ID);
    ClassLoader cl = app.classLoader

    Class modelClass = cl.loadClass(modelClassName)
    Class<?> model = cl.loadClass("org.pillarone.riskanalytics.core.model.Model")
    String version = model.getMethod("getModelVersion", Class).invoke(null, modelClass).toString()


    String jarTarget = "./src/java/${modelClass.getPackage().name.replace('.' as char, '/' as char)}"
    ant.mkdir(dir: jarTarget)

    String tempDirectory = "${projectWorkDir}/migration"
    File tempFile = new File(tempDirectory)
    tempFile.mkdirs()

    ant.copydir(src: classesDir, dest: tempDirectory)
    ant.copydir(src: pluginClassesDir, dest: tempDirectory)

    ant.jar(destfile: "${jarTarget}/${modelClass.simpleName}-v${version}.jar",
            basedir: tempDirectory, includes: packagesToInclude.collect { it.replace('.' as char, '/' as char) + "/**/*"}.join(" "))

    tempFile.deleteDir()
}

setDefaultTarget(main)
