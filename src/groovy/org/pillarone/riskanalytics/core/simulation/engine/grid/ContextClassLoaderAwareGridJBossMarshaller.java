package org.pillarone.riskanalytics.core.simulation.engine.grid;

import org.gridgain.grid.GridException;
import org.gridgain.grid.marshaller.jboss.GridJBossMarshaller;

import java.io.InputStream;

public class ContextClassLoaderAwareGridJBossMarshaller extends GridJBossMarshaller {

    @Override
    public <T> T unmarshal(InputStream in, ClassLoader clsLoader) throws GridException {
        return (T) super.unmarshal(in, Thread.currentThread().getContextClassLoader());
    }
}

