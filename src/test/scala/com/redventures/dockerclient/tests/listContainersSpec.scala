package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class listContainersSpec extends FlatSpec with Matchers {
	val docker = new DockerClient(imageName = "tpiecora/atlas-storage:0.1")
	"list container" must "return the running container" in {
		DockerUtils.startDockerMachine()
		docker.reuseOrCreate()
		docker.waitForPort(8000, 30)
		assert(docker.checkPort(8000))
		val first = docker.checkContainer("tpiecora/atlas-storage:0.1")
		assert(first.isDefined)
		println(first)
		docker.kill()
	}

	"list container" must "return empty when the container is not running" in {
		val second = docker.checkContainer("tpiecora/atlas-storage:0.1")
		assert(second.isEmpty)
		docker.remove()
	}

}
