package org.pillarone.riskanalytics.core.simulation.item

import org.pillarone.riskanalytics.core.ResourceDAO
import org.pillarone.riskanalytics.core.parameter.Parameter
import org.pillarone.riskanalytics.core.parameter.comment.ResourceCommentDAO
import org.pillarone.riskanalytics.core.parameter.comment.ResourceTag
import org.pillarone.riskanalytics.core.parameter.comment.Tag
import org.pillarone.riskanalytics.core.parameter.comment.workflow.WorkflowResourceCommentDAO
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import org.pillarone.riskanalytics.core.workflow.Status
import org.pillarone.riskanalytics.core.parameter.comment.CommentDAO
import org.pillarone.riskanalytics.core.components.IResource
import org.pillarone.riskanalytics.core.components.ResourceHolder

class Resource extends ParametrizedItem {

    String comment
    VersionNumber versionNumber
    List<ParameterHolder> parameterHolders
    List<Tag> tags = []
    boolean valid
    List validationErrors
    Status status

    Resource(String name, Class resourceClass) {
        super(name)
        this.modelClass = resourceClass
        parameterHolders = []
        status = Status.NONE
        versionNumber = new VersionNumber("1")
    }

    ResourceHolder getResourceInstance() {
        IResource instance = modelClass.newInstance()
        if (name != null) {
            if (!isLoaded()) {
                load()
            }
            for (ParameterHolder holder in parameterHolders) {
                instance[holder.path] = holder.businessObject
            }
        } else {
            instance.useDefault()
        }
        return new ResourceHolder(instance, name, versionNumber, instance.class)
    }

    @Override
    protected void addToDao(Parameter parameter, Object dao) {
        dao = dao as ResourceDAO
        dao.addToParameters(parameter)
    }

    @Override
    protected void removeFromDao(Parameter parameter, Object dao) {
        dao = dao as ResourceDAO
        dao.removeFromParameters(parameter)
    }

    @Override
    protected createDao() {
        return new ResourceDAO(name: name, resourceClassName: modelClass.name)
    }

    @Override
    def getDaoClass() {
        return ResourceDAO
    }

    @Override
    protected void mapToDao(Object dao) {
        dao = dao as ResourceDAO
        dao.itemVersion = versionNumber.toString()
        dao.name = name
        dao.creationDate = creationDate
        dao.modificationDate = modificationDate
        dao.valid = valid
        dao.resourceClassName = modelClass.name
        dao.creator = creator
        dao.lastUpdater = lastUpdater
        dao.comment = comment
        dao.status = status
        saveParameters(parameterHolders, dao.parameters, dao)
        saveComments(dao)
        saveTags(dao)
    }

    @Override
    protected void mapFromDao(Object dao, boolean completeLoad) {
        dao = dao as ResourceDAO
        long time = System.currentTimeMillis()
        id = dao.id
        versionNumber = new VersionNumber(dao.itemVersion)
        name = dao.name
        creationDate = dao.creationDate
        modificationDate = dao.modificationDate
        valid = dao.valid
        modelClass = getClass().getClassLoader().loadClass(dao.resourceClassName)
        creator = dao.creator
        lastUpdater = dao.lastUpdater
        status = dao.status
        comment = dao.comment
        if (completeLoad) {
            loadParameters(parameterHolders, dao.parameters)
            loadComments(dao)
            tags = dao.tags*.tag
            if (!tags) tags = []
        }
        LOG.info("Resource $name loaded in ${System.currentTimeMillis() - time}ms")
    }

    @Override
    protected loadFromDB() {
        return ResourceDAO.findByNameAndResourceClassName(name, modelClass.name)
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

    protected void saveTags(ResourceDAO dao) {
        List tagsToRemove = []
        for (ResourceTag tag in dao.tags) {
            if (!tags.contains(tag.tag)) {
                tagsToRemove << tag
            }
        }
        for (ResourceTag tag in tagsToRemove) {
            dao.removeFromTags(tag)

        }
        tagsToRemove.each {it.delete()}

        for (Tag tag in tags) {
            if (!dao.tags*.tag?.contains(tag)) {
                dao.addToTags(new ResourceTag(tag: tag))
            }
        }
    }

    private void loadComments(ResourceDAO dao) {
        comments = []

        for (ResourceCommentDAO c in dao.comments) {
            comments << new Comment(c)
        }

        for (WorkflowResourceCommentDAO c in dao.issues) {
            comments << new WorkflowComment(c)
        }
    }

    @Override
    CommentDAO getItemCommentDAO(Object dao) {
        return new ResourceCommentDAO(resourceDAO: dao)
    }

    protected void commentAdded(ResourceDAO dao, WorkflowComment comment) {
        WorkflowResourceCommentDAO commentDAO = new WorkflowResourceCommentDAO(resource: dao)
        comment.applyToDomainObject(commentDAO)
        dao.addToIssues(commentDAO)
        comment.added = false
    }

    protected void commentUpdated(ResourceDAO dao, WorkflowComment comment) {
        WorkflowResourceCommentDAO commentDAO = dao.issues.find { it.path == comment.path }
        if (commentDAO) {
            comment.applyToDomainObject(commentDAO)
            comment.updated = false
        }
    }

    protected boolean commentDeleted(ResourceDAO dao, WorkflowComment comment) {
        WorkflowResourceCommentDAO commentDAO = dao.issues.find { it.path == comment.path }
        if (commentDAO) {
            dao.removeFromIssues(commentDAO)
            commentDAO.delete()
            return true
        }
        return false
    }

    List getParameters(String path) {
        return parameterHolders.findAll {ParameterHolder parameter ->
            parameter.path == path && !parameter.removed
        }
    }


}
