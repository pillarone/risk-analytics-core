import grails.util.BuildSettingsHolder
import grails.util.GrailsNameUtils
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.plugins.repository.url.URLRepository
import org.apache.ivy.plugins.resolver.RepositoryResolver

includeTargets << new File("${releasePluginDir}/scripts/_GrailsMaven.groovy")

target(main: "Checks if the plugin is already deployed to maven and deploys if necessary") {
    depends(parseArguments)

    String pluginName = appClassName
    String version
    String groupId

    def pluginInstance = Thread.currentThread().contextClassLoader.loadClass(appClassName + "GrailsPlugin").newInstance()

    version = pluginInstance.version
    try {
        groupId = pluginInstance.groupId
    } catch (Exception e) {
        groupId = "org.grails.plugins"
    }

    pluginName = GrailsNameUtils.getScriptName(pluginName)

    String artefactPattern = [groupId.replace('.' as char, '/' as char), pluginName, version, pluginName + "-" + version + ".zip"].join('/')
    for (RepositoryResolver resolver in BuildSettingsHolder.settings.dependencyManager.chainResolver.resolvers) {
        if (resolver instanceof IBiblioResolver) {
            URLRepository repository = new URLRepository()
            String url = resolver.root + artefactPattern
            if (repository.getResource(url).exists()) {
                println "Plugin found in repository ${resolver.name}: $url"
                return
            }
        }
    }
    mavenDeploy()

}

setDefaultTarget(main)
