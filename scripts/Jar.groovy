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

    //Create a grails.xml file which is necessary to create a plugin manager (using _GrailsWar.groovy)
    ant.mkdir(dir: "${stagingDir}/WEB-INF")
    createDescriptor()

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
    ant.copy(todir: externalLibsTarget, flatten: true) {
        fileset(dir: './lib', includes: '*.jar')
        fileset(dir: pluginsDirPath, includes: '**/lib/*.jar')
        fileset(dir: grailsHome, includes: 'lib/*.jar dist/*.jar')
    }
}

//Returns a list of all relative paths (may be used for building classpaths
private List getRelativeClassPaths(String externalLibsTarget) {
    List classPath = []
    new File(externalLibsTarget).eachFileRecurse { File file ->
        classPath << "lib/${file.name}"
    }
    return classPath
}

setDefaultTarget('jarMain')
