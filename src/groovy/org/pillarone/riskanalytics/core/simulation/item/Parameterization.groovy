package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import org.apache.commons.lang.builder.HashCodeBuilder
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterizationTag
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ParameterizationCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowCommentDAO
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.parameterization.ParameterWriter
import org.pillarone.riskanalytics.core.parameterization.validation.IParameterizationValidator
import org.pillarone.riskanalytics.core.parameterization.validation.ParameterValidation
import org.pillarone.riskanalytics.core.parameterization.validation.ValidationType
import org.pillarone.riskanalytics.core.parameterization.validation.ValidatorRegistry
import org.pillarone.riskanalytics.core.simulation.ILimitedPeriodCounter
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter
import org.pillarone.riskanalytics.core.util.PropertiesUtils
import org.pillarone.riskanalytics.core.workflow.Status
import org.springframework.transaction.TransactionStatus
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry

class Parameterization extends ParametrizedItem {

    public static final String PERIOD_DATE_FORMAT = "yyyy-MM-dd"

    String comment
    VersionNumber versionNumber
    VersionNumber modelVersionNumber
    private ParameterizationDAO parameterizationDAO
    /**
     * This is the number of different periods available in this parameterization.
     * This is not necessarily the same as the period count in Simulation.
     */
    Integer periodCount
    List<ParameterHolder> parameterHolders
    List<Tag> tags = []
    List<String> periodLabels

    boolean orderByPath = false
    boolean valid

    List validationErrors

    Status status
    Long dealId
    DateTime valuationDate

    public Parameterization(Map params) {
        this(params.remove("name").toString())
        params.each {k, v ->
            this[k] = v
        }
    }

    @CompileStatic
    public Parameterization(String name) {
        super(name)
        setName(name)
        versionNumber = new VersionNumber('1')
        parameterHolders = []
        status = Status.NONE
        periodCount = 1
    }

    @CompileStatic
    public Parameterization(String name, Class modelClass) {
        this(name)
        this.modelClass = modelClass
    }

    @CompileStatic
    protected Object createDao() {
        return new ParameterizationDAO()
    }

    @CompileStatic
    public Object getDaoClass() {
        ParameterizationDAO
    }

    void validate() {
        valid = false
        List<ParameterValidation> errors = []
        for (IParameterizationValidator validator in ValidatorRegistry.getValidators()) {
            errors.addAll(validator.validate(parameterHolders.findAll { ParameterHolder it -> !it.removed }))
        }

        valid = errors.empty || errors.every {ParameterValidation validation -> validation.validationType != ValidationType.ERROR}
        validationErrors = errors
    }

    public save() {
        def result = null
        daoClass.withTransaction {TransactionStatus status ->
            def daoToBeSaved = getDao()
            validate()
            if (!validationErrors.empty) {
                LOG.warn("${daoToBeSaved} is not valid")
            }

            setChangeUserInfo()
            mapToDao(daoToBeSaved)

            if (!daoToBeSaved.save(flush: true)) logErrors(daoToBeSaved)

            changed = false
            dao = daoToBeSaved
            result = daoToBeSaved.id
            id = daoToBeSaved.id
            notifyItemSaved()
        }
        LOG.info("Saved parameterization ${this}")
        return result
    }

    @CompileStatic
    private List obtainPeriodLabelsFromParameters() {
        try {
            Model model = (Model) modelClass.newInstance()
            model.init()
            ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: this)
            applicator.init()
            applicator.applyParameterForPeriod(0)
            DateTimeFormatter formatter = DateTimeFormat.forPattern(PERIOD_DATE_FORMAT)
            IPeriodCounter counter = model.createPeriodCounter(null)
            if (counter == null) {
                return null
            }
            List result = []
            if (counter instanceof ILimitedPeriodCounter) {
                for (int i = 0; i < counter.periodCount(); i++) {
                    result << formatter.print(counter.currentPeriodStart)
                    counter.next()
                }
            }
            return result
        } catch (Exception e) {
            return null
        }
    }

    protected void mapToDao(Object dao) {
        dao = dao as ParameterizationDAO
        dao.itemVersion = versionNumber.toString()
        dao.name = name
        dao.periodCount = periodCount
        List periodDates = obtainPeriodLabelsFromParameters()
        if (periodDates != null) {
            periodLabels = periodDates
        }
        dao.periodLabels = periodLabels != null && !periodLabels.empty ? periodLabels.join(";") : null
        dao.creationDate = creationDate
        dao.modificationDate = modificationDate
        dao.valid = valid
        dao.modelClassName = modelClass.name
        if (modelVersionNumber != null) {
            dao.model = ModelDAO.findByModelClassNameAndItemVersion(modelClass.name, modelVersionNumber.toString())
        }
        dao.creator = creator
        dao.lastUpdater = lastUpdater
        dao.comment = comment
        dao.status = status
        dao.dealId = dealId
        dao.valuationDate = valuationDate
        saveParameters(parameterHolders, dao.parameters, dao)
        saveComments(dao)
        saveTags(dao)
    }

    @Override
    protected void addToDao(Parameter parameter, Object dao) {
        dao = dao as ParameterizationDAO
        dao.addToParameters(parameter)
    }

    @Override
    protected void removeFromDao(Parameter parameter, Object dao) {
        dao = dao as ParameterizationDAO
        dao.removeFromParameters(parameter)
    }

    protected void saveTags(ParameterizationDAO dao) {
        List tagsToRemove = []
        for (ParameterizationTag tag in dao.tags) {
            if (!tags.contains(tag.tag)) {
                tagsToRemove << tag
            }
        }
        for (ParameterizationTag tag in tagsToRemove) {
            dao.removeFromTags(tag)

        }
        tagsToRemove.each {it.delete()}

        for (Tag tag in tags) {
            if (!dao.tags*.tag?.contains(tag)) {
                dao.addToTags(new ParameterizationTag(tag: tag))
            }
        }
    }

    @Override
    @CompileStatic
    void addComment(Comment comment) {
        setChanged(true)
        super.addComment(comment)
    }

    @CompileStatic
    void addTaggedComment(String commentText, Tag tag) {
        String modelName = Model.getName(modelClass)
        Comment comment = new Comment(modelName, -1)
        comment.text = commentText
        comment.addTag(tag)
        this.addComment(comment)
    }

    @Override
    @CompileStatic
    void removeComment(Comment comment) {
        setChanged(true)
        super.removeComment(comment)
    }

    protected void commentAdded(ParameterizationDAO dao, WorkflowComment comment) {
        WorkflowCommentDAO commentDAO = new WorkflowCommentDAO(parameterization: dao)
        comment.applyToDomainObject(commentDAO)
        dao.addToIssues(commentDAO)
        comment.added = false
    }

    protected void commentUpdated(ParameterizationDAO dao, WorkflowComment comment) {
        WorkflowCommentDAO commentDAO = dao.issues.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            comment.applyToDomainObject(commentDAO)
            comment.updated = false
        }
    }

    protected boolean commentDeleted(ParameterizationDAO dao, WorkflowComment comment) {
        WorkflowCommentDAO commentDAO = dao.issues.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            dao.removeFromIssues(commentDAO)
            commentDAO.delete()
            return true
        }
        return false
    }

    CommentDAO getItemCommentDAO(def dao) {
        return new ParameterizationCommentDAO(parameterization: dao)
    }

    protected void mapFromDao(Object dao, boolean completeLoad) {
        dao = dao as ParameterizationDAO
        long time = System.currentTimeMillis()
        id = dao.id
        versionNumber = new VersionNumber(dao.itemVersion)
        name = dao.name
        periodCount = dao.periodCount
        periodLabels = dao.periodLabels != null && dao.periodLabels.trim().length() > 0 ? dao.periodLabels.split(';') : []
        creationDate = dao.creationDate
        modificationDate = dao.modificationDate
        valid = dao.valid
        modelClass = ModelRegistry.instance.getModelClass(dao.modelClassName)
        if (dao.model != null) {
            modelVersionNumber = new VersionNumber(dao.model.itemVersion)
        }
        creator = dao.creator
        lastUpdater = dao.lastUpdater
        status = dao.status
        dealId = dao.dealId
        valuationDate = dao.valuationDate
        comment = dao.comment
        if (completeLoad) {
            loadParameters(parameterHolders, dao.parameters)
            loadComments(dao)
            tags = dao.tags*.tag
            if (!tags) tags = []
        }
        LOG.info("Parameterization $name loaded in ${System.currentTimeMillis() - time}ms")
    }

    @Override
    void loaded(boolean completeLoad) {
        loaded = completeLoad
    }

    private void loadComments(ParameterizationDAO dao) {
        comments = []

        for (ParameterizationCommentDAO c in dao.comments) {
            comments << new Comment(c)
        }

        for (WorkflowCommentDAO c in dao.issues) {
            comments << new WorkflowComment(c)
        }
    }

    protected loadFromDB() {
        return ParameterizationDAO.find(name, getModelClass()?.name, versionNumber.toString())
    }

    @Override
    @CompileStatic
    void unload() {
        super.unload()
        loaded = false
        parameterHolders?.clear()
        comments?.clear()
        tags?.clear()
    }

    public getDao() {
        if (parameterizationDAO?.id == null) {
            parameterizationDAO = createDao()
            return parameterizationDAO
        } else {
            return getDaoClass().get(parameterizationDAO.id)
        }
    }

    public void setDao(def newDao) {
        parameterizationDAO = newDao
    }

    @CompileStatic
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder()
        hashCodeBuilder.append(name)
        hashCodeBuilder.append(modelClass)
        hashCodeBuilder.append(versionNumber.toString())
        return hashCodeBuilder.toHashCode()
    }

    public boolean equals(Object obj) {
        if (obj instanceof Parameterization) {
            return obj.name.equals(name) && obj.modelClass.equals(modelClass) && obj.versionNumber.equals(versionNumber)
        } else {
            return false
        }
    }

    public boolean isUsedInSimulation() {
        return SimulationRun.find("from ${SimulationRun.class.name} as run where run.parameterization.name = ? and run.parameterization.modelClassName = ? and run.parameterization.itemVersion =?", [name, modelClass.name, versionNumber.toString()]) != null
    }

    @Override
    @CompileStatic
    boolean isLoaded() {
        return loaded
    }

    @CompileStatic
    public boolean isEditable() {
        if (status != Status.NONE && status != Status.DATA_ENTRY) {
            return false
        }
        return !isUsedInSimulation()
    }

    @CompileStatic
    public List<String> getAllEditablePaths() {
        List result = []
        for (Comment comment in comments) {
            if (comment instanceof WorkflowComment) {
                result << comment.path
            }
        }
        return result
    }

    @CompileStatic
    public boolean newVersionAllowed() {
        return status == Status.NONE || status == Status.DATA_ENTRY
    }

    public List<SimulationRun> getSimulations() {
        if (!isLoaded()) {
            load()
        }
        List<SimulationRun> result
        try {
            result = SimulationRun.findAllByParameterizationAndToBeDeleted(dao, false)
        } catch (Exception e) {
            LOG.error("Exception in method isUsedInSimulation : $e.message", e)
        }
        return result
    }


    @Override
    @CompileStatic
    List<ParameterHolder> getAllParameterHolders() {
        return parameterHolders
    }

    @CompileStatic
    public List<Tag> getTags() {
        return tags
    }

    public void addRemoveLockTag() {
        if (!isLoaded()) load()
        Tag locked = Tag.findByName(Tag.LOCKED_TAG)
        boolean changed = false
        if (!tags.contains(locked) && isUsedInSimulation()) {
            tags << locked
            changed = true
        }
        else if (tags.contains(locked) && !isUsedInSimulation()) {
            tags.remove(locked)
            changed = true
        }
        if (changed) {
            save()
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

    List getParameters(String path) {
        def params = parameters.findAll {ParameterHolder parameter ->
            parameter.path == path && !parameter.removed
        }
        ArrayList list = params.toList().sort {orderByPath ? it.path : it.periodIndex }
        return list
    }

    List<ParameterHolder> getParameters() {
        return (orderByPath) ? parameterHolders.sort { it.path} : parameterHolders
    }

    ConfigObject toConfigObject() {
        if (!isLoaded()) {
            load()
        }

        ConfigObject original = new ConfigObject()
        original.model = getModelClass()
        original.periodCount = periodCount
        original.displayName = name
        original.applicationVersion = new PropertiesUtils().getProperties("/version.properties").getProperty("version", "N/A")
        if (periodLabels) {
            original.periodLabels = periodLabels
        }
        parameters.each {ParameterHolder p ->
            ConfigObject configObject = original

            String[] keys = "components:${p.path}".split(":")
            keys.eachWithIndex {key, index ->
                configObject = configObject[key]
                if (index + 1 == keys.length) {
                    configObject[p.periodIndex] = p.businessObject
                }
            }
        }
        original.comments = []
        comments.each { Comment comment ->
            original.comments << comment.toConfigObject()
        }

        original.tags = []
        tags.each {Tag tag ->
            if (tag.toString() != Tag.LOCKED_TAG) {
                original.tags << tag.toString()
            }
        }

        return original
    }

    @CompileStatic
    String getPeriodLabel(int index) {
        if (periodLabels != null && !periodLabels.empty) {
            return periodLabels[index]
        }
        return "P$index".toString()
    }

    @CompileStatic
    void setModelClass(Class clazz) {
        super.setModelClass(clazz)
        modelVersionNumber = Model.getModelVersion(clazz)
    }

    @CompileStatic
    IConfigObjectWriter getWriter() {
        return new ParameterWriter()
    }

    int getSize(Class commentType) {
        switch (commentType) {
            case ParameterizationCommentDAO:
                return ParameterizationCommentDAO.executeQuery("select count(*) from ${ParameterizationCommentDAO.class.name} as c where c.parameterization.name = ? and c.parameterization.itemVersion = ? and c.parameterization.modelClassName = ?", [name, versionNumber.toString(), modelClass.name])[0]
            case WorkflowCommentDAO:
                return WorkflowCommentDAO.executeQuery("select count(*) from ${WorkflowCommentDAO.class.name} as w where w.parameterization.name = ? and w.parameterization.itemVersion = ? and w.parameterization.modelClassName = ?", [name, versionNumber.toString(), modelClass.name])[0]
        }
        return 0
    }

    @Override
    @CompileStatic
    String toString() {
        nameAndVersion
    }

    @Override
    @CompileStatic
    String getNameAndVersion() {
        "$name v${versionNumber.toString()}"
    }
}
