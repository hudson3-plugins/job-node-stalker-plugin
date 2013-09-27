package com.datalex.jenkins.plugins.nodestalker.wrapper;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Label;
import org.junit.Test;
import util.LogHandler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.junit.Assert.*;
import org.jvnet.hudson.test.HudsonTestCase;
import static org.mockito.Mockito.*;

/**
 * Author: Fabio Neves, Baris Batiege
 * Date: 29/04/13
 * Time: 14:47
 */
public class MyNodeAssignementActionFunctionalTest extends HudsonTestCase{

    @Test
    public void testUpdateWorkspaceExistingFollowedProject() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject followed = createFreeStyleProject("JobA");
        FreeStyleBuild build = followed.scheduleBuild2(0).get();

        FreeStyleProject mockedProject = mock(FreeStyleProject.class);
        new MyNodeAssignmentAction().updateWorkspace(followed, mockedProject);
        verify(mockedProject, times(1)).setCustomWorkspace(build.getWorkspace().getRemote());
    }


    @Test
    public void testPluginIsnotPresentDefaultLabelIsGiven() throws IOException {
        FreeStyleProject followed = createFreeStyleProject("JobA");
        Label expected = Label.get("label1");
        followed.setAssignedLabel(expected);
        MyNodeAssignmentAction action = spy(new MyNodeAssignmentAction());
        Label result = action.getAssignedLabel(followed);
        assertEquals(expected, result);
    }

    @Test
    public void testPluginIsPresentButNoJobWasSpecified() throws Exception {
        createSlave("Node1", "label1", null);
        FreeStyleProject followed = createFreeStyleProject("JobA");
        NodeStalkerBuildWrapper plugin = new NodeStalkerBuildWrapper("", false);
        followed.addBuildWrapper(plugin);
        Label expected = Label.get("label1");
        followed.setAssignedLabel(expected);
        MyNodeAssignmentAction action = spy(new MyNodeAssignmentAction());
        Label result = action.getAssignedLabel(followed);
        assertEquals("label1", result.getDisplayName());
    }

    @Test
    public void testPluginIsPresentAndJobWasSpecified() throws Exception {
        FreeStyleProject followed = createFreeStyleProject("JobA");
        createSlave("Node1", "label1", null);
        Label expected = Label.get("label1");
        followed.setAssignedLabel(expected);
        followed.scheduleBuild2(0).get();
        FreeStyleProject stalkerProject = createFreeStyleProject("JobB");
        NodeStalkerBuildWrapper plugin = new NodeStalkerBuildWrapper("JobA", false);
        stalkerProject.addBuildWrapper(plugin);

        Label result = new MyNodeAssignmentAction().getAssignedLabel(stalkerProject);
        assertNotNull(result);
        assertEquals("Node1", result.getDisplayName());
    }

    @Test
    public void testPluginIsPresentAndJobWasSpecifiedSharingWorkspace() throws Exception {
        FreeStyleProject followed = createFreeStyleProject("JobA");
        createSlave("Node1", "label1", null);
        Label expected = Label.get("label1");
        followed.setAssignedLabel(expected);
        followed.scheduleBuild2(0).get();
        FreeStyleProject stalkerProject = createFreeStyleProject("JobB");
        NodeStalkerBuildWrapper plugin = new NodeStalkerBuildWrapper("JobA", true);
        stalkerProject.addBuildWrapper(plugin);
        MyNodeAssignmentAction action = spy(new MyNodeAssignmentAction());
        Label result = action.getAssignedLabel(stalkerProject);
        assertNotNull(result);
        assertEquals("Node1", result.getDisplayName());
        verify(action, times(1)).updateWorkspace(followed, stalkerProject);
        assertEquals(followed.getSomeWorkspace().getRemote(), stalkerProject.getCustomWorkspace());
    }
    
    @Test
    public void testPluginIsPresentAndFollowedJobDoesNotExist() throws Exception {
        FreeStyleProject followed = createFreeStyleProject("JobA");
        createSlave("Node1", "label1", null);
        Label expected = Label.get("label1");
        followed.setAssignedLabel(expected);
        followed.scheduleBuild2(0).get();

        FreeStyleProject stalkerProject = createFreeStyleProject("JobB");
        NodeStalkerBuildWrapper plugin = new NodeStalkerBuildWrapper("JobASD", true);
        stalkerProject.addBuildWrapper(plugin);
        MyNodeAssignmentAction action = new MyNodeAssignmentAction();
        MyNodeAssignmentAction.logger.setLevel(Level.ALL);
        MyNodeAssignmentAction.logger.setUseParentHandlers(false);
        LogHandler handler = new LogHandler();
        handler.setLevel(Level.ALL);
        MyNodeAssignmentAction.logger.addHandler(handler);

        Label result = action.getAssignedLabel(stalkerProject);
        assertEquals("master", result.getDisplayName());
        List<LogRecord> logs = handler.getRecords();
        assertEquals(1,logs.size());
        assertEquals(Level.WARNING, logs.get(0).getLevel());
        String logMessage = "Could not get the job for JobASD. Custom workspace will not be set";
        assertEquals(logMessage, logs.get(0).getMessage());
    }

    @Test
    public void testPluginIsPresentAndFollowedJobDoesExistButHasNeverRun() throws Exception {
        FreeStyleProject followed = createFreeStyleProject("JobA");
        createSlave("Node1", "label1", null);
        Label expected = Label.get("label1");
        followed.setAssignedLabel(expected);
        FreeStyleProject stalkerProject = createFreeStyleProject("JobB");
        NodeStalkerBuildWrapper plugin = new NodeStalkerBuildWrapper("JobA", true);
        stalkerProject.addBuildWrapper(plugin);
        String expectedWorkspace = stalkerProject.getCustomWorkspace();
        MyNodeAssignmentAction action = spy(new MyNodeAssignmentAction());
        Label result = action.getAssignedLabel(stalkerProject);
        assertNull(result);
        verify(action, times(1)).updateWorkspace(followed, stalkerProject);
        assertEquals(expectedWorkspace, stalkerProject.getCustomWorkspace());
    }
}
