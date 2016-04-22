package com.tpiecora.dockerclient.tests

import com.tpiecora.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class localawsContainerSpec extends FlatSpec with Matchers {
	"storage service" must "be available" in {
		DockerUtils.startDockerMachine()
		val docker = new DockerClient(imageName = "netchris/localaws:v2")
		docker.reuseOrCreate()
		docker.waitForPort(8000, 60)
		assert(docker.checkPort(8000))
		docker.kill()
		docker.remove()
	}

}
