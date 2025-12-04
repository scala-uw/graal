package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;
import com.oracle.truffle.espresso.classfile.constantpool.FieldRefConstant;


public class ClassGenericFieldListAttribute extends Attribute{
    public static final Symbol<Name> NAME = ParserNames.ClassGenericFieldList;

    public record Entry(FieldRefConstant.Indexes field, int classTypeParamIndex) {}

    private final Entry[] entries;

    public ClassGenericFieldListAttribute(Symbol<Name> name, Entry[] entries) {
        super(name, null);
        this.entries = entries;
    }

    public Entry[] getFieldTypes() {
        return entries;
    }

    @Override
    public String toString() {
        return "FieldTypeAttribute{" +
                        "..." +
                        '}';
    }
}
