package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints.TypeA;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class InstructionTypeArgumentsAttribute extends Attribute {

    public static final Symbol<Name> NAME = ParserNames.InstructionTypeArguments;

    public static final InstructionTypeArgumentsAttribute EMPTY = new InstructionTypeArgumentsAttribute(NAME, Entry.EMPTY_ARRAY);

    public static final class Entry {
        public static final Entry[] EMPTY_ARRAY = new Entry[0];
        private final int bytecodeOffset;
        private final TypeA[] typeArguments;
        public Entry(int bytecodeOffset, TypeA[] typeArguments) {
            this.bytecodeOffset = bytecodeOffset;
            this.typeArguments = typeArguments;
        }
        public int getBytecodeOffset() {
            return bytecodeOffset;
        }
        public TypeA[] getTypeArguments() {
            return typeArguments;
        }
        @Override
        public String toString() {
            return "Entry{" +
                            "bytecodeOffset=" + bytecodeOffset +
                            ", typeArguments=" + Arrays.toString(typeArguments) +
                            '}';
        }
    }

    private final Entry[] entries;

    public InstructionTypeArgumentsAttribute(Symbol<Name> name, Entry[] entries) {
        super(name, null);
        this.entries = entries;
    }

    public Entry[] getEntries() {
        return entries;
    }

    @Override
    public String toString() {
        return "InstructionTypeArgumentsAttribute{" +
                        "entries=" + Arrays.toString(entries) +
                        '}';
    }

}
