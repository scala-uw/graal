/*
 * Copyright (c) 2020, 2025, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.espresso.nodes.quick.invoke;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.espresso.classfile.JavaKind;
import com.oracle.truffle.espresso.classfile.attributes.reified.MethodTypeParameterCountAttribute;
import com.oracle.truffle.espresso.classfile.descriptors.SignatureSymbols;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.nodes.quick.QuickNode;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;

public abstract class InvokeQuickNode extends QuickNode {
    private static final Object[] EMPTY_ARGS = new Object[0];

    public final Method.MethodVersion method;

    // Helper information for easier arguments handling.
    protected final int resultAt;
    protected final int stackEffect;
    @CompilationFinal(dimensions = 1) protected final byte[] argsType;
    @CompilationFinal protected final byte alternatedReturnType;

    // Helps check for no foreign objects
    @CompilationFinal protected final JavaKind returnKind;
    private final boolean returnsPrimitive;

    public InvokeQuickNode(Method m, int top, int callerBCI) {
        super(top, callerBCI);
        this.method = m.getMethodVersion();
        this.resultAt = top -(SignatureSymbols.slotsForParameters(m.getParsedSignature()) + (m.hasReceiver() ? 1 : 0));
        this.stackEffect = (resultAt - top) + m.getReturnKind().getSlotCount();
        this.argsType = null;
        this.alternatedReturnType = 0;
        this.returnKind = m.getReturnKind();
        this.returnsPrimitive = this.returnKind.isPrimitive();
    }

    public InvokeQuickNode(Method.MethodVersion version, int top, int callerBCI) {
        super(top, callerBCI);
        this.method = version;
        Method m = version.getMethod();
        this.resultAt = top - (SignatureSymbols.slotsForParameters(m.getParsedSignature()) + (m.hasReceiver() ? 1 : 0));
        this.stackEffect = (resultAt - top) + m.getReturnKind().getSlotCount();
        this.argsType = null;
        this.alternatedReturnType = 0;
        this.returnKind = m.getReturnKind();
        this.returnsPrimitive = this.returnKind.isPrimitive();
    }

    public InvokeQuickNode(Method m, int top, int callerBCI, byte[] argsType, byte returnType) {
        super(top, callerBCI);
        this.method = m.getMethodVersion();
        this.resultAt = top -(SignatureSymbols.slotsForParameters(m.getParsedSignature()) + (m.hasReceiver() ? 1 : 0));
        this.stackEffect = (resultAt - top) + m.getReturnKind().getSlotCount();
        this.argsType = argsType;
        this.alternatedReturnType = returnType;
        this.returnKind = m.getReturnKind();
        this.returnsPrimitive = this.returnKind.isPrimitive();
    }

    public InvokeQuickNode(Method.MethodVersion version, int top, int callerBCI, byte[] argsType, byte returnType) {
        super(top, callerBCI);
        this.method = version;
        Method m = version.getMethod();
        this.resultAt = top - (SignatureSymbols.slotsForParameters(m.getParsedSignature()) + (m.hasReceiver() ? 1 : 0));
        this.stackEffect = (resultAt - top) + m.getReturnKind().getSlotCount();
        this.argsType = argsType;
        this.alternatedReturnType = returnType;
        this.returnKind = m.getReturnKind();
        this.returnsPrimitive = this.returnKind.isPrimitive();
    }

    public final StaticObject peekReceiver(VirtualFrame frame) {
        return EspressoFrame.peekReceiver(frame, top, method.getMethod());
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        return 0;
    }

    protected Object[] getArguments(VirtualFrame frame) {
        /*
         * Method signature does not change across methods. Can safely use the constant signature
         * from `method` instead of the non-constant signature from the lookup.
         */
        if (method.isStatic() && method.getMethod().getParameterCount() == 0) {
            // Don't create an array for empty arguments.
            return EMPTY_ARGS;
        }
        if (this.argsType == null) {
            return EspressoFrame.popArguments(frame, top, !method.isStatic(), method.getMethod().getParsedSignature());
        } else {
            return EspressoFrame.popArguments(frame, top, !method.isStatic(), method.getMethod().getParsedSignature(), this.argsType);
        }
    }

    public final int pushResult(VirtualFrame frame, int result) {
        EspressoFrame.putInt(frame, resultAt, result);
        return stackEffect;
    }

    public final int pushResult(VirtualFrame frame, long result) {
        EspressoFrame.putLong(frame, resultAt, result);
        return stackEffect;
    }

    public final int pushResult(VirtualFrame frame, float result) {
        EspressoFrame.putFloat(frame, resultAt, result);
        return stackEffect;
    }

    public final int pushResult(VirtualFrame frame, double result) {
        EspressoFrame.putDouble(frame, resultAt, result);
        return stackEffect;
    }

    public final int pushResult(VirtualFrame frame, StaticObject result) {
        getBytecodeNode().checkNoForeignObjectAssumption(result);
        EspressoFrame.putObject(frame, resultAt, result);
        return stackEffect;
    }

    public final int pushResult(VirtualFrame frame, Object result) {
        if (this.returnKind == JavaKind.Object && alternatedReturnType != 0) {
            switch (alternatedReturnType) {
                case TypeHints.BYTE: EspressoFrame.putInt(frame, resultAt, (byte) result); break;
                case TypeHints.CHAR: EspressoFrame.putInt(frame, resultAt, (char) result); break;
                case TypeHints.DOUBLE: EspressoFrame.putReifiedDouble(frame, resultAt, (double) result); break;
                case TypeHints.FLOAT: EspressoFrame.putFloat(frame, resultAt, (float) result); break;
                case TypeHints.INT: EspressoFrame.putInt(frame, resultAt, (int) result); break;
                case TypeHints.LONG: EspressoFrame.putReifiedLong(frame, resultAt, (long) result); break;
                case TypeHints.SHORT: EspressoFrame.putInt(frame, resultAt, (short) result); break;
                case TypeHints.BOOLEAN: EspressoFrame.putInt(frame, resultAt, ((boolean) result) ? 1 : 0); break;
                default: EspressoFrame.putObject(frame, resultAt, (StaticObject) result);
            }
        } else {
            EspressoFrame.putKind(frame, resultAt, result, this.returnKind);
        }
        return stackEffect;
        // if (!reifiedEnabled || returnTypeHint.isNoHint() || returnTypeHint.getKind() == TypeHints.REFERENCE){
        //     if (!returnsPrimitive) {
        //         getBytecodeNode().checkNoForeignObjectAssumption((StaticObject) result);
        //     }
        //     EspressoFrame.putKind(frame, resultAt, result, method.getMethod().getReturnKind());
        //     return stackEffect;
        // } else {
        //     Object[] args = getArguments(frame);
        //     // System.out.println("InvokeQuickNode.pushResult: args: ");
        //     // for (int i = 0; i < args.length; i++) {
        //     //     System.out.print("," + i + ": " + args[i] + " (" + (args[i] != null ? args[i].getClass().getName() : "null") + ")");
        //     // }
        //     // System.out.println(":fin");
        //     byte kind = returnTypeHint.getKind();
        //     int index = returnTypeHint.getIndex();
        //     byte reifiedValue = -1;
        //     if (kind == TypeHints.METHOD_TYPE_PARAM){
        //         reifiedValue = (byte) args[
        //             method.getMethod().getParameterCount() + (method.isStatic() ? 0 : 1)
        //             + index]; //TODO, check this
        //     } else if (kind == TypeHints.CLASS_TYPE_PARAM){
        //         //TODO
        //     } else {
        //         throw EspressoError.shouldNotReachHere("Unexpected type kind: " + kind + " in pushResult of inoke" + toString());
        //     }
        //     if (reifiedValue == TypeHints.TypeA.BYTE){
        //         EspressoFrame.putInt(frame, resultAt, (byte) result);
        //     } else if (reifiedValue == TypeHints.TypeA.CHAR){
        //         EspressoFrame.putInt(frame, resultAt, (char) result);
        //     } else if (reifiedValue == TypeHints.TypeA.DOUBLE) {
        //         EspressoFrame.putDouble(frame, resultAt, (double) result);
        //     } else if (reifiedValue == TypeHints.TypeA.FLOAT) {
        //         EspressoFrame.putFloat(frame, resultAt, (float) result);
        //     } else if (reifiedValue == TypeHints.TypeA.INT){
        //         EspressoFrame.putInt(frame, resultAt, (int) result);
        //     } else if (reifiedValue == TypeHints.TypeA.LONG) {
        //         EspressoFrame.putLong(frame, resultAt, (long) result);
        //     } else if (reifiedValue == TypeHints.TypeA.SHORT){
        //         EspressoFrame.putInt(frame, resultAt, (short) result);
        //     } else if (reifiedValue == TypeHints.TypeA.BOOLEAN){
        //         EspressoFrame.putInt(frame, resultAt, (boolean) result ? 1 : 0);
        //     } else {
        //         EspressoFrame.putObject(frame, resultAt, (StaticObject) result);
        //     }
        //     return stackEffect;
        // }
    }

    @Override
    public final boolean removedByRedefinition() {
        if (method.getRedefineAssumption().isValid()) {
            return false;
        } else {
            return method.getMethod().isRemovedByRedefinition();
        }
    }

    @Override
    public final String toString() {
        return "INVOKE: " + method.getDeclaringKlass().getExternalName() + "." + method.getName() + ":" + method.getRawSignature();
    }
}
