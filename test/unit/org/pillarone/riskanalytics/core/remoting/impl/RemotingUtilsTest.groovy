package org.pillarone.riskanalytics.core.remoting.impl

import grails.util.Holders
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.support.MockApplicationContext
import org.junit.Test
import org.pillarone.riskanalytics.core.remoting.ITransactionService
import org.pillarone.riskanalytics.core.remoting.TransactionInfo
import org.springframework.context.ApplicationContext

class RemotingUtilsTest {

    @Test
    void checkSorting() {
        ApplicationContext mainContext = new MockApplicationContext()
        mainContext.registerMockBean("transactionService", new TestTransactionService())
        GrailsApplication application = new DefaultGrailsApplication(mainContext: mainContext)
        Holders.setGrailsApplication(application)
        List transactions = RemotingUtils.allTransactions
        assert ['aTest2','ATest2','test','Test','Test2'] == transactions.name
    }

    class TestTransactionService implements ITransactionService{
        @Override
        List<TransactionInfo> getAllTransactions() {
            [
                    new TransactionInfo(1,'test','some url'),
                    new TransactionInfo(1,'Test','some url'),
                    new TransactionInfo(1,'Test2','some url'),
                    new TransactionInfo(1,'aTest2','some url'),
                    new TransactionInfo(1,'ATest2','some url'),
            ]
        }

        @Override
        List<TransactionInfo> getLinkableTransactions() {
            null
        }
    }
}
