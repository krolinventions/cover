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

import com.oracle.truffle.api.frame.VirtualFrame;

/**
 * An expression that doesn't do anything. It returns null.
 * This is useful when you need to return an expression but don't have anything useful to do at runtime.
 */
public class CoverNopExpression extends CoverTypedExpressionNode {
    @Override
    public void executeVoid(VirtualFrame frame) {
    }

    @Override
    public CoverType getType() {
        return CoverType.VOID;
    }

    @Override
    public Object executeGeneric(VirtualFrame frame) {
        return null;
    }
}
