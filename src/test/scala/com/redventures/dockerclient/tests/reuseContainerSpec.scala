package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class reuseContainerSpec extends FlatSpec with Matchers {
	val docker = new DockerClient(imageName = "tpiecora/atlas-storage:0.1")
	val docker2 = new DockerClient(imageName = "tpiecora/atlas-storage:0.1")
	"list container" must "return the running container" in {
		DockerUtils.startDockerMachine()
		docker.reuseOrCreate()
		docker.waitForPort(8000, 30)
		assert(docker.checkPort(8000))
		val first = docker.checkContainer("tpiecora/atlas-storage:0.1")
		assert(first.isDefined)
	}

	"reuse container" must "be take over and operate on a container started by another instance" in {
		docker2.reuseContainer(docker.checkContainer(docker2.imageName))
		docker2.restart()
		docker2.waitForPort(8000)
		assert(docker2.checkPort(8000))
	}

	"reused container kill" must "successfully kill the container" in {
		docker2.kill()
		assert(docker2.checkContainer(docker2.imageName).isEmpty)
	}

	"list container" must "return empty when the container was killed by another instance" in {
		assert(docker.checkContainer(docker.imageName).isEmpty)
	}

}
