package org.pillarone.riskanalytics.core.simulation.item

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.components.Component
import org.pillarone.riskanalytics.core.components.ComponentUtils
import org.pillarone.riskanalytics.core.model.Model
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolderFactory
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterObjectParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.util.GroovyUtils

abstract class ParametrizedItem extends CommentableItem {

    protected static Log LOG = LogFactory.getLog(ParametrizedItem)

    private Set<IParametrizedItemListener> listeners = new HashSet<IParametrizedItemListener>()

    ParametrizedItem(String name) {
        super(name)
    }

    void addComponent(String basePath, Component component) {
        internalAddComponent(basePath, component)
        fireComponentAdded(basePath, component)
    }

    private void internalAddComponent(String basePath, Component component) {
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
            } else if (fieldName.startsWith("sub")) {
                internalAddComponent([basePath, fieldName].join(":"), value as Component)
            }
        }
    }

    void copyComponent(String oldPath, String newPath, Component component, boolean copyComments) {
        ParameterHolderFactory.duplicateParameters(this, oldPath, newPath)
        if (copyComments) {
            for (Comment comment in comments.findAll { Comment it -> it.path.contains(oldPath) }) {
                Comment clone = comment.clone()
                clone.path = clone.path.replace(oldPath, newPath)
                addComment(clone)
            }
        }
        fireComponentAdded(newPath, component)
    }

    void renameComponent(String oldPath, String newPath, Component newComponent) {
        List<String> changedPaths = ParameterHolderFactory.renamePathOfParameter(this, oldPath, newPath, newComponent)
        for (Comment comment in comments.findAll { Comment it -> it.path.contains(oldPath) }) {
            Comment clone = comment.clone()
            clone.path = clone.path.replace(oldPath, newPath)
            addComment(clone)
            removeComment(comment)
        }
        fireComponentAdded(newPath, newComponent)
        fireComponentRemoved(oldPath)
        fireValuesChanged(changedPaths)
    }

    void removeComponent(String path) {
        Model model = (Model) modelClass.newInstance()
        String pathWithoutModel = ComponentUtils.removeModelFromPath(path, model)
        for (ParameterHolder holder in notDeletedParameterHolders.findAll { ParameterHolder it -> it.path.startsWith(pathWithoutModel) }) {
            removeParameter(holder)
        }
        for (Comment comment in comments.findAll { Comment it -> it.path.startsWith(path) && !it.deleted }) {
            removeComment(comment)
        }
        fireComponentRemoved(pathWithoutModel)
    }

    void addListener(IParametrizedItemListener listener) {
        listeners.add(listener)
    }

    @Override
    void removeAllModellingItemChangeListener() {
        itemChangedListener.clear()
        listeners.clear()
    }

    void removeListener(IParametrizedItemListener listener) {
        listeners.remove(listener)
    }

    protected fireComponentAdded(String path, Component component) {
        for (IParametrizedItemListener listener in listeners) {
            listener.componentAdded(path, component)
        }
    }

    protected fireValuesChanged(List<String> paths) {
        for (IParametrizedItemListener listener in listeners) {
            listener.parameterValuesChanged(paths)
        }
    }

    protected fireClassifierChanged(String path) {
        for (IParametrizedItemListener listener in listeners) {
            listener.classifierChanged(path)
        }
    }

    protected fireComponentRemoved(String path) {
        for (IParametrizedItemListener listener in listeners) {
            listener.componentRemoved(path)
        }
    }

    abstract Integer getPeriodCount()

    protected void loadParameters(List<ParameterHolder> parameterHolders, Collection<Parameter> parameters) {
        List<ParameterHolder> existingHolders = (List<ParameterHolder>) parameterHolders.clone()
        parameterHolders.clear()
        parameterHolders.addAll(getNewlyAddedHolders(existingHolders))

        for (Parameter p in parameters) {
            final ParameterHolder existingParameterHolder = existingHolders.find { ParameterHolder it -> it.path == p.path && it.periodIndex == p.periodIndex }
            if (existingParameterHolder != null) {
                existingParameterHolder.setParameter(p)
                parameterHolders << existingParameterHolder
            } else {
                parameterHolders << ParameterHolderFactory.getHolder(p)
            }
        }
    }

    private List<ParameterHolder> getNewlyAddedHolders(List<ParameterHolder> parameterHolders) {
        List<ParameterHolder> result = []
        for (ParameterHolder existingParameterHolder in parameterHolders) {
            if (existingParameterHolder.added) {
                result << existingParameterHolder
            }
        }
        result
    }

    protected void saveParameters(List<ParameterHolder> parameterHolders, Collection<Parameter> parameters, def dao) {
        sanityCheck(parameterHolders)
        Iterator<ParameterHolder> iterator = parameterHolders.iterator()
        while (iterator.hasNext()) {
            ParameterHolder parameterHolder = iterator.next()
            if (parameterHolder.hasParameterChanged()) {
                LOG.debug("Parameter ${parameterHolder.path} P${parameterHolder.periodIndex} is marked as changed and will be updated.")
                Parameter parameter = parameters.find { Parameter it -> it.path == parameterHolder.path && it.periodIndex == parameterHolder.periodIndex }
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
                Parameter parameter = parameters.find { Parameter it -> it.path == parameterHolder.path && it.periodIndex == parameterHolder.periodIndex }
                removeFromDao(parameter, dao)
                parameter.delete()
                iterator.remove()
            }
        }
    }

    private void sanityCheck(List<ParameterHolder> holders) {
        Map<String, List<ParameterHolder>> groupedByPath = holders.groupBy { "$it.path-$it.periodIndex" }
        groupedByPath.each { String path, List<ParameterHolder> paramsForPath ->
            if (paramsForPath.size() > 2) {
                throw new IllegalStateException("there are more than two parameterHolders for path-periodIndex: $path")
            }
            if (paramsForPath.size() == 2) {
                if (!paramsForPath[0].removed) {
                    throw new IllegalStateException("there are two parameterHolders for path $path. This is only allowed, when the first one is flagged as removed")
                }
                if (!paramsForPath[1].added) {
                    throw new IllegalStateException("there are two parameterHolders for path $path. This is only allowed, when the second one is flagged as added")
                }
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
        if (isNested) {
            String subPath = path.substring(0, nestedIndex)
            List<ParameterHolder> findAll = notDeletedParameterHolders.findAll { ParameterHolder it -> it.path == subPath && it.periodIndex == periodIndex }
            if (findAll.size() == 0) {
                throw new ParameterNotFoundException("Parameter $path does not exist for period $periodIndex (base path $subPath not found)")
            }
            if (findAll.size() > 1) {
                throw new IllegalStateException("There is more than one not removed parameter for path $path (base path $subPath)")
            }
            ParameterHolder parameterHolder = findAll.first()
            return getNestedParameterHolder(parameterHolder, path.substring(nestedIndex + 1).split(":"), periodIndex)
        } else {
            List<ParameterHolder> findAll = notDeletedParameterHolders.findAll { ParameterHolder it -> it.path == path && it.periodIndex == periodIndex }
            if (findAll.size() == 0) {
                throw new ParameterNotFoundException("Parameter $path does not exist")
            }
            if (findAll.size() > 1) {
                throw new IllegalStateException("There is more than one not removed parameter for path $path")
            }
            return findAll.first()
        }
    }

    List<ParameterHolder> getParameterHoldersForAllPeriods(String path) {
        int parmIndex = path.indexOf(":parm")
        int nestedIndex = path.indexOf(":", parmIndex + 1)
        boolean isNested = nestedIndex > -1
        if (isNested) {
            String subPath = path.substring(0, nestedIndex)
            List<ParameterHolder> parameterHolders = notDeletedParameterHolders.findAll { ParameterHolder it -> it.path == subPath }
            if (parameterHolders.empty == 0) {
                throw new ParameterNotFoundException("Parameter $path does not exist (base path $subPath not found)")
            }
            List<ParameterHolder> result = []
            String[] pathElements = path.substring(nestedIndex + 1).split(":")
            for (ParameterHolder holder in parameterHolders) {
                if (hasParameterAtPath(path, holder.periodIndex)) {
                    result << getNestedParameterHolder(holder, pathElements, holder.periodIndex)
                }
            }
            return result
        } else {
            Collection<ParameterHolder> parameterHolder = notDeletedParameterHolders.findAll { ParameterHolder it -> it.path == path }
            if (parameterHolder.empty) {
                throw new ParameterNotFoundException("Parameter $path does not exist")
            }
            return parameterHolder
        }
    }

    ParameterHolder getParameterHoldersForFirstPeriod(String path) {
        int nestedIndex = path.indexOf(":", path.indexOf(":parm") + 1)
        def isNested = nestedIndex > -1
        String basePath = isNested ? path.substring(0, nestedIndex) : path
        Integer minimalPeriodIndex = notDeletedParameterHolders.findAll { ParameterHolder it ->
            boolean fits = it.path == basePath
            //check if holder has correct classifiers
            if (isNested) {
                fits &= hasParameterAtPath(path, it.periodIndex)
            }
            fits

        }.periodIndex.min()
        if (minimalPeriodIndex == null) {
            throw new ParameterNotFoundException("No parameter found for path $path")
        }
        getParameterHolder(path, minimalPeriodIndex)
    }


    boolean hasParameterAtPath(String path) { //TODO improve?
        try {
            getParameterHoldersForAllPeriods(path)
            return true
        } catch (ParameterNotFoundException iae) {
            return false
        }
    }

    boolean hasParameterAtPath(String path, int periodIndex) { //TODO improve?
        try {
            getParameterHolder(path, periodIndex)
            return true
        } catch (ParameterNotFoundException iae) {
            return false
        }
    }

    protected ParameterHolder getNestedParameterHolder(ParameterHolder baseParameter, String[] pathElements, int periodIndex) {
        ParameterHolder current = baseParameter
        for (String path in pathElements) {
            if (current instanceof ParameterObjectParameterHolder) {
                current = current.classifierParameters.entrySet().find { Map.Entry<String, ParameterHolder> it -> it.key == path }?.value
                if (current == null) {
                    throw new ParameterNotFoundException("Element $path does not exist for period $periodIndex")
                }
            } else {
                throw new ParameterNotFoundException("Element at $path expected to be a parameter object, but was ${current?.class}")
            }
        }

        return current
    }

    abstract protected void addToDao(Parameter parameter, def dao)

    abstract protected void removeFromDao(Parameter parameter, def dao)

    abstract protected List<ParameterHolder> getAllParameterHolders()

    List<ParameterHolder> getNotDeletedParameterHolders() {
        allParameterHolders.findAll { !it.removed }
    }

    void addParameter(ParameterHolder parameter) {
        allParameterHolders.add(parameter)
        parameter.added = true
    }

    void removeParameter(ParameterHolder parameter) {
        if (parameter.added) {
            allParameterHolders.remove(parameter)
            return
        }
        parameter.removed = true
        parameter.modified = false
    }

    void updateParameterValue(String path, int periodIndex, def newValue) {
        updateParameterValue(getParameterHolder(path, periodIndex), newValue)
    }

    protected void updateParameterValue(ParameterHolder holder, def newValue) {
        holder.value = newValue
        fireValuesChanged([holder.path])
    }

    protected void updateParameterValue(ParameterObjectParameterHolder holder, def newValue) {
        holder.value = newValue
        for (Comment comment in comments.findAll { Comment it -> it.path.contains(holder.path) }) {
            removeComment(comment)
        }
        fireClassifierChanged(holder.path)
    }
}
