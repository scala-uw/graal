package com.oracle.truffle.espresso.analysis.typehints;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.espresso.analysis.BlockIterator;
import com.oracle.truffle.espresso.analysis.GraphBuilder;
import com.oracle.truffle.espresso.analysis.graph.Graph;
import com.oracle.truffle.espresso.analysis.graph.LinkedBlock;
import com.oracle.truffle.espresso.impl.Method;
import com.oracle.truffle.espresso.runtime.EspressoContext;


public class TypeHintAnalysis {
    public static TypeHintAnalysis analyze(Method.MethodVersion methodVersion){
        Method method = methodVersion.getMethod();
        Graph<? extends LinkedBlock> graph = GraphBuilder.build(method);
        System.out.println("Analyzing type hints for method: " + method.getName());
        System.out.println("Graph: " + graph);
        EspressoContext context = method.getContext();
        int codeLength = method.getOriginalCode().length;
        int maxLocals = methodVersion.getMaxLocals();
        int maxStack = methodVersion.getMaxStackSize();
        int totalBlocks = graph.totalBlocks();
        TypePropagationClosure closure = new TypePropagationClosure(context, codeLength, methodVersion, maxLocals, maxStack, totalBlocks);
        BlockIterator.analyze(method, graph, closure);
        System.out.println("Type hints analysis completed for method: " + method.getName());
        System.out.println("Resulting type hints: ");
        for (int i = 0; i < closure.getRes().length; i++) {
            TypeAnalysisResult res = closure.getRes()[i];
            if (res != null) System.out.println("BCI " + i + ": " + closure.getRes()[i]);
        }
        return new TypeHintAnalysis(closure.getRes());
    }

    @CompilationFinal(dimensions = 1)
    private final TypeAnalysisResult[] res;

    private TypeHintAnalysis(TypeAnalysisResult[] res) {
        this.res = res;
    }

    public TypeAnalysisResult[] getRes() {
        return res;
    }


}
