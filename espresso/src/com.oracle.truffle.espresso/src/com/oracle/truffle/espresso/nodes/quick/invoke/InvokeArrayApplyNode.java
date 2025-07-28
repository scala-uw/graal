package com.oracle.truffle.espresso.nodes.quick.invoke;

import java.util.Arrays;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.analysis.typehints.TypeAnalysisResult;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.BytecodeNode;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;

public final class InvokeArrayApplyNode extends InvokeScalaNode {

    // array_apply takes two inputs: the array and the index.
    // The first operand is the array, which is expected to be of a specific type if this quick node is used

    private final TypeHints.TypeB typeHint;
    private final int frameStartReifiedTypes;

    public InvokeArrayApplyNode(Method method, int top, int callerBCI, TypeAnalysisResult typeAnalysis, int frameStartReifiedTypes) {
        // array_apply is not annotated with attributes
        super(method, top, callerBCI);
        TypeHints.TypeB[] operands = typeAnalysis.getOperandsTypes();
        assert operands.length == 2 : "Expected two operands for InvokeArrayApplyNode, got " + Arrays.toString(operands);
        assert operands[0] != null && operands[1] == null : "Expected first operand to be non-null and second to be null, got " + Arrays.toString(operands);
        assert !method.isStatic();
        typeHint = operands[0];
        this.frameStartReifiedTypes = frameStartReifiedTypes;
        if (BytecodeNode.DEBUG) System.out.println("InvokeArrayApplyNode: typeHint=" + typeHint);
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        StaticObject array = nullCheck(EspressoFrame.popObject(frame, top - 2));
        int index = EspressoFrame.popInt(frame, top - 1);
        byte reifiedType = TypeHints.TypeB.resolveArrayElementReifiedType(typeHint, frame, frameStartReifiedTypes);
        if (BytecodeNode.DEBUG) System.out.println("InvokeArrayApplyNode: array=" + array.toVerboseString() + ", index=" + index + ", reifiedType=" + reifiedType + " resultAt=" + resultAt);
        switch (reifiedType) {
            case TypeHints.TypeA.BYTE:
                byte b = getContext().getInterpreterToVM().getArrayByte(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, b);
                break;
            case TypeHints.TypeA.CHAR:
                char c = getContext().getInterpreterToVM().getArrayChar(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, c);
                break;
            case TypeHints.TypeA.DOUBLE:
                double d = getContext().getInterpreterToVM().getArrayDouble(getLanguage(), index, array);
                EspressoFrame.putReifiedDouble(frame, resultAt, d);
                break;
            case TypeHints.TypeA.FLOAT:
                float f = getContext().getInterpreterToVM().getArrayFloat(getLanguage(), index, array);
                EspressoFrame.putFloat(frame, resultAt, f);
                break;
            case TypeHints.TypeA.INT:
                int i = getContext().getInterpreterToVM().getArrayInt(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, i);
                break;
            case TypeHints.TypeA.LONG:
                long l = getContext().getInterpreterToVM().getArrayLong(getLanguage(), index, array);
                EspressoFrame.putReifiedLong(frame, resultAt, l);
                break;
            case TypeHints.TypeA.SHORT:
                short s = getContext().getInterpreterToVM().getArrayShort(getLanguage(), index, array);
                EspressoFrame.putInt(frame, resultAt, s);
                break;
            case TypeHints.TypeA.BOOLEAN:
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
