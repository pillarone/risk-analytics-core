package org.pillarone.riskanalytics.core.simulation.item

import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.DeleteSimulationStrategy
import org.pillarone.riskanalytics.core.output.ResultConfigurationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.SimulationTag
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ResultCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.FunctionComment
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry

class Simulation extends ParametrizedItem {

    Parameterization parameterization
    ResultConfiguration template
    ModelStructure structure // TODO (Sep 9, 2009, msh): implement as query
    VersionNumber modelVersionNumber // TODO (Sep 9, 2009, msh): use ModelItem

    DateTime beginOfFirstPeriod
    int numberOfIterations
    /**
     * The number of periods run in this simulation. Might be different than the number of periods in the parameterization.
     */
    int periodCount
    Integer randomSeed
    volatile DateTime start
    volatile DateTime end

    String comment

    List<ParameterHolder> runtimeParameters = []
    List<Tag> tags

    private SimulationRun run

    public Simulation(String name) {
        super(name)
        tags = []
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
        run.resultConfiguration = ResultConfigurationDAO.find(template.name, run.model, template.versionNumber.toString())
        run.startTime = start
        run.endTime = end
        run.iterations = numberOfIterations
        run.periodCount = periodCount
        if (modelVersionNumber != null) {
            run.usedModel = ModelDAO.findByModelClassNameAndItemVersion(modelClass.name, modelVersionNumber.toString())
        }
        run.beginOfFirstPeriod = beginOfFirstPeriod
        run.creationDate = creationDate
        run.creator = creator
        run.modificationDate = modificationDate
        run.randomSeed = randomSeed
        saveComments(run)
        saveTags(run)
        saveParameters(runtimeParameters, run.runtimeParameters, run)
        println()
    }

    protected void mapFromDao(def source, boolean completeLoad) {
        SimulationRun run = source as SimulationRun
        comment = run.comment
        modelClass = ModelRegistry.instance.getModelClass(run.model)
        Parameterization parameterization = new Parameterization(run.parameterization.name)
        parameterization.versionNumber = new VersionNumber(run.parameterization.itemVersion)
        parameterization.modelClass = modelClass
        this.parameterization = parameterization
        ResultConfiguration resultConfiguration = new ResultConfiguration(run.resultConfiguration.name)
        resultConfiguration.versionNumber = new VersionNumber(run.resultConfiguration.itemVersion)
        resultConfiguration.modelClass = modelClass
        template = resultConfiguration
        structure = ModelStructure.getStructureForModel(modelClass)
        numberOfIterations = run.iterations
        periodCount = run.periodCount
        start = run.startTime
        end = run.endTime
        if (run.usedModel != null) {
            modelVersionNumber = new VersionNumber(run.usedModel.itemVersion)
        }
        beginOfFirstPeriod = run.beginOfFirstPeriod
        creationDate = run.creationDate
        creator = run.creator
        modificationDate = run.modificationDate
        randomSeed = run.randomSeed
        if (completeLoad) {
            loadComments(run)
            loadParameters(runtimeParameters, run.runtimeParameters)
            tags = run.tags*.tag
        }
    }

    @Override
    protected void addToDao(Parameter parameter, Object dao) {
        dao = dao as SimulationRun
        dao.addToRuntimeParameters(parameter)
    }

    @Override
    protected void removeFromDao(Parameter parameter, Object dao) {
        dao = dao as SimulationRun
        dao.removeFromRuntimeParameters(parameter)
    }

    void addParameter(ParameterHolder parameter) {
        runtimeParameters << parameter
        parameter.added = true
    }

    void removeParameter(ParameterHolder parameter) {
        if (parameter.added) {
            runtimeParameters.remove(parameter)
            return
        }
        parameter.removed = true
        parameter.modified = false
    }

    private void loadComments(SimulationRun dao) {
        comments = []

        for (ResultCommentDAO c in dao.comments) {
            comments << new FunctionComment(c)
        }

    }

    protected void saveTags(SimulationRun run) {
        List tagsToRemove = []
        for (SimulationTag tag in run.tags) {
            if (!tags.contains(tag.tag)) {
                tagsToRemove << tag
            }
        }
        for (SimulationTag tag in tagsToRemove) {
            run.removeFromTags(tag)

        }
        tagsToRemove.each {it.delete()}

        for (Tag tag in tags) {
            if (!run.tags*.tag?.contains(tag)) {
                run.addToTags(new SimulationTag(tag: tag))
            }
        }
    }

    public void setTags(Set selectedTags) {
        selectedTags.each {Tag tag ->
            if (!tags.contains(tag))
                tags << tag
        }
        List tagsToRemove = []
        tags.each {Tag tag ->
            if (!selectedTags.contains(tag))
                tagsToRemove << tag
        }
        if (tagsToRemove.size() > 0)
            tags.removeAll(tagsToRemove)
    }

    /**
     * safely called within a transaction from the delete() template method
     */
    protected Object deleteDaoImpl(simulation) {
        assert simulation instanceof SimulationRun
        DeleteSimulationStrategy.instance.deleteSimulation(simulation)
        return true
    }

    public SimulationRun getSimulationRun() {
        //cache SimulationRun to avoid db query every time this method is called
        if (this.run == null) {
            this.run = dao
        }
        return this.run
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

    void setModelClass(Class clazz) {
        super.setModelClass(clazz)
        modelVersionNumber = Model.getModelVersion(clazz)
    }

    CommentDAO getItemCommentDAO(def dao) {
        return new ResultCommentDAO(simulationRun: dao)
    }

    int getSize(Class commentType) {
        return ResultCommentDAO.executeQuery("select count(*) from ${ResultCommentDAO.class.name} as r where r.simulationRun.name = ? and  r.simulationRun.model = ?", [name, modelClass.name])[0]

    }

}
