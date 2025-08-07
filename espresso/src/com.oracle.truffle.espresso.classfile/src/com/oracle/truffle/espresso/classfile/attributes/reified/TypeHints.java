package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives;

public class TypeHints {
    public static class TypeA {
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
    
        public static final byte[] LIST_AVAILABLE = new byte[]{BYTE, CHAR, DOUBLE, FLOAT, INT, LONG, SHORT, BOOLEAN, REFERENCE};
        public static int findIndex(byte reified) {
            switch (reified) {
                case BYTE: return 0;
                case CHAR: return 1;
                case DOUBLE: return 2;
                case FLOAT: return 3;
                case INT: return 4;
                case LONG: return 5;
                case SHORT: return 6;
                case BOOLEAN: return 7;
                default:
                    assert reified == REFERENCE; 
                    return 8;
            }
        }
        
        public static final TypeA TYPEA_BYTE = 
            new TypeA(BYTE, 0);
        public static final TypeA TYPEA_CHAR =
            new TypeA(CHAR, 0);
        public static final TypeA TYPEA_DOUBLE =
            new TypeA(DOUBLE, 0);
        public static final TypeA TYPEA_FLOAT =
            new TypeA(FLOAT, 0);
        public static final TypeA TYPEA_INT =
            new TypeA(INT, 0);
        public static final TypeA TYPEA_LONG =
            new TypeA(LONG, 0);
        public static final TypeA TYPEA_SHORT =
            new TypeA(SHORT, 0);
        public static final TypeA TYPEA_BOOLEAN =
            new TypeA(BOOLEAN, 0);
        public static final TypeA TYPEA_REFERENCE =
            new TypeA(REFERENCE, 0);

        @CompilerDirectives.CompilationFinal
        private final byte kind;
        @CompilerDirectives.CompilationFinal
        private final int outerClassIndex;
        @CompilerDirectives.CompilationFinal
        private final int index;

        public TypeA(byte kind, int index) {
            assert kind == BYTE || kind == CHAR || 
                   kind == DOUBLE || kind == FLOAT || 
                   kind == INT || kind == LONG || 
                   kind == SHORT || kind == BOOLEAN || kind == REFERENCE;
            this.kind = kind;
            this.index = index;
            this.outerClassIndex = 0;
        }

        public TypeA(byte kind, int outerClassIndex, int index){
            assert kind == CLASS_TYPE_PARAM || kind == METHOD_TYPE_PARAM;
            this.kind = kind;
            this.outerClassIndex = outerClassIndex;
            this.index = index;
        }

        public byte getKind() { return kind; }
        public int getOuterClassIndex() { return outerClassIndex; }
        public int getIndex() { return index; }

        public byte resolve(byte[] methodTypeParams) {
            if (this.kind == METHOD_TYPE_PARAM) {
                return methodTypeParams[this.index];
            } else if (this.kind == CLASS_TYPE_PARAM) {
                return REFERENCE; // TODO: class type params
            } else return this.kind;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypeA)) {
                return false;
            }
            TypeA other = (TypeA) obj;
            return this.kind == other.kind && this.index == other.index
                    && this.outerClassIndex == other.outerClassIndex;
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, outerClassIndex, index);
        }

        @Override
        public String toString() {
            return "TypeA{kind=" + (char) kind +
                        ", outerClassIndex=" + outerClassIndex +
                        ", index=" + index +
                        '}';            
        }
    }

    public static class TypeB {
        public static final byte EMPTY_KIND = 0;
        public static final byte CLASS_TYPE_PARAM = 'K';
        public static final byte METHOD_TYPE_PARAM = 'M';
        public static final byte ARR_CLASS_TYPE_PARAM = 'k';
        public static final byte ARR_METHOD_TYPE_PARAM = 'm';

        @CompilerDirectives.CompilationFinal
        private final byte kind;
        @CompilerDirectives.CompilationFinal
        private final int outerClassIndex;
        @CompilerDirectives.CompilationFinal
        private final int index;

        public TypeB(byte kind, int index, int outerClassIndex) {
            assert kind == CLASS_TYPE_PARAM || kind == METHOD_TYPE_PARAM || 
                   kind == ARR_CLASS_TYPE_PARAM || kind == ARR_METHOD_TYPE_PARAM;
            this.kind = kind;
            this.outerClassIndex = outerClassIndex;
            this.index = index;
        }

        public byte getKind() { return kind; }
        public int getOuterClassIndex() { return outerClassIndex; }
        public int getIndex() { return index; }
        public boolean isGenericArray() { return kind == ARR_CLASS_TYPE_PARAM || kind == ARR_METHOD_TYPE_PARAM; }
        
        public byte resolve(byte[] methodTypeParams) {
            if (this.kind == METHOD_TYPE_PARAM) {
                return methodTypeParams[this.index];
            } else if (this.kind == CLASS_TYPE_PARAM) {
                return TypeA.REFERENCE; // TODO: class type params
            } else {
                return TypeA.REFERENCE;
            }
        }
        
        public byte resolveArrayElement(byte[] methodTypeParams) {
            assert kind == ARR_CLASS_TYPE_PARAM || kind == ARR_METHOD_TYPE_PARAM;
            if (kind == ARR_METHOD_TYPE_PARAM) {
                return methodTypeParams[index];
            } else {
                return TypeA.REFERENCE; // TODO: class type params
            }
        }

        @Override
        public boolean equals(Object obj){
            if (!(obj instanceof TypeB)) {
                return false;
            }
            TypeB other = (TypeB) obj;
            return this.kind == other.kind && this.index == other.index 
                        && this.outerClassIndex == other.outerClassIndex;
        }

        @Override
        public int hashCode(){
            return Objects.hash(kind, outerClassIndex, index);
        }

        @Override
        public String toString() {
            return "TypeB{kind=" + (char) kind +
                        ", outerClassIndex=" + outerClassIndex +
                        ", index=" + index +
                        '}';
        }
    }
}
