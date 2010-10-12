package org.pillarone.riskanalytics.core.simulation.item

import java.text.SimpleDateFormat
import org.apache.commons.lang.builder.HashCodeBuilder
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.parameterization.ParameterWriter
import org.pillarone.riskanalytics.core.parameterization.validation.IParameterizationValidator
import org.pillarone.riskanalytics.core.parameterization.validation.ParameterValidationError
import org.pillarone.riskanalytics.core.parameterization.validation.ValidatorRegistry
import org.pillarone.riskanalytics.core.simulation.ILimitedPeriodCounter
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter
import org.pillarone.riskanalytics.core.util.PropertiesUtils
import org.springframework.transaction.TransactionStatus
import org.pillarone.riskanalytics.core.workflow.Status
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowCommentDAO

class Parameterization extends ModellingItem {

    public static final String PERIOD_DATE_FORMAT = "yyyy-MM-dd"

    String comment
    VersionNumber versionNumber
    private ParameterizationDAO parameterizationDAO
    /**
     * This is the number of different periods available in this parameterization.
     * This is not necessarily the same as the period count in Simulation.
     */
    Integer periodCount
    List<ParameterHolder> parameterHolders
    List<Comment> comments
    List periodLabels

    boolean orderByPath = false
    boolean valid

    List validationErrors

    Status status
    Long dealId

    public Parameterization(Map params) {
        this(params.remove("name").toString())
        params.each {k, v ->
            this[k] = v
        }
    }

    public Parameterization(String name) {
        super(name)
        setName(name)
        versionNumber = new VersionNumber('1')
        parameterHolders = []
        comments = []
        status = Status.NONE
    }

    protected Object createDao() {
        return new ParameterizationDAO()
    }

    public Object getDaoClass() {
        ParameterizationDAO
    }

    void validate() {
        valid = false
        List<ParameterValidationError> errors = []
        for (IParameterizationValidator validator in ValidatorRegistry.getValidators()) {
            errors.addAll(validator.validate(parameterHolders.findAll { !it.removed }))
        }
        parameterHolders*.clearCachedValues()

        valid = errors.empty
        validationErrors = errors
    }

    public save() {
        def result = null
        def daoToBeSaved = getDao()
        daoClass.withTransaction {TransactionStatus status ->
            if (daoToBeSaved.id != null) {
                daoToBeSaved = daoToBeSaved.merge()
            }
            validate()
            if (!validationErrors.empty) {
                LOG.warn("${daoToBeSaved} is not valid\n" + validationErrors.join("\n"))
            }

            setChangeUserInfo()
            mapToDao(daoToBeSaved)

            notifyItemSaved()

            if (!daoToBeSaved.save(flush: true)) logErrors(daoToBeSaved)

            changed = false
            dao = daoToBeSaved
            result = daoToBeSaved.id
            id = daoToBeSaved.id
        }

        return result
    }

    private List obtainPeriodLabelsFromParameters() {
        try {
            Model model = modelClass.newInstance()
            model.init()
            ParameterApplicator applicator = new ParameterApplicator(model: model, parameterization: this)
            applicator.init()
            applicator.applyParameterForPeriod(0)
            SimpleDateFormat dateFormat = new SimpleDateFormat(PERIOD_DATE_FORMAT)
            IPeriodCounter counter = model.createPeriodCounter(null)
            if (counter == null) {
                return null
            }
            List result = []
            if (counter instanceof ILimitedPeriodCounter) {
                for (int i = 0; i < counter.periodCount(); i++) {
                    result << dateFormat.format(counter.getCurrentPeriodStart().toDate())
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
        dao.creator = creator
        dao.lastUpdater = lastUpdater
        dao.status = status
        dao.dealId = dealId
        saveParameters(dao)
        saveComments(dao)
    }

    protected void saveParameters(ParameterizationDAO dao) {
        Iterator<ParameterHolder> iterator = parameterHolders.iterator()
        while (iterator.hasNext()) {
            ParameterHolder parameterHolder = iterator.next()
            if (parameterHolder.hasParameterChanged()) {
                Parameter parameter = dao.parameters.find { it.path == parameterHolder.path && it.periodIndex == parameterHolder.periodIndex }
                parameterHolder.applyToDomainObject(parameter)
                parameterHolder.modified = false
            } else if (parameterHolder.added) {
                Parameter newParameter = parameterHolder.createEmptyParameter()
                parameterHolder.applyToDomainObject(newParameter)
                dao.addToParameters(newParameter)
                parameterHolder.added = false
            } else if (parameterHolder.removed) {
                Parameter parameter = dao.parameters.find { it.path == parameterHolder.path && it.periodIndex == parameterHolder.periodIndex }
                dao.removeFromParameters(parameter)
                parameter.delete()
                iterator.remove()
            }
        }
    }

    protected void saveComments(ParameterizationDAO dao) {
        Iterator<Comment> iterator = comments.iterator()
        while (iterator.hasNext()) {
            Comment comment = iterator.next()
            if (comment.added) {
                commentAdded(dao, comment)
            } else if (comment.updated) {
                commentUpdated(dao, comment)
            } else if (comment.deleted) {
                if (commentDeleted(dao, comment)) {
                    iterator.remove()
                }
            }
        }
    }

    private void commentAdded(ParameterizationDAO dao, Comment comment) {
        CommentDAO commentDAO = new CommentDAO(parameterization: dao)
        comment.applyToDomainObject(commentDAO)
        dao.addToComments(commentDAO)
        comment.added = false
    }

    private void commentAdded(ParameterizationDAO dao, WorkflowComment comment) {
        WorkflowCommentDAO commentDAO = new WorkflowCommentDAO(parameterization: dao)
        comment.applyToDomainObject(commentDAO)
        dao.addToIssues(commentDAO)
        comment.added = false
    }

    private void commentUpdated(ParameterizationDAO dao, Comment comment) {
        CommentDAO commentDAO = dao.comments.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            comment.applyToDomainObject(commentDAO)
            comment.updated = false
        }
    }

    private void commentUpdated(ParameterizationDAO dao, WorkflowComment comment) {
        WorkflowCommentDAO commentDAO = dao.issues.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            comment.applyToDomainObject(commentDAO)
            comment.updated = false
        }
    }

    private boolean commentDeleted(ParameterizationDAO dao, Comment comment) {
        CommentDAO commentDAO = dao.comments.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            dao.removeFromComments(commentDAO)
            commentDAO.delete()
            return true
        }
        return false
    }

    private boolean commentDeleted(ParameterizationDAO dao, WorkflowComment comment) {
        WorkflowCommentDAO commentDAO = dao.issues.find { it.path == comment.path && it.periodIndex == comment.period }
        if (commentDAO) {
            dao.removeFromIssues(commentDAO)
            commentDAO.delete()
            return true
        }
        return false
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
        modelClass = getClass().getClassLoader().loadClass(dao.modelClassName)
        creator = dao.creator
        lastUpdater = dao.lastUpdater
        status = dao.status
        dealId = dao.dealId
        if (completeLoad) {
            loadParameters(dao)
            loadComments(dao)
        }
        LOG.info("Parameterization $name loaded in ${System.currentTimeMillis() - time}ms")
    }

    private void loadParameters(ParameterizationDAO dao) {
        parameterHolders = []

        for (Parameter p in dao.parameters) {
            parameterHolders << ParameterHolderFactory.getHolder(p)
        }
    }

    private void loadComments(ParameterizationDAO dao) {
        comments = []

        for (CommentDAO c in dao.comments) {
            comments << new Comment(c)
        }

        for (WorkflowCommentDAO c in dao.issues) {
            comments << new WorkflowComment(c)
        }
    }

    protected loadFromDB() {
        return ParameterizationDAO.find(name, getModelClass()?.name, versionNumber.toString())
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
        if (!isLoaded()) {
            load()
        }
        List<SimulationRun> result = null
        try {
            result = SimulationRun.findAllByParameterizationAndToBeDeleted(dao, false)
        } catch (Exception e) {
            LOG.error("Exception in method isUsedInSimulation : $e.message", e)
        }
        return result.size() > 0 && result.any { it.endTime != null }
    }

    public boolean isEditable() {
        if (status != Status.NONE && status != Status.DATA_ENTRY) {
            return false
        }
        return !isUsedInSimulation()
    }

    public List<String> getAllEditablePaths() {
        List result = []
        for (Comment comment in comments) {
            if (comment instanceof WorkflowComment) {
                result << comment.path
            }
        }
        return result
    }

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


    void addParameter(ParameterHolder parameter) {
        parameterHolders << parameter
        parameter.added = true
    }

    void removeParameter(ParameterHolder parameter) {
        if (parameter.added) {
            parameterHolders.remove(parameter)
            return
        }
        parameter.removed = true
        parameter.modified = false
    }

    void addComment(Comment comment) {
        comments << comment
        comment.added = true
    }

    void removeComment(Comment comment) {
        if (comment.added) {
            comments.remove(comment)
            comment.deleted = true
            return
        }
        comment.deleted = true
        comment.updated = false
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

        return original
    }

    String getPeriodLabel(int index) {
        if (periodLabels != null && !periodLabels.empty) {
            return periodLabels[index]
        }
        return "P$index".toString()
    }

    IConfigObjectWriter getWriter() {
        return new ParameterWriter()
    }

}
