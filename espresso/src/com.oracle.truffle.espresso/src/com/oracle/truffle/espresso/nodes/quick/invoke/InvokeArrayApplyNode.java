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
            case TypeHints.TypeA.BYTE:
                byte b = getContext().getInterpreterToVM().getArrayByte(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxByte(b));
                break;
            case TypeHints.TypeA.CHAR:
                char c = getContext().getInterpreterToVM().getArrayChar(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxCharacter(c));
                break;
            case TypeHints.TypeA.DOUBLE:
                double d = getContext().getInterpreterToVM().getArrayDouble(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxDouble(d));
                break;
            case TypeHints.TypeA.FLOAT:
                float f = getContext().getInterpreterToVM().getArrayFloat(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxFloat(f));
                break;
            case TypeHints.TypeA.INT:
                int i = getContext().getInterpreterToVM().getArrayInt(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxInteger(i));
                break;
            case TypeHints.TypeA.LONG:
                long l = getContext().getInterpreterToVM().getArrayLong(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxLong(l));
                break;
            case TypeHints.TypeA.SHORT:
                short s = getContext().getInterpreterToVM().getArrayShort(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxShort(s));
                break;
            case TypeHints.TypeA.BOOLEAN:
                byte bool = getContext().getInterpreterToVM().getArrayByte(getLanguage(), index, array);
                EspressoFrame.putObject(frame, resultAt, getMeta().boxBoolean(bool != 0));
                break;
            default:
                EspressoFrame.putObject(frame, resultAt, getContext().getInterpreterToVM().getArrayObject(getLanguage(), index, array));
                break;
        }
        return stackEffect;
    }
  
}
