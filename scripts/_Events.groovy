import groovy.io.FileType
import net.sf.jasperreports.engine.JasperCompileManager

eventCompileEnd = {
    println "[jasper] - compiling reports"
    for (File reportFolder in reportFolders) {
        if (reportFolder.exists()) {
            reportFolder.eachFileRecurse FileType.FILES, { File sourceFile ->
                if (sourceFile.name.endsWith(".jrxml")) {
                    File jasperFile = new File(sourceFile.absolutePath.replace(".jrxml", ".jasper"))
                    if (!jasperFile.exists() || jasperFile.lastModified() < sourceFile.lastModified()) {
                        println "[jasper] - compiling ${sourceFile.name}"
                        JasperCompileManager.compileReportToStream(sourceFile.newInputStream(), jasperFile.newOutputStream())
                    }
                }
            }
        } else {
            println "[jasper] - no reports found in ${reportFolder}"
        }
    }
}

eventCleanEnd = {
    println "[jasper] - deleting reports"
    for (File reportFolder in reportFolders) {
        if (reportFolder.exists()) {
            reportFolder.eachFileRecurse FileType.FILES, { File file ->
                if (file.name.endsWith(".jasper")) {
                    println "[jasper] - deleting ${file.name}"
                    file.delete()
                }
            }
        }
    }
}

private List<File> getReportFolders() {
    final def folders = buildConfig.reportFolders
    if (folders instanceof List) {
        return folders
    }

    return []
}