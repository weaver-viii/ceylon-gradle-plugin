package com.athaydes.gradle.ceylon.task

import com.athaydes.gradle.ceylon.CeylonConfig
import com.athaydes.gradle.ceylon.util.MavenSettingsFileCreator
import org.gradle.api.Project

class CleanTask {

    static List inputs( Project project, CeylonConfig config ) {
        [ project.buildDir ] +
                GenerateOverridesFileTask.outputs( project, config ) +
                CompileCeylonTask.outputs( project, config ) +
                CompileCeylonTestTask.outputs( project, config ) +
                CreateDependenciesPomsTask.outputs( project, config ) +
                CreateMavenRepoTask.outputs( project, config ) +
                CreateJavaRuntimeTask.outputs( project, config ) +
                MavenSettingsFileCreator.mavenSettingsFile( project, config ) +
                CreateModuleDescriptorsTask.outputs( project, config )
    }

}
