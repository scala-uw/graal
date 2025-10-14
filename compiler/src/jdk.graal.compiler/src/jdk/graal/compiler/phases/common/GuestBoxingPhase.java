package jdk.graal.compiler.phases.common;

import java.util.Optional;

import jdk.graal.compiler.nodes.StructuredGraph;
import jdk.graal.compiler.phases.Phase;
import jdk.graal.compiler.truffle.host.TruffleHostEnvironment;

import com.oracle.truffle.compiler.HostMethodInfo;

import jdk.graal.compiler.nodes.GraphState;

public class GuestBoxingPhase extends Phase {


    public GuestBoxingPhase() {}

    @Override
    protected void run(StructuredGraph graph) {
        TruffleHostEnvironment env = TruffleHostEnvironment.get(graph.method());
        if (env == null) return;
        HostMethodInfo info = env.getHostMethodInfo(graph.method());
        if (graph.toString().indexOf("unboxInteger") >= 0) System.out.println(graph.toString() + ": " + info.isGuestBoxing() + info.isGuestUnboxing());
        if (info.isGuestBoxing() || info.isGuestUnboxing()) System.out.println(graph.toString());
    }

    @Override
    public Optional<NotApplicable> notApplicableTo(GraphState graphState) {
        return Optional.empty();
    }

}