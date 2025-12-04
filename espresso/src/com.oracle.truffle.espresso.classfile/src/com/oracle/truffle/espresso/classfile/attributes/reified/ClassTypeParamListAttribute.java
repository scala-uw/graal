package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.constantpool.FieldRefConstant;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class ClassTypeParamListAttribute extends Attribute {
    public static final Symbol<Name> NAME = ParserNames.ClassTypeParamList;

    private final FieldRefConstant.Indexes[] typeParams;

    public FieldRefConstant.Indexes[] gettypeParams() {
        return typeParams;
    }

    public ClassTypeParamListAttribute(Symbol<Name> name, FieldRefConstant.Indexes[] typeParams) {
        super(name, null);
        this.typeParams = typeParams;
    }

    @Override
    public String toString() {
        return "ClassTypeParameterCountAttribute{" +
                        "..." +
                        '}';
    }
    
}
