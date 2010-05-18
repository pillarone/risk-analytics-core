package org.pillarone.riskanalytics.core.simulation.item

import org.apache.commons.lang.builder.HashCodeBuilder
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.output.SimulationRun
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameterization.ParameterWriter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.util.IConfigObjectWriter
import org.pillarone.riskanalytics.core.util.PropertiesUtils
import org.springframework.transaction.TransactionStatus
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameterization.ParameterApplicator
import org.pillarone.riskanalytics.core.simulation.IPeriodCounter
import java.text.SimpleDateFormat
import org.pillarone.riskanalytics.core.simulation.ILimitedPeriodCounter
import org.pillarone.riskanalytics.core.parameterization.validation.IParameterizationValidator
import org.pillarone.riskanalytics.core.parameterization.validation.ValidatorRegistry
import org.pillarone.riskanalytics.core.parameterization.ParameterValidationError

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
    List periodLabels

    Date creationDate
    Date modificationDate
    boolean orderByPath = false
    boolean valid

    def validationErrors


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
    }

    protected Object createDao() {
        return new ParameterizationDAO()
    }

    public Object getDaoClass() {
        ParameterizationDAO
    }

    List validate() {
        List<ParameterValidationError> errors = []
        for (IParameterizationValidator validator in ValidatorRegistry.getValidators()) {
            errors.addAll(validator.validate(parameterHolders))
        }

        return errors
    }

    public save() {
        def result = null
        def daoToBeSaved = getDao()
        daoClass.withTransaction {TransactionStatus status ->
            if (daoToBeSaved.id != null) {
                daoToBeSaved = daoToBeSaved.merge()
            }
            //Always set valid to true until validation actually works, because the ui already checks this flag
            valid = true
            mapToDao(daoToBeSaved)

            setChangeUserInfo(daoToBeSaved)
            notifyItemSaved()
            
            if (!daoToBeSaved.save(flush: true)) logErrors(daoToBeSaved)

            changed = false
            dao = daoToBeSaved
            result = daoToBeSaved.id
            id = daoToBeSaved.id

            validationErrors = validate()
            if (validationErrors) {
                LOG.warn("${daoToBeSaved} is not valid\n" + validationErrors.join("\n"))
            } else {
                daoToBeSaved.valid = true
                daoToBeSaved.save(flush: true)
            }
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
        dao.itemVersion = versionNumber.toString()
        dao.name = name
        dao.periodCount = periodCount
        dao.periodLabels = periodLabels != null && !periodLabels.empty ? periodLabels.join(";") : obtainPeriodLabelsFromParameters()?.join(";")
        dao.creationDate = creationDate
        dao.modificationDate = modificationDate
        dao.valid = valid
        dao.modelClassName = modelClass.name
        saveParameters(dao)
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

    protected void mapFromDao(Object dao, boolean completeLoad) {
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

        if (completeLoad) {
            loadParameters(dao)
        }
        LOG.info("Parameterization $name loaded in ${System.currentTimeMillis() - time}ms")
    }

    private void loadParameters(ParameterizationDAO dao) {
        parameterHolders = []

        for (Parameter p in dao.parameters) {
            parameterHolders << ParameterHolderFactory.getHolder(p)
        }
    }

    protected loadFromDB() {
        return ParameterizationDAO.find(name, getModelClass()?.name, versionNumber.toString())
    }


    public getDao() {
        if (parameterizationDAO == null) {
            parameterizationDAO = createDao()
        }
        return parameterizationDAO
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
        if (!obj instanceof Parameterization) {
            return false
        } else {
            return obj.name.equals(name) && obj.modelClass.equals(modelClass) && obj.versionNumber.equals(versionNumber)
        }
    }

    public boolean isUsedInSimulation() {
        if (!isLoaded()) {
            load()
        }
        def result
        try {
            result = SimulationRun.findByParameterizationAndToBeDeleted(dao, false)
        } catch (Exception e) {
            LOG.error("Exception in method isUsedInSimulation : $e.message", e)
        }
        result != null
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

    List getParameters(String path) {
        def params = parameters.findAll {ParameterHolder parameter ->
            parameter.path == path
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
