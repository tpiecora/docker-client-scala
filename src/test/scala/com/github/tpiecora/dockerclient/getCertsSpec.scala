package com.github.tpiecora.dockerclient

import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class getCertsSpec extends FlatSpec with Matchers with BeforeAndAfter{
	before(DockerUtils.startDockerMachine())

	"invalid vm" must "must return empty string" in {
		assert(DockerUtils.getCerts("some_invalid_vm_name") == "" )
	}

	"valid vm" must "return path to certs" in {
		assert(DockerUtils.getCerts("default").length > 1 )
	}
}
