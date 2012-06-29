package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.parameterization.global.Global;
import org.pillarone.riskanalytics.core.simulation.engine.IterationScope;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class GlobalParameterComponent extends Component {

    private IterationScope iterationScope;
    private Map<String, Method> cachedMethods = null;

    @Override
    final protected void doCalculation() {
        if (iterationScope.isFirstIteration() && iterationScope.getPeriodScope().isFirstPeriod()) {
            validateRuntimeGlobalParameters();
        }
    }

    /**
     * Called in first period and iteration. This implementation itself is void.
     */
    protected void validateRuntimeGlobalParameters() {
    }

    public final Map<String, Method> getGlobalMethods() {
        if (cachedMethods != null) return cachedMethods;

        Map<String, Method> result = new HashMap<String, Method>();

        Class thisClass = this.getClass();
        for (Method method : thisClass.getMethods()) {
            Global annotation = method.getAnnotation(Global.class);
            if (annotation != null) {
                result.put(annotation.identifier().toLowerCase(), method);
            }
        }
        cachedMethods = result;
        return result;
    }

    public IterationScope getIterationScope() {
        return iterationScope;
    }

    public void setIterationScope(IterationScope iterationScope) {
        this.iterationScope = iterationScope;
    }
}
