package org.pillarone.riskanalytics.core.simulation.item

class VersionNumber implements Comparable, Cloneable {

    List versionNumbers
    boolean workflow = false

    public VersionNumber(String version) {
        parse(version)
    }

    private void parse(String versionString) {
        List list = new LinkedList<Integer>()
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

    static List getExistingVersions(ModellingItem item) {
        Collection existingVersions = item.daoClass.findAllByNameAndModelClassName(item.name, item.modelClass.name).collect {
            new VersionNumber(it.itemVersion)
        }
        return existingVersions
    }

    static List getExistingVersions(ModelItem item) {
        Collection existingVersions = item.daoClass.findAllByName(item.name).collect {
            new VersionNumber(it.itemVersion)
        }
        return existingVersions
    }

    int getLevel() {
        versionNumbers.size()
    }

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


    String toString() {
        StringBuffer buffer = new StringBuffer(workflow ? "R" : "")
        for (Integer version in versionNumbers) {
            buffer << version << '.'
        }
        buffer.delete(buffer.size() - 1, buffer.size())
        buffer.toString()
    }

    int hashCode() {
        return toString().hashCode()
    }

    boolean equals(Object obj) {
        if (obj instanceof VersionNumber) {
            return toString().equals(obj.toString())
        } else {
            return false
        }
    }

    int compareTo(Object o) {
        o = o as VersionNumber
        if(workflow) {
            if(!o.workflow) {
                return 1
            }
        } else {
            if(o.workflow) {
                return -1
            }
        }
        int size = versionNumbers.size() > o.versionNumbers.size() ? versionNumbers.size() : o.versionNumbers.size()
        for (int i = 0; i < size; i++) {
            if (versionNumbers[i] != o.versionNumbers[i]) {
                return versionNumbers[i] > o.versionNumbers[i] ? 1 : -1
            }
        }
        return versionNumbers.size() == o.versionNumbers.size() ? 0 : versionNumbers.size() > o.versionNumbers.size() ? 1 : -1
    }

    Object clone() {
        VersionNumber newObject = super.clone();
        newObject.versionNumbers = new LinkedList()
        versionNumbers.each {Integer it ->
            newObject.versionNumbers << new Integer(it.intValue())
        }
        newObject
    }

    private void addSubversion() {
        versionNumbers.add(1)
    }

    private void incrementLastSubversion() {
        versionNumbers[versionNumbers.size() - 1]++
    }


}