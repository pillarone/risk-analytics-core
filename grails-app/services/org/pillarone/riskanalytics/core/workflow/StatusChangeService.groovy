package org.pillarone.riskanalytics.core.workflow

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.parameter.comment.workflow.IssueStatus
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import org.pillarone.riskanalytics.core.user.UserManagement
import static org.pillarone.riskanalytics.core.workflow.Status.*

class StatusChangeService {

    private static Log LOG = LogFactory.getLog(StatusChangeService)

    public static StatusChangeService getService() {
        return (StatusChangeService) ApplicationHolder.application.mainContext.getBean("statusChangeService")
    }

    private Map<Status, Closure> actions = [
            (NONE): { Parameterization parameterization ->
                throw new IllegalStateException("Cannot change status to ${NONE.getDisplayName()}")
            },
            (DATA_ENTRY): { Parameterization parameterization ->
                if (parameterization.status == IN_REVIEW) {
                    parameterization.status = Status.REJECTED
                    parameterization.save()
                } else if (parameterization.status == NONE) {
                    ParameterizationDAO dao = ParameterizationDAO.findByNameAndItemVersionLike(parameterization.name, "R%")
                    if (dao != null) {
                        throw new WorkflowException(parameterization.name, DATA_ENTRY, "Parameterization is already in workflow.")
                    }
                }
                Parameterization newParameterization = incrementVersion(parameterization, parameterization.status == NONE)
                newParameterization.status = DATA_ENTRY
                newParameterization.save()
                if (parameterization.status == Status.REJECTED) {
                    audit(IN_REVIEW, DATA_ENTRY, parameterization, newParameterization)
                } else {
                    audit(NONE, DATA_ENTRY, null, newParameterization)
                }
                return newParameterization
            },
            (IN_REVIEW): { Parameterization parameterization ->
                audit(parameterization.status, IN_REVIEW, parameterization, parameterization)
                parameterization.status = IN_REVIEW
                parameterization.save()
                return parameterization
            },
            (IN_PRODUCTION): { Parameterization parameterization ->
                audit(parameterization.status, IN_PRODUCTION, parameterization, parameterization)
                parameterization.status = IN_PRODUCTION
                parameterization.save()
                return parameterization
            }
    ]

    Parameterization changeStatus(Parameterization parameterization, Status to) {
        Parameterization newParameterization = null
        reviewComments(parameterization, to)
        AuditLog.withTransaction { status ->
            newParameterization = actions.get(to).call(parameterization)
        }
        return newParameterization
    }

    //TODO: re-use MIF

    private Parameterization incrementVersion(Parameterization item, boolean newR) {
        Parameterization newItem = new Parameterization(item.name)

        List newParameters = ParameterizationHelper.copyParameters(item.parameters)
        newParameters.each {
            newItem.addParameter(it)
        }
        newItem.periodCount = item.periodCount
        newItem.periodLabels = item.periodLabels
        newItem.modelClass = item.modelClass
        newItem.versionNumber = newR ? new VersionNumber("R1") : VersionNumber.incrementVersion(item)
        newItem.dealId = item.dealId
        newItem.valuationDate = item.valuationDate

        for (Comment comment in item.comments) {
            if (comment instanceof WorkflowComment) {
                if (comment.status != IssueStatus.CLOSED) {
                    newItem.addComment(comment.clone())
                }
            } else {
                newItem.addComment(comment.clone())
            }
        }

        def newId = newItem.save()
        newItem.load()
        return newItem
    }

    private void audit(Status from, Status to, Parameterization fromParameterization, Parameterization toParameterization) {
        AuditLog auditLog = new AuditLog(fromStatus: from, toStatus: to, fromParameterization: fromParameterization?.dao, toParameterization: toParameterization.dao)
        auditLog.date = new Date()
        auditLog.person = UserManagement.getCurrentUser()
        if (!auditLog.save(flush: true)) {
            LOG.error "Error saving audit log: ${auditLog.errors}"
        }
    }

    private void reviewComments(Parameterization from, Status to) {
        switch (to) {
            case DATA_ENTRY:
                for (Comment comment in from.comments) {
                    if (comment instanceof WorkflowComment) {
                        if (comment.status == IssueStatus.RESOLVED) {
                            throw new WorkflowException(from.name, to, "Resolved comments found - must be closed or reopened.")
                        }
                    }
                }
                break;
            case IN_REVIEW:
                for (Comment comment in from.comments) {
                    if (comment instanceof WorkflowComment) {
                        if (comment.status == IssueStatus.OPEN) {
                            throw new WorkflowException(from.name, to, "Open comments found - must be resolved first.")
                        }
                    }
                }
                break;
            case IN_PRODUCTION:
                for (Comment comment in from.comments) {
                    if (comment instanceof WorkflowComment) {
                        if (comment.status != IssueStatus.CLOSED) {
                            throw new WorkflowException(from.name, to, "Unclosed comments found - must be closed first.")
                        }
                    }
                }
                break;
        }
    }
}

class WorkflowException extends RuntimeException {

    public WorkflowException(String itemName, Status to, String cause) {
        super("Cannot change status of $itemName to ${to.displayName}. Cause: $cause".toString())
    }
}
