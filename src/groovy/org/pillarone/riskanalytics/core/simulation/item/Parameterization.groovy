package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import org.apache.commons.lang.builder.HashCodeBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.pillarone.riskanalytics.core.ModelDAO
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.model.MigratableModel
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.model.registry.ModelRegistry
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

import static org.pillarone.riskanalytics.core.workflow.Status.DATA_ENTRY
import static org.pillarone.riskanalytics.core.workflow.Status.NONE

class Parameterization extends ParametrizedItem {

    private static final Log LOG = LogFactory.getLog(Parameterization)

    public static final String PERIOD_DATE_FORMAT = "yyyy-MM-dd"

    String comment
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

    //Warning: Dont trust Intellij to refactor this to parameterValidations.
    //There are references to existing name that don't get picked up because of the joys of dynamic typing.
    //Then it crashes and burns when you try to open a parameterization in the gui five days later...
    List<ParameterValidation> validationErrors

    Status status
    Long dealId
    DateTime valuationDate

    public Parameterization(String name) {
        super(name)
        versionNumber = new VersionNumber('1')
        parameterHolders = []
        status = NONE
        periodCount = 1
    }

    Parameterization(String name, Class modelClass) {
        this(name)
        this.modelClass = modelClass
    }

    void logDeleteSuccess() {
        LOG.info("DELETED  ${name} v${versionNumber} (status: ${status})")
    }

    //frahman 20140104 Convenience method for occasional checkups outside of model migrations.
    //Can just call this during BootStrap against a copy of your DB and present your users with any errors.
    //
    static void warnAllPnValidationErrors(Class<? extends MigratableModel> modelClass) {
        LOG.info("Checking all ${modelClass.name} Pns for validation errors")
        for (ParameterizationDAO dao in ParameterizationDAO.findAllByModelClassName(modelClass.name)) {
            Parameterization parameterization = new Parameterization(dao.name)
            parameterization.versionNumber = new VersionNumber(dao.itemVersion)
            parameterization.modelClass = modelClass
            parameterization.load(true)
            LOG.info("Validating ${parameterization.nameAndVersion} for errors")
            parameterization.validate()
            if (!parameterization.valid) {
                LOG.warn("${parameterization.nameAndVersion} has validation errors: [" + parameterization.getValidationErrors() + "]")
            }
        }
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
        List<ParameterValidation> validations = []
        for (IParameterizationValidator validator in ValidatorRegistry.validators) {
            validations.addAll(validator.validate(notDeletedParameterHolders))
        }

        valid = validations.empty || validations.every { ParameterValidation validation -> validation.validationType != ValidationType.ERROR }
        validationErrors = validations
    }

    //Beware of naming a method getXXX because it might suppress a generated field getter and then you screw up
    //callers expecting to get the field instead. More joys of dynamic typed 'languages'.
    String getRealValidationErrors() {
        StringBuilder errors = new StringBuilder();
        validationErrors.each {
            if (it.validationType == ValidationType.ERROR) {
                errors.append("Error msg: ${it.msg} for path ${it.path}; ")
                // Eg Error msg: period.value.below.min.period for path structures:subOverall:parmContractStrategy:structure;
            }
        }
        return errors.toString();
    }

    Long save() {
        def result = null
        daoClass.withTransaction { TransactionStatus status ->
            def daoToBeSaved = dao
            validate()
            if (!valid) {
                LOG.warn("${daoToBeSaved} has validation errors: [" + realValidationErrors + "]")
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
        LOG.info("SAVED ${name} v${versionNumber} (status: ${status})")
        return result
    }

    @CompileStatic
    private List obtainPeriodLabelsFromParameters() {
        Model model = (Model) modelClass.newInstance()
        model.init()
        ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: this)
        try {
            applicator.init()
        } catch (ParameterApplicator.ApplicableParameterCreationException ignored) {
            LOG.warn("failed to init applicator")
            return null
        } catch (Exception e) {
            LOG.error("failed to init applicator", e)
        }
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
    }

    protected void mapToDao(Object dao) {
        ParameterizationDAO parameterizationDAO = dao as ParameterizationDAO
        parameterizationDAO.itemVersion = versionNumber.toString()
        parameterizationDAO.name = name
        parameterizationDAO.periodCount = periodCount
        List periodDates = obtainPeriodLabelsFromParameters()
        if (periodDates != null) {
            periodLabels = periodDates
        }
        parameterizationDAO.periodLabels = periodLabels != null && !periodLabels.empty ? periodLabels.join(";") : null
        parameterizationDAO.creationDate = creationDate
        parameterizationDAO.modificationDate = modificationDate
        parameterizationDAO.valid = valid
        parameterizationDAO.modelClassName = modelClass.name
        if (modelVersionNumber != null) {
            parameterizationDAO.model = ModelDAO.findByModelClassNameAndItemVersion(modelClass.name, modelVersionNumber.toString())
        }
        parameterizationDAO.creator = creator
        parameterizationDAO.lastUpdater = lastUpdater
        parameterizationDAO.comment = comment
        parameterizationDAO.status = status
        parameterizationDAO.dealId = dealId
        parameterizationDAO.valuationDate = valuationDate
        saveParameters(parameterHolders, parameterizationDAO.parameters, parameterizationDAO)
        saveComments(parameterizationDAO)
        saveTags(parameterizationDAO)
    }

    @Override
    protected void addToDao(Parameter parameter, Object dao) {
        (dao as ParameterizationDAO).addToParameters(parameter)
    }

    @Override
    protected void removeFromDao(Parameter parameter, Object dao) {
        (dao as ParameterizationDAO).removeFromParameters(parameter)
    }

    protected void saveTags(ParameterizationDAO dao) {
        List<ParameterizationTag> tagsToRemove = []
        String tagDeltas = "" //PMO-2737
        for (ParameterizationTag tag in dao.tags) {
            if (!tags.contains(tag.tag)) {
                tagsToRemove << tag
            }
        }
        for (ParameterizationTag tag in tagsToRemove) {
            dao.removeFromTags(tag)
            tagDeltas += "-${tag.tag.name},"
        }
        tagsToRemove.each { it.delete() }

        for (Tag tag in tags) {
            if (!dao.tags*.tag?.contains(tag)) {
                dao.addToTags(new ParameterizationTag(tag: tag))
                tagDeltas += "+${tag.name},"
            }
        }
        if (tagDeltas.length() > 0) {
            LOG.info("+/-TAGS on '${nameAndVersion}': $tagDeltas")
        }
    }

    @Override
    @CompileStatic
    void addComment(Comment comment) {
        changed = true
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
        changed = true
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
        ParameterizationDAO parameterizationDAO = dao as ParameterizationDAO
        long time = System.currentTimeMillis()
        id = parameterizationDAO.id
        versionNumber = new VersionNumber(parameterizationDAO.itemVersion)
        name = parameterizationDAO.name
        periodCount = parameterizationDAO.periodCount
        periodLabels = parameterizationDAO.periodLabels != null && parameterizationDAO.periodLabels.trim().length() > 0 ? parameterizationDAO.periodLabels.split(';') : []
        creationDate = parameterizationDAO.creationDate
        modificationDate = parameterizationDAO.modificationDate
        valid = parameterizationDAO.valid
        modelClass = ModelRegistry.instance.getModelClass(parameterizationDAO.modelClassName)
        if (parameterizationDAO.model != null) {
            modelVersionNumber = new VersionNumber(parameterizationDAO.model.itemVersion)
        }
        creator = parameterizationDAO.creator
        lastUpdater = parameterizationDAO.lastUpdater
        status = parameterizationDAO.status
        dealId = parameterizationDAO.dealId
        valuationDate = parameterizationDAO.valuationDate
        comment = parameterizationDAO.comment
        if (completeLoad) {
            loadParameters(parameterHolders, parameterizationDAO.parameters)
            loadComments(parameterizationDAO)
            tags = parameterizationDAO.tags*.tag
            if (!tags) tags = []
        }
        LOG.debug("LOADED ${name} v${versionNumber} (status: ${status}) in ${System.currentTimeMillis() - time}ms")
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
        return ParameterizationDAO.find(name, modelClass?.name, versionNumber.toString())
    }

    @Override
    @CompileStatic
    void unload() {
        super.unload()
        parameterHolders?.clear()
        comments?.clear()
        tags?.clear()
    }

    def getDao() {
        if (parameterizationDAO?.id == null) {
            parameterizationDAO = createDao() as ParameterizationDAO
            return parameterizationDAO
        } else {
            //TODO If this is just a verbose way of repeating the prior return, refactor.
            //Otherwise, a short explanation of the 'magic' would be useful for the non-wizards among us..
            return daoClass.get(parameterizationDAO.id)
        }
    }

    void setDao(def newDao) {
        parameterizationDAO = newDao
    }

    @CompileStatic
    int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder()
        hashCodeBuilder.append(name)
        hashCodeBuilder.append(modelClass)
        hashCodeBuilder.append(versionNumber.toString())
        return hashCodeBuilder.toHashCode()
    }

    boolean equals(Object obj) {
        if (obj instanceof Parameterization) {
            return super.equals(obj) && obj.versionNumber.equals(versionNumber)
        } else {
            return false
        }
    }

    boolean isUsedInSimulation() {
        return SimulationRun.find("from ${SimulationRun.class.name} as run where run.parameterization.name = ? and run.parameterization.modelClassName = ? and run.parameterization.itemVersion =?", [name, modelClass.name, versionNumber.toString()]) != null
    }


    @CompileStatic
    boolean isEditable() {
        if (status != NONE && status != DATA_ENTRY) {
            return false
        }
        return !usedInSimulation
    }

    @CompileStatic
    List<String> getAllEditablePaths() {
        List result = []
        for (Comment comment in comments) {
            if (comment instanceof WorkflowComment) {
                result << comment.path
            }
        }
        return result
    }

    @CompileStatic
    boolean newVersionAllowed() {
        return status == NONE || status == DATA_ENTRY
    }

    List<Simulation> getSimulations() {
        if (!loaded) {
            load()
        }
        SimulationRun.findAllByParameterizationAndToBeDeleted(dao, false).collect {
            new Simulation(it.name)
        }
    }

    @Override
    @CompileStatic
    List<ParameterHolder> getAllParameterHolders() {
        return parameterHolders
    }

    @CompileStatic
    List<Tag> getTags() {
        return tags
    }

    void addRemoveLockTag() {
        if (!loaded) {
            load()
        }
        Tag locked = Tag.findByName(Tag.LOCKED_TAG)
        if (!locked) {
            throw new IllegalStateException("Tag with name $Tag.LOCKED_TAG has to be in database")
        }
        boolean changed = false
        if (!tags.contains(locked) && usedInSimulation) {
            tags << locked
            changed = true
        } else if (tags.contains(locked) && !usedInSimulation) {
            tags.remove(locked)
            changed = true
        }
        if (changed) {
            save()
        }
    }

    void setTags(Set selectedTags) {
        selectedTags.each { Tag tag ->
            if (!tags.contains(tag)) {
                tags << tag
            }
        }
        List tagsToRemove = []
        tags.each { Tag tag ->
            if (!selectedTags.contains(tag)) {
                tagsToRemove << tag
            }
        }
        if (tagsToRemove.size() > 0) {
            tags.removeAll(tagsToRemove)
        }
    }

    List getParameters(String path) {
        def params = parameters.findAll { ParameterHolder parameter ->
            parameter.path == path && !parameter.removed
        }
        ArrayList list = params.toList().sort { orderByPath ? it.path : it.periodIndex }
        return list
    }

    List<ParameterHolder> getParameters() {
        return (orderByPath) ? parameterHolders.sort { it.path } : parameterHolders
    }

    ConfigObject toConfigObject() {
        if (!loaded) {
            load()
        }
        ConfigObject original = new ConfigObject()
        original.model = modelClass
        original.periodCount = periodCount
        original.displayName = name
        original.applicationVersion = new PropertiesUtils().getProperties("/version.properties").getProperty("version", "N/A")
        if (periodLabels) {
            original.periodLabels = periodLabels
        }
        parameters.each { ParameterHolder p ->
            ConfigObject configObject = original

            String[] keys = "components:${p.path}".split(":")
            keys.eachWithIndex { key, index ->
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
        tags.each { Tag tag ->
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
        super.modelClass = clazz
        modelVersionNumber = clazz ? Model.getModelVersion(clazz) : null
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
    String getNameAndVersion() {
        "$name v${versionNumber.toString()}"
    }
}
