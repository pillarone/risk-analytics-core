package org.pillarone.riskanalytics.core.remoting;

import java.util.List;

public interface ITransactionService {

    /**
     * Get all Transactions
     * @return
     */
    List<TransactionInfo> getAllTransactions();

    /**
     * Get all transactions that can be linked from P1
     * @return
     */
    List<TransactionInfo> getLinkableTransactions();



}
