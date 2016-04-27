package com.github.tpiecora.dockerclient

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class getIpSpec extends FlatSpec with Matchers {

	"stopped docker-machine IP" must "be null" in {
		DockerUtils.stopDockerMachine()
		assert(DockerUtils.getApiAddress() == null )
	}

	"running docker-machine IP" must "not be null" in {
		DockerUtils.startDockerMachine()
		assert(DockerUtils.getApiAddress().length > 6 )
	}
}
