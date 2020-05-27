package de.viadee.bpm.vPAV;

import de.viadee.bpm.vPAV.config.model.RuleSet;
import de.viadee.bpm.vPAV.processing.ElementGraphBuilder;
import de.viadee.bpm.vPAV.processing.ProcessVariablesScanner;
import de.viadee.bpm.vPAV.processing.code.flow.BasicNode;
import de.viadee.bpm.vPAV.processing.code.flow.FlowAnalysis;
import de.viadee.bpm.vPAV.processing.model.data.ElementChapter;
import de.viadee.bpm.vPAV.processing.model.graph.Graph;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SubprocessTest {

    @BeforeClass
    public static void setup() throws MalformedURLException {
        final File file = new File(".");
        final String currentPath = file.toURI().toURL().toString();
        final URL classUrl = new URL(currentPath + "src/test/java");
        final URL[] classUrls = { classUrl };
        ClassLoader cl = new URLClassLoader(classUrls);
        RuntimeConfig.getInstance().setClassLoader(cl);
        RuntimeConfig.getInstance().setTest(true);
    }

    @Test
    public void testSubprocess() {
        BpmnModelInstance modelInstance = Bpmn.createProcess("MyProcess").startEvent("MyStartEvent").subProcess()
                .embeddedSubProcess().startEvent("MyStartEventSub")
                .serviceTask("MyTaskSub")
                .endEvent("MyEndEventSub")
                .subProcessDone()
                .endEvent("MyEndEvent")
                .done();

        Map<String, BpmnModelInstance> processIdToModelInstance = new HashMap<>();
        processIdToModelInstance.put("MyProcess", modelInstance);
        ElementGraphBuilder graphBuilder = new ElementGraphBuilder(processIdToModelInstance);
        FlowAnalysis flowAnalysis = new FlowAnalysis();
        Collection<Graph> graphCollection = graphBuilder
                .createProcessGraph(new FileScanner(new RuleSet()), modelInstance, "MyProcess", null,
                        new ProcessVariablesScanner(null), flowAnalysis);
        flowAnalysis.analyze(graphCollection);

        Assert.assertEquals("MyStartEvent",
                flowAnalysis.getNodes().get("MyStartEventSub").getPredecessors().get(0).getPredecessors().get(0)
                        .getGraphId());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().size());
        Assert.assertEquals("MyStartEventSub",
                flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().get(0).getGraphId());
        Assert.assertEquals("MyEndEventSub",
                flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors().get(0)
                        .getGraphId());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors().size());
        Assert.assertEquals("MyEndEvent",
                flowAnalysis.getNodes().get("MyEndEventSub").getSuccessors().get(0).getSuccessors().get(0)
                        .getGraphId());
    }

    @Test
    public void testSubprocessStartListener() {
        BpmnModelInstance modelInstance = Bpmn.createProcess("MyProcess").startEvent("MyStartEvent")
                .subProcess("MySubProcess")
                .camundaExecutionListenerExpression("start", "${variable}")
                .embeddedSubProcess().startEvent("MyStartEventSub")
                .serviceTask("MyTaskSub")
                .endEvent("MyEndEventSub")
                .subProcessDone()
                .endEvent("MyEndEvent")
                .done();

        Map<String, BpmnModelInstance> processIdToModelInstance = new HashMap<>();
        processIdToModelInstance.put("MyProcess", modelInstance);
        ElementGraphBuilder graphBuilder = new ElementGraphBuilder(processIdToModelInstance);
        FlowAnalysis flowAnalysis = new FlowAnalysis();
        Collection<Graph> graphCollection = graphBuilder
                .createProcessGraph(new FileScanner(new RuleSet()), modelInstance, "MyProcess", null,
                        new ProcessVariablesScanner(null), flowAnalysis);
        flowAnalysis.analyze(graphCollection);

        Assert.assertEquals("MySubProcess__0",
                flowAnalysis.getNodes().get("MyStartEventSub").getPredecessors().get(0).getGraphId());
        Assert.assertEquals("MySubProcess__0",
                flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().get(0).getGraphId());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().size());
        Assert.assertEquals("MyEndEventSub",
                flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors().get(0)
                        .getGraphId());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors().size());
        Assert.assertEquals("MyEndEvent",
                flowAnalysis.getNodes().get("MyEndEventSub").getSuccessors().get(0).getSuccessors().get(0)
                        .getGraphId());
    }

    @Test
    public void testSubprocessEndListener() {
        BpmnModelInstance modelInstance = Bpmn.createProcess("MyProcess").startEvent("MyStartEvent")
                .subProcess("MySubProcess")
                .camundaExecutionListenerExpression("end", "${variable}")
                .embeddedSubProcess().startEvent("MyStartEventSub")
                .serviceTask("MyTaskSub")
                .endEvent("MyEndEventSub")
                .subProcessDone()
                .endEvent("MyEndEvent")
                .done();

        Map<String, BpmnModelInstance> processIdToModelInstance = new HashMap<>();
        processIdToModelInstance.put("MyProcess", modelInstance);
        ElementGraphBuilder graphBuilder = new ElementGraphBuilder(processIdToModelInstance);
        FlowAnalysis flowAnalysis = new FlowAnalysis();
        Collection<Graph> graphCollection = graphBuilder
                .createProcessGraph(new FileScanner(new RuleSet()), modelInstance, "MyProcess", null,
                        new ProcessVariablesScanner(null), flowAnalysis);
        flowAnalysis.analyze(graphCollection);

        Assert.assertEquals("MyStartEvent",
                flowAnalysis.getNodes().get("MyStartEventSub").getPredecessors().get(0).getPredecessors().get(0)
                        .getGraphId());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().size());
        Assert.assertEquals("MyStartEventSub",
                flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().get(0).getGraphId());

        Assert.assertEquals("MySubProcess__0",
                flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors().get(0)
                        .getGraphId());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors().size());
        Assert.assertEquals("MySubProcess__0",
                flowAnalysis.getNodes().get("MyEndEventSub").getSuccessors().get(0)
                        .getGraphId());
    }

    @Test
    public void testSubprocessBothListener() {
        BpmnModelInstance modelInstance = Bpmn.createProcess("MyProcess").startEvent("MyStartEvent")
                .subProcess("MySubProcess")
                .camundaExecutionListenerExpression("start", "${variable2}")
                .camundaExecutionListenerExpression("end", "${variable}")
                .embeddedSubProcess().startEvent("MyStartEventSub")
                .serviceTask("MyTaskSub")
                .endEvent("MyEndEventSub")
                .subProcessDone()
                .endEvent("MyEndEvent")
                .done();

        Map<String, BpmnModelInstance> processIdToModelInstance = new HashMap<>();
        processIdToModelInstance.put("MyProcess", modelInstance);
        ElementGraphBuilder graphBuilder = new ElementGraphBuilder(processIdToModelInstance);
        FlowAnalysis flowAnalysis = new FlowAnalysis();
        Collection<Graph> graphCollection = graphBuilder
                .createProcessGraph(new FileScanner(new RuleSet()), modelInstance, "MyProcess", null,
                        new ProcessVariablesScanner(null), flowAnalysis);
        flowAnalysis.analyze(graphCollection);

        Assert.assertEquals(ElementChapter.ExecutionListenerStart,
                ((BasicNode) flowAnalysis.getNodes().get("MyStartEventSub").getPredecessors().get(0))
                        .getElementChapter());
        Assert.assertEquals(ElementChapter.ExecutionListenerStart,
                ((BasicNode) flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().get(0))
                        .getElementChapter());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyStartEvent").getSuccessors().get(0).getSuccessors().size());

        Assert.assertEquals(ElementChapter.ExecutionListenerEnd,
                ((BasicNode) flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors()
                        .get(0)).getElementChapter());
        Assert.assertEquals(1,
                flowAnalysis.getNodes().get("MyEndEvent").getPredecessors().get(0).getPredecessors().size());
        Assert.assertEquals(ElementChapter.ExecutionListenerEnd,
                ((BasicNode) flowAnalysis.getNodes().get("MyEndEventSub").getSuccessors().get(0)).getElementChapter());

    }

    public void testSubprocessInSubprocess() {
// TODO
    }

}
