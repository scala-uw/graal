package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public class TypeAnalysisResult {
    final TypeHints.TypeB[] operands;

    TypeAnalysisResult(TypeHints.TypeB[] content) {
        this.operands = content;
    }

    public TypeHints.TypeB[] getOperandsTypes() {
        return operands;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TypeAnalysisResult{");
        sb.append("operands=[");
        for (TypeHints.TypeB operand : operands) {
            sb.append(operand != null ? operand.toString() : "null").append(", ");
        }
        sb.append("]}");
        return sb.toString();
    }
}
