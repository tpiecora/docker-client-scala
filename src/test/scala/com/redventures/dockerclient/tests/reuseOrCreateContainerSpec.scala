package com.redventures.dockerclient.tests

import com.redventures.dockerclient.{DockerClient, DockerUtils}
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class reuseOrCreateContainerSpec extends FlatSpec with Matchers {
	val docker = new DockerClient(imageName = "tpiecora/atlas-storage:0.1")
	val docker2 = new DockerClient(imageName = "tpiecora/atlas-storage:0.1")
	val docker3 = new DockerClient(imageName = "tpiecora/atlas-storage:0.1")

	"list container" must "return the running container" in {
		DockerUtils.startDockerMachine()
		docker.reuseOrCreate()
		docker.waitForPort(8000, 30)
		assert(docker.checkPort(8000))
		assert(docker.checkContainer().isDefined)
	}

	"reuseOrCreate container reuse" must "reuse the existing container" in {
		docker2.reuseOrCreate()
		docker2.restart()
		docker2.waitForPort(8000)
		assert(docker2.checkPort(8000))
	}

	"reusedOrCreate container create" must "create the missing container" in {
		docker.kill()
		docker2.kill()
		docker2.remove()
		docker3.reuseOrCreate()
		docker3.waitForPort(8000)
		assert(docker2.checkContainer().isEmpty)

		assert(docker3.checkPort(8000))
	}

	"cleanup" must "remove all created containers" in {
		docker3.kill()
		assert(docker3.checkContainer().isEmpty)
		docker3.remove()
	}

}
