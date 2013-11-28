package org.pillarone.riskanalytics.core.simulation.item.parameter

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.pillarone.riskanalytics.core.parameter.EnumParameter
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.ParameterEntry
import org.pillarone.riskanalytics.core.parameter.ParameterObjectParameter
import org.pillarone.riskanalytics.core.parameterization.IParameterObject
import org.pillarone.riskanalytics.core.parameterization.IParameterObjectClassifier
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.simulation.InvalidParameterException

class ParameterObjectParameterHolder extends ParameterHolder implements IMarkerValueAccessor {

    private static Log LOG = LogFactory.getLog(ParameterObjectParameterHolder)

    //Sufficient for the UI (faster to instantiate)
    IParameterObjectClassifier classifier
    Map<String, ParameterHolder> classifierParameters

    public ParameterObjectParameterHolder(Parameter parameter) {
        super(parameter);
    }

    @TypeChecked
    public ParameterObjectParameterHolder(String path, int periodIndex, IParameterObject value) {
        super(path, periodIndex);
        if (value == null) {
            throw new IllegalArgumentException("Parameter object is null at $path P$periodIndex")
        }

        classifierParameters = new HashMap<String, ParameterHolder>()
        this.classifier = value.type

        for (Map.Entry entry in value.parameters.entrySet()) {
            classifierParameters.put(entry.key.toString(), ParameterHolderFactory.getHolder(path + ":$entry.key", periodIndex, entry.value))
        }
        check()
    }

    @Override
    void setParameter(Parameter parameter) {
        HashMap<String, ParameterHolder> existingParameters = new HashMap<String, ParameterHolder>(classifierParameters ?: [:])
        classifierParameters = new HashMap<String, ParameterHolder>()
        this.classifier = Thread.currentThread().contextClassLoader.loadClass(parameter.type.parameterType).valueOf(parameter.type.parameterValue)
        for (ParameterEntry entry in parameter.parameterEntries) {
            ParameterHolder holder = existingParameters.get(entry.parameterEntryKey)
            if (holder == null) {
                holder = ParameterHolderFactory.getHolder(entry.parameterEntryValue)
            } else {
                holder.setParameter(entry.parameterEntryValue)
            }
            classifierParameters.put(entry.parameterEntryKey, holder)
        }
        existingParameters.clear()
        check()
    }

    @CompileStatic
    IParameterObject getBusinessObject() {
        if (classifier == null) {
            throw new IllegalStateException("Classifier null in $path P$periodIndex")
        }
        return classifier.getParameterObject(getParameterMap())
    }

    @CompileStatic
    Map getParameterMap() {
        Map result = [:]
        for (Map.Entry<String, ParameterHolder> entry in classifierParameters.entrySet()) {
            result.put(entry.key, entry.value.getBusinessObject())
        }
        return result
    }

    void applyToDomainObject(Parameter parameter) {
        parameter.type.parameterValue = classifier.toString()
        List<ParameterEntry> usableEntries = parameter.parameterEntries != null ? parameter.parameterEntries.toList() : []
        for (Map.Entry<String, ParameterHolder> entry in classifierParameters) {
            ParameterEntry currentEntry = null
            if (!usableEntries.empty) {
                currentEntry = usableEntries.remove(0 as int)
                currentEntry.parameterEntryValue.delete()
            } else {
                currentEntry = new ParameterEntry()
                parameter.addToParameterEntries(currentEntry)
            }
            currentEntry.parameterEntryKey = entry.key
            currentEntry.parameterEntryValue = entry.value.createEmptyParameter()
            entry.value.applyToDomainObject(currentEntry.parameterEntryValue)
        }
        for (ParameterEntry unusedEntry in usableEntries) {
            parameter.removeFromParameterEntries(unusedEntry)
            unusedEntry.delete()
        }
    }


    @CompileStatic
    Parameter createEmptyParameter() {
        ParameterObjectParameter parameter = new ParameterObjectParameter(path: path, periodIndex: periodIndex)
        parameter.type = new EnumParameter(path: path + ":type", periodIndex: periodIndex, parameterType: classifier.getClass().name)
        return parameter
    }

    protected void updateValue(Object newValue) {
        classifier = classifier.valueOf(newValue)
        Map<String, ParameterHolder> newClassifierParameters = new HashMap<String, ParameterHolder>()
        for (Map.Entry entry in classifier.parameters) {
            ParameterHolder holder = null
            ParameterHolder oldEntry = classifierParameters.get(entry.key)
            if (oldParametersReusable(oldEntry, entry.value.class.name)) {
                holder = classifierParameters.get(entry.key)
            } else {
                holder = ParameterHolderFactory.getHolder(path + ":$entry.key", periodIndex, entry.value)
            }
            newClassifierParameters.put(entry.key, holder)
        }
        classifierParameters = newClassifierParameters
    }

    @CompileStatic
    private boolean oldParametersReusable(ParameterHolder oldEntry, String newClassName) {
        if (oldEntry == null) return false
        try {
            String oldClassName = oldEntry.businessObject.class.name
            return oldClassName == newClassName
        }
        catch (InvalidParameterException ex) {
            // the exception is thrown if oldEntry.businessObject fails due to invalid parameters
            return false;
        }
    }

    @CompileStatic
    boolean hasParameterChanged() {
        if (added || removed) return false

        boolean result = modified
        if (!result) {
            result = classifierParameters.values().any { ParameterHolder it -> it.hasParameterChanged() }
        }
        return result
    }

    @CompileStatic
    public ParameterObjectParameterHolder clone() {
        ParameterObjectParameterHolder holder = (ParameterObjectParameterHolder) super.clone();
        holder.classifier = classifier
        holder.classifierParameters = new HashMap<String, ParameterHolder>()
        for (Map.Entry<String, ParameterHolder> entry in classifierParameters.entrySet()) {
            holder.classifierParameters.put(entry.key, (ParameterHolder) entry.value.clone())
        }
        return holder
    }

    @CompileStatic
    public void clearCachedValues() {
        for (ParameterHolder p in classifierParameters.values()) {
            p.clearCachedValues()
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        String classifierClass = classifier.getClass().getName()
        String classifierType = classifier.displayName

        out.writeObject classifierClass
        out.writeObject classifierType
        out.writeObject classifierParameters
    }

    private void readObject(ObjectInputStream inStream) throws IOException, ClassNotFoundException {
        String classifierClass = inStream.readObject()
        String classifierType = inStream.readObject()

        Class clazz = Thread.currentThread().contextClassLoader.loadClass(classifierClass)
        this.classifier = clazz.valueOf(classifierType)

        classifierParameters = inStream.readObject()
    }

    @CompileStatic
    List<String> referencePaths(Class markerInterface, String refValue) {
        if (classifierParameters.size() > 0) {
            List<String> references = new ArrayList<String>()
            for (ParameterHolder parameterHolder : classifierParameters.values()) {
                if (parameterHolder instanceof MultiDimensionalParameterHolder) {
                    references.addAll((parameterHolder as IMarkerValueAccessor).referencePaths(markerInterface, refValue))
                } else if (parameterHolder instanceof ParameterObjectParameterHolder) {
                    references.addAll((parameterHolder as IMarkerValueAccessor).referencePaths(markerInterface, refValue))
                }
            }
            return references
        }
        return Collections.emptyList()
    }

    @CompileStatic
    List<String> updateReferenceValues(Class markerInterface, String oldValue, String newValue) {
        if (classifierParameters.size() > 0) {
            List<String> referencePaths = new ArrayList<String>()
            for (ParameterHolder parameterHolder : classifierParameters.values()) {
                if (parameterHolder instanceof MultiDimensionalParameterHolder) {
                    referencePaths.addAll( (parameterHolder as IMarkerValueAccessor).updateReferenceValues(markerInterface, oldValue, newValue))
                }
                if (parameterHolder instanceof ParameterObjectParameterHolder) {
                    referencePaths.addAll ((parameterHolder as IMarkerValueAccessor).updateReferenceValues(markerInterface, oldValue, newValue))
                }
            }
            return referencePaths
        }
        return Collections.emptyList()
    }

    @CompileStatic
    private void check() {
        if (classifier == null) { //TODO: would like to throw an exception here, but then migration fails
            LOG.error("Classifier null in path $path period $periodIndex")
        }
    }
}
