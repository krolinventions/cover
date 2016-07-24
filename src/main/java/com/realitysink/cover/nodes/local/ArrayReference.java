package com.realitysink.cover.nodes.local;

import com.oracle.truffle.api.frame.FrameSlot;

public class ArrayReference {
    private FrameSlot frameSlot;
    private long index;

    public ArrayReference(FrameSlot frameSlot, long l) {
        this.frameSlot = frameSlot;
        this.index = l;
    }
    
    public FrameSlot getFrameSlot() {
        return frameSlot;
    }
    public void setFrameSlot(FrameSlot frameSlot) {
        this.frameSlot = frameSlot;
    }
    public long getIndex() {
        return index;
    }
    public void setIndex(long index) {
        this.index = index;
    }
}
