package org.openzen.zenscript.codemodel;

import org.openzen.zencode.shared.*;
import org.openzen.zenscript.codemodel.generic.*;
import org.openzen.zenscript.codemodel.type.*;

import java.util.*;

public class GenericMapper {
    
    public static final GenericMapper EMPTY = new GenericMapper(CodePosition.BUILTIN, null, Collections
            .emptyMap());
    
    public final CodePosition position;
    public final GlobalTypeRegistry registry;
    private final Map<TypeParameter, TypeID> mapping;
    
    public GenericMapper(CodePosition position, GlobalTypeRegistry registry, Map<TypeParameter, TypeID> mapping) {
        if(mapping == null)
            throw new IllegalArgumentException();
        
        this.position = position;
        this.registry = registry;
        this.mapping = mapping;
    }
    
    public Map<TypeParameter, TypeID> getMapping() {
        return mapping;
    }
    
    public TypeID map(TypeID original) {
        return mapping.isEmpty() ? original : original.instance(this);
    }
    
    public TypeID[] map(TypeID[] original) {
        if(mapping.isEmpty() || original.length == 0)
            return original;
        
        TypeID[] mapped = new TypeID[original.length];
        for(int i = 0; i < original.length; i++)
            mapped[i] = original[i].instance(this);
        return mapped;
    }
    
    public TypeID mapGeneric(GenericTypeID type) {
        //if (!mapping.containsKey(type.parameter))
        //	throw new IllegalStateException("No mapping found for type " + type);
        
        return mapping.getOrDefault(type.parameter, type);
    }
    
    public FunctionHeader map(FunctionHeader original) {
        return mapping.isEmpty() ? original : original.withGenericArguments(this);
    }
    
    public GenericMapper getInner(CodePosition position, GlobalTypeRegistry registry, Map<TypeParameter, TypeID> mapping) {
        Map<TypeParameter, TypeID> resultMap = new HashMap<>(this.mapping);
        mapping.forEach((typeParameter, storedType) -> {
            if(resultMap.containsKey(typeParameter)) {
                if(storedType instanceof GenericTypeID && ((GenericTypeID) storedType).parameter == typeParameter) {
                    return;
                }
            }
            resultMap.put(typeParameter, storedType);
        });
        
        return new GenericMapper(position, registry, resultMap);
    }
    
    public GenericMapper getInner(CodePosition position, GlobalTypeRegistry registry, TypeParameter[] parameters) {
        Map<TypeParameter, TypeID> resultMap = new HashMap<>(this.mapping);
        for(TypeParameter parameter : parameters)
            resultMap.put(parameter, registry.getGeneric(parameter));
        return new GenericMapper(position, registry, resultMap);
    }
    
    @Override
    public String toString() {
        if(mapping.isEmpty())
            return "{}";
        
        StringBuilder result = new StringBuilder();
        result.append('{');
        boolean first = true;
        for(Map.Entry<TypeParameter, TypeID> entry : mapping.entrySet()) {
            if(first) {
                first = false;
            } else {
                result.append(", ");
            }
            result.append(entry.getKey().toString()).append(": ").append(entry.getValue());
        }
        result.append('}');
        return result.toString();
    }
}
