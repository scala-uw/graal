package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;

public final class InvokeArrayApplyNode extends InvokeScalaNode {

    // array_apply takes two inputs: the array and the index.
    // The first operand is the array, which is expected to be of a specific type if this quick node is used

    @CompilerDirectives.CompilationFinal private final byte arrayElementType;

    public InvokeArrayApplyNode(Method method, int top, int callerBCI, byte arrayElementReifiedType) {
        // array_apply is not annotated with attributes
        super(method, top, callerBCI);
        assert !method.isStatic();
        this.arrayElementType = arrayElementReifiedType;
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        StaticObject array = nullCheck(EspressoFrame.popObject(frame, top - 2));
        int index = EspressoFrame.popInt(frame, top - 1);
        CompilerAsserts.partialEvaluationConstant(arrayElementType);
        switch (arrayElementType) {
            case TypeHints.BYTE:
                byte b = getContext().getInterpreterToVM().getArrayByte(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, b);
                break;
            case TypeHints.CHAR:
                char c = getContext().getInterpreterToVM().getArrayChar(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, c);
                break;
            case TypeHints.DOUBLE:
                double d = getContext().getInterpreterToVM().getArrayDouble(getLanguage(), index, array);
                EspressoFrame.putReifiedDouble(frame, resultAt, d);
                break;
            case TypeHints.FLOAT:
                float f = getContext().getInterpreterToVM().getArrayFloat(getLanguage(), index, array);
                EspressoFrame.putFloat(frame, resultAt, f);
                break;
            case TypeHints.INT:
                int i = getContext().getInterpreterToVM().getArrayInt(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, i);
                break;
            case TypeHints.LONG:
                long l = getContext().getInterpreterToVM().getArrayLong(getLanguage(), index, array);
                EspressoFrame.putReifiedLong(frame, resultAt, l);
                break;
            case TypeHints.SHORT:
                short s = getContext().getInterpreterToVM().getArrayShort(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, s);
                break;
            case TypeHints.BOOLEAN:
                byte bool = getContext().getInterpreterToVM().getArrayByte(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, bool);
                break;
            default:
                EspressoFrame.putObject(frame, resultAt, getContext().getInterpreterToVM().getArrayObject(getLanguage(), index, array));
                break;
        }
        return stackEffect;
    }
  
}
