package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class MethodTypeParameterCountAttribute extends Attribute {

    public static final Symbol<Name> NAME = ParserNames.MethodTypeParameterCount;

    private final int count;

    public MethodTypeParameterCountAttribute(Symbol<Name> name, int count) {
        super(name, null);
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "MethodTypeParameterCountAttribute{" +
                        "count=" + count +
                        '}';
    }
}
