import com.typesafe.sbt.packager.docker.DockerChmodType
import ReleaseTransformations._

// Versions
lazy val scala2Version = "2.13.5"

// Library versions
val zioVersion = "1.0.4-2"
val grpcVersion = "1.36.0"

// Graal/JVM stuff. Needs to be available here https://github.com/orgs/graalvm/packages/container/graalvm-ce/versions
val jvmVersion = "11"
val graalVersion = "21.0.0.2"
val baseGraalOptions = Seq(
  "--verbose",
  "--no-fallback",
  "--no-server",
  "--install-exit-handlers",
  "--allow-incomplete-classpath",
  "--enable-http",
  "--enable-https",
  "--enable-url-protocols=https,http",
  "--initialize-at-build-time",
  "--report-unsupported-elements-at-runtime",
  "-H:+RemoveSaturatedTypeFlows",
  "-H:+ReportExceptionStackTraces",
  "-H:-ThrowUnsafeOffsetErrors",
  "-H:+PrintClassInitialization"
)

// Global settings
Global / cancelable := false
Global / onChangedBuildSource := ReloadOnSourceChanges

// Variables
lazy val baseName = "prokzio"
lazy val baseImage = "alpine:3.13.1"
lazy val dockerBasePath = "/opt/docker/bin"

// Libraries
lazy val zioLibs = Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-streams" % zioVersion,
  "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
  "dev.zio" %% "zio-test-magnolia" % zioVersion % Test
)

lazy val grpcLibs = Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  "io.grpc" % "grpc-netty" % grpcVersion
)

// (Sub) projects
lazy val prokzio = (project in file("."))
  .aggregate(
    util,
    service
  )
  .settings(
    crossScalaVersions := Nil,
    publish / skip := true,
    name := baseName
  )

lazy val util = (project in file("util"))
  .settings(
    commonSettings,
    testSettings,
    scalafmtSettings,
    name := "util"
  )

lazy val service = createProjectModule(
  "service",
  "HTTP Proxy Service. For now just Hello World",
  "xyz.graphiq.prokzio.service.HelloWorld"
)

// methods
def createProjectModule(
    moduleName: String,
    moduleDescription: String,
    runClass: String
): Project =
  Project(moduleName, file(moduleName))
    .enablePlugins(NativeImagePlugin, GraalVMNativeImagePlugin, DockerPlugin)
    .settings(
      name := "service",
      description := moduleDescription,
      Compile / mainClass := Some(runClass),
      dockerBinaryPath := s"$dockerBasePath/$moduleName",
      commonSettings,
      testSettings,
      grpcSettings,
      scalafmtSettings,
      graalLocalSettings,
      graalDockerSettings
    )
    .dependsOn(
      util
    )

// Settings
lazy val commonSettings = Seq(
  organization := "xyz.graphiq",
  scalaVersion := scala2Version,
  libraryDependencies ++= zioLibs ++ grpcLibs,
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "UTF-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
    "-language:experimental.macros", // Allow macro definition (besides implementation and application)
    "-language:higherKinds", // Allow higher-kinded types
    "-language:implicitConversions", // Allow definition of implicit functions called views
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
    "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
    "-Xlint:option-implicit", // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
//    "-Ywarn-unused:imports", // Warn if an import selector is not referenced. // Doesn't work with autogenerated code
    "-Ywarn-unused:locals", // Warn if a local definition is unused.
//    "-Ywarn-unused:params", // Warn if a value parameter is unused. // Doesn't work with autogenerated code
    "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates", // Warn if a private member is unused.
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-value-discard", // Warn when non-Unit expression results are unused.
    s"-target:jvm-$jvmVersion"
  ),
  javacOptions ++= Seq("-source", jvmVersion, "-target", jvmVersion)
)

lazy val testSettings = Seq(
  Test / testOptions += Tests.Argument("-oDT"),
  Test / parallelExecution := true,
  Test / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)

lazy val scalafmtSettings = Seq(
  scalafmtOnCompile in ThisBuild := true
)

lazy val grpcSettings = Seq(
  Compile / PB.targets := Seq(
    scalapb.gen(grpc = true) -> (sourceManaged in Compile).value,
    scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value
  )
)

lazy val graalLocalSettings = Seq(
  Global / excludeLintKeys += nativeImageVersion,
  nativeImageVersion := graalVersion,
  Global / excludeLintKeys += nativeImageJvm,
  nativeImageJvm := s"graalvm-java$jvmVersion",
  nativeImageOptions ++= baseGraalOptions,
  nativeImageOutput := file("output") / name.value
)

lazy val graalDockerSettings = Seq(
  GraalVMNativeImage / containerBuildImage := GraalVMNativeImagePlugin
    .generateContainerBuildImage(s"ghcr.io/graalvm/graalvm-ce:java$jvmVersion-$graalVersion")
    .value,
  graalVMNativeImageOptions ++= baseGraalOptions ++ Seq(
    "--static"
  ),
  dockerBaseImage := baseImage,
  dockerChmodType := DockerChmodType.Custom("ugo=rwX"),
  dockerAdditionalPermissions += (DockerChmodType.Custom(
    "ugo=rwx"
  ), dockerBinaryPath.value),
  mappings in Docker := Seq(
    ((target in GraalVMNativeImage).value / name.value) -> dockerBinaryPath.value
  ),
  dockerExposedPorts := Seq(9000, 9001), // TODO <- correct this with config?
  dockerEntrypoint := Seq(dockerBinaryPath.value),
  dockerRepository := sys.env.get("DOCKER_REPOSITORY"),
  dockerAlias := DockerAlias(
    dockerRepository.value,
    dockerUsername.value,
    s"$baseName/$baseName-${name.value}".toLowerCase,
    Some(version.value)
  ),
  dockerUpdateLatest := true,
  dockerUsername := sys.env.get("DOCKER_USERNAME").map(_.toLowerCase)
)

lazy val dockerBinaryPath = settingKey[String]("Get the docker path")

// Release
val nextReleaseBump = sbtrelease.Version.Bump.Minor

def publishNativeDocker(project: Project): ReleaseStep =
  ReleaseStep(
    action = { beginState: State =>
      val extracted = Project.extract(beginState)
      Seq(
        (state: State) => extracted.runTask(packageBin in GraalVMNativeImage in project, state),
        (state: State) => extracted.runTask(publish in Docker in project, state)
      ).foldLeft(beginState) { case (newState, runTask) =>
        runTask(newState)._1
      }
    }
  )

val commonPreReleaseSteps: Seq[ReleaseStep] = Seq(
  checkSnapshotDependencies,
  inquireVersions,
  runClean, // <-- TODO Run Lint/fmt
  runTest
)

val publishSteps: Seq[ReleaseStep] = Seq(
  publishNativeDocker(service)
)

val releaseProcessBumpAndTag: Seq[ReleaseStep] =
  commonPreReleaseSteps ++
    Seq(setReleaseVersion) ++
    publishSteps ++
    Seq(
      commitReleaseVersion,
      tagRelease,
      pushChanges
    ) // <-- TODO Add changelog / TODO add coverage analyzer badges etc

val releaseProcessSnapshotBump: Seq[ReleaseStep] = commonPreReleaseSteps ++
  Seq(setNextVersion) ++
  publishSteps ++
  Seq(ReleaseStep(commitNextVersion), pushChanges)

def bumpedVersion(bump: sbtrelease.Version.Bump, state: State)(version: String): String = {
  sbtrelease
    .Version(version)
    .map {
      case v if version == v.withoutQualifier.string =>
        v.bump(bump).withoutQualifier.string
      case v => v.withoutQualifier.string
    }
    .getOrElse(sbtrelease.versionFormatError(version))
}

def nextSnapshotVersion(bump: sbtrelease.Version.Bump, state: State)(version: String): String = {
  val shortHashLength = 7
  val shortHash = vcs(state).currentHash.substring(0, shortHashLength)
  sbtrelease
    .Version(version)
    .map(
      _.copy(qualifier = Some(s"-$shortHash-SNAPSHOT")).string
    )
    .getOrElse(sbtrelease.versionFormatError(version))
}

def bump(bump: sbtrelease.Version.Bump, steps: Seq[ReleaseStep])(
    state: State
): State = {
  Command.process(
    "release with-defaults",
    Project
      .extract(state)
      .appendWithoutSession(
        Seq(
          releaseVersionBump := bump,
          releaseProcess := steps,
          releaseVersion := bumpedVersion(bump, state),
          releaseNextVersion := nextSnapshotVersion(releaseVersionBump.value, state)
        ),
        state
      )
  )
}

def vcs(state: State): sbtrelease.Vcs =
  Project
    .extract(state)
    .get(releaseVcs)
    .getOrElse(
      sys.error("Aborting release. Working directory is not a repository of a recognized VCS.")
    )

commands += Command.command("bumpRelease")(
  bump(nextReleaseBump, releaseProcessBumpAndTag)
)
commands += Command.command("bumpSnapshot")(
  bump(nextReleaseBump, releaseProcessSnapshotBump)
)

/*
lazy val changelogTemplatePath    = settingKey[Path]("Path to CHANGELOG.md template")
lazy val changelogDestinationPath = settingKey[Path]("Path to CHANGELOG.md destination")
lazy val changelogGenerate        = taskKey[Unit]("Generates CHANGELOG.md file based on git log")

changelogGenerate := {
  val changelog = ChangeLogger.generateChangelogString(
    changelogTemplatePath.value,
    version.value,
    LocalDate.now(),
    unreleasedCommits.value.map(_.msg)
  )

  IO.write(changelogDestinationPath.value.toFile, changelog.getBytes(StandardCharsets.UTF_8))
}
 */
