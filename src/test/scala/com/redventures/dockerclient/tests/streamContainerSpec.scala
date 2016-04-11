package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class streamContainerSpec extends FlatSpec with Matchers {
	"storage service" must "be available" in {
		val ports = Set(4567)
		DockerUtils.startDockerMachine()
		val docker = new DockerClient(imageName = "tpiecora/atlas-stream:0.1", ports = ports)
		docker.initDocker()
		docker.waitForPort(4567, 30)
		assert(docker.checkPort(4567))
		docker.kill()
	}

}
