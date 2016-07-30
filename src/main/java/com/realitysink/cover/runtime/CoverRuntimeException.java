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
package com.realitysink.cover.runtime;

import com.oracle.truffle.api.nodes.Node;

public class CoverRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CoverRuntimeException(Node node, String arg0, Throwable arg1) {
        super(nodeDescription(node) + arg0, arg1);
    }

    public CoverRuntimeException(Node node, String arg0) {
        super(nodeDescription(node) + arg0);
    }

    public CoverRuntimeException(Node node, Throwable arg0) {
        super(nodeDescription(node) + arg0.getMessage(), arg0);
    }
    
    private static String nodeDescription(Node node) {
        if (node.getSourceSection() == null || node.getSourceSection().getSource() == null) {
            return "<unknown>: ";
        }
        return node.getSourceSection().getSource().getName()+":"+node.getSourceSection().getStartLine()+": " + node.getSourceSection().getCode()+": ";
    }
}
