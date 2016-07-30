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
    private final CoverType type;
    @Child
    private SLExpressionNode length;
    
    public CoverNewArrayNode(CoverType type, SLExpressionNode length) {
        this.type = type;
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
        if (type.getBasicType() == BasicType.LONG) {
            return new long[l];
        } else if (type.getBasicType() == BasicType.DOUBLE) {
            return new double[l];
        } else {
            throw new CoverRuntimeException(this, "unsupported array type: " + type);
        }        
    }

    @Override
    public CoverType getType() {
        CompilerAsserts.neverPartOfCompilation();
        if ("int".equals(type)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.LONG));
        } else if ("long".equals(type)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.LONG));
        } else if ("char".equals(type)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.LONG));
        } else if ("double".equals(type)) {
            return new CoverType(BasicType.ARRAY).setArrayType(new CoverType(BasicType.DOUBLE));
        } else {
            throw new CoverRuntimeException(this, "unsupported array type: " + type);
        }
    }
}
