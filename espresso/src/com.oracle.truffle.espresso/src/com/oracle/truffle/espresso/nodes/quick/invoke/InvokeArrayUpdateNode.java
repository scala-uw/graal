package com.oracle.truffle.espresso.nodes.quick.invoke;

import java.util.Arrays;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.EspressoLanguage;
import com.oracle.truffle.espresso.analysis.typehints.TypeAnalysisResult;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.meta.EspressoError;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;

public final class InvokeArrayUpdateNode extends InvokeScalaNode {

    // array_update takes three inputs: the array, the index, and the value to update.
    // The first operand is the array, which is expected to be of a specific type if this quick node is used.

    private final TypeHints.TypeB typeHint;

    public InvokeArrayUpdateNode(Method method, int top, int callerBCI, TypeAnalysisResult typeAnalysis) {
        // array_update is not annotated with attributes
        super(method, top, callerBCI, 1);
        TypeHints.TypeB[] operands = typeAnalysis.getOperandsTypes();
        assert operands.length == 3 : "Expected three operands for InvokeArrayUpdateNode, got " + Arrays.toString(operands);
        assert operands[0] != null && operands[1] == null && operands[2] != null : "Expected non-null, null, non-null, got " + Arrays.toString(operands);
        assert !method.isStatic();
        typeHint = operands[0];
        System.out.println("InvokeArrayUpdateNode: typeHint=" + typeHint);
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        // Object[] args = getArguments(frame);
        // System.out.println("InvokeArrayUpdateNode: args=" + Arrays.toString(args));
        // System.out.println("typeArgs cnt: " + typeArgCnt);
        StaticObject array = null;
        int index = -1;
        byte reifiedType = TypeHints.TypeA.REFERENCE; // Default to REFERENCE if not specified
        StaticObject newElement = null;
        byte kind = typeHint.getKind();
        switch (kind){
            case TypeHints.TypeB.ARR_CLASS_TYPE_PARAM:
                //TODO
                break;
            case TypeHints.TypeB.ARR_METHOD_TYPE_PARAM:
                //top 4 should be top-4: array, top-3: index, top-2: new elememnt, top-1: reified type
                array = nullCheck(EspressoFrame.popObject(frame, top - 4));
                index = EspressoFrame.popInt(frame, top - 3);
                newElement = EspressoFrame.popObject(frame, top - 2);
                reifiedType = (byte) EspressoFrame.popInt(frame, top - 1);
                break;
            default:
                throw EspressoError.shouldNotReachHere("Unexpected kind for InvokeArrayUpdateNode: " + kind);
        }
        System.out.println("InvokeArrayUpdateNode: array=" + array.toVerboseString() + ", index=" + index + ", newElement=" + newElement.toVerboseString() + ", reifiedType=" + reifiedType + " resultAt=" + resultAt);
        EspressoLanguage language = getLanguage();
        switch (reifiedType) {
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