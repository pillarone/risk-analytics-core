package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.ResourceDAO

class VersionNumber implements Comparable, Cloneable, Serializable {

    private static final long serialVersionUID = -1640758346987125721L;

    List<Integer> versionNumbers
    boolean workflow = false

    @CompileStatic
    public VersionNumber(String version) {
        parse(version)
    }

    @CompileStatic
    private void parse(String versionString) {
        List<Integer> list = new LinkedList<Integer>()
        int from = 0
        if (versionString.startsWith("R")) {
            workflow = true
            from++
        }
        int index
        while ((index = versionString.indexOf('.', from)) != -1) {
            list.add(Integer.parseInt(versionString.substring(from, index)))
            from = index + 1
        }
        list.add(Integer.parseInt(versionString.substring(from)))
        versionNumbers = list
    }

    static VersionNumber incrementVersion(ModellingItem item) {
        VersionNumber newVersion = item.versionNumber.clone()
        Collection existingVersions = getExistingVersions(item)

        VersionNumber temp = newVersion.clone()
        temp.incrementLastSubversion()
        if (!existingVersions.contains(temp)) {
            newVersion = temp
        } else {
            newVersion.addSubversion()

            while (existingVersions.contains(newVersion)) {
                newVersion.incrementLastSubversion()
            }
        }
        return newVersion
    }

    static List<VersionNumber> getExistingVersions(ModellingItem item) {
        Collection existingVersions = item.daoClass.findAllByNameAndModelClassName(item.name, item.modelClass.name).collect {
            new VersionNumber(it.itemVersion)
        }
        return existingVersions
    }

    static List<VersionNumber> getExistingVersions(Resource item) {
        Collection existingVersions = ResourceDAO.findAllByNameAndResourceClassName(item.name, item.modelClass.name).collect {
            new VersionNumber(it.itemVersion)
        }
        return existingVersions
    }

    static List<VersionNumber> getExistingVersions(ModelItem item) {
        Collection existingVersions = item.daoClass.findAllByName(item.name).collect {
            new VersionNumber(it.itemVersion)
        }
        return existingVersions
    }

    static VersionNumber getHighestNonWorkflowVersion(ModellingItem item) {
        List<VersionNumber> allVersions = getExistingVersions(item).findAll { !it.workflow }
        if(allVersions.empty) {
            return null
        }
        return allVersions.sort()[-1]
    }

    @CompileStatic
    int getLevel() {
        versionNumbers.size()
    }

    @CompileStatic
    boolean isDirectChildVersionOf(VersionNumber versionNumber) {
        if (versionNumbers.size() <= versionNumber.versionNumbers.size()) {
            return false
        } else {
            if (level == versionNumber.level + 1) {
                for (int i = 0; i < level - 1; i++) {
                    if (!(versionNumbers[i] == versionNumber.versionNumbers[i])) {
                        return false
                    }
                }
                return true
            } else {
                return false
            }
        }
    }

    @CompileStatic
    String toString() {
        StringBuffer buffer = new StringBuffer(workflow ? "R" : "")
        for (Integer version in versionNumbers) {
            buffer << version << '.'
        }
        buffer.delete(buffer.size() - 1, buffer.size())
        buffer.toString()
    }

    @CompileStatic
    int hashCode() {
        return toString().hashCode()
    }

    @CompileStatic
    boolean equals(Object obj) {
        if (obj instanceof VersionNumber) {
            return toString().equals(obj.toString())
        } else {
            return false
        }
    }

    @CompileStatic
    int compareTo(Object o) {
        VersionNumber vn = o as VersionNumber
        if (workflow) {
            if (!vn.workflow) {
                return 1
            }
        } else {
            if (vn.workflow) {
                return -1
            }
        }
        int size = versionNumbers.size() > vn.versionNumbers.size() ? versionNumbers.size() : vn.versionNumbers.size()
        for (int i = 0; i < size; i++) {
            if (versionNumbers[i] != vn.versionNumbers[i]) {
                return versionNumbers[i] > vn.versionNumbers[i] ? 1 : -1
            }
        }
        return versionNumbers.size() == vn.versionNumbers.size() ? 0 : versionNumbers.size() > vn.versionNumbers.size() ? 1 : -1
    }

    @CompileStatic
    Object clone() {
        VersionNumber newObject = (VersionNumber) super.clone();
        newObject.versionNumbers = new LinkedList()
        versionNumbers.each {Integer it ->
            newObject.versionNumbers << new Integer(it.intValue())
        }
        newObject
    }

    @CompileStatic
    private void addSubversion() {
        versionNumbers.add(1)
    }

    private void incrementLastSubversion() {
        versionNumbers[versionNumbers.size() - 1]++
    }


}