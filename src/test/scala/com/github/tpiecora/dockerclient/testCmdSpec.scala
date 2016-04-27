package com.github.tpiecora.dockerclient

import org.scalatest.{FlatSpec, Matchers}

/**
  * Created by mpiecora on 4/11/16.
  */
class testCmdSpec extends FlatSpec with Matchers {
	"testCmd with valid command" must "return true" in {
		assert(DockerUtils.testCmd("date"))
	}

	"testCmd with invalid command" must "return false" in {
		assert(!DockerUtils.testCmd("asdf"))
	}

}
