package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.EspressoLanguage;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;

public final class InvokeArrayUpdateNode extends InvokeScalaNode {

    // array_update takes three inputs: the array, the index, and the value to update.
    // The first operand is the array, which is expected to be of a specific type if this quick node is used.

    @CompilerDirectives.CompilationFinal private final byte arrayElementType;

    public InvokeArrayUpdateNode(Method method, int top, int callerBCI, byte arrayElementReifiedType) {
        // array_update is not annotated with attributes
        super(method, top, callerBCI);
        assert !method.isStatic();
        this.arrayElementType = arrayElementReifiedType;
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        StaticObject array = nullCheck(EspressoFrame.popObject(frame, top - 3));
        int index = EspressoFrame.popInt(frame, top - 2);
        StaticObject newElement = EspressoFrame.popObject(frame, top - 1);
        //System.out.println("InvokeArrayUpdateNode: array=" + array.toVerboseString() + ", index=" + index + ", newElement=" + newElement.toVerboseString() + ", reifiedType=" + reifiedType + " resultAt=" + resultAt);
        EspressoLanguage language = getLanguage();
        CompilerAsserts.partialEvaluationConstant(arrayElementType);
        switch (arrayElementType) {
            case TypeHints.TypeA.BYTE:
                getContext().getInterpreterToVM().setArrayByte(language, getMeta().unboxByte(newElement), index, array);
                break;
            case TypeHints.TypeA.CHAR:
                getContext().getInterpreterToVM().setArrayChar(language, getMeta().unboxCharacter(newElement), index, array);
                break;
            case TypeHints.TypeA.DOUBLE:
                getContext().getInterpreterToVM().setArrayDouble(language, getMeta().unboxDouble(newElement), index, array);
                break;
            case TypeHints.TypeA.FLOAT:
                getContext().getInterpreterToVM().setArrayFloat(language, getMeta().unboxFloat(newElement), index, array);
                break;
            case TypeHints.TypeA.INT:
                getContext().getInterpreterToVM().setArrayInt(language, getMeta().unboxInteger(newElement), index, array);
                break;
            case TypeHints.TypeA.LONG:
                getContext().getInterpreterToVM().setArrayLong(language, getMeta().unboxLong(newElement), index, array);
                break;
            case TypeHints.TypeA.SHORT:
                getContext().getInterpreterToVM().setArrayShort(language, getMeta().unboxShort(newElement), index, array);
                break;
            case TypeHints.TypeA.BOOLEAN:
                getContext().getInterpreterToVM().setArrayByte(language, (byte) (getMeta().unboxBoolean(newElement) ? 1 : 0), index, array);
                break;
            default:
                getContext().getInterpreterToVM().setArrayObject(language, newElement, index, array);
                break;
        }
        return stackEffect;
    }
}