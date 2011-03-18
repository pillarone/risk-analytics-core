import org.codehaus.groovy.grails.commons.GrailsApplication
import grails.util.GrailsUtil
import org.springframework.context.ApplicationContext

includeTargets << grailsScript('_GrailsBootstrap')

target(main: "Creates a jar file of the current model class files for migration") {
    depends(parseArguments, packageApp)

    String[] arguments = args.split()

    String modelClassName = arguments[0]
    String[] packagesToInclude = arguments[1].split(",")

    ApplicationContext ctx = GrailsUtil.bootstrapGrailsFromClassPath();
    GrailsApplication app = (GrailsApplication) ctx.getBean(GrailsApplication.APPLICATION_ID);
    ClassLoader cl = app.classLoader

    Class modelClass = cl.loadClass(modelClassName)
    Class<?> model = cl.loadClass("org.pillarone.riskanalytics.core.model.Model")
    String version = model.getMethod("getModelVersion", Class).invoke(null, modelClass).toString()


    String jarTarget = "./src/java/${modelClass.getPackage().name.replace('.' as char, '/' as char)}"
    ant.mkdir(dir: jarTarget)

    ant.jar(destfile: "${jarTarget}/${modelClass.simpleName}-v${version}.jar",
            basedir: classesDir, includes: packagesToInclude.collect { it.replace('.' as char, '/' as char) + "/**/*"}.join(" "))

}

setDefaultTarget(main)
