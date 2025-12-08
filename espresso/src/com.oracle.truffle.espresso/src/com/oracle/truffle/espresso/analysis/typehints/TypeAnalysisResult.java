package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public class TypeAnalysisResult {
    @CompilerDirectives.CompilationFinal(dimensions = 1)
    public final TypeHints.TypeB[] operands;
    @CompilerDirectives.CompilationFinal
    public final boolean isInvoke;
    @CompilerDirectives.CompilationFinal
    public final boolean ignoreInvoke;

    TypeAnalysisResult(TypeHints.TypeB[] content) {
        this.operands = content;
        this.isInvoke = false;
        this.ignoreInvoke = false;
    }

    public TypeAnalysisResult(TypeHints.TypeB[] content, boolean isInvoke, boolean ignoreInvoke) {
        this.operands = content;
        this.isInvoke = isInvoke;
        this.ignoreInvoke = ignoreInvoke;
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
