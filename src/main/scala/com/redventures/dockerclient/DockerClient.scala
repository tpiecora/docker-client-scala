package com.redventures.dockerclient

import java.net.URI
import java.nio.file.Paths

import com.spotify.docker.client.DockerClient._
import com.spotify.docker.client._
import com.spotify.docker.client.messages._

import scala.collection.JavaConverters._
import scala.sys.process.{ProcessLogger, _}
import scala.util.control.NonFatal
import scala.util.matching.Regex

/**
  * Created by mpiecora on 4/5/16.
  */
class DockerClient (
					 imageName: String,
					 env: Map[String, String] = Map().empty,
					 ports: Set[Int] = Set(8000),
					 binds: List[String] = List()
				   ) {
	var docker: DefaultDockerClient = _
	var containerId: String = _
	var dockerIp: String = _
	var externalPort: Int = _

	def execCreate(cmd: Array[String]): String = {
//		val noDetach = new ExecCreateParam("Detach", "false")
//		val stdout = new ExecCreateParam("AttachStdout", "true")
//		val stderr = new ExecCreateParam("AttachStderr", "true")

		val params = Seq(ExecCreateParam.tty(true))
		docker.execCreate(containerId, cmd, params:_*)
	}
	def execCreateSingle(cmd: String): String = {
		//		val noDetach = new ExecCreateParam("Detach", "false")
		//		val stdout = new ExecCreateParam("AttachStdout", "true")
		//		val stderr = new ExecCreateParam("AttachStderr", "true")
		val aCmd: Array[String] = Array(cmd)
		val params = Seq(ExecCreateParam.tty(true), ExecCreateParam.attachStderr(true))
		docker.execCreate(containerId, aCmd)
	}
	def execStart(execId: String): String = {
		val stream: LogStream = docker.execStart(execId)
		//Thread.sleep(3000)
		stream.readFully()
	}
	def execInspect(execId: String): ExecState = {
		docker.execInspect(execId)
	}

	def checkPort(port: Int): Boolean = {
		try {
			val socket = new java.net.Socket(dockerIp, port)
			socket.close()
			true
		} catch {
			case _: Throwable => false
		}
	}

	def waitForPort(port: Int, duration: Int = 30): Unit = {
		var i = 0
		while (i < duration) {
			if (checkPort(port)) {
				i = duration
			} else {
				i += 1
				print(".")
				Thread.sleep(1000)
			}
		}
		if (i >= duration && !checkPort(port)) {
			kill()
			throw new Exception("Port failed to open before the specified duration expired.")
		}
	}



	def initDocker(): Unit = {
		try {
			docker = DefaultDockerClient.builder()
			  .uri(URI.create(s"https://${DockerUtils.getIp().toString()}:2376"))
			  .dockerCertificates(new DockerCertificates(Paths.get(DockerUtils.getCerts())))
			  .build()

			try {
				docker.ping()
			} catch {
				case NonFatal(e) =>
					println("Exception while connecting to Docker. Check whether Docker is running.")
					throw e
			}

			try {
				docker.inspectImage(imageName)
			} catch {
				case e: ImageNotFoundException =>
					println(s"Docker image ${imageName} not found; pulling image from registry.")
					docker.pull(imageName)
			}

			//			externalPort = {
			//				val sock = new ServerSocket(0)
			//				val port = sock.getLocalPort
			//				sock.close()
			//				port
			//			}

			val portBindingsMap = for {
				port <- ports
			} yield (s"${port}/tcp" -> List(PortBinding.of("0,0,0,0", port)).asJava)

			val exposedPorts = for {
				port <- ports
			} yield s"${port}/tcp"

			dockerIp = DockerUtils.getIp()
			val hostConfig: HostConfig = HostConfig.builder()
			  .appendBinds(binds.asJava)
			  .networkMode("bridge")
			  .portBindings(
				  portBindingsMap.toMap.asJava)
			  // Map(s"${port}/tcp" -> List(PortBinding.of("0.0.0.0", externalPort)).asJava).asJava)
			  .build()

			val config = ContainerConfig.builder()
			  .image(imageName)
			  .networkDisabled(false)
			  .env(env.map { case(k, v) => s"$k=$v" }.toSeq.asJava)
			  .hostConfig(hostConfig)
			  //			  .exposedPorts(s"${port}/tcp")
			  .exposedPorts(exposedPorts.asJava)
			  .build()

			containerId = docker.createContainer(config).id
			try {
				docker.startContainer(containerId)
			} catch {
				case e:DockerRequestException => println("error:" + e.message())
			}

		} catch {
			case NonFatal(e) =>
				println(e)
				try {
					kill()
				} finally {
					throw e
				}
		}
	}

	def kill(): Unit = {
		try {
			if (docker != null)
				try {
					if (containerId != null) {
						docker.killContainer(containerId)
						docker.removeContainer(containerId)
					}
				} catch {
					case NonFatal(e) =>
						println(s"Could not stop container $containerId", e)
				} finally {
					docker.close()
				}
		}
	}
}
