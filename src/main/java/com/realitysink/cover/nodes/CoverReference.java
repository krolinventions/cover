/*
 * Copyright (c) 2016 Gerard Krol
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.realitysink.cover.nodes;

import com.oracle.truffle.api.frame.FrameSlot;
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
