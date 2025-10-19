package jdk.graal.compiler.phases.common;

import java.util.Optional;

import jdk.graal.compiler.nodes.StructuredGraph;
import jdk.graal.compiler.nodes.Invoke;
import jdk.graal.compiler.nodes.ValueNode;
import jdk.graal.compiler.phases.Phase;
import jdk.graal.compiler.graph.NodeInputList;
import jdk.graal.compiler.graph.Node;
import jdk.graal.compiler.truffle.host.TruffleHostEnvironment;

import com.oracle.truffle.compiler.HostMethodInfo;

import jdk.graal.compiler.nodes.GraphState;
import jdk.vm.ci.meta.ResolvedJavaMethod;

public class GuestBoxingPhase extends Phase {


    public GuestBoxingPhase() {}

    @Override
    protected void run(StructuredGraph graph) {
        for (Invoke invoke : graph.getInvokes()) {
            if (isGuestUnboxing(invoke.getTargetMethod())) {
                NodeInputList<ValueNode> argsUnbox = invoke.callTarget().arguments();
                assert argsUnbox.size() == 2;
                ValueNode unboxSrc = argsUnbox.get(1);
                if (unboxSrc instanceof Invoke) {
                    Invoke unboxSrcInvoke = (Invoke) unboxSrc;
                    if (isGuestBoxing(unboxSrcInvoke.getTargetMethod())) {
                        NodeInputList<ValueNode> argsBox = unboxSrcInvoke.callTarget().arguments();
                        assert argsBox.size() == 2;
                        ValueNode boxSrc = argsBox.get(1);
                        assert invoke instanceof Node;
                        ((Node) invoke).replaceAtUsages(boxSrc);
                    }
                }
            }
        }
        
        if (graph.toString().indexOf("runIdentityInt") < 0) return;
        for (Invoke invoke : graph.getInvokes()) {
            if (isGuestBoxing(invoke.getTargetMethod()) || isGuestUnboxing(invoke.getTargetMethod())) {
                System.out.println(invoke.toString() + ": usage " + ((Node) invoke).getUsageCount());
                for (Node usage : ((Node) invoke).usages()) System.out.println(usage.toString());
            }
        }
    }

    private boolean isGuestBoxing(ResolvedJavaMethod method) {
        TruffleHostEnvironment env = TruffleHostEnvironment.get(method);
        if (env == null) return false;
        HostMethodInfo info = env.getHostMethodInfo(method);
        return info.isGuestBoxing();
    }

    private boolean isGuestUnboxing(ResolvedJavaMethod method) {
        TruffleHostEnvironment env = TruffleHostEnvironment.get(method);
        if (env == null) return false;
        HostMethodInfo info = env.getHostMethodInfo(method);
        return info.isGuestUnboxing();
    }

    @Override
    public Optional<NotApplicable> notApplicableTo(GraphState graphState) {
        return Optional.empty();
    }

}