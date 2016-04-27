package com.github.tpiecora.dockerclient

import java.net.{Inet4Address, InetAddress, NetworkInterface}

import scala.collection.JavaConverters._
import scala.sys.process._
import scala.util.Try
import scala.util.control.NonFatal
import scala.util.matching.Regex

object DockerUtils {

	// just a wrapper for trying shell commands that can throw exceptions.
	// returns either the output or an empty string
	var vm: String = "default"

	// this is a de facto environment check to see if docker-machine and docker are on the path
	// if docker-machine is then we know we are not running docker natively
	val dockerMachine = testCmd("docker-machine")
	val docker = testCmd("docker")

	def testCmd(cmd: ProcessBuilder): Boolean = {
		try {
			cmd !! ProcessLogger(stdout append _)
			true
		} catch {
			case _: Throwable => false
		}

	}

	def tryCmd(cmd: ProcessBuilder): String = {
		try {
			cmd !! ProcessLogger(stdout append _)
		} catch {
			case _: Throwable => ""
		}
	}

	def startDockerMachine(vm: String = vm): Boolean = {
		var res = false
		if (dockerMachine) {
			val checkRunning = tryCmd(s"docker-machine status $vm").trim()
			if (checkRunning == "Running") {
				res = true
				println(s"$vm already running.")
			} else {
				println(s"Starting $vm...")
				s"docker-machine start $vm" !!
				val checkAgain = tryCmd(s"docker-machine status $vm").trim()
				if (checkAgain == "Running") res = true;
				println(s"$vm was started.")
			}
		}
		res
	}

	def stopDockerMachine(vm: String = vm): Boolean = {
		var res = false
		if (dockerMachine) {
			val checkRunning = tryCmd(s"docker-machine status $vm").trim()
			if (checkRunning == "Stopped") {
				res = true
				println(s"$vm already stopped.")
			} else {
				println(s"Stopping $vm...")
				s"docker-machine stop $vm".!!
				val checkAgain = tryCmd(s"docker-machine status $vm").trim()
				if (checkAgain == "Stopped") res = true
				println(s"$vm was stopped.")
			}
		}
		res
	}

	def getApiAddress(vm: String = vm): String = {
		// using docker-machine, means we are not running native and so we need the IP of the VM
		val addr = if (dockerMachine) {
			val checkRunning = tryCmd(s"docker-machine status $vm").trim()
			if (checkRunning == "Stopped") {
				null
			} else {
				val ip = s"docker-machine ip $vm" !! ProcessLogger(stdout append _)
				ip.trim()
			}
		} else {
			//s"docker inspect c65767213eac | grep \"IPAddress\": | awk '{ print $2 }'"
			"unix:///var/run/docker.sock"
		}
		addr
	}

	def getCerts(vm: String = vm): String = {
		val stdout = new StringBuilder
		val dockerCertsR = new Regex("(?<=export DOCKER_CERT_PATH\\=\")[^\"]*?(?=\")")
		val envDocker = tryCmd(s"docker-machine env $vm")
		val dockerCertsPath = dockerCertsR findFirstIn envDocker
		dockerCertsPath.getOrElse("")
	}
}

