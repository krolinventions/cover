package com.realitysink.cover.nodes;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import com.oracle.truffle.api.CompilerAsserts;
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
        BOOLEAN,
        ARRAY,
        ARRAY_ELEMENT,
        FUNCTION,
        OBJECT,
        JAVA_OBJECT,
        VOID
    }
    
    public static CoverType VOID = new CoverType(BasicType.VOID);
    public static CoverType LONG = new CoverType(BasicType.LONG);
    public static CoverType DOUBLE = new CoverType(BasicType.DOUBLE);
    public static CoverType BOOLEAN = new CoverType(BasicType.BOOLEAN);
    public static CoverType FUNCTION = new CoverType(BasicType.FUNCTION);
    public static CoverType STRING = new CoverType(BasicType.STRING);
    public static CoverType ARRAY = new CoverType(BasicType.ARRAY);
    
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
        CompilerAsserts.neverPartOfCompilation();
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

    public CoverType getTypeOfArrayContents() {
        return arrayType;
    }

    public CoverType setArrayType(CoverType typeOfContents) {
        this.arrayType = typeOfContents;
        return this;
    }

    public boolean isPrimitiveType(IASTNode node) {
        CompilerAsserts.neverPartOfCompilation();
        switch (basicType) {
        case LONG: return true;
        case DOUBLE: return true;
        case BOOLEAN: return true;
        case OBJECT: return false;
        case STRING: return false;
        case JAVA_OBJECT: return false;
        case ARRAY: return false;
        case ARRAY_ELEMENT: return arrayType.isPrimitiveType(node);
        default:   throw new CoverParseException(node, "unsupported reference for isUnboxed: " + basicType.toString());
        }
    }

    public boolean canAccept(CoverType type) {
        CompilerAsserts.neverPartOfCompilation();
        if (isPrimitiveType(null) && getBasicType() == type.getBasicType()) {
            return true; // FIXME, array types!
        }
        if (getBasicType() == BasicType.DOUBLE && type.getBasicType() == BasicType.LONG) {
            return true;
        }
        if (basicType == BasicType.ARRAY_ELEMENT && getTypeOfArrayContents().canAccept(type)) {
            return true;
        }
        return false;
    }

    public CoverType combine(IASTNode node, CoverType other) {
        CompilerAsserts.neverPartOfCompilation();
        if (this.equals(other)) {
            return this;
        }
        if (basicType == BasicType.LONG && other.basicType == BasicType.DOUBLE) {
            return other; // convert to double
        }
        if (basicType == BasicType.DOUBLE && other.basicType == BasicType.LONG) {
            return this; // convert to double
        }
        throw new CoverParseException(node, "incompatible types: " + basicType + " and " + other.basicType);
    }

    @Override
    public int hashCode() {
        CompilerAsserts.neverPartOfCompilation();
        final int prime = 31;
        int result = 1;
        result = prime * result + ((arrayType == null) ? 0 : arrayType.hashCode());
        result = prime * result + ((basicType == null) ? 0 : basicType.hashCode());
        result = prime * result + Arrays.hashCode(functionArguments);
        result = prime * result + ((functionReturn == null) ? 0 : functionReturn.hashCode());
        result = prime * result + ((objectMembers == null) ? 0 : objectMembers.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        CompilerAsserts.neverPartOfCompilation();
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CoverType other = (CoverType) obj;
        if (arrayType == null) {
            if (other.arrayType != null)
                return false;
        } else if (!arrayType.equals(other.arrayType))
            return false;
        if (basicType != other.basicType)
            return false;
        if (!Arrays.equals(functionArguments, other.functionArguments))
            return false;
        if (functionReturn == null) {
            if (other.functionReturn != null)
                return false;
        } else if (!functionReturn.equals(other.functionReturn))
            return false;
        if (objectMembers == null) {
            if (other.objectMembers != null)
                return false;
        } else if (!objectMembers.equals(other.objectMembers))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "CoverType [basicType=" + basicType + ", functionArguments=" + Arrays.toString(functionArguments)
                + ", functionReturn=" + functionReturn + ", objectMembers=" + objectMembers + ", arrayType="
                + arrayType + "]";
    }    
}
