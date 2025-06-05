package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints.TypeB;

public class TypeAnalysisResult {
    final TypeB[] operands;

    TypeAnalysisResult(TypeB[] content) {
        this.operands = content;
    }

    public TypeB[] getOperandsTypes() {
        return operands;
    }
}
