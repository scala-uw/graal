package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class InvokeReturnTypeAttribute extends Attribute {
    public static final Symbol<Name> NAME = ParserNames.InvokeReturnType;

    public record Entry(int bytecodeOffset, TypeHints.TypeB returnType) {}

    private final Entry[] entries;

    public Entry[] getEntries() {
        return entries;
    }

    public InvokeReturnTypeAttribute(Symbol<Name> name, Entry[] entries) {
        super(name, null);
        this.entries = entries;
    }

    @Override
    public String toString() {
        return "InvokeReturnTypeAttribute{" +
                        "entries=" + Arrays.toString(entries) +
                        '}';
    }

}
