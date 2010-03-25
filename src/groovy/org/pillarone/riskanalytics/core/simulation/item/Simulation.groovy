package org.pillarone.riskanalytics.core.simulation.item

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun

class Simulation extends ModellingItem {

    Parameterization parameterization
    ResultConfiguration template
    private ModelStructure structure // TODO (Sep 9, 2009, msh): implement as query
    private VersionNumber modelVersionNumber // TODO (Sep 9, 2009, msh): use ModelItem

    DateTime beginOfFirstPeriod
    int numberOfIterations
    /**
     * The number of periods run in this simulation. Might be different than the number of periods in the parameterization.
     */
    int periodCount
    Integer randomSeed
    Date start
    Date end

    String comment

    Date creationDate
    Date modificationDate


    public Simulation(String name) {
        super(name)
    }

    protected Object createDao() {
        return new SimulationRun(name: name, model: modelClass?.name)
    }

    public Object getDaoClass() {
        SimulationRun
    }


    public def loadFromDB() {
        SimulationRun.findByName(name)
    }

    protected void mapToDao(def target) {
        SimulationRun run = target as SimulationRun
        run.name = name
        run.comment = comment
        run.model = getModelClass()?.name
        run.parameterization = ParameterizationDAO.find(parameterization.name, run.model, parameterization.versionNumber.toString())
        run.resultConfiguration = ResultConfigurationDAO.findByNameAndItemVersion(template.name, template.versionNumber.toString())
        run.startTime = start
        run.endTime = end
        run.iterations = numberOfIterations
        run.periodCount = periodCount
        run.modelVersionNumber = modelVersionNumber.toString()
        run.beginOfFirstPeriod = beginOfFirstPeriod
        run.creationDate = creationDate
        run.modificationDate = modificationDate
        run.randomSeed = randomSeed
    }

    protected void mapFromDao(def source) {
        SimulationRun run = source as SimulationRun
        comment = run.comment
        modelClass = this.class.classLoader.loadClass(run.model)
        Parameterization parameterization = new Parameterization(run.parameterization.name)
        parameterization.versionNumber = new VersionNumber(run.parameterization.itemVersion)
        this.parameterization = parameterization
        ResultConfiguration resultConfiguration = new ResultConfiguration(run.resultConfiguration.name)
        resultConfiguration.versionNumber = new VersionNumber(run.resultConfiguration.itemVersion)
        template = resultConfiguration
        structure = ModelStructure.getStructureForModel(modelClass)
        numberOfIterations = run.iterations
        periodCount = run.periodCount
        start = run.startTime
        end = run.endTime
        modelVersionNumber = new VersionNumber(run.modelVersionNumber)
        beginOfFirstPeriod = run.beginOfFirstPeriod
        creationDate = run.creationDate
        modificationDate = run.modificationDate
        randomSeed = run.randomSeed
    }

    /**
     * safely called within a transaction from the delete() template method
     */
    protected Object deleteDaoImpl(simulation) {
        assert simulation instanceof SimulationRun
        simulation.deleteSimulationService.deleteSimulation(simulation)
    }

    public SimulationRun getSimulationRun() {
        return dao
    }

    public static List findSimulationNamesForModel(Class modelClass) {
        def results = []

        if (modelClass) {
            def c = SimulationRun.createCriteria()
            results = c.list {
                eq("model", modelClass.name)
                projections {
                    property("name")
                }
            }
        }

        return results
    }

    public VersionNumber getModelVersionNumber() {
        // TODO (Sep 9, 2009, msh): get from ModelItem as soon as reference is present
        return modelVersionNumber
    }

    public void setModelVersionNumber(VersionNumber versionNumber) {
        modelVersionNumber = versionNumber
    }

}
