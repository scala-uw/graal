package com.oracle.truffle.espresso.nodes.quick.invoke;

import java.util.Arrays;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.analysis.typehints.TypeAnalysisResult;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.meta.EspressoError;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;

public final class InvokeArrayApplyNode extends InvokeScalaNode {

    // array_apply takes two inputs: the array and the index.
    // The first operand is the array, which is expected to be of a specific type if this quick node is used

    private final TypeHints.TypeB typeHint;

    public InvokeArrayApplyNode(Method method, int top, int callerBCI, TypeAnalysisResult typeAnalysis) {
        // array_apply is not annotated with attributes
        super(method, top, callerBCI, 1);
        TypeHints.TypeB[] operands = typeAnalysis.getOperandsTypes();
        assert operands.length == 2 : "Expected two operands for InvokeArrayApplyNode, got " + Arrays.toString(operands);
        assert operands[0] != null && operands[1] == null : "Expected first operand to be non-null and second to be null, got " + Arrays.toString(operands);
        assert !method.isStatic();
        typeHint = operands[0];
        System.out.println("InvokeArrayApplyNode: typeHint=" + typeHint);
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        StaticObject array = null;
        int index = -1;
        byte reifiedType = TypeHints.TypeA.REFERENCE; // Default to REFERENCE if not specified
        byte kind = typeHint.getKind();
        switch (kind) {
            case TypeHints.TypeB.ARR_CLASS_TYPE_PARAM:
                //TODO
                break;
            case TypeHints.TypeB.ARR_METHOD_TYPE_PARAM:
                //top 3 should be top-3: array, top-2: index, top-1: reified type
                array = nullCheck(EspressoFrame.popObject(frame, top - 3));
                index = EspressoFrame.popInt(frame, top - 2);
                reifiedType = (byte) EspressoFrame.popInt(frame, top - 1);
                break;
            default:
                throw EspressoError.shouldNotReachHere("Unexpected kind for InvokeArrayApplyNode: " + kind);
        }
        System.out.println("InvokeArrayApplyNode: array=" + array.toVerboseString() + ", index=" + index + ", reifiedType=" + reifiedType + " resultAt=" + resultAt);
        switch (reifiedType) {
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
        return stackEffect; //?
    }
  
}
