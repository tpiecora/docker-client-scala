package com.tpiecora.dockerclient.tests

import com.tpiecora.dockerclient.DockerClient
import com.tpiecora.dockerclient._
import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class getCertsSpec extends FlatSpec with Matchers {
	"invalid vm" must "must return empty string" in {
		assert(DockerUtils.getCerts("some_invalid_vm_name") == "" )
	}

	"valid vm" must "return path to certs" in {
		assert(DockerUtils.getCerts().length > 1 )
	}
}
