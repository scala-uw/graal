package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashSet;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class ExtraBoxUnboxAttribute extends Attribute {

    public static final Symbol<Name> NAME = ParserNames.ExtraBoxUnbox;

    private final AbstractSet<Integer> bcOffsets;

    public ExtraBoxUnboxAttribute(Symbol<Name> name, int[] bcOffsets) {
        super(name, null);
        this.bcOffsets = new HashSet<>();
        for (int v : bcOffsets) {
            this.bcOffsets.add(v);
        }
    }

    public AbstractSet<Integer> getBCOffsets() {
        return this.bcOffsets;
    }

    @Override
    public String toString() {
        return "InstructionTypeArgumentsAttribute{" +
                        "entries=" + this.bcOffsets.toString() +
                        '}';
    }

}
