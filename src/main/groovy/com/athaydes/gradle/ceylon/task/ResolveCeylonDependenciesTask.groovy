package com.athaydes.gradle.ceylon.task

import com.athaydes.gradle.ceylon.CeylonConfig
import com.athaydes.gradle.ceylon.parse.CeylonModuleParser
import com.athaydes.gradle.ceylon.util.DependencyTree
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class ResolveCeylonDependenciesTask {

    static final Logger log = Logging.getLogger( ResolveCeylonDependenciesTask )
    public static final String CEYLON_DEPENDENCIES = 'CeylonDependencies'

    static List inputs( Project project, CeylonConfig config ) {
        // lazily-evaluated elements
        [ { moduleFile( project, config ) }, { project.allprojects*.buildFile } ]
    }

    static def run( Project project, CeylonConfig config ) {
        File module = moduleFile( project, config )
        log.info( "Parsing Ceylon module file at ${module.path}" )

        if ( !module.file ) {
            throw new GradleException( 'Ceylon module file does not exist.' +
                    ' Please make sure that you set "sourceRoot" and "module"' +
                    ' correctly in the "ceylon" configuration.' )
        }

        def moduleDeclaration = parse module.path, module.text

        def mavenDependencies = moduleDeclaration.imports.findAll { it.name.contains( ':' ) }

        def existingDependencies = project.configurations.getByName( 'ceylonCompile' ).dependencies.collect {
            "${it.group}:${it.name}:${it.version}"
        }

        log.debug "Project existing dependencies: {}", existingDependencies

        mavenDependencies.each { Map dependency ->
            if ( !existingDependencies.contains(
                    "${dependency.name}:${dependency.version}" ) ) {
                addMavenDependency dependency, project
            } else {
                log.info "Not adding transitive dependencies of module " +
                        "$dependency as it already existed in the project"
            }
        }

        project.configurations*.resolve()

        def dependencyTree = dependencyTreeOf( project, moduleDeclaration )

        log.info( 'No dependency problems found!' )

        project.extensions.add( CEYLON_DEPENDENCIES, dependencyTree )
    }

    static File moduleFile( Project project, CeylonConfig config ) {
        if ( !config.module ) {
            log.error( '''|The Ceylon module has not been specified.
                          |To specify the name of your Ceylon module, add a declaration like
                          |the following to your build.gradle file:
                          |
                          |ceylon {
                          |  module = 'name.of.ceylon.module'
                          |}
                          |'''.stripMargin() )

            throw new GradleException( "The Ceylon module must be specified" )
        }

        def moduleNameParts = config.module.split( /\./ ).toList()

        List locations = [ ]
        for ( root in config.sourceRoots ) {
            def modulePath = ( [ root ] +
                    moduleNameParts +
                    [ 'module.ceylon' ] ).join( '/' )

            locations << modulePath
            def module = project.file( modulePath )
            if ( module.exists() ) return module
        }

        throw new GradleException( "Module file cannot be located. " +
                "Looked at the following locations: $locations" )
    }

    static DependencyTree dependencyTreeOf( Project project, Map moduleDeclaration ) {
        new DependencyTree( project, moduleDeclaration )
    }

    private static void addMavenDependency( Map dependency, Project project ) {
        log.info "Adding dependency: ${dependency.name}:${dependency.version}"
        project.dependencies.add( 'ceylonCompile', "${dependency.name}:${dependency.version}" )
    }

    private static Map parse( String name, String moduleText ) {
        new CeylonModuleParser().parse( name, moduleText )
    }

}
