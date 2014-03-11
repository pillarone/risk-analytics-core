package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.SimulationProfileDAO
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder

class SimulationProfile extends ParametrizedItem {

    ResultConfiguration template

    Integer randomSeed
    List<ParameterHolder> runtimeParameters = []
    Integer numberOfIterations

    SimulationProfile(String name) {
        super(name)
    }

    static boolean exists(String name) {
        SimulationProfileDAO.countByName(name) != 0
    }

    static List<String> findAllNamesForModelClass(Class modelClass) {
        SimulationProfileDAO.allNamesForModelClass(modelClass).list()
    }

    @Override
    protected createDao() {
        new SimulationProfileDAO()
    }

    @Override
    def getDaoClass() {
        SimulationProfileDAO
    }

    @Override
    protected void mapToDao(def dao) {
        SimulationProfileDAO simulationSettingsDao = dao as SimulationProfileDAO
        simulationSettingsDao.name = name
        simulationSettingsDao.template = ResultConfigurationDAO.find(template.name, template.modelClass.name, template.versionNumber.toString())
        simulationSettingsDao.creationDate = creationDate
        simulationSettingsDao.creator = creator
        simulationSettingsDao.lastUpdater = lastUpdater
        simulationSettingsDao.modificationDate = modificationDate
        simulationSettingsDao.randomSeed = randomSeed
        simulationSettingsDao.numberOfIterations = numberOfIterations
        saveParameters(runtimeParameters, simulationSettingsDao.runtimeParameters, simulationSettingsDao)
    }

    @Override
    protected void mapFromDao(Object dao, boolean completeLoad) {
        SimulationProfileDAO simulationSettingsDAO = dao as SimulationProfileDAO
        modelClass = ModelRegistry.instance.getModelClass(simulationSettingsDAO.template.modelClassName)
        ResultConfiguration resultConfiguration = new ResultConfiguration(simulationSettingsDAO.template.name)
        resultConfiguration.versionNumber = new VersionNumber(simulationSettingsDAO.template.itemVersion)
        resultConfiguration.modelClass = modelClass
        template = resultConfiguration
        numberOfIterations = simulationSettingsDAO.numberOfIterations
        creationDate = simulationSettingsDAO.creationDate
        creator = simulationSettingsDAO.creator
        lastUpdater = simulationSettingsDAO.lastUpdater
        modificationDate = simulationSettingsDAO.modificationDate
        randomSeed = simulationSettingsDAO.randomSeed
        loadParameters(runtimeParameters, simulationSettingsDAO.runtimeParameters)
    }

    @Override
    Integer getPeriodCount() {
        return null
    }

    @Override
    void addComponent(String basePath, Component component) {
        throw new UnsupportedOperationException("not allowed for ${SimulationProfile.simpleName}")
    }

    @Override
    void copyComponent(String oldPath, String newPath, Component component, boolean copyComments) {
        throw new UnsupportedOperationException("not allowed for ${SimulationProfile.simpleName}")
    }

    @Override
    void renameComponent(String oldPath, String newPath, Component newComponent) {
        throw new UnsupportedOperationException("not allowed for ${SimulationProfile.simpleName}")
    }

    @Override
    void removeComponent(String path) {
        throw new UnsupportedOperationException("not allowed for ${SimulationProfile.simpleName}")
    }

    @Override
    protected void addToDao(Parameter parameter, def Object dao) {
        (dao as SimulationProfileDAO).addToRuntimeParameters(parameter)
    }

    @Override
    protected void removeFromDao(Parameter parameter, def Object dao) {
        (dao as SimulationProfileDAO).removeFromRuntimeParameters(parameter)
    }

    @Override
    protected List<ParameterHolder> getAllParameterHolders() {
        runtimeParameters
    }

    @Override
    protected loadFromDB() {
        SimulationProfileDAO.findByName(name)
    }
}
