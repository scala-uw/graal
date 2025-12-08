package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.espresso.analysis.BlockIterator;
import com.oracle.truffle.espresso.analysis.GraphBuilder;
import com.oracle.truffle.espresso.analysis.graph.Graph;
import com.oracle.truffle.espresso.analysis.graph.LinkedBlock;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.runtime.EspressoContext;


public class TypeHintAnalysis {
    public static TypeAnalysisResult[] analyze(Method.MethodVersion methodVersion) {
        Method method = methodVersion.getMethod();
        Graph<? extends LinkedBlock> graph = GraphBuilder.build(method);
        EspressoContext context = method.getContext();
        int codeLength = method.getOriginalCode().length;
        int maxLocals = methodVersion.getMaxLocals();
        int maxStack = methodVersion.getMaxStackSize();
        int totalBlocks = graph.totalBlocks();
        TypePropagationClosure closure = new TypePropagationClosure(context, codeLength, methodVersion, maxLocals, maxStack, totalBlocks);
        BlockIterator.analyze(method, graph, closure);
        return closure.getRes();
    }
}
