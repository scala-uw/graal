package com.oracle.truffle.espresso.analysis.typehints;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public class TypeAnalysisResult {
    //this class represents the result of stack and locals at one bci
    static class TypeInfo {
        private final TypeHints.TypeB type;

        TypeInfo(TypeHints.TypeB type) {
            this.type = type;
        }

        TypeInfo copy() {
            return new TypeInfo(type);
        }

        public TypeHints.TypeB getType() {
            return type;
        }

        @Override
        public String toString() {
            return "TypeInfo{" + "type=" + type + '}';
        }
    }

    final TypeInfo[] locals;
    final TypeInfo[] stack;
    final int stackTop; 
    //stackTop points to the next position in the stack
    // init to 0

    public TypeAnalysisResult(int maxLocals, int maxStack) {
        this.locals = new TypeInfo[maxLocals];
        this.stack = new TypeInfo[maxStack];
        this.stackTop = 0;
    }

    public TypeAnalysisResult(TypeAnalysisResult y) {
        this.locals = new TypeInfo[y.locals.length];
        this.stack = new TypeInfo[y.stack.length];
        for (int i = 0; i < y.locals.length; i++) {
            this.locals[i] = y.locals[i];
        }
        for (int i = 0; i < y.stack.length; i++) {
            this.stack[i] = y.stack[i];
        }
        this.stackTop = y.stackTop;
    }

    public TypeAnalysisResult copy() {
        return new TypeAnalysisResult(this);
    }

    public static TypeAnalysisResult merge(List<TypeAnalysisResult> states, int maxLocals, int maxStack){
        TypeAnalysisResult merged = new TypeAnalysisResult(maxLocals, maxStack);
        assert states.size() == 1 : "currently only one state is supported";
        return states.get(0).copy();
        //return merged;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TypeAnalysisResult{");
        sb.append("locals=[");
        for (TypeInfo local : locals) {
            sb.append(local != null ? local.getType().toString() : "null").append(", ");
        }
        sb.append("], stack=[");
        for (int i = 0; i < stackTop; i++) {
            sb.append(stack[i] != null ? stack[i].getType().toString() : "null").append(", ");
        }
        sb.append("]}\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof TypeAnalysisResult)){
            return false;
        }
        TypeAnalysisResult other = (TypeAnalysisResult) obj;
        return Arrays.equals(this.locals, other.locals) &&
               Arrays.equals(this.stack, other.stack) &&
                this.stackTop == other.stackTop;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(locals), Arrays.hashCode(stack), stackTop);
    }
}
