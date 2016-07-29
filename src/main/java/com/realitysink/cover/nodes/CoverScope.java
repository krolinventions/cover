package com.realitysink.cover.nodes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTNode;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.realitysink.cover.nodes.CoverType.BasicType;
import com.realitysink.cover.parser.CoverParseException;
import com.realitysink.cover.runtime.SLFunction;

public class CoverScope {
    private FrameDescriptor frameDescriptor = new FrameDescriptor();
    private Map<String,CoverReference> definitions = new HashMap<String, CoverReference>();
    private Map<String,String> typedefs = new HashMap<String, String>();
    private CoverScope parent;
    
    public CoverScope(CoverScope parent) {
        this.parent = parent;
        if (parent != null) {
            this.frameDescriptor = parent.frameDescriptor;
        }
    }
    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }
    
    public CoverReference findReference(String identifier) {
        CoverReference definition = definitions.get(identifier);
        if (definition != null) {
            return definition;
        } else if (parent != null) {
            return parent.findReference(identifier);
        }
        return null;
    }
    
    public CoverReference define(IASTNode node, String identifier, CoverType type) {
        if (definitions.containsKey(identifier)) {
            throw new CoverParseException(node, "identifier already exists in this scope");
        }
        CoverReference ref = new CoverReference(type); 
        if (type.getBasicType() != BasicType.FUNCTION) {
            // function references don't use frameslots, they use the SLFunction object itself
            FrameSlot slot = frameDescriptor.addFrameSlot(new Object());
            FrameSlotKind frameSlotKind = type.getFrameSlotKind(node);
            slot.setKind(frameSlotKind);
            System.err.println("added " + slot);
            ref.setFrameSlot(slot);
        }
        definitions.put(identifier, ref);
        System.err.println("defined " + identifier + " as " + type.getBasicType());
        return ref;
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
