package com.realitysink.cover.nodes;

import java.util.Map;

import com.oracle.truffle.api.frame.FrameSlotKind;
import com.realitysink.cover.parser.CoverParseException;

/**
 * The type of "something" in the Cover language. This can be a long variable, a function
 * or a Java object. Whatever you want, really.
 */
public class CoverType {
    public enum BasicType {
        LONG,
        DOUBLE,
        STRING,
        FUNCTION,
        OBJECT,
        JAVA_OBJECT
    }
    
    private BasicType basicType;
    
    /*
     * Is this actually and array of elements of this type?
     */
    private boolean isArray = false;
    
    private CoverType[] functionArguments;
    private CoverType functionReturn;
    
    /*
     * For variables.
     */
    
    private Map<String, CoverType> objectMembers;
    
    public CoverType(BasicType basicType) {
        this.basicType = basicType;
    }
    
    public CoverType[] getFunctionArguments() {
        return functionArguments;
    }

    public CoverType setFunctionArguments(CoverType[] functionArguments) {
        this.functionArguments = functionArguments;
        return this;
    }

    public CoverType getFunctionReturn() {
        return functionReturn;
    }

    public CoverType setFunctionReturn(CoverType functionReturn) {
        this.functionReturn = functionReturn;
        return this;
    }

    public Map<String, CoverType> getObjectMembers() {
        return objectMembers;
    }

    public CoverType setObjectMembers(Map<String, CoverType> objectMembers) {
        this.objectMembers = objectMembers;
        return this;
    }

    public boolean getIsArray() {
        return isArray;
    }

    public CoverType setIsArray(boolean isArray) {
        this.isArray = isArray;
        return this;
    }

    public FrameSlotKind getFrameSlotKind() {
        switch (basicType) {
        case LONG: return FrameSlotKind.Long;
        case DOUBLE: return FrameSlotKind.Double;
        case OBJECT: return FrameSlotKind.Object;
        case STRING: return FrameSlotKind.Object;
        case JAVA_OBJECT: return FrameSlotKind.Object;
        default:   throw new CoverParseException(null, "unsupported reference for frameslotkind: " + basicType.toString());
        }
    }

    public BasicType getBasicType() {
        return basicType;
    }
}
