package org.pillarone.riskanalytics.core.remoting;

import java.io.Serializable;

/**
 * Allianz Risk Transfer  ATOM
 * User: bzetterstrom
 */
public class TransactionInfo implements Serializable {

    private static final long serialVersionUID = 3293174273647246400L;
    private long dealId;
    private String name;
    private String transactionUrl;

    public TransactionInfo(final long dealId, final String name, final String transactionUrl) {
        this.dealId = dealId;
        this.name = name;
        this.transactionUrl = transactionUrl;
    }

    public long getDealId() {
        return dealId;
    }

    public void setDealId(final long dealId) {
        this.dealId = dealId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getTransactionUrl() {
        return transactionUrl;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getName() + " (" + getDealId() + ")]";
    }
}
