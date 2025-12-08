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
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
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
    @CompilerDirectives.CompilationFinal(dimensions=2) private byte[][] cacheKeys = null;
    private final FrameDescriptor frameDescriptor;
    
    @CompilerDirectives.CompilationFinal
    private int typeParamCount = 0;
    private TypeAnalysisResult[] analysis;
    private final Method.MethodVersion methodVersion;
    private final boolean trivialBytecode;
    
    @CompilerDirectives.CompilationFinal
    public static final boolean SHOW_TYPEANALYSIS = false;

    MethodWithBytecodeNode(BytecodeNode bytecodeNode) {
        super(bytecodeNode.getMethodVersion());
        this.bytecodeNode = bytecodeNode;
        this.methodVersion = bytecodeNode.getMethodVersion();
        this.trivialBytecode = BytecodeNode.isTrivialBytecodes(methodVersion);
        this.frameDescriptor = bytecodeNode.getFrameDescriptor();
        this.specializations = null;
    }

    MethodWithBytecodeNode(Method.MethodVersion methodVersion) {
        super(methodVersion);
        this.methodVersion = methodVersion;
        this.trivialBytecode = BytecodeNode.isTrivialBytecodes(methodVersion);

        CompilerAsserts.neverPartOfCompilation();
        MethodTypeParameterCountAttribute attr = methodVersion.getMethod().getMethodTypeParameterCountAttribute();
        this.typeParamCount = attr != null ? attr.getCount() : 0;

        this.analysis = TypeHintAnalysis.analyze(methodVersion);
        if (this.analysis != null) {
            this.bytecodeNode = null;
            this.frameDescriptor = BytecodeNode.calcFrameDescriptor(methodVersion);
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
        return executeSpecialization(frame.getArguments(), frame);
    }

    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    private Object executeSpecialization(Object[] args, VirtualFrame frame){
        if (this.bytecodeNode != null) {
            this.bytecodeNode.initializeFrame(frame);
            return this.bytecodeNode.execute(frame);
        }
        byte[] key = collectReifiedValues(args);
        
        for (int i = 0; i < cacheKeys.length; i++){
            if (Arrays.equals(cacheKeys[i], key)){
                specializations[i].initializeFrame(frame);
                return specializations[i].execute(frame);
            }
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();

        BytecodeNode newNode = insertSpecialization(key);
        newNode.initializeFrame(frame);
        return newNode.execute(frame);
    }

    private byte[] collectReifiedValues(Object[] args){
        byte[] key = new byte[typeParamCount];
        for (int i = 0; i < typeParamCount; i++){
            key[i] = (byte) args[args.length - typeParamCount + i];
        }
        //TODO: class type parameters
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
        notifyInserted(node);

        return node;        
    }

    @Override
    @SuppressFBWarnings(value = "BC_IMPOSSIBLE_INSTANCEOF", justification = "bytecodeNode may be replaced by instrumentation with a wrapper node")
    boolean isTrivial() {
        // Instrumented nodes are not trivial.
        if (bytecodeNode != null) {
            return !(bytecodeNode instanceof WrapperNode) && bytecodeNode.isTrivial();
        } else {
            return this.trivialBytecode;
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
