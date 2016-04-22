package com.tpiecora.dockerclient.tests

import com.tpiecora.dockerclient._
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class getIpSpec extends FlatSpec with Matchers {

	"stopped docker-machine IP" must "be null" in {
		DockerUtils.stopDockerMachine()
		assert(DockerUtils.getIp() == null )
	}

	"running docker-machine IP" must "not be null" in {
		DockerUtils.startDockerMachine()
		assert(DockerUtils.getIp().length > 6 )
	}
}
