/*
 * Copyright (c) 2018, 2021, Oracle and/or its affiliates. All rights reserved.
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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.GuestBoxing;
import com.oracle.truffle.api.nodes.GuestUnboxing;
import com.oracle.truffle.espresso.classfile.JavaKind;
import com.oracle.truffle.espresso.classfile.descriptors.SignatureSymbols;
import com.oracle.truffle.espresso.descriptors.EspressoSymbols.Names;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoFrame;
import com.oracle.truffle.espresso.nodes.EspressoRootNode;
import com.oracle.truffle.espresso.nodes.bytecodes.InvokeStatic;
import com.oracle.truffle.espresso.nodes.bytecodes.InvokeStaticNodeGen;
import com.oracle.truffle.espresso.runtime.staticobject.StaticObject;
import com.oracle.truffle.espresso.vm.VM;

public final class InvokeStaticQuickNode extends InvokeQuickNode {

    @Child InvokeStatic invokeStatic;
    final boolean isDoPrivilegedCall;
    @CompilationFinal final boolean isGuestBoxing;
    @CompilationFinal final boolean isGuestUnboxing;
    @CompilationFinal final char boxKind;

    public InvokeStaticQuickNode(Method method, int top, int curBCI) {
        super(method, top, curBCI);
        assert method.isStatic();
        this.isDoPrivilegedCall = method.getMeta().java_security_AccessController.equals(method.getDeclaringKlass()) &&
                        Names.doPrivileged.equals(method.getName());
        this.invokeStatic = insert(InvokeStaticNodeGen.create(method));
        String className = method.getDeclaringKlass().getNameAsString(), methodName = method.getNameAsString();
        this.isGuestBoxing = className.equals("scala/runtime/BoxesRunTime") && (
                methodName.equals("boxToBoolean") ||
                methodName.equals("boxToCharacter") ||
                methodName.equals("boxToByte") ||
                methodName.equals("boxToShort") ||
                methodName.equals("boxToInteger") ||
                methodName.equals("boxToLong") ||
                methodName.equals("boxToFloat") ||
                methodName.equals("boxToDouble")
            );
        this.isGuestUnboxing = className.equals("scala/runtime/BoxesRunTime") && (
                methodName.equals("unboxToBoolean") ||
                methodName.equals("unboxToChar") ||
                methodName.equals("unboxToByte") ||
                methodName.equals("unboxToShort") ||
                methodName.equals("unboxToInt") ||
                methodName.equals("unboxToLong") ||
                methodName.equals("unboxToFloat") ||
                methodName.equals("unboxToDouble")
            );
        if (this.isGuestBoxing) {
            this.boxKind = (char) SignatureSymbols.parameterType(method.getParsedSignature(), 0).byteAt(0);
        } else if (this.isGuestUnboxing) {
            this.boxKind = method.getReturnKind().getTypeChar();
        } else this.boxKind = 0;
        if (this.isGuestBoxing || this.isGuestUnboxing) {
            assert method.getParameterCount() == 1 && this.typeArgCnt == 0;
            assert this.boxKind == JavaKind.Boolean.getTypeChar() || this.boxKind == JavaKind.Char.getTypeChar() || this.boxKind == JavaKind.Byte.getTypeChar()  || this.boxKind == JavaKind.Short.getTypeChar()
                || this.boxKind == JavaKind.Int.getTypeChar()     || this.boxKind == JavaKind.Long.getTypeChar() || this.boxKind == JavaKind.Float.getTypeChar() || this.boxKind == JavaKind.Double.getTypeChar();
        }
    }

    @Override
    public int execute(VirtualFrame frame, boolean isContinuationResume) {
        // Support for AccessController.doPrivileged.
        if (isDoPrivilegedCall) {
            EspressoRootNode rootNode = (EspressoRootNode) getRootNode();
            if (rootNode != null) {
                // Put cookie in the caller frame.
                rootNode.setFrameId(frame, VM.GlobalFrameIDs.getID());
            }
        }
        
        if (isGuestBoxing) {
            switch (this.boxKind) {
                case 'Z':
                    return pushResultNoForeign(frame, guestBoxBoolean(EspressoFrame.popInt(frame, this.top - 1) != 0));
                case 'B':
                    return pushResultNoForeign(frame, guestBoxByte((byte) EspressoFrame.popInt(frame, this.top - 1)));
                case 'S':
                    return pushResultNoForeign(frame, guestBoxShort((short) EspressoFrame.popInt(frame, this.top - 1)));
                case 'C':
                    return pushResultNoForeign(frame, guestBoxChar((char) EspressoFrame.popInt(frame, this.top - 1)));
                case 'I':
                    return pushResultNoForeign(frame, guestBoxInt(EspressoFrame.popInt(frame, this.top - 1)));
                case 'F':
                    return pushResultNoForeign(frame, guestBoxFloat(EspressoFrame.popFloat(frame, this.top - 1)));
                case 'J':
                    return pushResultNoForeign(frame, guestBoxLong(EspressoFrame.popLong(frame, this.top - 1)));
                case 'D':
                    return pushResultNoForeign(frame, guestBoxDouble(EspressoFrame.popDouble(frame, this.top - 1)));
                default:
                    throw new AssertionError("Unrecognized boxKind " + this.boxKind);
            }
        } else if (isGuestUnboxing) {
            switch (this.boxKind) {
                case 'Z':
                    return pushResult(frame, guestUnboxBoolean(EspressoFrame.popObject(frame, this.top - 1)) ? 1 : 0);
                case 'B':
                    return pushResult(frame, guestUnboxByte(EspressoFrame.popObject(frame, this.top - 1)));
                case 'S':
                    return pushResult(frame, guestUnboxShort(EspressoFrame.popObject(frame, this.top - 1)));
                case 'C':
                    return pushResult(frame, guestUnboxChar(EspressoFrame.popObject(frame, this.top - 1)));
                case 'I':
                    return pushResult(frame, guestUnboxInt(EspressoFrame.popObject(frame, this.top - 1)));
                case 'F':
                    return pushResult(frame, guestUnboxFloat(EspressoFrame.popObject(frame, this.top - 1)));
                case 'J':
                    return pushResult(frame, guestUnboxLong(EspressoFrame.popObject(frame, this.top - 1)));
                case 'D':
                    return pushResult(frame, guestUnboxDouble(EspressoFrame.popObject(frame, this.top - 1)));
                default:
                    throw new AssertionError("Unrecognized boxKind " + this.boxKind);
            }
        } else {
            Object[] args = getArguments(frame);
            return pushResult(frame, invokeStatic.execute(args));
        }
    }

    public final int pushResultNoForeign(VirtualFrame frame, StaticObject result) {
        EspressoFrame.putObject(frame, resultAt, result);
        return stackEffect;
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxBoolean(boolean arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxByte(byte arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxShort(short arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxChar(char arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxInt(int arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxFloat(float arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxLong(long arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestBoxing
    private StaticObject guestBoxDouble(double arg) {
        return (StaticObject) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private boolean guestUnboxBoolean(StaticObject arg) {
        return (boolean) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private byte guestUnboxByte(StaticObject arg) {
        return (byte) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private short guestUnboxShort(StaticObject arg) {
        return (short) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private char guestUnboxChar(StaticObject arg) {
        return (char) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private int guestUnboxInt(StaticObject arg) {
        return (int) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private float guestUnboxFloat(StaticObject arg) {
        return (float) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private long guestUnboxLong(StaticObject arg) {
        return (long) invokeStatic.execute(new Object[]{arg});
    }

    @TruffleBoundary
    @GuestUnboxing
    private double guestUnboxDouble(StaticObject arg) {
        return (double) invokeStatic.execute(new Object[]{arg});
    }

    public void initializeResolvedKlass() {
        invokeStatic.getStaticMethod().getDeclaringKlass().safeInitialize();
    }
}
