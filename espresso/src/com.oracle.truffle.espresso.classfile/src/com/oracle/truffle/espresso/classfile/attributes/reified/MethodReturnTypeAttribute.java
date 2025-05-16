package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints.TypeB;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class MethodReturnTypeAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.MethodReturnType;
    public static final MethodReturnTypeAttribute EMPTY = new MethodReturnTypeAttribute(NAME, TypeHints.TypeB.NO_HINT);

    private final TypeB returnType;
    public MethodReturnTypeAttribute(Symbol<Name> name, TypeB returnType) {
        super(name, null);
        this.returnType = returnType;
    }
    public TypeB getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "MethodReturnTypeAttribute{" +
                        "returnType=" + returnType +
                        '}';
    }
}
