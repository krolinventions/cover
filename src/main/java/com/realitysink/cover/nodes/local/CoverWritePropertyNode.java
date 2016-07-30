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

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.NodeFields;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObject;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;
import com.realitysink.cover.runtime.CoverRuntimeException;

@NodeChildren({@NodeChild("object"), @NodeChild("value")})
@NodeFields({
    @NodeField(name="propertyName", type=String.class),
    @NodeField(name="type", type=CoverType.class)
})
@NodeInfo(shortName="=")
public abstract class CoverWritePropertyNode extends CoverTypedExpressionNode {
    @Specialization
    protected Object writeLongArrayElement(VirtualFrame frame, DynamicObject object, Object value) {
        System.err.println("setting property " + getPropertyName() + " to " + value);
        if (!object.set(getPropertyName(), value)) {
            throw new CoverRuntimeException(this, "property " + getPropertyName() + " not found");
        }
        return value;
    }

    protected abstract Object getPropertyName();
}
