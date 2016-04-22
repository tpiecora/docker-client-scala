package com.tpiecora.dockerclient

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

	def tryCmd(cmd: ProcessBuilder): String = {
		try {
			cmd !! ProcessLogger(stdout append _)
		} catch {
			case _ => ""
		}
	}

	def startDockerMachine(vm: String = vm): Boolean = {
		val checkRunning = tryCmd("docker-machine ls" #| s"grep $vm" #| "grep Running")
		var res = false
		if (checkRunning.length > 1) {
			res = true
			println(s"$vm already running.")
		} else {
			println(s"Starting $vm...")
			s"docker-machine start $vm" !!
			val checkAgain = tryCmd("docker-machine ls" #| s"grep $vm" #| "grep Running")
			if (checkAgain.length > 1) res = true;
			println(s"$vm was started.")
		}
		res
	}

	def stopDockerMachine(vm: String = vm): Boolean = {
		val checkRunning = tryCmd("docker-machine ls" #| s"grep $vm" #| "grep Running")
		var res = false
		if (checkRunning.length < 1) {
			res = true
			println(s"$vm already stopped.")
		} else {
			println(s"Stopping $vm...")
			s"docker-machine stop $vm" !!
			val checkAgain = tryCmd("docker-machine ls" #| s"grep $vm" #| "grep Running")
			if (checkAgain.length < 1) res = true
			println(s"$vm was stopped.")
		}
		res
	}

	def getIp(vm: String = vm): String = {
		val checkRunning = tryCmd("docker-machine ls" #| s"grep $vm" #| "grep Running")
		if (checkRunning.length < 1) {
			null
		} else {
			val ip = s"docker-machine ip $vm" !! ProcessLogger(stdout append _)
			//println(ip)
			ip.trim()
		}
	}

	def getCerts(vm: String = vm): String = {
		val stdout = new StringBuilder
		val dockerCertsR = new Regex("(?<=export DOCKER_CERT_PATH\\=\")[^\"]*?(?=\")")
		val envDocker = tryCmd(s"docker-machine env $vm")
		val dockerCertsPath = dockerCertsR findFirstIn envDocker
		dockerCertsPath.getOrElse("")
	}
}

