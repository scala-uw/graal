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
        //StaticObject newElement = EspressoFrame.popObject(frame, top - 1);
        //System.out.println("InvokeArrayUpdateNode: array=" + array.toVerboseString() + ", index=" + index + ", newElement=" + newElement.toVerboseString() + ", reifiedType=" + reifiedType + " resultAt=" + resultAt);
        EspressoLanguage language = getLanguage();
        CompilerAsserts.partialEvaluationConstant(arrayElementType);
        switch (arrayElementType) {
            case TypeHints.BYTE:
                byte newByte = (byte) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayByte(language, newByte, index, array);
                break;
            case TypeHints.CHAR:
                char newChar = (char) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayChar(language, newChar, index, array);
                break;
            case TypeHints.DOUBLE:
                double newDouble = EspressoFrame.popDouble(frame, top - 1);
                getContext().getInterpreterToVM().setArrayDouble(language, newDouble, index, array);
                break;
            case TypeHints.FLOAT:
                float newFloat = EspressoFrame.popFloat(frame, top - 1);
                getContext().getInterpreterToVM().setArrayFloat(language, newFloat, index, array);
                break;
            case TypeHints.INT:
                int newInt = EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayInt(language, newInt, index, array);
                break;
            case TypeHints.LONG:
                long newLong = EspressoFrame.popLong(frame, top - 1);
                getContext().getInterpreterToVM().setArrayLong(language, newLong, index, array);
                break;
            case TypeHints.SHORT:
                short newShort = (short) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayShort(language, newShort, index, array);
                break;
            case TypeHints.BOOLEAN:
                byte newBool = (byte) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayByte(language, newBool, index, array);
                break;
            default:
                StaticObject newElement = nullCheck(EspressoFrame.popObject(frame, top - 1));
                getContext().getInterpreterToVM().setArrayObject(language, newElement, index, array);
                break;
        }
        return stackEffect;
    }
}