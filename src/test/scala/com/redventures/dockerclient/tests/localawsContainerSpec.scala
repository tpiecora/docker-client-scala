package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class localawsContainerSpec extends FlatSpec with Matchers {
	"storage service" must "be available" in {
		DockerUtils.startDockerMachine()
		val docker = new DockerClient(imageName = "netchris/localaws:v2")
		docker.initDocker()
		docker.waitForPort(8000, 60)
		docker.checkPort(8000)
	}

}
