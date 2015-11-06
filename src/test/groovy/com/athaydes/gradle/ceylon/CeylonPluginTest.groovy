package com.athaydes.gradle.ceylon

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class CeylonPluginTest {

    @Test
    void "All tasks added to project"() {
        Project project = ProjectBuilder.builder()
                .withName( 'test-project' )
                .build()

        project.apply plugin: 'com.athaydes.ceylon'

        assert project.tasks.compileCeylon
        assert project.extensions.ceylon instanceof CeylonConfig
    }


}