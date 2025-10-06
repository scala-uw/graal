package jdk.graal.compiler.phases.common;

import jdk.graal.compiler.nodes.StructuredGraph;
import jdk.graal.compiler.phases.Phase;
import com.oracle.truffle.api.nodes.GuestBoxing;

public class GuestBoxingPhase extends Phase {

    public GuestBoxingPhase() {}

    @Override
    protected void run(StructuredGraph graph) {
        if (graph.method().getAnnotation(GuestBoxing.class) != null) System.out.println(graph.toString());
    }

}