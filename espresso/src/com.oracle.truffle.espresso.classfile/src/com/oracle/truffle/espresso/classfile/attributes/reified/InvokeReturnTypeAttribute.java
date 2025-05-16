package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Arrays;

import com.oracle.truffle.espresso.classfile.attributes.Attribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints.TypeA;
import com.oracle.truffle.espresso.classfile.descriptors.Name;
import com.oracle.truffle.espresso.classfile.descriptors.ParserSymbols.ParserNames;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;

public class InvokeReturnTypeAttribute extends Attribute {
    public static final Symbol<Name> NAME = ParserNames.InvokeReturnType;

    public static final InvokeReturnTypeAttribute EMPTY = new InvokeReturnTypeAttribute(NAME, Entry.EMPTY_ARRAY);

    public static final class Entry {
        public static final Entry[] EMPTY_ARRAY = new Entry[0];
        private final int bytecodeOffset;
        private final TypeA returnType;
        public Entry(int bytecodeOffset, TypeA returnType) {
            this.bytecodeOffset = bytecodeOffset;
            this.returnType = returnType;
        }
        public int getBytecodeOffset() {
            return bytecodeOffset;
        }
        public TypeA getReturnType() {
            return returnType;
        }
        @Override
        public String toString() {
            return "Entry{" +
                            "bytecodeOffset=" + bytecodeOffset +
                            ", returnType=" + returnType +
                            '}';
        }
    }

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
