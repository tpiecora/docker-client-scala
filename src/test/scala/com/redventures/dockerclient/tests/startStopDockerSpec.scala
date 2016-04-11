package com.redventures.dockerclient.tests

import com.redventures.dockerclient.DockerUtils
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class startStopDockerSpec extends FlatSpec with Matchers {

	"stop docker-machine" must "stop if already running or say already stopped" in {
		assert(DockerUtils.stopDockerMachine() == true)
	}

	"start docker-machine" must "start the vm" in {
	assert(DockerUtils.startDockerMachine() == true)
	}

	"start docker-machine" must "say already started" in {
		assert(DockerUtils.startDockerMachine() == true)
	}

	"stop docker-machine" must "stop the vm" in {
	assert(DockerUtils.stopDockerMachine() == true)
	}

	"stop docker-machine" must "say already stopped" in {
		assert(DockerUtils.stopDockerMachine() == true)
	}
}
