package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class clusterContainerSpec extends FlatSpec with Matchers {
	"storage service" must "be available" in {
		val ports = Set(8080,8081,8088,8042,4040,443,80)
		DockerUtils.startDockerMachine()
		val docker = new DockerClient(imageName = "tpiecora/atlas-cluster:0.1", ports = ports)
		docker.initDocker()
		docker.waitForPort(8088, 30)
		assert(docker.checkPort(8088))
//		docker.kill()
	}

}
