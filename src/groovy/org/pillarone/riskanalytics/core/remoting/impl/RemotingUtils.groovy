package org.pillarone.riskanalytics.core.remoting.impl

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.pillarone.riskanalytics.core.remoting.ITransactionService
import org.pillarone.riskanalytics.core.remoting.TransactionInfo

class RemotingUtils {

    private static Log LOG = LogFactory.getLog(RemotingUtils)

    public static ITransactionService getTransactionService() {
        ITransactionService transactionService = (ITransactionService) Holders.grailsApplication.mainContext.getBean("transactionService")
        try {
            transactionService.getAllTransactions()
            return transactionService
        } catch (Throwable t) {
            LOG.error "Error obtaining remote service: ${t.message}"
            return [
                    getAllTransactions: {
                        return [new TransactionInfo(1, "Connection failed - contact support.", "")]
                    }
            ] as ITransactionService
        }
    }
}
