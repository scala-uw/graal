/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.truffle.espresso.nodes;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.instrumentation.StandardTags.RootBodyTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.interop.NodeLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.espresso.analysis.typehints.TypeAnalysisResult;
import com.oracle.truffle.espresso.analysis.typehints.TypeHintAnalysis;
import com.oracle.truffle.espresso.classfile.attributes.reified.MethodTypeParameterCountAttribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.impl.SuppressFBWarnings;

/**
 * {@link RootTag} node that separates the Java method prolog e.g. copying arguments to the frame,
 * initializes {@code bci=0}, from the execution of the {@link BytecodeNode bytecodes/body}.
 * 
 * This class exists to conform to the Truffle instrumentation APIs, namely {@link RootTag} and
 * {@link RootBodyTag} in order to support proper unwind and re-enter.
 */
@ExportLibrary(NodeLibrary.class)
final class MethodWithBytecodeNode extends EspressoInstrumentableRootNodeImpl {

    @Child AbstractInstrumentableBytecodeNode bytecodeNode;
    @Children BytecodeNode[] specializations;
    @CompilerDirectives.CompilationFinal(dimensions=1)
    private byte[][] cacheKeys = null;
    private final FrameDescriptor frameDescriptor;
    
    @CompilerDirectives.CompilationFinal
    private int typeParamCount = 0;
    @CompilerDirectives.CompilationFinal(dimensions=1)
    private TypeAnalysisResult[] analysis = null;
    private final Method.MethodVersion methodVersion;
    
    @CompilerDirectives.CompilationFinal
    public static final boolean SHOW_TYPEANALYSIS = false;
    public static final boolean DEBUG = false;

    MethodWithBytecodeNode(BytecodeNode bytecodeNode) {
        super(bytecodeNode.getMethodVersion());
        this.bytecodeNode = bytecodeNode;
        this.methodVersion = bytecodeNode.getMethodVersion();
        this.frameDescriptor = bytecodeNode.getFrameDescriptor();
        this.specializations = null;
    }

    MethodWithBytecodeNode(Method.MethodVersion methodVersion) {
        super(methodVersion);
        this.methodVersion = methodVersion;

        CompilerAsserts.neverPartOfCompilation();
        MethodTypeParameterCountAttribute attr = methodVersion.getMethod().getMethodTypeParameterCountAttribute();
        this.typeParamCount = attr != null ? attr.getCount() : 0;

        if (this.typeParamCount != 0) {
            if (DEBUG) System.out.println("Method " + methodVersion.getMethod().getNameAsString() +
                " has " + typeParamCount + " reified type parameters.");
            this.analysis = TypeHintAnalysis.analyze(methodVersion, SHOW_TYPEANALYSIS).getRes();
            this.bytecodeNode = null;
            byte[] bt = new byte[typeParamCount];
            for (int i =0; i < typeParamCount; i++){
                bt[i] = TypeHints.TypeA.REFERENCE;
            }
            this.frameDescriptor = new BytecodeNode(methodVersion, null, bt).getFrameDescriptor();

            this.specializations = new BytecodeNode[0];
            this.cacheKeys = new byte[0][];
        } else {
            BytecodeNode t = new BytecodeNode(methodVersion, null, new byte[]{});
            this.bytecodeNode = t;
            this.frameDescriptor = t.getFrameDescriptor();
        }
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    @Override
    public int getBci(Frame frame) {
        return EspressoFrame.getBCI(frame);
    }

    @Override
    Object execute(VirtualFrame frame) {
        BytecodeNode node = findSpecialization(frame.getArguments());
        node.initializeFrame(frame);
        Object result = node.execute(frame);
        return result;
    }

    @ExplodeLoop
    private BytecodeNode findSpecialization(Object[] args){
        // if (methodVersion.getMethod().getNameAsString().contains("identity")){
        //     System.out.println("Args length: " + args.length + "typeParamCount: " + typeParamCount +
        //         "bytecodeNode : " + bytecodeNode +
        //         " for method: " + methodVersion.getMethod().getName());

        // }
        if (typeParamCount == 0) {
            return (BytecodeNode) bytecodeNode;
        }
        byte[] key = collectReifiedValues(args);
        if (DEBUG) {
            System.out.println("for method " + methodVersion.getMethod().getNameAsString() +
                " with args: " + Arrays.toString(args) + " with keys: " + Arrays.toString(key));
        }
        
        for (int i = 0; i < specializations.length; i++){
            if (Arrays.equals(cacheKeys[i], key)){
                return specializations[i];
            }
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();

        return insertSpecialization(key);
    }

    private byte[] collectReifiedValues(Object[] args){
        byte[] key = new byte[typeParamCount];
        // get method type parameters reified values
        for (int i = 0; i < typeParamCount; i++){
            key[i] = (byte) args[args.length - typeParamCount + i];
        }
        if (DEBUG) System.out.println("keys: for method" + methodVersion.getMethod().getNameAsString() + Arrays.toString(key));
        //also get class type parameters reified values from fields
        //TODO
        return key;
    }

    private BytecodeNode insertSpecialization(byte[] key){
        CompilerDirectives.transferToInterpreterAndInvalidate();

        BytecodeNode node = new BytecodeNode(methodVersion, analysis, key);
        node = this.insert(node);

        int len = specializations.length;
        BytecodeNode[] newSpecializations = Arrays.copyOf(specializations, len + 1);
        
        newSpecializations[len] = node;
        this.specializations = newSpecializations;

        cacheKeys = Arrays.copyOf(cacheKeys, len + 1);
        cacheKeys[len] = key;

        return node;        
    }

    // private static final class ByteKey{
    //     private final byte[] key;
    //     private final int hash;

    //     ByteKey(byte[] key){
    //         this.key = key;
    //         this.hash = Arrays.hashCode(key);
    //     }

    //     @Override
    //     public boolean equals(Object obj) {
    //         if (this == obj) {
    //             return true;
    //         }
    //         if (obj instanceof ByteKey){
    //             ByteKey other = (ByteKey) obj;
    //             return Arrays.equals(this.key, other.key);
    //         }
    //         return false;
    //     }

    //     @Override
    //     public int hashCode() {
    //         return hash;
    //     }
    // }

    @Override
    @SuppressFBWarnings(value = "BC_IMPOSSIBLE_INSTANCEOF", justification = "bytecodeNode may be replaced by instrumentation with a wrapper node")
    boolean isTrivial() {
        // Instrumented nodes are not trivial.
        if (bytecodeNode != null) {
            return !(bytecodeNode instanceof WrapperNode) && bytecodeNode.isTrivial();
        } else {
            return specializations[8].isTrivial();
        }
    }

    @Override
    public boolean hasTag(Class<? extends Tag> tag) {
        if (tag == StandardTags.RootTag.class) {
            return true;
        }
        return false;
    }

    @ExportMessage
    @SuppressWarnings("static-method")
    public boolean hasScope(@SuppressWarnings("unused") Frame frame) {
        return true;
    }

    @ExportMessage
    public Object getScope(Frame frame, boolean nodeEnter) {
        return getScopeSlowPath(frame != null ? frame.materialize() : null, nodeEnter);
    }

    @TruffleBoundary
    private Object getScopeSlowPath(MaterializedFrame frame, boolean nodeEnter) {
        return bytecodeNode.getScope(frame, nodeEnter);
    }
}
