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
package com.realitysink.cover.parser;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public class CoverParseException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private IASTNode node;

    public CoverParseException(IASTNode node, String arg0) {
        super(CoverParser.nodeMessage(node, arg0));
        this.setNode(node);
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1) {
        super(CoverParser.nodeMessage(node, arg0), arg1);
        this.setNode(node);
    }

    public CoverParseException(IASTNode node, String arg0, Throwable arg1, boolean arg2, boolean arg3) {
        super(CoverParser.nodeMessage(node, arg0), arg1, arg2, arg3);
        this.setNode(node);
    }

    public IASTNode getNode() {
        return node;
    }

    private void setNode(IASTNode node) {
        this.node = node;
    }
}
