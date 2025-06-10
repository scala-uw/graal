package com.oracle.truffle.espresso.analysis.typehints;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public class TypeAnalysisState {
    TypeHints.TypeB[] locals;
    TypeHints.TypeB[] stack;
    int stackTop; 
    //stackTop points to the next position in the stack
    // init to 0

    public TypeAnalysisState(int maxLocals, int maxStack) {
        this.locals = new TypeHints.TypeB[maxLocals];
        this.stack = new TypeHints.TypeB[maxStack];
        this.stackTop = 0;
    }

    public TypeAnalysisState(TypeAnalysisState y) {
        this.locals = new TypeHints.TypeB[y.locals.length];
        this.stack = new TypeHints.TypeB[y.stack.length];
        for (int i = 0; i < y.locals.length; i++) {
            this.locals[i] = y.locals[i];
        }
        for (int i = 0; i < y.stack.length; i++) {
            this.stack[i] = y.stack[i];
        }
        this.stackTop = y.stackTop;
    }

    public TypeAnalysisState copy() {
        return new TypeAnalysisState(this);
    }

    public static TypeAnalysisState merge(List<TypeAnalysisState> states, int maxLocals, int maxStack){
        TypeAnalysisState ret = states.get(0).copy();
        for (TypeAnalysisState v : states) {
            assert v.stackTop == ret.stackTop : "stackTop unmatch when merging control flows";
            assert Arrays.equals(ret.locals, v.locals) : "locals unmatch when merging control flows";
            assert Arrays.equals(ret.stack, v.stack) : "stack unmatch when merging control flows";
        }
        return ret;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TypeAnalysisResult{");
        sb.append("locals=[");
        for (TypeHints.TypeB local : locals) {
            sb.append(local != null ? local.toString() : "null").append(", ");
        }
        sb.append("], stack=[");
        for (int i = 0; i < stackTop; i++) {
            sb.append(stack[i] != null ? stack[i].toString() : "null").append(", ");
        }
        sb.append("]}\n");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof TypeAnalysisState)){
            return false;
        }
        TypeAnalysisState other = (TypeAnalysisState) obj;
        return Arrays.equals(this.locals, other.locals) &&
               Arrays.equals(this.stack, other.stack) &&
                this.stackTop == other.stackTop;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(locals), Arrays.hashCode(stack), stackTop);
    }
}
