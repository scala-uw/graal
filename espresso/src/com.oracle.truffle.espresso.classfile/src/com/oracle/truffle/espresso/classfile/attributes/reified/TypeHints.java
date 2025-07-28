package com.oracle.truffle.espresso.classfile.attributes.reified;

import java.util.Objects;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.frame.VirtualFrame;

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

        private final byte kind;
        private final int index;

        public TypeA(byte kind, int index) {
            this.kind = kind;
            this.index = index;
        }

        public byte getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof TypeA)) {
                return false;
            }
            TypeA other = (TypeA) obj;
            return this.kind == other.kind && this.index == other.index;
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, index);
        }

        @Override
        public String toString() {
            return "TypeA{" +
                            "kind=" + (char) kind +
                            ", index=" + index +
                            '}';            
        }
    }

    public static class TypeB {
        // public static final byte IS_ARRAY = 0;
        // public static final byte NOT_ARRAY = 1;

        public static final byte EMPTY_KIND = 0;
        public static final byte CLASS_TYPE_PARAM = 'K';
        public static final byte METHOD_TYPE_PARAM = 'M';
        public static final byte ARR_CLASS_TYPE_PARAM = 'k';
        public static final byte ARR_METHOD_TYPE_PARAM = 'm';
        // public static final byte REFERENCE = 'L';

        // private final byte isArray;
        private final byte kind;
        private final int index;

        // public TypeB(byte isArray, byte kind, int index) {
        //     this.isArray = isArray;
        //     this.kind = kind;
        //     this.index = index;
        // }
        public TypeB(byte kind, int index) {
            assert kind == CLASS_TYPE_PARAM || kind == METHOD_TYPE_PARAM || kind == ARR_CLASS_TYPE_PARAM || kind == ARR_METHOD_TYPE_PARAM;
            this.kind = kind;
            this.index = index;
        }

        public byte getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }

        public static byte resolveReifiedType(TypeB typeBInstance, VirtualFrame frame, int startMethodTypeParams) {
            if (typeBInstance == null) {
                return TypeA.REFERENCE;
            }
            CompilerAsserts.partialEvaluationConstant(typeBInstance.kind);
            CompilerAsserts.partialEvaluationConstant(typeBInstance.index);
            if (typeBInstance.kind == METHOD_TYPE_PARAM) {
                return (byte) frame.getIntStatic(startMethodTypeParams + typeBInstance.index);
            } else if (typeBInstance.kind == CLASS_TYPE_PARAM) {
                return TypeA.REFERENCE; // TODO: class type params
            } else {
                return TypeA.REFERENCE;
            }
        }

        public static byte resolveArrayElementReifiedType(TypeB typeBInstance, VirtualFrame frame, int startMethodTypeParams) {
            assert typeBInstance != null;
            CompilerAsserts.partialEvaluationConstant(typeBInstance.kind);
            CompilerAsserts.partialEvaluationConstant(typeBInstance.index);
            byte kind = typeBInstance.kind;
            assert kind == ARR_CLASS_TYPE_PARAM || kind == ARR_METHOD_TYPE_PARAM;
            if (kind == ARR_METHOD_TYPE_PARAM) {
                return (byte) frame.getIntStatic(startMethodTypeParams + typeBInstance.index);
            } else {
                // TODO: class type params
                return TypeA.REFERENCE;
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
            return "TypeB{" +
                            "kind=" + (char) kind +
                            ", index=" + index +
                            '}';
        }
    }

    // public static interface TypeA{}
    // public static interface TypeB{}

    // public static class NoHint implements TypeB {
    //     public NoHint() {}
    // }
    
    // public static enum PrimitiveType implements TypeA {
    //     BYTE('B'),
    //     CHAR('C'),
    //     DOUBLE('D'),
    //     FLOAT('F'),
    //     INT('I'),
    //     LONG('J'),
    //     SHORT('S'),
    //     BOOLEAN('Z'),
    //     REFERENCE('L');

    //     private final char descriptor;

    //     PrimitiveType(char descriptor) {
    //         this.descriptor = descriptor;
    //     }
    // }
    // // representing K< number >
    // public static class ClassTypeParameter implements TypeA, TypeB {
    //     private final int index;

    //     public ClassTypeParameter(int index) {
    //         this.index = index;
    //     }
    //     public int getIndex() {
    //         return index;
    //     }
    // }
    // // representing M< number >
    // public static class MethodTypeParameter implements TypeA, TypeB {
    //     private final int index;

    //     public MethodTypeParameter(int index) {
    //         this.index = index;
    //     }
    //     public int getIndex() {
    //         return index;
    //     }
    // }

    // public static class ArrayTypeParameter implements TypeB {
    //     private final TypeB inner;
        
    //     public ArrayTypeParameter(TypeB inner) {
    //         this.inner = inner;
    //     }
    //     public TypeB getInner() {
    //         return inner;
    //     }
    // }
}
