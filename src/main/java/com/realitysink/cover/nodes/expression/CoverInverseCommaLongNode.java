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
package com.realitysink.cover.nodes.expression;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.realitysink.cover.nodes.CoverType;
import com.realitysink.cover.nodes.CoverTypedExpressionNode;

/**
 * The INVERSE "comma" operator. Evaluates both expressions and returns the value of the FIRST.
 * @author gerard
 *
 */
@NodeInfo(shortName = "+")
@NodeChildren({@NodeChild("leftNode"), @NodeChild("rightNode")})
public abstract class CoverInverseCommaLongNode extends CoverTypedExpressionNode {
    @Specialization
    protected long add(long left, Object right) { // FIXME: are there faster ways to ignore the first node?
        return left;
    }
    public CoverType getType() {
        return CoverType.LONG;
    }
}
