package com.realitysink.cover.nodes;

import com.oracle.truffle.api.frame.FrameSlot;
import com.realitysink.cover.nodes.CoverType.BasicType;
import com.realitysink.cover.runtime.SLFunction;

public class CoverReference {
    private CoverType type;
    
    // only one of these is non-null
    private SLFunction function;
    private FrameSlot frameSlot;
    private Object javaObject;
    
    // If this is an array, this indicates the index in it 
    private Integer arrayIndex;
    
    public CoverReference(CoverType type) {
        this.type = type;
    }
    
    public CoverType getType() {
        return type;
    }
    public CoverReference setType(CoverType type) {
        this.type = type;
        return this;
    }
    public SLFunction getFunction() {
        return function;
    }
    public CoverReference setFunction(SLFunction function) {
        this.function = function;
        return this;
    }
    public FrameSlot getFrameSlot() {
        return frameSlot;
    }
    public CoverReference setFrameSlot(FrameSlot frameSlot) {
        this.frameSlot = frameSlot;
        return this;
    }
    public Object getJavaObject() {
        return javaObject;
    }
    public CoverReference setJavaObject(Object javaObject) {
        this.javaObject = javaObject;
        return this;
    }
    public Integer getArrayIndex() {
        return arrayIndex;
    }
    public CoverReference setArrayIndex(Integer arrayIndex) {
        this.arrayIndex = arrayIndex;
        return this;
    }
}
