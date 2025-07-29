package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class MethodReturnTypeAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.MethodReturnType;

    private final TypeHints.TypeB returnType;
    public MethodReturnTypeAttribute(Symbol<Name> name, TypeHints.TypeB returnType) {
        super(name, null);
        this.returnType = returnType;
    }
    public TypeHints.TypeB getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "MethodReturnTypeAttribute{" +
                        "returnType=" + returnType +
                        '}';
    }
}
