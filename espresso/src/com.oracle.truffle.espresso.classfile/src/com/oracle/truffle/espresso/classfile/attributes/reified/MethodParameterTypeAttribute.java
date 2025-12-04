package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class MethodParameterTypeAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.MethodParameterType;

    private final TypeHints.TypeB[] parameterTypes;
    public TypeHints.TypeB[] getParameterTypes() {
        return parameterTypes;
    }
    public MethodParameterTypeAttribute(Symbol<Name> name, TypeHints.TypeB[] parameterTypes) {
        super(name, null);
        this.parameterTypes = parameterTypes;
    }

    @Override
    public String toString() {
        return "MethodParameterTypeAttribute{" +
                        ", parameterTypes=" + Arrays.toString(parameterTypes) +
                        '}';
    }
}
