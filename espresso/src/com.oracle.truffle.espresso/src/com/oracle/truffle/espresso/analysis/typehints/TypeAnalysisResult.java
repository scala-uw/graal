package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public class TypeAnalysisResult {
    @CompilerDirectives.CompilationFinal(dimensions = 1)
    public final TypeHints.TypeB[] operands;
    @CompilerDirectives.CompilationFinal
    public final TypeHints.TypeB invokeReturnType;
    @CompilerDirectives.CompilationFinal
    public final boolean ignoreInvoke;
    @CompilerDirectives.CompilationFinal
    public final int stackTopAdjustment; // adjust stack top for an ignored call

    TypeAnalysisResult(TypeHints.TypeB[] content) {
        this.operands = content;
        this.invokeReturnType = null;
        this.ignoreInvoke = false;
        this.stackTopAdjustment = 0;
    }

    public TypeAnalysisResult(TypeHints.TypeB[] content, TypeHints.TypeB invokeReturnType, boolean ignoreInvoke) {
        this.operands = content;
        this.invokeReturnType = invokeReturnType;
        this.ignoreInvoke = ignoreInvoke;
        this.stackTopAdjustment = 0;
    }

    public TypeAnalysisResult(TypeHints.TypeB[] operands, int stackTopAdjustment) { // ignored call
        this.operands = operands;
        this.invokeReturnType = null;
        this.ignoreInvoke = true;
        this.stackTopAdjustment = stackTopAdjustment;
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
