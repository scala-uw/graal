package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;
import com.oracle.truffle.espresso.vm.InterpreterToVM;

public final class InvokeArrayLengthNode extends InvokeScalaNode {

    // array_apply takes two inputs: the array and the index.
    // The first operand is the array, which is expected to be of a specific type if this quick node is used

    @CompilerDirectives.CompilationFinal private final byte arrayElementType;

    public InvokeArrayLengthNode(Method method, int top, int callerBCI, byte arrayElementReifiedType) {
        // array_apply is not annotated with attributes
        super(method, top, callerBCI);
        assert !method.isStatic();
        this.arrayElementType = arrayElementReifiedType;
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        StaticObject array = nullCheck(EspressoFrame.popObject(frame, top - 1));
        int arrayLength = InterpreterToVM.arrayLength(array, getLanguage());
        CompilerAsserts.partialEvaluationConstant(arrayElementType);
        EspressoFrame.putInt(frame, resultAt, arrayLength);
        return stackEffect;
    }
  
}
