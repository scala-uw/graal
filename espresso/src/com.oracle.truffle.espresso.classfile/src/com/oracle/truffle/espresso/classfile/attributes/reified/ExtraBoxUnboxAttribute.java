package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class ExtraBoxUnboxAttribute extends Attribute {

    public static final Symbol<Name> NAME = ParserNames.ExtraBoxUnbox;

    private final int[] bcOffsets;

    public ExtraBoxUnboxAttribute(Symbol<Name> name, int[] bcOffsets) {
        super(name, null);
        this.bcOffsets = bcOffsets;
    }

    public int[] getBCOffsets() {
        return bcOffsets;
    }

    @Override
    public String toString() {
        return "InstructionTypeArgumentsAttribute{" +
                        "entries=" + Arrays.toString(bcOffsets) +
                        '}';
    }

}
