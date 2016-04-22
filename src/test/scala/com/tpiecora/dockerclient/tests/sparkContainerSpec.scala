package com.tpiecora.dockerclient.tests

import com.tpiecora.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class sparkContainerSpec extends FlatSpec with Matchers {
	"spark container" must "be available" in {
		val ports = Set(8088)
		DockerUtils.startDockerMachine()
		val docker = new DockerClient(imageName = "tpiecora/atlas-spark:0.1")
		docker.reuseOrCreate(ports = ports)
		docker.waitForPort(8088, 30)
		assert(docker.checkPort(8088))
		assert(docker.checkContainer().isDefined)
		docker.kill()
	}

}
