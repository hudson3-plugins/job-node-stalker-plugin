package com.datalex.jenkins.plugins.nodestalker.wrapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hudson.matrix.MatrixProject;
import hudson.maven.MavenModuleSet;
import hudson.model.FreeStyleProject;
import java.io.IOException;

import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;

/**
 * Author: Fabio Neves, Baris Batiege Date: 29/04/13 Time: 10:53
 */
public class UtilTest extends HudsonTestCase {

//    @Test(expected = IllegalArgumentException.class)
//    public void testGetProjectWithNull() throws Exception {
//        Util.getProject(null);
//    }

    @Test
    public void testGetProjectWithEmptyString() throws Exception {
        assertNull(Util.getProject(""));
    }

    @Test
    public void testJobFound() throws Exception {
        FreeStyleProject expected = createFreeStyleProject("JobA");
        FreeStyleProject project = (FreeStyleProject) Util.getProject("JobA");
        assertNotNull(project);
        assertEquals(expected, project);
    }

    @Test
    public void testJobDoesNotExist() throws Exception {
        createFreeStyleProject("JobA");
        FreeStyleProject project = (FreeStyleProject) Util.getProject("JobB");
        assertNull(project);
    }

    @Test
    public void testMavenModuleSet() throws Exception {
        MavenModuleSet expected = createMavenProject("JobA");
        MavenModuleSet project = (MavenModuleSet) Util.getProject("JobA");
        assertNotNull(project);
        assertEquals(expected, project);
    }

    @Test
    public void testMatrixProject() throws Exception {
        MatrixProject expected = createMatrixProject("JobA");
        MatrixProject project = (MatrixProject) Util.getProject("JobA");
        assertNotNull(project);
        assertEquals(expected, project);
    }

    protected MavenModuleSet createMavenProject() throws IOException {
        return createMavenProject(createUniqueProjectName());
    }

    protected MavenModuleSet createMavenProject(String name) throws IOException {
        return hudson.createProject(MavenModuleSet.class, name);
    }
}
