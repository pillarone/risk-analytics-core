import grails.util.BuildSettingsHolder
import grails.util.GrailsNameUtils

includeTargets << new File("${releasePluginDir}/scripts/_GrailsMaven.groovy")


target(main: "Checks if the plugin is already deployed to maven and deploys if necessary") {
    depends(parseArguments, packageApp)

    String pluginName
    String version
    String groupId

    def pluginInstance = new GroovyClassLoader().loadClass("${appClassName}GrailsPlugin").newInstance()

    RepoDelegate delegate = new RepoDelegate()
    Closure c = BuildSettingsHolder.settings.config.grails.project.dependency.resolution
    c.delegate = delegate
    c.call()

    version = pluginInstance.version
    if (!version.endsWith('-SNAPSHOT')) {

        try {
            groupId = pluginInstance.groupId
        } catch (Exception e) {
            groupId = "org.grails.plugins"
        }

        pluginName = GrailsNameUtils.getScriptName(appClassName)

        String artefactPattern = [groupId.replace('.' as char, '/' as char), pluginName, version, pluginName + "-" + version + ".zip"].join('/')
        for (Repo repo in delegate.repos) {
            URL url = new URL("${repo.url}$artefactPattern");
            javax.net.ssl.HttpsURLConnection uc = url.openConnection();

            String userpass = "${repo.user}:${repo.pass}"
            String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

            uc.setRequestProperty("Authorization", basicAuth);
            int responseCode = uc.getResponseCode()
            if (responseCode == 200) {
                println "Artefact $artefactPattern already exists in repo: $repo.url. Skip deploy"
                return
            }
        }
    }
    mavenDeploy()

}

setDefaultTarget(main)

class RepoDelegate {

    List<Repo> repos = []

    void mavenRepo(String s, Closure c) {
        Repo repo = new Repo(url: s)
        c.delegate = repo
        c.call()

        repos << repo
    }

    void methodMissing(String s, Object o) {
    }

    void grailsHome() {
    }

}

class Repo {
    String url
    String user
    String pass

    void auth(def info) {
        user = info["username"]
        pass = info["password"]
    }
}


