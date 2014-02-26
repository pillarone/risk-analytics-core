package org.pillarone.riskanalytics.core.workflow

import grails.orm.HibernateCriteriaBuilder
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.joda.time.DateTime
import org.pillarone.riskanalytics.core.ParameterizationDAO
import org.pillarone.riskanalytics.core.parameter.comment.workflow.IssueStatus
import org.pillarone.riskanalytics.core.parameterization.ParameterizationHelper
import org.pillarone.riskanalytics.core.remoting.TransactionInfo
import org.pillarone.riskanalytics.core.remoting.impl.RemotingUtils
import org.pillarone.riskanalytics.core.simulation.item.Parameterization
import org.pillarone.riskanalytics.core.simulation.item.VersionNumber
import org.pillarone.riskanalytics.core.simulation.item.parameter.ParameterHolder
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.Comment
import org.pillarone.riskanalytics.core.simulation.item.parameter.comment.workflow.WorkflowComment
import org.pillarone.riskanalytics.core.user.UserManagement

import static org.pillarone.riskanalytics.core.workflow.Status.*

class StatusChangeService {

    private static Log LOG = LogFactory.getLog(StatusChangeService)

    @CompileStatic
    public static StatusChangeService getService() {
        return (StatusChangeService) Holders.grailsApplication.mainContext.getBean(StatusChangeService.class)
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
                    HibernateCriteriaBuilder criteria = ParameterizationDAO.createCriteria()
                    List<ParameterizationDAO> p14nsInWorkflow = criteria.list {
                        and {
                            eq("dealId", parameterization.dealId)
                            ne("id", parameterization.id)
                            ne("status", org.pillarone.riskanalytics.core.workflow.Status.NONE)
                            //Check model class name as well
                            eq("modelClassName", parameterization.modelClass.name)
                        }
                    }

                    if (!p14nsInWorkflow.isEmpty()) {
                        ParameterizationDAO firstExistingWorkflowP14n = p14nsInWorkflow.first();
                        String nameAndVersion = firstExistingWorkflowP14n?.name + " v" + firstExistingWorkflowP14n?.itemVersion;
                        throw new WorkflowException(
                                "P14n '" + parameterization.name + "'",
                                DATA_ENTRY,
                                "Deal '" + getTransactionName(parameterization.dealId) + "' already used in Model " + (parameterization.modelClass?.simpleName - "Model") +
                                        "\nEg in workflow P14n: '" + nameAndVersion + "'" +
                                        "\nCan you work with one of the existing workflow P14ns, or choose a different deal ?"
                        )
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
                validate(parameterization)
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

    void validate(Parameterization parameterization) {
        //are there any validation errors ?
        parameterization.validate()
        if (parameterization.realValidationErrors) {
            throw new WorkflowException("P14n '" + parameterization.name + "'", IN_REVIEW, "Pls fix validation errors in model.")
        }

    }

    @CompileStatic
    Parameterization changeStatus(Parameterization parameterization, Status to) {
        Parameterization newParameterization = null
        reviewComments(parameterization, to)
        AuditLog.withTransaction { status ->
            newParameterization = (Parameterization) actions.get(to).call(parameterization)
        }
        return newParameterization
    }

    void clearAudit(Parameterization parameterization) {
        AuditLog.withTransaction {
            parameterization.load(false)
            List<AuditLog> auditLogs = AuditLog.findAllByFromParameterizationOrToParameterization(parameterization.dao, parameterization.dao)
            auditLogs*.delete()
        }
    }

    //TODO: re-use MIF

    @CompileStatic
    private Parameterization incrementVersion(Parameterization item, boolean newR) {
        String parameterizationName = newR ? getTransactionName(item.dealId) : item.name
        Parameterization newItem = new Parameterization(parameterizationName)

        List<ParameterHolder> newParameters = ParameterizationHelper.copyParameters(item.parameters)
        newParameters.each { ParameterHolder it ->
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
                if ((comment as WorkflowComment).status != IssueStatus.CLOSED) {
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

    @TypeChecked
    private void audit(Status from, Status to, Parameterization fromParameterization, Parameterization toParameterization) {
        AuditLog auditLog = new AuditLog(fromStatus: from, toStatus: to, fromParameterization: (ParameterizationDAO) fromParameterization?.dao, toParameterization: (ParameterizationDAO) toParameterization.dao)
        auditLog.date = new DateTime()
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

    @CompileStatic
    private String getTransactionName(long dealId) {
        return RemotingUtils.allTransactions.find { TransactionInfo it -> it.dealId == dealId }.name
    }
}

@CompileStatic
class WorkflowException extends RuntimeException {

    public WorkflowException(String itemName, Status to, String cause) {
        super("Cannot change status of $itemName to ${to.displayName}.\nCause: $cause".toString())
    }
}
