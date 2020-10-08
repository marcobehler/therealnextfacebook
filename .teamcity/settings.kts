import jetbrains.buildServer.configs.kotlin.v10.toExtId
import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2020.1"

project {

    vcsRoot(Repo)

    var chain = sequential {
        buildType(Mvn("Compile", "clean compile"))

        parallel {
            buildType(Mvn("Fast Test", "test", "-Dtest=\"*.unit.*Test\""))
            buildType(Mvn("Slow Test", "test", "-Dtest=\"*.integration.*Test\""))
        }

        val p = Mvn("Package", "package", "-DskipTests")
        p.triggers {
            vcs {
            }
        }
        buildType(p)
    }


    chain.buildTypes().forEach{ bt -> buildType (bt) }

    //buildTypesOrder = listOf(Build, FastTest, SlowTest, Package)
}

object Repo : GitVcsRoot ({
   /* name = "${DslContext.getParameter("gitRepo")} Repo"
    url = DslContext.getParameter("gitUrl")
    branch = DslContext.getParameter("gitBranch")
*/
    name = "My GitHub Repo"
    url = "https://github.com/marcobehler/therealnextfacebook.git"
    branch = "refs/heads/main"
})

class Mvn(val configurationName: String, val mavenGoals: String, val mavenRunnerArgs: String = "") : BuildType({
    id("Build_${configurationName}".toExtId())
    name = configurationName
    vcs {
        root(Repo)
    }

    steps {
        maven {
            goals = mavenGoals
            runnerArgs = mavenRunnerArgs
        }
    }
})