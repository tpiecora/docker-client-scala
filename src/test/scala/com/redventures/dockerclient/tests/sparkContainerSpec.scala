package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class sparkContainerSpec extends FlatSpec with Matchers {
	"spark container" must "be available" in {
		val ports = Set(8088)
		DockerUtils.startDockerMachine()
		val docker = new DockerClient(imageName = "tpiecora/atlas-spark:0.1", ports = ports)
		docker.reuseOrCreate()
		docker.waitForPort(8088, 30)
		assert(docker.checkPort(8088))
//		docker.kill()
	}

}
