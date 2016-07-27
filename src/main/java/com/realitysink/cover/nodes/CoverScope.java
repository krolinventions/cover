package com.realitysink.cover.nodes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.realitysink.cover.parser.CoverParseException;
import com.realitysink.cover.runtime.SLFunction;

public class CoverScope {
    private FrameDescriptor frameDescriptor = new FrameDescriptor();
    private Map<String,SLFunction> functions = new HashMap<String, SLFunction>();
    private Map<String,FrameSlot> variables = new HashMap<String, FrameSlot>();
    private Map<String,String> typedefs = new HashMap<String, String>();
    private CoverScope parent;
    
    public CoverScope(CoverScope parent) {
        this.parent = parent;
        if (parent != null) {
            this.frameDescriptor = parent.frameDescriptor;
        }
    }
    public Map<String, SLFunction> getFunctions() {
        return functions;
    }
    public void setFunctions(Map<String, SLFunction> functions) {
        this.functions = functions;
    }
    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }
    public FrameSlot findFrameSlot(IASTNode node, String identifier) {
        FrameSlot slot = findFrameSlotInternal(identifier);
        if (slot == null) {
            throw new CoverParseException(node, "could not find variable " + identifier);
        }
        return slot;
    }
    private FrameSlot findFrameSlotInternal(String identifier) {
        FrameSlot frameSlot = variables.get(identifier);
        if (frameSlot != null) {
            //System.err.println("found " + identifier + " in this = " + this + " slot is " + System.identityHashCode(frameSlot));
            return frameSlot;
        } else if (parent != null) {
            //System.err.println("searching for " + identifier + " in parent scope. this = " + this);
            return parent.findFrameSlotInternal(identifier);
        }
        return null;
    }

    public SLFunction findFunction(IASTNode node, String identifier) {
        SLFunction function = findFunctionInternal(identifier);
        if (function == null) {
            throw new CoverParseException(node, "could not find function " + identifier);
        }
        return function;
    }
    
    private SLFunction findFunctionInternal(String identifier) {
        SLFunction function = functions.get(identifier);
        if (function != null) {
            return function;
        } else if (parent != null) {
            return parent.findFunctionInternal(identifier);
        }
        return null;
    }
    
    public FrameSlot addFrameSlot(IASTNode node, String identifier) {
        if (variables.containsKey(identifier)) {
            throw new CoverParseException(node, "identifier already exists in this scope");
        }
        // We just use an new Object() as identifier, as it just has to be unique.
        // If we use the name itself we run into trouble with variable scopes.
        FrameSlot slot = frameDescriptor.addFrameSlot(new Object());
        variables.put(identifier, slot);
        return slot;
    }
    
    public void addFunction(String functionName, SLFunction function) {
        functions.put(functionName, function);        
    }
    public void addTypeDef(String oldType, String newType) {
        System.err.println("adding new type " + newType + " (" + oldType + ")");
        typedefs.put(newType, typedefTranslate(oldType));
    }
    public String typedefTranslate(String rawSignature) {
        String translated = typedefs.get(rawSignature);
        if (translated != null) {
            return translated;
        } else if (parent != null) {
            return parent.typedefTranslate(rawSignature);
        } else {
            return rawSignature; // FIXME: check?
        }
    }
}
