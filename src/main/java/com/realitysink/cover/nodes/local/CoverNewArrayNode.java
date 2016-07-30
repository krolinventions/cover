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
package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.UnexpectedResultException;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverType.BasicType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.nodes.SLExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

public class CoverNewArrayNode extends CoverTypedExpressionNode {
    private final String typeName;
    @Child
    private SLExpressionNode length;
    
    public CoverNewArrayNode(String typeName, SLExpressionNode length) {
        this.typeName = typeName;
        this.length = length;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        int l;
        try {
            l = (int) length.executeLong(frame);
        } catch (UnexpectedResultException e) {
            throw new CoverRuntimeException(this, "could not create array", e);
        }
        if ("int".equals(typeName)) {
            return new long[l];
        } else if ("long".equals(typeName)) {
            return new long[l];
        } else if ("char".equals(typeName)) {
            return new long[l];
        } else if ("double".equals(typeName)) {
            return new double[l];
        } else {
            throw new CoverRuntimeException(this, "unsupported array type: " + typeName);
        }        
    }

    @Override
    public CoverType getType() {
        CompilerAsserts.neverPartOfCompilation();
        if ("int".equals(typeName)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.LONG));
        } else if ("long".equals(typeName)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.LONG));
        } else if ("char".equals(typeName)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.LONG));
        } else if ("double".equals(typeName)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.DOUBLE));
        } else {
            throw new CoverRuntimeException(this, "unsupported array type: " + typeName);
        }
    }
}
