package org.jvnet.jenkins.plugins.nodestalker.wrapper;

import hudson.model.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created with IntelliJ IDEA.
 * User: barisbatiege
 * Date: 4/24/13
 * Time: 1:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class UtilTest  {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test(expected = IllegalArgumentException.class)
    public void testWithNullJob() throws Exception {
        String node = Util.getNodeJobLastRan(null);
    }

    @Test
    public void testWithEmptyString() throws Exception {
        assertNull(Util.getNodeJobLastRan(""));
    }

    @Test
    public void testFollowedJobRanOnNode1() throws Exception {
        Slave slave = j.createSlave("Node1","label1",null);
        FreeStyleProject parent = j.createFreeStyleProject("JobA");
        parent.setAssignedLabel(Label.get("label1"));
        FreeStyleBuild build = parent.scheduleBuild2(0).get();

        String node = Util.getNodeJobLastRan("JobA");
        assertEquals("Node1", node);
    }


}