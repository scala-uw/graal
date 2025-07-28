package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.impl.Method;

public abstract class InvokeScalaNode extends InvokeQuickNode {
    public InvokeScalaNode(Method method, int top, int curBCI) {
        super(method, top, curBCI);
    }

    @Override
    public abstract int execute(VirtualFrame frame, boolean isContinuationResume);
}