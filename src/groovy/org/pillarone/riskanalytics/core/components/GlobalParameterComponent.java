package org.pillarone.riskanalytics.core.components;

import org.pillarone.riskanalytics.core.parameterization.global.Global;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class GlobalParameterComponent extends Component {

    private Map<String, Method> cachedMethods = null;

    @Override
    final protected void doCalculation() {
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
}
