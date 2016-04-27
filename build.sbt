


lazy val commonSettings = Seq(
	crossScalaVersions := Seq("2.11.7", "2.10.6"),
	organization := "com.github.tpiecora",
	version := "0.2.0",
	resolvers ++= Seq()
)


lazy val dockerclient = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
	  libraryDependencies ++= Seq(
		  "org.scalatest" %% "scalatest" % "2.2.5" % Test,
		  "com.spotify"	% "docker-client" % "3.6.8"
	  )
  )

// These things are for publishing to Sonatype
publishMavenStyle := true
publishTo := {
	val nexus = "https://oss.sonatype.org/"
	if (isSnapshot.value)
		Some("snapshots" at nexus + "content/repositories/snapshots")
	else
		Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
pomExtra := (
  <url>http://github.com/tpiecora/docker-client-scala</url>
	<licenses>
		<license>
			<name>BSD-style</name>
			<url>http://www.opensource.org/licenses/bsd-license.php</url>
			<distribution>repo</distribution>
		</license>
	</licenses>
	<scm>
		<url>git@github.com:tpiecora/dockerclient.git</url>
		<connection>scm:git:git@github.com:tpiecora/dockerclient.git</connection>
	</scm>
	<developers>
		<developer>
			<id>tpiecora</id>
			<name>Ted Piecora</name>
			<url>http://tedpiecora.com</url>
		</developer>
	</developers>)