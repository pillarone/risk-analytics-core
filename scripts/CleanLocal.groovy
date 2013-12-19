includeTargets << grailsScript("_GrailsInit")

target(cleanLocal: "The description of the script goes here!") {
    ant.delete(dir: classesDirPath)
    ant.delete(dir: testDirPath)
}

setDefaultTarget(cleanLocal)
