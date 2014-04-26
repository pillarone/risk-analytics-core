package org.pillarone.riskanalytics.core.simulation.item

import groovy.transform.CompileStatic
import org.apache.log4j.Logger
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.user.Person
import org.pillarone.riskanalytics.core.user.UserManagement
import org.springframework.transaction.TransactionStatus

import static com.google.common.base.Preconditions.checkNotNull

abstract class ModellingItem implements Serializable {
    private static final Logger LOG = Logger.getLogger(ModellingItem)

    String name
    Class modelClass

    DateTime creationDate
    DateTime modificationDate

    Person creator
    Person lastUpdater

    VersionNumber versionNumber

    boolean changed = false

    protected List<IModellingItemChangeListener> itemChangedListener

    Long id
    protected boolean loaded

    ModellingItem(String name) {
        this(name, null)
    }

    ModellingItem(String name, Class modelClass) {
        this.name = checkNotNull(name)
        this.modelClass = modelClass
        itemChangedListener = []
    }

    abstract protected def createDao()

    abstract public def getDaoClass()

    abstract protected void mapToDao(def dao)

    abstract protected void mapFromDao(def dao, boolean completeLoad)

    protected void saveDependentData(def dao) {}

    protected void deleteDependentData(def dao) {}

    @CompileStatic
    void setChanged(boolean changed) {
        this.changed = changed
        notifyItemChanged()
    }

    @CompileStatic
    void unload() {
        this.dao = null
        this.id = null
        changed = false
        loaded = false
    }

    void load(boolean completeLoad = true) {
        daoClass.withTransaction { TransactionStatus status ->
            def loadedDao = loadFromDB()
            if (loadedDao) {
                mapFromDao(loadedDao, completeLoad)
            }
            dao = loadedDao
            this.@loaded = completeLoad
        }
    }

    @CompileStatic
    boolean isLoaded() {
        return loaded
    }

    @CompileStatic
    boolean isEditable() {
        return false
    }

    abstract protected def loadFromDB()

    @CompileStatic
    void rename(String newName) {
        if (!isLoaded()) {
            load()
        }
        String oldName = name
        name = newName
        if (!save()) {
            name = oldName
        }
    }

    Long save() {
        daoClass.withTransaction { status ->
            def daoToBeSaved = dao

            if (daoToBeSaved == null) {
                daoToBeSaved = createDao()
            }

            setChangeUserInfo()
            mapToDao(daoToBeSaved)

            notifyItemSaved()

            saveDependentData(daoToBeSaved)
            logErrors(daoToBeSaved)

            if (!daoToBeSaved.save(flush: true)) {
                logErrors(daoToBeSaved)
            }
            changed = false
            // TODO (msh): error handling
            dao = daoToBeSaved
            daoToBeSaved.id
        }
    }

    @CompileStatic
    protected void setChangeUserInfo() {
        DateTime date = new DateTime()
        Person currentUser = UserManagement.currentUser
        if (creationDate == null) {
            creationDate = date
            creator = currentUser
        }

        modificationDate = date
        lastUpdater = currentUser
    }

    void updateChangeUserAndDate() {
        modificationDate = new DateTime()
    }

    protected void logErrors(def dao) {
        if (dao?.hasErrors()) {
            dao.errors.each {
                LOG.error(it)
            }
        }
    }

    void logDeleteSuccess() {
        LOG.info("DELETED ${getClass().simpleName}: ${name})")
    }

    final boolean delete() {
        boolean result = false

        if (!loaded) {
            load()
        }
        daoClass.withTransaction { TransactionStatus status ->
            try {
                def dao = dao
                if (dao != null && deleteDaoImpl(dao)) {
                    result = true
                    logDeleteSuccess()
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
        return true
    }


    @CompileStatic
    void addModellingItemChangeListener(IModellingItemChangeListener listener) {
        itemChangedListener << listener
    }

    @CompileStatic
    void removeModellingItemChangeListener(IModellingItemChangeListener listener) {
        itemChangedListener.remove(listener)
    }

    @CompileStatic
    void removeAllModellingItemChangeListener() {
        itemChangedListener.clear()
    }

    //fja: changed to public, it will be by edit a comment belonging to used item

    void notifyItemChanged() {
        itemChangedListener.each {
            it.itemChanged(this)
        }
    }

    void notifyItemSaved() {
        itemChangedListener.each {
            it.itemSaved(this)
        }
    }

    def saveDao(def dao) {
        if (dao.hasErrors()) {
            logErrors(dao)
        }

        def result = dao.save()

        if (!result) {
            logErrors(dao)
        }

        return result
    }

    protected def getDao() {
        if (id) {
            return daoClass.get(id)
        }
        return null
    }

    protected void setDao(def newDao) {
        if (newDao) {
            id = newDao.id
        }
    }

    @CompileStatic
    boolean isUsedInSimulation() {
        return false
    }

    @CompileStatic
    List<Simulation> getSimulations() {
        return []
    }

    String getNameAndVersion() {
        name
    }

    @Override
    boolean equals(Object obj) {
        if (obj instanceof ModellingItem) {

            if (id != null && obj.id != null) {
                // Hack to allow Batches to be renamed at ART.
                // Avoid eg a Batch with id 1 being treated as equal to a P14n or Simulation with id 1
                //
                // (Might be correct to simply replace check for ModellingItem above, with a check for Class equality
                // and then can drop this hack.)
                String thisName = this.getClass().getCanonicalName();
                String rhsName = obj.getClass().getCanonicalName();
                if( !thisName.equals(rhsName) ){
                    return false;
                }

                return obj.id.equals(id)
            } else {
                return obj.name.equals(name) && obj.modelClass.equals(modelClass)
            }
        }
        return false
    }

    @Override
    String toString() {
        nameAndVersion
    }
}

interface IModellingItemChangeListener {

    public void itemChanged(ModellingItem item)

    public void itemSaved(ModellingItem item)
}
