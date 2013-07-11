package org.pillarone.riskanalytics.core.log

import groovy.transform.CompileStatic
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement

import java.util.concurrent.ConcurrentHashMap

@CompileStatic
class TraceLogManager {

    private Map<Long, List<String>> messages = new ConcurrentHashMap<Long, List<String>>()

    void log(String message) {
        List<String> list = getList()
        if (list != null) {
            list << message
        }
    }

    private List<String> getList() {
        Long id = getId()
        return messages.get(id)
    }

    void activateLogging() {
        messages.put(getId(), new ArrayList<String>())
    }

    void deactivateLogging() {
        messages.remove(getId())
    }

    private Long getId() {
        Person user = UserManagement.currentUser
        return user?.id ?: 0L
    }

    void clear() {
        getList()?.clear()
    }

    List<String> getTrace() {
        return getList()
    }
}
