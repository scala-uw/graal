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
    private final TypeAnalysisResult[] resAtBlockEnd;
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
        for (int i = 0; i < codeLength; i++) {
            this.resAtBCI[i] = new TypeAnalysisResult(maxLocals, maxStack);
        }
        this.methodVersion = methodVersion;
        this.maxLocals = maxLocals;
        this.maxStack = maxStack;
        this.resAtBlockEnd = new TypeAnalysisResult[totalBlocks];
    }

    @Override
    public BlockIterator.BlockProcessResult processBlock(LinkedBlock block, BytecodeStream bs, AnalysisProcessor processor) {
        TypeAnalysisResult inState = mergePredecessors(block);
        TypeAnalysisResult outState = analyzeInBlock(block, bs, inState);
        TypeAnalysisResult previousState = this.resAtBlockEnd[block.id()];
        if (outState.equals(previousState)){
            return BlockIterator.BlockProcessResult.SKIP;
        } else {
            this.resAtBlockEnd[block.id()] = outState.copy();
            return BlockIterator.BlockProcessResult.DONE;
        }
    }

    private TypeAnalysisResult mergePredecessors(LinkedBlock block) {
        List<TypeAnalysisResult> states = new ArrayList<>();
        //root block: method entry
        if (block.predecessorsID().length == 0){
            TypeAnalysisResult rootState = new TypeAnalysisResult(maxLocals, maxStack);
            MethodParameterTypeAttribute methodParameterTypeAttribute = 
                getMethod().getMethodParameterTypeAttribute();
            assert methodParameterTypeAttribute != null : 
                "in mergePredecessors: Method " + getMethod().getName() + " does not have method parameter type attribute";
            TypeHints.TypeB[] methodParameterTypes = 
                methodParameterTypeAttribute.getParameterTypes();
            int localIndex = getMethod().isStatic() ? 0 : 1; //skip 'this' for non-static methods
            for (TypeHints.TypeB typeB: methodParameterTypes){
                rootState.locals[localIndex] = new TypeAnalysisResult.TypeInfo(typeB);
                localIndex++;
            }
            return rootState;
            
        }
        for (int predId : block.predecessorsID()){
            TypeAnalysisResult predState = this.resAtBlockEnd[predId];
            if (predState != null) {
                states.add(predState.copy());
            } else {
                states.add(new TypeAnalysisResult(maxLocals, maxStack));
            }
        }
        return TypeAnalysisResult.merge(states, maxLocals, maxStack);
    }

    private TypeAnalysisResult analyzeInBlock(LinkedBlock block, BytecodeStream bs, TypeAnalysisResult inState){
        TypeAnalysisResult state = inState.copy();
        int bci = block.start();
        while (bci <= block.end()){
            int opcode = bs.currentBC(bci);
            int cpi; int stackEffect;
            System.out.println("Processing opcode: " + Bytecodes.nameOf(opcode) + " at bci: " + bci);
            System.out.println("Current state: " + state);
            this.resAtBCI[bci] = state.copy(); //store the state before processing the opcode
            switch (opcode){
                case ASTORE:
                case ASTORE_0:
                case ASTORE_1:
                case ASTORE_2:
                case ASTORE_3:
                    int astoreIndex = opcode == ASTORE ? bs.readLocalIndex(bci) : (opcode - ASTORE_0); ;
                    System.out.println("Astore index: " + astoreIndex + " stack top: " + state.stack[state.stackTop - 1]);
                    if (state.stackTop > 0){
                        if (state.stack[state.stackTop - 1] == null){
                            state.locals[astoreIndex] = null;
                        }
                        else {
                            state.locals[astoreIndex] = state.stack[state.stackTop - 1].copy();
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
                    System.out.println("Aload index: " + aloadIndex + " locals at index: " + state.locals[aloadIndex]);
                    if (state.locals[aloadIndex] == null){
                        state.stack[state.stackTop] = null;
                    } else {
                        state.stack[state.stackTop] = state.locals[aloadIndex].copy();
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
                    stackEffect = Bytecodes.stackEffectOf(opcode) + fieldSlotCount;
                    if (opcode == GETSTATIC || opcode == GETFIELD){
                        //pushes onto the stack:
                        if (state.stackTop + stackEffect > maxStack) {
                            throw new AssertionError("Stack overflow at bci: " + bci);
                        }
                        for (int i = 0; i < stackEffect; i++){
                            state.stack[state.stackTop++] = null; //TODO: change to type of the field
                        }
                    } else if (opcode == PUTFIELD || opcode == PUTSTATIC){
                        //pops from the statck:
                        if (state.stackTop < stackEffect) {
                            throw new AssertionError("Not enough stack elements at bci: " + bci);
                        }
                        for (int i = 0; i < stackEffect; i++) {
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
                    int methodArgsCount = resolvedMethod.getArgumentCount();
                    
                    System.out.println("Resolved method: " + resolvedMethod.getName() + " at bci: " + bci 
                        + " with args count: " + methodArgsCount);

                    for (int i = 0; i < methodArgsCount; i++) {
                        if (state.stackTop > 0) {
                            state.stackTop--;
                            state.stack[state.stackTop] = null;
                        } else {
                            throw new AssertionError("Invoke without enough stack elements at bci: " + bci);
                        }
                    }

                    int returnValueSlotCount = resolvedMethod.getReturnKind().getSlotCount();

                    MethodReturnTypeAttribute invokedMethodReturnTypeAttribute = resolvedMethod.getMethodReturnTypeAttribute();
                    if (invokedMethodReturnTypeAttribute != null){
                        System.out.println("Invoked method return type: " + invokedMethodReturnTypeAttribute.getReturnType());
                        TypeHints.TypeB returnType = invokedMethodReturnTypeAttribute.getReturnType();
                        // int index = returnType.getIndex();
                        // byte kind = returnType.getKind();
                        for (int i = 0; i < returnValueSlotCount; i++){
                            state.stack[state.stackTop++] = returnType.isNoHint() ? null : new TypeAnalysisResult.TypeInfo(returnType);
                        }
                        // //the return type of the invoked method is known
                        // if (kind == TypeHints.TypeB.M_KIND){
                        //    TypeHints.TypeB retTypeB = new TypeB()
                        // } else if (kind == TypeHints.TypeB.K_KIND) {
                        //     //TODO
                        // } else if (kind == TypeHints.TypeB.ARR_K_KIND) {
                        //     //TODO
                        // } else if (kind == TypeHints.TypeB.ARR_M_KIND) {
                        //     //TODO
                        // }
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
                    if (state.stackTop > 0) {
                        if (state.stack[state.stackTop - 1] == null){
                             state.stack[state.stackTop] = null;
                        } else {
                            state.stack[state.stackTop] = state.stack[state.stackTop - 1].copy();
                        }
                        state.stackTop++;
                    } else {
                        throw new AssertionError("Dup without stack element at bci: " + bci);
                    }
                    break;
                case SWAP:
                    if (state.stackTop > 1) {
                        TypeAnalysisResult.TypeInfo tmp = state.stack[state.stackTop - 1];
                        state.stack[state.stackTop - 1] = state.stack[state.stackTop - 2];
                        if (tmp == null){
                            state.stack[state.stackTop - 2] = null;
                        } else {
                            state.stack[state.stackTop - 2] = tmp.copy();
                        }
                    } else {
                        throw new AssertionError("Swap without enough stack elements at bci: " + bci);
                    }
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

    public TypeAnalysisResult[] getResAtBCI() {
        return resAtBCI;
    }
    
}
