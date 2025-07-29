package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class MethodParameterTypeAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.MethodParameterType;
    public static final MethodParameterTypeAttribute EMPTY = new MethodParameterTypeAttribute(NAME, 0, new TypeHints.TypeB[0]);

    private final int parameterCount;
    private final TypeHints.TypeB[] parameterTypes;
    public int getParameterCount() {
        return parameterCount;
    }
    public TypeHints.TypeB[] getParameterTypes() {
        return parameterTypes;
    }
    public MethodParameterTypeAttribute(Symbol<Name> name, int parameterCount, TypeHints.TypeB[] parameterTypes) {
        super(name, null);
        this.parameterCount = parameterCount;
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        return "MethodParameterTypeAttribute{" +
                        "parameterCount=" + parameterCount +
                        ", parameterTypes=" + Arrays.toString(parameterTypes) +
                        '}';
    }
}
