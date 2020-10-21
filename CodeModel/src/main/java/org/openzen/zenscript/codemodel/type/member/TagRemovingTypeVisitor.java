package org.openzen.zenscript.codemodel.type.member;

import org.openzen.zenscript.codemodel.type.*;

import java.util.Arrays;

public class TagRemovingTypeVisitor implements TypeVisitor<StoredType> {
    
    private final LocalMemberCache cache;
    
    public TagRemovingTypeVisitor(LocalMemberCache cache) {
        
        this.cache = cache;
    }
    
    @Override
    public StoredType visitBasic(BasicTypeID basic) {
        return basic.stored;
    }
    
    @Override
    public StoredType visitString(StringTypeID string) {
        return string.stored();
    }
    
    @Override
    public StoredType visitArray(ArrayTypeID array) {
        return new ArrayTypeID(cache.getRegistry(), array.elementType.type.accept(this), array.dimension).stored();
    }
    
    @Override
    public StoredType visitAssoc(AssocTypeID assoc) {
        return new AssocTypeID(cache.getRegistry(), assoc.keyType.type.accept(this), assoc.valueType.type.accept(this)).stored();
    }
    
    @Override
    public StoredType visitGenericMap(GenericMapTypeID map) {
        return new GenericMapTypeID(cache.getRegistry(), map.value.type.accept(this), map.key).stored();
    }
    
    @Override
    public StoredType visitIterator(IteratorTypeID iterator) {
        return new IteratorTypeID(cache.getRegistry(), Arrays.stream(iterator.iteratorTypes).map(storedType -> storedType.type).map(typeID -> typeID.accept(this)).toArray(StoredType[]::new)).stored();
    }
    
    @Override
    public StoredType visitFunction(FunctionTypeID function) {
        return function.stored();
    }
    
    @Override
    public StoredType visitDefinition(DefinitionTypeID definition) {
        return definition.stored();
    }
    
    @Override
    public StoredType visitGeneric(GenericTypeID generic) {
        return generic.stored();
    }
    
    @Override
    public StoredType visitRange(RangeTypeID range) {
        return new RangeTypeID(cache.getRegistry(), range.baseType.type.accept(this)).stored;
    }
    
    @Override
    public StoredType visitOptional(OptionalTypeID type) {
        return new OptionalTypeID(cache.getRegistry(), type.baseType.accept(this).type).stored();
    }
}
