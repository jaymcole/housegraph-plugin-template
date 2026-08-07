package com.example.housegraph.hello.nodes;

import io.github.jaymcole.housegraph.graph.ProcessContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A node's logic is testable without a display, and without HouseGraph running — which is the
 * discipline the host project holds itself to as well. Note this never calls
 * {@code createNodeContent()}: the JavaFX toolkit isn't running here, and the interesting part of
 * a node is what {@code process} computes.
 */
class HelloWorldNodeTest {

    @Test
    void greetsTheNameOnItsInput() {
        HelloWorldNode node = new HelloWorldNode();
        node.getInputs().get(0).setValue("HouseGraph");

        node.process(ProcessContext.uncancelled());

        assertEquals("Hello, HouseGraph!", node.getOutputs().get(0).getValue());
    }

    @Test
    void fallsBackWhenNothingIsWiredIn() {
        HelloWorldNode node = new HelloWorldNode();

        node.process(ProcessContext.uncancelled());

        assertEquals("Hello, world!", node.getOutputs().get(0).getValue());
    }
}
