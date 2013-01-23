package org.pillarone.riskanalytics.core.util

import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.simulation.item.ModellingItem
import org.pillarone.riskanalytics.core.util.ConfigObjectUtils

abstract class ModelInjector {
    Model model
    final ConfigObject configObject
    final String configurationFileName

    public ModelInjector(String configurationFileName) {
        this.@configurationFileName = configurationFileName
        this.@configObject = loadConfiguration(configurationFileName)
        ConfigObjectUtils.spreadRanges(this.configObject)
    }

    public ModelInjector(ModellingItem item) {
        this.@configurationFileName = item.name
        this.@configObject = item.data
        ConfigObjectUtils.spreadRanges(this.configObject)
    }

    public ModelInjector(ConfigObject configObject, String name = 'configObject') {
        this.@configurationFileName = name
        this.@configObject = configObject
        ConfigObjectUtils.spreadRanges(this.configObject)
    }

    void injectConfiguration(Model model) {
        this.model = model
        checkModelMatch(configObject, model)
        injectConfigToModel(configObject, model)
    }

    void checkModelMatch(ConfigObject configObject, Model model) {
        if (!configObject.containsKey("model")) {
            throw new IllegalArgumentException("Configuration error in file ${configurationFileName}. No model specified.")
        }
        if ((getClass().getClassLoader().loadClass(configObject.model.name).name != model.getModelClass().name)) {
            throw new IllegalArgumentException("Configuration error in file ${configurationFileName}. ${configObject.model} does not match model.")
        }
    }

    protected ConfigObject loadConfiguration(String configurationFileName) {
        ConfigObject config = retrieveConfigObject(configurationFileName)
        return config
    }

    protected ConfigObject retrieveConfigObject(String configurationFileName) {
        File file = new File("${configurationFileName}.groovy")
        if (!file.exists()) {
            throw new FileNotFoundException(file.absolutePath)
        }
        ConfigObject config
        GroovyUtils.parseGroovyScript file.text, { ConfigObject conf ->
            config = conf
        }
        return config
    }

    protected abstract void injectConfigToModel(ConfigObject configObject, Model model)
}