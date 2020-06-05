package org.openzen.zenscript.scriptingexample.tests.helpers;


import org.openzen.zenscript.codemodel.*;

import java.util.*;

public class FunctionParameterList {
    private final List<FunctionParameter> parameters;
    private final List<Object> values;
    
    public FunctionParameterList() {
        this.parameters = new ArrayList<>();
        this.values = new ArrayList<>();
    }
    
    public void addParameter(FunctionParameter parameter, Object value) {
        parameters.add(parameter);
        values.add(value);
    }
    
    public FunctionParameter[] getParameters() {
        return parameters.toArray(FunctionParameter.NONE);
    }
    
    public Map<FunctionParameter, Object> getParameterMap() {
        final Map<FunctionParameter, Object> results = new HashMap<>();
        for(int i = 0; i < parameters.size(); i++) {
            results.put(parameters.get(i), values.get(i));
        }
        return results;
    }
}
