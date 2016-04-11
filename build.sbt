


lazy val commonSettings = Seq(
	scalaVersion := "2.11.8",
	organization := "com.redventures",
	version := "0.1.0-SNAPSHOT",
	resolvers ++= Seq(
		//		"Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
		//		"twttr" at "https://maven.twttr.com/"
	)
)


lazy val dockerclient = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
	  libraryDependencies ++= Seq(
		  "org.scalatest" %% "scalatest" % "2.2.5" % Test,
		  "com.spotify"	% "docker-client" % "3.6.8"
	  )
  )

