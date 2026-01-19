package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives;

public class TypeHints {
    public static final byte BYTE = 'B';
    public static final byte CHAR = 'C';
    public static final byte DOUBLE = 'D';
    public static final byte FLOAT = 'F';
    public static final byte INT = 'I';
    public static final byte LONG = 'J';
    public static final byte SHORT = 'S';
    public static final byte BOOLEAN = 'Z';
    public static final byte REFERENCE = 'L';
    public static final byte CLASS_TYPE_PARAM = 'K';
    public static final byte METHOD_TYPE_PARAM = 'M';

    public static final byte[] LIST_PRIMITIVES = new byte[]{BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, BOOLEAN};
    public static final byte[] LIST_AVAILABLE = new byte[]{BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, BOOLEAN, REFERENCE};

    public static class TypeB {
        public static final byte EMPTY_KIND = 0;
        public static final byte CLASS_TYPE_PARAM = 'K';
        public static final byte METHOD_TYPE_PARAM = 'M';
        public static final byte ARR_CLASS_TYPE_PARAM = 'k';
        public static final byte ARR_METHOD_TYPE_PARAM = 'm';

        @CompilerDirectives.CompilationFinal
        private final byte kind;
        @CompilerDirectives.CompilationFinal
        private final int index;

        public TypeB(byte kind, int index) {
            this.kind = kind;
            this.index = index;
        }

        public byte getKind() { return kind; }
        public int getIndex() { return index; }
        public boolean isGenericArray() { return kind == ARR_CLASS_TYPE_PARAM || kind == ARR_METHOD_TYPE_PARAM; }
        
        public byte resolve(byte[] methodTypeParams) {
            if (this.kind == METHOD_TYPE_PARAM) {
                return methodTypeParams[this.index];
            } else if (this.kind == CLASS_TYPE_PARAM) {
                return TypeHints.REFERENCE; // TODO: class type params
            } else {
                for (byte primitiveChar : LIST_PRIMITIVES) {
                    if (this.kind == primitiveChar) {
                        return primitiveChar;
                    }
                }
                return TypeHints.REFERENCE;
            }
        }
        
        public byte resolveArrayElement(byte[] methodTypeParams) {
            assert kind == ARR_CLASS_TYPE_PARAM || kind == ARR_METHOD_TYPE_PARAM;
            if (kind == ARR_METHOD_TYPE_PARAM) {
                return methodTypeParams[index];
            } else {
                return TypeHints.REFERENCE; // TODO: class type params
            }
        }

        @Override
        public boolean equals(Object obj){
            if (!(obj instanceof TypeB)) {
                return false;
            }
            TypeB other = (TypeB) obj;
            return this.kind == other.kind && this.index == other.index;
        }

        @Override
        public int hashCode(){
            return Objects.hash(kind, index);
        }

        @Override
        public String toString() {
            return "TypeB{kind=" + (char) kind +
                        ", index=" + index +
                        '}';
        }
    }
}
