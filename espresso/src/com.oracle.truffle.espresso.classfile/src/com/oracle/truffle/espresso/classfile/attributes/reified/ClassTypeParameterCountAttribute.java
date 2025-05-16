package com.oracle.truffle.espresso.classfile.attributes.reified;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class ClassTypeParameterCountAttribute extends Attribute {
    public static final Symbol<Name> NAME = ParserNames.ClassTypeParameterCount;
    public static final ClassTypeParameterCountAttribute EMPTY = new ClassTypeParameterCountAttribute(NAME, 0);

    private final int count;

    public int getCount() {
        return count;
    }

    public ClassTypeParameterCountAttribute(Symbol<Name> name, int count) {
        super(name, null);
        this.count = count;
    }

    @Override
    public String toString() {
        return "ClassTypeParameterCountAttribute{" +
                        "count=" + count +
                        '}';
    }
    
}
