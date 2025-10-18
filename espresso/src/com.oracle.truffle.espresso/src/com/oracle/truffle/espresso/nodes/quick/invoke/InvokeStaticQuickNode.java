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
import com.oracle.truffle.espresso.descriptors.EspressoSymbols.Names;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.nodes.EspressoRootNode;
import com.oracle.truffle.espresso.nodes.bytecodes.InvokeStatic;
import com.oracle.truffle.espresso.nodes.bytecodes.InvokeStaticNodeGen;
import com.oracle.truffle.espresso.vm.VM;

public final class InvokeStaticQuickNode extends InvokeQuickNode {

    @Child InvokeStatic invokeStatic;
    final boolean isDoPrivilegedCall;
    @CompilationFinal final boolean isGuestBoxing;
    @CompilationFinal final boolean isGuestUnboxing;

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
        
        Object[] args = getArguments(frame);
        if (isGuestBoxing) {
            return pushResult(frame, guestBox(args));
        } else if (isGuestUnboxing) {
            return pushResult(frame, guestUnbox(args));
        } else return pushResult(frame, invokeStatic.execute(args));
    }

    @TruffleBoundary
    @GuestBoxing
    private Object guestBox(Object[] args) {
        return invokeStatic.execute(args);
    }

    @TruffleBoundary
    @GuestUnboxing
    private Object guestUnbox(Object[] args) {
        return invokeStatic.execute(args);
    }

    public void initializeResolvedKlass() {
        invokeStatic.getStaticMethod().getDeclaringKlass().safeInitialize();
    }
}
