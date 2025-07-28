package com.oracle.truffle.espresso.nodes.quick.invoke;

import java.util.Arrays;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.EspressoLanguage;
import com.oracle.truffle.espresso.analysis.typehints.TypeAnalysisResult;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;

public final class InvokeArrayUpdateNode extends InvokeScalaNode {

    // array_update takes three inputs: the array, the index, and the value to update.
    // The first operand is the array, which is expected to be of a specific type if this quick node is used.

    private final TypeHints.TypeB typeHint;
    private final int frameStartReifiedTypes;

    public InvokeArrayUpdateNode(Method method, int top, int callerBCI, TypeAnalysisResult typeAnalysis, int frameStartReifiedTypes) {
        // array_update is not annotated with attributes
        super(method, top, callerBCI, 1);
        TypeHints.TypeB[] operands = typeAnalysis.getOperandsTypes();
        assert operands.length == 3 : "Expected three operands for InvokeArrayUpdateNode, got " + Arrays.toString(operands);
        assert operands[0] != null && operands[1] == null && operands[2] != null : "Expected non-null, null, non-null, got " + Arrays.toString(operands);
        assert !method.isStatic();
        typeHint = operands[0];
        this.frameStartReifiedTypes = frameStartReifiedTypes;
        System.out.println("InvokeArrayUpdateNode: typeHint=" + typeHint);
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        StaticObject array = nullCheck(EspressoFrame.popObject(frame, top - 3));
        int index = EspressoFrame.popInt(frame, top - 2);
        byte reifiedType = TypeHints.TypeB.resolveArrayElementReifiedType(typeHint, frame, frameStartReifiedTypes);
        //System.out.println("InvokeArrayUpdateNode: array=" + array.toVerboseString() + ", index=" + index + ", newElement=" + newElement.toVerboseString() + ", reifiedType=" + reifiedType + " resultAt=" + resultAt);
        EspressoLanguage language = getLanguage();
        switch (reifiedType) {
            case TypeHints.TypeA.BYTE:
                byte newByte = (byte) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayByte(language, newByte, index, array);
                break;
            case TypeHints.TypeA.CHAR:
                char newChar = (char) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayChar(language, newChar, index, array);
                break;
            case TypeHints.TypeA.DOUBLE:
                double newDouble = EspressoFrame.popDouble(frame, top - 1);
                getContext().getInterpreterToVM().setArrayDouble(language, newDouble, index, array);
                break;
            case TypeHints.TypeA.FLOAT:
                float newFloat = EspressoFrame.popFloat(frame, top - 1);
                getContext().getInterpreterToVM().setArrayFloat(language, newFloat, index, array);
                break;
            case TypeHints.TypeA.INT:
                int newInt = EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayInt(language, newInt, index, array);
                break;
            case TypeHints.TypeA.LONG:
                long newLong = EspressoFrame.popLong(frame, top - 1);
                getContext().getInterpreterToVM().setArrayLong(language, newLong, index, array);
                break;
            case TypeHints.TypeA.SHORT:
                short newShort = (short) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayShort(language, newShort, index, array);
                break;
            case TypeHints.TypeA.BOOLEAN:
                byte newBool = (byte) EspressoFrame.popInt(frame, top - 1);
                getContext().getInterpreterToVM().setArrayByte(language, newBool, index, array);
                break;
            default:
                StaticObject newElement = EspressoFrame.popObject(frame, top - 1);
                getContext().getInterpreterToVM().setArrayObject(language, newElement, index, array);
                break;
        }
        return stackEffect;
    }
}