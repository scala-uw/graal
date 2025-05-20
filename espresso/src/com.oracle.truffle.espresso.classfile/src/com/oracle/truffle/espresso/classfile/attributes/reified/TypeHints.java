package com.oracle.truffle.espresso.classfile.attributes.reified;

public class TypeHints {
    public static class TypeA {
        public static final int BYTE = 'B';
        public static final int CHAR = 'C';
        public static final int DOUBLE = 'D';
        public static final int FLOAT = 'F';
        public static final int INT = 'I';
        public static final int LONG = 'J';
        public static final int SHORT = 'S';
        public static final int BOOLEAN = 'Z';
        public static final int REFERENCE = 'L';

        public static final byte K_KIND = 'K';
        public static final byte M_KIND = 'M';
        public static final byte PRIMITIVE_KIND = 'P';
        
        public static final TypeA TYPEA_BYTE = 
            new TypeA(PRIMITIVE_KIND, BYTE);
        public static final TypeA TYPEA_CHAR =
            new TypeA(PRIMITIVE_KIND, CHAR);
        public static final TypeA TYPEA_DOUBLE =
            new TypeA(PRIMITIVE_KIND, DOUBLE);
        public static final TypeA TYPEA_FLOAT =
            new TypeA(PRIMITIVE_KIND, FLOAT);
        public static final TypeA TYPEA_INT =
            new TypeA(PRIMITIVE_KIND, INT);
        public static final TypeA TYPEA_LONG =
            new TypeA(PRIMITIVE_KIND, LONG);
        public static final TypeA TYPEA_SHORT =
            new TypeA(PRIMITIVE_KIND, SHORT);
        public static final TypeA TYPEA_BOOLEAN =
            new TypeA(PRIMITIVE_KIND, BOOLEAN);
        public static final TypeA TYPEA_REFERENCE =
            new TypeA(PRIMITIVE_KIND, REFERENCE);

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
        public String toString() {
            if (kind == PRIMITIVE_KIND) {
                return "TypeA{" +
                                "kind=" + (char) kind +
                                ", index=" + (char) index +
                                '}';
            }
            return "TypeA{" +
                            "kind=" + (char) kind +
                            ", index=" + index +
                            '}';            
        }
    }

    public static class TypeB {
        public static final TypeB NO_HINT = 
            new TypeB((byte)0, -1);
        // public static final byte IS_ARRAY = 0;
        // public static final byte NOT_ARRAY = 1;

        public static final byte K_KIND = 'K';
        public static final byte M_KIND = 'M';
        public static final byte ARR_K_KIND = 'k';
        public static final byte ARR_M_KIND = 'm';

        // private final byte isArray;
        private final byte kind;
        private final int index;

        // public TypeB(byte isArray, byte kind, int index) {
        //     this.isArray = isArray;
        //     this.kind = kind;
        //     this.index = index;
        // }
        public TypeB(byte kind, int index) {
            this.kind = kind;
            this.index = index;
        }

        public byte getKind() {
            return kind;
        }

        public int getIndex() {
            return index;
        }

        public boolean isNoHint() {
            return this == NO_HINT || (this.kind == 0 && this.index == -1);
        }

        @Override
        public String toString() {
            if (isNoHint()) {
                return "TypeB{NO_HINT}";
            }
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
