package com.oracle.truffle.espresso.analysis.typehints;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.espresso.analysis.AnalysisProcessor;
import com.oracle.truffle.espresso.analysis.BlockIterator;
import com.oracle.truffle.espresso.analysis.BlockIteratorClosure;
import com.oracle.truffle.espresso.analysis.graph.LinkedBlock;
import com.oracle.truffle.espresso.classfile.attributes.reified.InstructionTypeArgumentsAttribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.MethodParameterTypeAttribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.MethodReturnTypeAttribute;
import com.oracle.truffle.espresso.classfile.attributes.reified.TypeHints;
import com.oracle.truffle.espresso.classfile.bytecode.BytecodeStream;
import com.oracle.truffle.espresso.classfile.bytecode.Bytecodes;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ALOAD;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ALOAD_0;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ALOAD_1;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ALOAD_2;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ALOAD_3;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ASTORE;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ASTORE_0;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ASTORE_1;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ASTORE_2;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.ASTORE_3;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.DUP;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.GETFIELD;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.GETSTATIC;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.INVOKEINTERFACE;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.INVOKESPECIAL;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.INVOKESTATIC;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.INVOKEVIRTUAL;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.PUTFIELD;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.PUTSTATIC;
import static com.oracle.truffle.espresso.classfile.bytecode.Bytecodes.SWAP;
import com.oracle.truffle.espresso.classfile.constantpool.MethodRefConstant;
import com.oracle.truffle.espresso.classfile.constantpool.Resolvable;
import com.oracle.truffle.espresso.classfile.descriptors.SignatureSymbols;
import com.oracle.truffle.espresso.classfile.descriptors.Symbol;
import com.oracle.truffle.espresso.classfile.descriptors.Type;
import com.oracle.truffle.espresso.constantpool.Resolution;
import com.oracle.truffle.espresso.constantpool.RuntimeConstantPool;
import com.oracle.truffle.espresso.impl.Field;
import com.oracle.truffle.espresso.impl.Klass;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.impl.ObjectKlass;
import com.oracle.truffle.espresso.runtime.EspressoContext;
import com.oracle.truffle.espresso.runtime.EspressoLinkResolver;
import com.oracle.truffle.espresso.shared.resolver.CallSiteType;
import com.oracle.truffle.espresso.shared.resolver.ResolvedCall;
import com.oracle.truffle.espresso.substitutions.standard.Target_java_lang_invoke_MethodHandleNatives.SiteTypes;

public class TypePropagationClosure extends BlockIteratorClosure{

    private final EspressoContext ctx;
    private final TypeAnalysisResult[] resAtBCI;
    private final TypeAnalysisState[] resAtBlockEnd;
    private final Method.MethodVersion methodVersion;
    private final int maxLocals;
    private final int maxStack;

    public TypePropagationClosure(
                    EspressoContext ctx,
                    int codeLength, 
                    Method.MethodVersion methodVersion,
                    int maxLocals,
                    int maxStack,
                    int totalBlocks) {
        this.ctx = ctx;
        this.resAtBCI = new TypeAnalysisResult[codeLength];
        this.methodVersion = methodVersion;
        this.maxLocals = maxLocals;
        this.maxStack = maxStack;
        this.resAtBlockEnd = new TypeAnalysisState[totalBlocks];
    }

    @Override
    public BlockIterator.BlockProcessResult processBlock(LinkedBlock block, BytecodeStream bs, AnalysisProcessor processor) {
        TypeAnalysisState inState = mergePredecessors(block);
        TypeAnalysisState outState = analyzeInBlock(block, bs, inState);
        TypeAnalysisState previousState = this.resAtBlockEnd[block.id()];
        if (outState.equals(previousState)){
            return BlockIterator.BlockProcessResult.SKIP;
        } else {
            this.resAtBlockEnd[block.id()] = outState.copy();
            return BlockIterator.BlockProcessResult.DONE;
        }
    }

    private TypeAnalysisState mergePredecessors(LinkedBlock block) {
        List<TypeAnalysisState> states = new ArrayList<>();
        //root block: method entry
        if (block.predecessorsID().length == 0){
            TypeAnalysisState rootState = new TypeAnalysisState(maxLocals, maxStack);
            MethodParameterTypeAttribute methodParameterTypeAttribute = 
                getMethod().getMethodParameterTypeAttribute();
            if (methodParameterTypeAttribute == null) return rootState;
            Symbol<Type>[] signature = getMethod().getParsedSignature();
            int paramCnt = SignatureSymbols.parameterCount(signature);
            TypeHints.TypeB[] methodParameterTypes = 
                methodParameterTypeAttribute.getParameterTypes();
            int localIndex = getMethod().isStatic() ? 0 : 1; //skip 'this' for non-static methods
            for (int i = 0; i < paramCnt; i++){
                Symbol<Type> cur = SignatureSymbols.parameterType(signature, i);
                if (cur.byteAt(0) == 'J' || cur.byteAt(0) == 'D'){
                    assert methodParameterTypes[i].isNoHint();
                    rootState.locals[localIndex] = null;
                    localIndex ++;
                } else {
                    rootState.locals[localIndex] = methodParameterTypes[i].isNoHint() ? null : methodParameterTypes[i];
                }
                localIndex++;
            }
            return rootState;
            
        }
        for (int predId : block.predecessorsID()){
            TypeAnalysisState predState = this.resAtBlockEnd[predId];
            if (predState != null) {
                states.add(predState.copy());
            } else {
                states.add(new TypeAnalysisState(maxLocals, maxStack));
            }
        }
        return TypeAnalysisState.merge(states, maxLocals, maxStack);
    }

    private TypeAnalysisState analyzeInBlock(LinkedBlock block, BytecodeStream bs, TypeAnalysisState inState){
        TypeAnalysisState state = inState.copy();
        int bci = block.start();
        while (bci <= block.end()){
            int opcode = bs.currentBC(bci);
            int cpi; int stackEffect;
            System.out.println("Processing opcode: " + Bytecodes.nameOf(opcode) + " at bci: " + bci);
            System.out.println("Current state: " + state);
            switch (opcode){
                case ASTORE:
                case ASTORE_0:
                case ASTORE_1:
                case ASTORE_2:
                case ASTORE_3:
                    int astoreIndex = opcode == ASTORE ? bs.readLocalIndex(bci) : (opcode - ASTORE_0);
                    assert state.stackTop > 0;
                    this.resAtBCI[bci] = new TypeAnalysisResult(new TypeHints.TypeB[]{state.stack[state.stackTop - 1]});
                    System.out.println("Astore index: " + astoreIndex + " stack top: " + state.stack[state.stackTop - 1]);
                    if (state.stackTop > 0){
                        if (state.stack[state.stackTop - 1] == null){
                            state.locals[astoreIndex] = null;
                        }
                        else {
                            state.locals[astoreIndex] = state.stack[state.stackTop - 1];
                        }
                        state.stackTop--;
                        state.stack[state.stackTop] = null;
                    } else {
                        throw new AssertionError("Astore without stack element at bci: " + bci);
                    }
                    break;
                case ALOAD:
                case ALOAD_0:
                case ALOAD_1:
                case ALOAD_2:
                case ALOAD_3:
                    int aloadIndex = opcode == ALOAD ? bs.readLocalIndex(bci) : (opcode - ALOAD_0);
                    this.resAtBCI[bci] = new TypeAnalysisResult(new TypeHints.TypeB[]{state.locals[aloadIndex]});
                    System.out.println("Aload index: " + aloadIndex + " locals at index: " + state.locals[aloadIndex]);
                    if (state.locals[aloadIndex] == null){
                        state.stack[state.stackTop] = null;
                    } else {
                        state.stack[state.stackTop] = state.locals[aloadIndex];
                    }
                    state.stackTop++;
                    break;
                case GETSTATIC:
                case GETFIELD:
                case PUTSTATIC:
                case PUTFIELD:
                    cpi = bs.readCPI(bci);
                    Field field = getConstantPool().resolvedFieldAt(getDeclaringKlass(), cpi);
                    // if (field.needsReResolution()) {
                    //     CompilerDirectives.transferToInterpreterAndInvalidate();
                    //     getMethod().getContext().getClassRedefinition().check();
                    //     field = getConstantPool().resolveFieldAndUpdate(getMethod().getDeclaringKlass(), cpi, field);
                    // }
                    System.out.println("Resolved Field: " + field.getName() + " at bci: " + bci);
                    int fieldSlotCount = field.getKind().getSlotCount();
                    for (int i = 0; i < -Bytecodes.stackEffectOf(opcode); i++) {
                        state.stackTop--;
                        state.stack[state.stackTop] = null;
                    }
                    assert state.stackTop >= 0;
                    if (opcode == GETSTATIC || opcode == GETFIELD){
                        //pushes onto the stack:
                        if (state.stackTop + fieldSlotCount > maxStack) {
                            throw new AssertionError("Stack overflow at bci: " + bci);
                        }
                        for (int i = 0; i < fieldSlotCount; i++){
                            state.stack[state.stackTop++] = null; //TODO: change to type of the field
                        }
                    } else if (opcode == PUTFIELD || opcode == PUTSTATIC){ // TODO: record the stack top types to resAtBCI
                        //pops from the statck:
                        if (state.stackTop < fieldSlotCount) {
                            throw new AssertionError("Not enough stack elements at bci: " + bci);
                        }
                        for (int i = 0; i < fieldSlotCount; i++) {
                            state.stackTop--;
                            state.stack[state.stackTop] = null;
                        }
                    } else {
                        throw new AssertionError("Should not reach here in case get/put field");
                    }
                    break;
                case INVOKEVIRTUAL:
                case INVOKESPECIAL:
                case INVOKESTATIC:
                case INVOKEINTERFACE:
                    cpi = bs.readCPI(bci);
                    //following the logic in BytecodeNode getResolvedInvoke
                    MethodRefConstant methodRefConstant =
                        getConstantPool().resolvedMethodRefAt(
                            getDeclaringKlass(), cpi);
                    Method resolutionSeed = (Method) ((Resolvable.ResolvedConstant) methodRefConstant).value();
                    Klass symbolicRef = Resolution.getResolvedHolderKlass(
                        getConstantPool().methodAt(cpi), 
                        getConstantPool(),
                        getDeclaringKlass()
                        );
                    CallSiteType callSiteType = SiteTypes.callSiteFromOpCode(opcode);
                    ResolvedCall<Klass, Method, Field> resolvedCall = 
                        EspressoLinkResolver.resolveCallSiteOrThrow(ctx, getDeclaringKlass(), resolutionSeed, callSiteType, symbolicRef);
                    Method resolvedMethod = resolvedCall.getResolvedMethod();
                    Symbol<Type>[] signature = resolvedMethod.getParsedSignature();
                    int paramCnt = SignatureSymbols.parameterCount(signature);
                    TypeHints.TypeB[] argsHint = new TypeHints.TypeB[paramCnt];
                    for (int i = paramCnt - 1; i >= 0; i--){
                        Symbol<Type> cur = SignatureSymbols.parameterType(signature, i);
                        if (cur.byteAt(0) == 'J' || cur.byteAt(0) == 'D') {
                            assert state.stack[state.stackTop - 1] == null && state.stack[state.stackTop - 2] == null;
                            argsHint[i] = null;
                            state.stackTop -= 2;
                        } else {
                            argsHint[i] = state.stack[--state.stackTop];
                        }
                    }
                    this.resAtBCI[bci] = new TypeAnalysisResult(argsHint);
                    if (!resolvedMethod.isStatic()) {
                        assert state.stack[state.stackTop - 1] == null; // We should ban calling methods of Any (e.g. hashCode) on a value typed T
                        --state.stackTop;
                    }
                    
                    System.out.println("Resolved method: " + resolvedMethod.getName() + " at bci: " + bci 
                        + " with param count: " + paramCnt);

                    int returnValueSlotCount = resolvedMethod.getReturnKind().getSlotCount();

                    MethodReturnTypeAttribute invokedMethodReturnTypeAttribute = resolvedMethod.getMethodReturnTypeAttribute();
                    if (invokedMethodReturnTypeAttribute != null){
                        System.out.println("Invoked method return type: " + invokedMethodReturnTypeAttribute.getReturnType());
                        TypeHints.TypeB returnType = invokedMethodReturnTypeAttribute.getReturnType();
                        for (int i = 0; i < returnValueSlotCount; i++){
                            state.stack[state.stackTop++] = returnType.isNoHint() ? null : returnType;
                        }
                    } else {
                        //no type hints for the return value
                        for (int i = 0; i < returnValueSlotCount; i++){
                            state.stack[state.stackTop++] = null;
                        }
                    }
                    InstructionTypeArgumentsAttribute methodTypeArgumentsAttribute = 
                        getMethod().getInstructionTypeArgumentsAttribute();
                    if (methodTypeArgumentsAttribute != null) {
                        //TODO:
                    } else {
                        throw new AssertionError("Method " + getMethod().getName() + "does not have type arguments attribute");
                    }
                    break;
                case DUP:
                    assert state.stackTop > 0;
                    state.stack[state.stackTop] = state.stack[state.stackTop - 1];
                    state.stackTop++;
                    break;
                case SWAP:
                    assert state.stackTop > 1;
                    TypeHints.TypeB tmp = state.stack[state.stackTop - 1];
                    state.stack[state.stackTop - 1] = state.stack[state.stackTop - 2];
                    state.stack[state.stackTop - 2] = tmp;
                    break;
                default:
                    stackEffect = Bytecodes.stackEffectOf(opcode);
                    if (stackEffect > 0){
                        for (int i = 0; i < stackEffect; i++) {
                            state.stack[state.stackTop++] = null;
                        }
                    } else if (stackEffect < 0){
                         for (int i = 0; i < -stackEffect; i++) {
                            state.stackTop--;
                            state.stack[state.stackTop] = null;
                        }
                    }
                    if (state.stackTop < 0 || state.stackTop > maxStack) {
                        throw new AssertionError("Stack invalid at bci: " + bci);
                    }
                    break;
            }
            bci = bs.nextBCI(bci);
        }
        return state;
    }

    private Method getMethod() {
        return this.methodVersion.getMethod();
    }

    private RuntimeConstantPool getConstantPool(){
        return this.methodVersion.getPool();
    }

    private ObjectKlass getDeclaringKlass() {
        return this.methodVersion.getDeclaringKlass();
    }

    public TypeAnalysisResult[] getRes() {
        return resAtBCI;
    }
    
}
