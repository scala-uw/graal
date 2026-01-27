package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public class TypeAnalysisResult {
    @CompilerDirectives.CompilationFinal(dimensions = 1)
    public final TypeHints.TypeB[] operands;
    @CompilerDirectives.CompilationFinal
    public final TypeHints.TypeB invokeReturnType;
    @CompilerDirectives.CompilationFinal
    public final boolean isInvoke;
    @CompilerDirectives.CompilationFinal
    public final boolean ignoreInvoke;

    TypeAnalysisResult(TypeHints.TypeB[] content) {
        this.operands = content;
        this.invokeReturnType = null;
        this.isInvoke = false;
        this.ignoreInvoke = false;
    }

    public TypeAnalysisResult(TypeHints.TypeB[] content, TypeHints.TypeB invokeReturnType, boolean isInvoke, boolean ignoreInvoke) {
        this.operands = content;
        this.invokeReturnType = invokeReturnType;
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
