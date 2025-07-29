package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;


public class FieldTypeAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.FieldType;

    private final TypeHints.TypeB fieldType;

    public FieldTypeAttribute(Symbol<Name> name, TypeHints.TypeB fieldType) {
        super(name, null);
        this.fieldType = fieldType;
    }

    public TypeHints.TypeB getFieldType() {
        return fieldType;
    }

    @Override
    public String toString() {
        return "FieldTypeAttribute{" +
                        "fieldType=" + fieldType +
                        '}';
    }
}
