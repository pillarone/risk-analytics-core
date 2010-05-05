package org.pillarone.riskanalytics.core.simulation.item

import org.apache.log4j.Logger
import org.springframework.transaction.TransactionStatus

abstract class ModellingItem {
    final static Logger LOG = Logger.getLogger(ModellingItem)

    String name
    Class modelClass

//    protected def dao
    boolean changed = false

    private List itemChangedListener

    public def id

    public ModellingItem(String name) {
        this.name = name;
        itemChangedListener = []
    }

    abstract protected def createDao() {
    }

    abstract public def getDaoClass() {
    }

    abstract protected void mapToDao(def dao) {
    }

    abstract protected void mapFromDao(def dao, boolean completeLoad) {
    }

    protected void saveDependentData(def dao) {}

    protected void deleteDependentData(def dao) {}

    public void setChanged(boolean newChangedValue) {
        this.changed = newChangedValue
        notifyItemChanged()
    }

    public boolean isLoaded() {
        return this.id != null
    }

    public void unload() {
        this.dao = null
        this.id = null
        changed = false
    }

    public void load(boolean completeLoad = true) {
        daoClass.withTransaction {TransactionStatus status ->
            def loadedDao = loadFromDB()
            if (loadedDao) {
                mapFromDao(loadedDao, completeLoad)
            }
            dao = loadedDao
        }
    }


    abstract protected def loadFromDB()

    public void rename(String newName) {
        load()
        String oldName = name
        name = newName
        if (!save()) {
            name = oldName
        }
    }

    public def save() {
        def result = null
        daoClass.withTransaction {status ->
            def daoToBeSaved = dao

            if (daoToBeSaved == null) {
                daoToBeSaved = createDao()
            }

            mapToDao(daoToBeSaved)

            setChangeUserInfo(daoToBeSaved)

            saveDependentData(daoToBeSaved)
            logErrors(daoToBeSaved)

            if (!daoToBeSaved.save(flush: true)) {
                logErrors(daoToBeSaved)
            }
            changed = false
            // TODO (msh): error handling
            dao = daoToBeSaved
            result = daoToBeSaved.id
        }
        return result
    }

    protected def setChangeUserInfo(daoToBeSaved) {
        //check if the object doesn't exist in DB
        if (daoToBeSaved.properties.keySet().contains("creationDate") && daoToBeSaved.creationDate == null) {
            daoToBeSaved.creationDate = new Date()
        }

        if (daoToBeSaved.properties.keySet().contains("modificationDate")) {
            daoToBeSaved.modificationDate = new Date()
        }
    }

    public void updateChangeUserAndDate() {
        if (this.properties.keySet().contains("modificationDate")) {
            this.modificationDate = new Date()
        }
    }

    protected void logErrors(def dao) {
        if (dao.hasErrors()) {
            dao.errors.each {
                LOG.error(it)
            }
        }
    }

    final boolean delete() {
        boolean result = false
        if (!loaded) { // make sure we have an id
            LOG.warn "attempt to delete an unloaded dao: $dao"
            load()     // todo: can't we simply return?
        }
        daoClass.withTransaction {TransactionStatus status ->
            try {
                def dao = getDao()
                if (dao != null && deleteDaoImpl(dao)) {
                    result = true
                } else {
                    logErrors(dao)
                }
            } catch (Throwable ex) {
                LOG.error "Delete Exception: ${ex}", ex
                return false
            }

        }
        return result
    }

    protected Object deleteDaoImpl(dao) {
        dao.delete(flush: true)
    }


    public void addModellingItemChangeListener(IModellingItemChangeListener listener) {
        itemChangedListener << listener
    }

    public void removeModellingItemChangeListener(IModellingItemChangeListener listener) {
        itemChangedListener.remove(listener)
    }

    public void removeAllModellingItemChangeListener() {
        itemChangedListener.clear()
    }

    private void notifyItemChanged() {
        itemChangedListener.each {
            it.itemChanged(this)
        }
    }


    protected def saveDao(def dao) {
        if (dao.hasErrors()) {
            logErrors(dao)
        }

        def result = dao.save()

        if (!result) {
            logErrors(dao)
        }

        return result
    }

    public getDao() {
        if (this.id) {
            return daoClass.get(this.id)
        }
        return null
    }

    public void setDao(def newDao) {
        if (newDao) {
            this.id = newDao.id
        }
    }

}

interface IModellingItemChangeListener {

    public void itemChanged(ModellingItem item)
}
