package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class storageContainerSpec extends FlatSpec with Matchers {
	"storage service" must "be available" in {
		DockerUtils.startDockerMachine()
		val docker = new DockerClient(imageName = "tpiecora/atlas-storage:0.1")
		docker.initDocker()
		docker.waitForPort(8000, 30)
		assert(docker.checkPort(8000))
		docker.kill()
	}

}
