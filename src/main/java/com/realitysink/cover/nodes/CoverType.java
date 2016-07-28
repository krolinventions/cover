package com.realitysink.cover.nodes;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTNode;

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
        ARRAY,
        ARRAY_ELEMENT,
        FUNCTION,
        OBJECT,
        JAVA_OBJECT
    }
    
    private BasicType basicType;
    
    private CoverType[] functionArguments;
    private CoverType functionReturn;
    
    /*
     * For variables.
     */
    private Map<String, CoverType> objectMembers;
    
    private CoverType arrayType;
    
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

    public FrameSlotKind getFrameSlotKind(IASTNode node) {
        switch (basicType) {
        case LONG: return FrameSlotKind.Long;
        case DOUBLE: return FrameSlotKind.Double;
        case OBJECT: return FrameSlotKind.Object;
        case STRING: return FrameSlotKind.Object;
        case ARRAY: return FrameSlotKind.Object;
        case JAVA_OBJECT: return FrameSlotKind.Object;
        case ARRAY_ELEMENT: return arrayType.getFrameSlotKind(node);
        default:   throw new CoverParseException(node, "unsupported reference for frameslotkind: " + basicType.toString());
        }
    }

    public BasicType getBasicType() {
        return basicType;
    }

    public CoverType getArrayType() {
        return arrayType;
    }

    public CoverType setArrayType(CoverType typeOfContents) {
        this.arrayType = typeOfContents;
        return this;
    }

    public boolean isUnboxed(IASTNode node) {
        switch (basicType) {
        case LONG: return true;
        case DOUBLE: return true;
        case OBJECT: return false;
        case STRING: return false;
        case JAVA_OBJECT: return false;
        case ARRAY: return false;
        case ARRAY_ELEMENT: return arrayType.isUnboxed(node);
        default:   throw new CoverParseException(node, "unsupported reference for isUnboxed: " + basicType.toString());
        }
    }

}
