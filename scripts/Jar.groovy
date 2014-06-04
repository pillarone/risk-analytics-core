import org.codehaus.groovy.grails.io.support.Resource
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.project.packaging.GrailsProjectWarCreator

includeTargets << grailsScript("_GrailsWar")

stagingDir = null

target(jarMain: '''
Compiles a grails application and moves the output to the specified directory and optionally creates
a JAR file or a runnable .cmd file.
The directory is structured in a way that a grails application can locally be run without
the need for a grails executable. Web Content (i.e. views) is not completely included.

Options:
-destination (required): target directory for the compiled output
-buildJar (optional): Creates a jar file of the project
-buildClasses (optional): Creates a directory which contains all output and a .cmd file to start the project
-mainClass (required for buildJar and buildClasses): main class to start
-excludeJars (optional): A comma separated list of jar file names that are excluded from the class path.
''') {
    depends(parseArguments, packageApp)

    stagingDir = argsMap.destination
    if (!stagingDir) {
        throw new RuntimeException("no destination dir set")
    }

    ant.mkdir(dir: stagingDir)

    //copy applicationContext.xml from the project to the target directory
    ant.copy(todir: stagingDir, overwrite: true) {
        fileset(dir: "${basedir}/web-app/WEB-INF", includes: "applicationContext.xml")
    }

    //copy the compiler output to the target directory
    ant.copy(todir: "${stagingDir}") {
        fileset(dir: classesDirPath) {
            exclude(name: "hibernate")
            exclude(name: "spring")
            exclude(name: "hibernate/*")
            exclude(name: "spring/*")
        }
        fileset(dir: pluginClassesDirPath) {
            exclude(name: "hibernate")
            exclude(name: "spring")
            exclude(name: "hibernate/*")
            exclude(name: "spring/*")
        }
        fileset(dir: resourcesDirPath, includes: "**/*")
    }

    //Create a grails.xml file which is necessary to create a plugin manager
    ant.mkdir(dir: "${stagingDir}/WEB-INF")
    def warCreator = new GrailsProjectWarCreator(grailsSettings, eventListener, projectPackager, ant, isInteractive)
    warCreator.stagingDir = new File(stagingDir)
    warCreator.createDescriptor()

    //Project ready to run in stagingDir, optionally create a jar or cmd file to run the application
    if (argsMap.buildJar) {
        buildJar()
    }
    if (argsMap.buildClasses) {
        createRunnableFiles()
    }

}

//Creates an executable jar file including manifest
private void buildJar() {
    String mainClasses = argsMap.mainClass
    if (!mainClasses) throw new RuntimeException("no main class set")

    String[] mains = mainClasses.split(",")

    String jarTarget = "${projectTargetDir}/jar"
    ant.mkdir(dir: jarTarget)

    ant.delete(includeemptydirs: "true") {
        fileset(dir: jarTarget, includes: "**/*")
    }

    copyLibraries(jarTarget)
    List classPath = getRelativeClassPaths("$jarTarget/lib")
    String excludedJarFiles = argsMap.excludeJars
    if (excludedJarFiles) {
        excludedJarFiles.split(',').each { String jarFileToExclude ->
            classPath.removeAll { String classPathEntry -> classPathEntry.matches(".*$jarFileToExclude") }
        }
    }

    String manifestTarget = "${jarTarget}/MANIFEST.MF"
    ant.manifest(file: manifestTarget) {
        attribute(name: "Class-Path", value: classPath.join(" "))
        section(name: "Grails Application") {
            attribute(name: "Implementation-Title", value: "${grailsAppName}")
            attribute(name: "Implementation-Version", value: "${metadata.getApplicationVersion()}")
            attribute(name: "Grails-Version", value: "${metadata.getGrailsVersion()}")
        }
    }
    String excludes = mains.collect { it.replace(".", "/") + ".class" }.join(" ")
    ant.jar(destfile: "${jarTarget}/${grailsAppName}.jar", basedir: stagingDir, manifest: manifestTarget, excludes: excludes)

    classPath.add("${grailsAppName}.jar")
    for (String main in mains) {
        ant.manifest(file: manifestTarget) {
            attribute(name: "Main-Class", value: main)
            attribute(name: "Class-Path", value: classPath.join(" "))
            section(name: "Grails Application") {
                attribute(name: "Implementation-Title", value: "${grailsAppName}")
                attribute(name: "Implementation-Version", value: "${metadata.getApplicationVersion()}")
                attribute(name: "Grails-Version", value: "${metadata.getGrailsVersion()}")
            }
        }
        ant.jar(destfile: "${jarTarget}/${main}.jar", basedir: stagingDir, manifest: manifestTarget, includes: "${main.replace(".", "/")}.class")
    }
    ant.delete(file: manifestTarget)
}

//Copies all classes & resources to a directory
private void createRunnableFiles() {
    String target = "${projectTargetDir}/runner"
    ant.mkdir(dir: target)

    ant.delete(includeemptydirs: "true") {
        fileset(dir: target, includes: "**/*")
    }

    copyLibraries(target)
    ant.copy(todir: "${target}/classes") {
        fileset(dir: stagingDir)
    }
}

//Copies all external libraries required by this project (inclusive plugin & grails libs) to the target dir
private void copyLibraries(String target) {

    def externalLibsTarget = "${target}/lib"
    ant.mkdir(dir: externalLibsTarget)
    //files in plugin/lib directories are not included in runtime dependencies below..
    def pluginPath = pluginsDirPath as String
    List excluded = []
    if (argsMap.excludeJars) {
       excluded = argsMap.excludeJars.split(',')
    }
    GrailsPluginUtils.getPluginLibDirectories(pluginPath).each { Resource resource ->
        File file = resource.file
        if (file.exists()) {
            file.eachFileRecurse {File jar ->
                if (excluded.contains(jar.name)) {
                    println("excluded: ${jar.name}")
                    return
                }
                ant.copy(toDir:externalLibsTarget, flatten:true) {
                       fileset(file:jar)
                }
            }
        }
    }
    for (File lib in grailsSettings.runtimeDependencies) {
        ant.copy(todir: externalLibsTarget, flatten: true, file: lib.absolutePath)
    }
}

//Returns a list of all relative paths (may be used for building classpaths
private List getRelativeClassPaths(String externalLibsTarget) {
    List classPath = []
    new File(externalLibsTarget).eachFileRecurse { File file ->
        classPath << "lib/${file.name}"
    }
    //the order is important. Somewhere there is a conflict. In this order it works for now, but could well be it brakes in future.
    //TODO: spend more time to remove all remaining class path conflicts.
    return classPath.sort().reverse()
}

setDefaultTarget('jarMain')
