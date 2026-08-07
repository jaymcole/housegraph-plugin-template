package com.example.housegraph.hello.nodes;

import io.github.jaymcole.housegraph.annotations.Display;
import io.github.jaymcole.housegraph.annotations.Node;
import io.github.jaymcole.housegraph.graph.BaseNode;
import io.github.jaymcole.housegraph.graph.FlowPort;
import io.github.jaymcole.housegraph.graph.NodeVariable;
import io.github.jaymcole.housegraph.graph.ProcessContext;
import io.github.jaymcole.housegraph.sdk.NodeContentProvider;
import javafx.scene.control.Label;

/**
 * The minimal HouseGraph node: one input, one output, flow in and out, and an inline label showing
 * its last result.
 *
 * <p><b>Always annotate a node with {@code @Node.Type}.</b> It pins the id this node is written
 * under in save files, independent of the class name. Without it, renaming or moving this class
 * strands every graph anyone has saved using it. Prefix the id with your library's name so it can
 * never collide with another library's node type — the host resolves a collision by the owning
 * library recorded in the save file, but only files saved after both were installed carry that.
 */
@Display.Name("Hello World")
@Node.Type("hello.HelloWorldNode")
public class HelloWorldNode extends BaseNode implements NodeContentProvider {

    // manuallyEditable = true gives this input an inline text field, since String is a type the
    // host knows how to edit. For your own types, call ValueEditors.register(...).
    private final NodeVariable<String> who = new NodeVariable<>("Who", String.class, true);
    private final NodeVariable<String> greeting = new NodeVariable<>("Greeting", String.class);

    private Label label;

    @Override
    public void configureInputs() {
        addInput(who);
    }

    @Override
    public void configureOutputs() {
        addOutput(greeting);
    }

    @Override
    public void configureFlowInputs() {
        addFlowInput(new FlowPort("", FlowPort.Direction.IN));
    }

    @Override
    public void configureFlowOutputs() {
        addFlowOutput(new FlowPort("", FlowPort.Direction.OUT));
    }

    /**
     * Read inputs through the {@link ProcessContext} rather than off the variable directly: it
     * carries the values for <em>this</em> run, which is what keeps concurrent runs isolated.
     */
    @Override
    public void process(ProcessContext ctx) {
        greeting.setValue("Hello, " + ctx.get(who, "world") + "!");
    }

    @Override
    public javafx.scene.Node createNodeContent() {
        label = new Label("—");
        return label;
    }

    /**
     * Called after every run. This reaches you on the JavaFX thread — the engine dispatches it
     * through the host's callback executor — so you can touch your controls directly. Work you
     * start yourself (a socket, an HTTP call) is a different matter: keep that off the FX thread.
     */
    @Override
    protected void onExecuted() {
        if (label != null) {
            label.setText(greeting.getValue());
        }
    }
}
