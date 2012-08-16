package org.pillarone.riskanalytics.core.simulation.item

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.util.GroovyUtils

abstract class ParametrizedItem extends CommentableItem {

    private static Log LOG = LogFactory.getLog(ParametrizedItem)

    private Set<IParametrizedItemListener> listeners = new HashSet<IParametrizedItemListener>()

    ParametrizedItem(String name) {
        super(name)
    }

    void addComponent(String basePath, Component component) {
        for (Map.Entry<String, Object> entry : GroovyUtils.getProperties(component).entrySet()) {
            String fieldName = entry.key
            def value = entry.value
            if (fieldName.startsWith("parm")) {
                for (int i = 0; i < getPeriodCount(); i++) {
                    if (value instanceof Cloneable) {
                        value = value.clone()
                    }
                    addParameter(ParameterHolderFactory.getHolder([basePath, fieldName].join(":"), i, value))
                }
            }
        }
        fireComponentAdded(basePath, component)
    }

    void addListener(IParametrizedItemListener listener) {
        listeners.add(listener)
    }

    void removeListener(IParametrizedItemListener listener) {
        listeners.remove(listener)
    }

    protected fireComponentAdded(String path, Component component) {
        listeners*.componentAdded(path, component)
    }

    abstract Integer getPeriodCount()

    protected void loadParameters(List<ParameterHolder> parameterHolders, Collection<Parameter> parameters) {
        List<ParameterHolder> existingHolders = parameterHolders.clone()
        parameterHolders.clear()

        for (Parameter p in parameters) {
            final ParameterHolder existingParameterHolder = existingHolders.find { it.path == p.path && it.periodIndex == p.periodIndex}
            if (existingParameterHolder != null) {
                existingParameterHolder.setParameter(p)
                parameterHolders << existingParameterHolder
            } else {
                parameterHolders << ParameterHolderFactory.getHolder(p)
            }
        }
    }

    protected void saveParameters(List<ParameterHolder> parameterHolders, Collection<Parameter> parameters, def dao) {
        Iterator<ParameterHolder> iterator = parameterHolders.iterator()
        while (iterator.hasNext()) {
            ParameterHolder parameterHolder = iterator.next()
            if (parameterHolder.hasParameterChanged()) {
                LOG.debug("Parameter ${parameterHolder.path} P${parameterHolder.periodIndex} is marked as changed and will be updated.")
                Parameter parameter = parameters.find { it.path == parameterHolder.path && it.periodIndex == parameterHolder.periodIndex }
                parameterHolder.applyToDomainObject(parameter)
                clearModifiedFlag(parameterHolder)
            } else if (parameterHolder.added) {
                LOG.debug("Parameter ${parameterHolder.path} P${parameterHolder.periodIndex} is marked as added and will be added.")
                Parameter newParameter = parameterHolder.createEmptyParameter()
                parameterHolder.applyToDomainObject(newParameter)
                addToDao(newParameter, dao)
                parameterHolder.added = false
            } else if (parameterHolder.removed) {
                LOG.debug("Parameter ${parameterHolder.path} P${parameterHolder.periodIndex} is marked as deleted and will be removed.")
                Parameter parameter = parameters.find { it.path == parameterHolder.path && it.periodIndex == parameterHolder.periodIndex }
                removeFromDao(parameter, dao)
                parameter.delete()
                iterator.remove()
            }
        }
    }

    private void clearModifiedFlag(ParameterHolder holder) {
        holder.modified = false
    }

    private void clearModifiedFlag(ParameterObjectParameterHolder holder) {
        holder.modified = false
        for (ParameterHolder parameterHolder in holder.classifierParameters.values()) {
            clearModifiedFlag(parameterHolder)
        }
    }

    ParameterHolder getParameterHolder(String path, int periodIndex) {
        int parmIndex = path.indexOf(":parm")
        int nestedIndex = path.indexOf(":", parmIndex + 1)
        boolean isNested = nestedIndex > -1
        if (!isNested) {
            ParameterHolder parameterHolder = getAllParameterHolders().find { it.path == path && it.periodIndex == periodIndex}
            if (parameterHolder == null) {
                throw new IllegalArgumentException("Parameter $path does not exist for period $periodIndex")
            }
            return parameterHolder
        } else {
            String subPath = path.substring(0, nestedIndex)
            ParameterHolder parameterHolder = getAllParameterHolders().find { it.path == subPath && it.periodIndex == periodIndex }
            if (parameterHolder == null) {
                throw new IllegalArgumentException("Parameter $path does not exist for period $periodIndex (base path $subPath not found)")
            }
            return getNestedParameterHolder(parameterHolder, path.substring(nestedIndex + 1).split(":"), periodIndex)
        }
    }

    List<ParameterHolder> getParameterHoldersForAllPeriods(String path) {
        int parmIndex = path.indexOf(":parm")
        int nestedIndex = path.indexOf(":", parmIndex + 1)
        boolean isNested = nestedIndex > -1
        if (!isNested) {
            List<ParameterHolder> parameterHolder = getAllParameterHolders().findAll { it.path == path }
            if (parameterHolder.empty) {
                throw new IllegalArgumentException("Parameter $path does not exist")
            }
            return parameterHolder
        } else {
            String subPath = path.substring(0, nestedIndex)
            List<ParameterHolder> parameterHolder = getAllParameterHolders().findAll { it.path == subPath }
            if (parameterHolder == null) {
                throw new IllegalArgumentException("Parameter $path does not exist for period $periodIndex (base path $subPath not found)")
            }

            List<ParameterHolder> result = []
            String[] pathElements = path.substring(nestedIndex + 1).split(":")
            for (ParameterHolder holder in parameterHolder) {
                if (hasParameterAtPath(path, holder.periodIndex)) {
                    result << getNestedParameterHolder(holder, pathElements, holder.periodIndex)
                }
            }
            return result
        }
    }

    boolean hasParameterAtPath(String path) { //TODO improve?
        try {
            getParameterHoldersForAllPeriods(path)
            return true
        } catch (IllegalArgumentException iae) {
            return false
        }
    }

    boolean hasParameterAtPath(String path, int periodIndex) { //TODO improve?
        try {
            getParameterHolder(path, periodIndex)
            return true
        } catch (IllegalArgumentException iae) {
            return false
        }
    }

    protected ParameterHolder getNestedParameterHolder(ParameterObjectParameterHolder baseParameter, String[] pathElements, int periodIndex) {
        ParameterHolder current = baseParameter
        for (String path in pathElements) {
            if (current instanceof ParameterObjectParameterHolder) {
                current = current.classifierParameters.find { it.key == path}?.value
                if (current == null) {
                    throw new IllegalArgumentException("Element $path does not exist for period $periodIndex")
                }
            } else {
                throw new IllegalArgumentException("Element at $path expected to be a parameter object, but was ${current?.class}")
            }
        }

        return current
    }

    abstract protected void addToDao(Parameter parameter, def dao)

    abstract protected void removeFromDao(Parameter parameter, def dao)

    abstract List<ParameterHolder> getAllParameterHolders()

    void addParameter(ParameterHolder parameter) {
        getAllParameterHolders().add(parameter)
        parameter.added = true
    }

    void removeParameter(ParameterHolder parameter) {
        if (parameter.added) {
            getAllParameterHolders().remove(parameter)
            return
        }
        parameter.removed = true
        parameter.modified = false
    }
}
