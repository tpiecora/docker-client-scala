package com.redventures.dockerclient

import java.net.URI
import java.nio.file.Paths

import com.spotify.docker.client.DockerClient._
import com.spotify.docker.client._
import com.spotify.docker.client.messages._

import scala.collection.JavaConverters._
import scala.sys.process.{ProcessLogger, _}
import scala.util.control.NonFatal

/**
  * Created by mpiecora on 4/5/16.
  */
class DockerClient (
					 val imageName: String,
					 env: Map[String, String] = Map().empty,
					 ports: Set[Int] = Set(8000),
					 binds: List[String] = List(),
					 val hostname: String = "localhost"
				   ) {
	var docker: DefaultDockerClient = _
	var containerId: String = _
	var dockerIp: String = _
	var externalPort: Int = _

	def init(): Unit = {
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

			dockerIp = DockerUtils.getIp()

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
	init()

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


	// spins up a new container of the supplied image type
	def initContainer(): Unit = {
		try {
			val portBindingsMap = for {
				port <- ports
			} yield (s"${port}/tcp" -> List(PortBinding.of("0,0,0,0", port)).asJava)

			val exposedPorts = for {
				port <- ports
			} yield s"${port}/tcp"

			val hostConfig: HostConfig = HostConfig.builder()
			  .appendBinds(binds.asJava)
			  .networkMode("bridge")
			  .portBindings(
				  portBindingsMap.toMap.asJava)
			  // Map(s"${port}/tcp" -> List(PortBinding.of("0.0.0.0", externalPort)).asJava).asJava)
			  .build()

			val config = ContainerConfig.builder()
			  .image(imageName)
			  .hostname(hostname)
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
			if (docker != null && containerId != null) {
				docker.killContainer(containerId)
			}
		} catch {
			case NonFatal(e) =>
				println(s"Could not kill container $containerId", e)
		} finally {
			//			docker.close()
		}
	}

	def remove(): Unit = {
		try {
			if (docker != null && containerId != null) {
				docker.removeContainer(containerId)
			}
		} catch {
			case NonFatal(e) =>
				println(s"Could not remove container $containerId", e)
		} finally {
			docker.close()
		}
	}

	def pause(): Unit = {
		try {
			if (docker != null && containerId != null) {
				docker.pauseContainer(containerId)
			}
		} catch {
			case NonFatal(e) =>
				println(s"Could not pause container $containerId", e)
		}
	}

	def unpause(): Unit = {
		try {
			if (docker != null && containerId != null) {
				docker.unpauseContainer(containerId)
			}
		} catch {
			case NonFatal(e) =>
				println(s"Could not unpause container $containerId", e)
		}
	}

	def restart(): Unit = {
		try {
			if (docker != null && containerId != null) {
				docker.restartContainer(containerId)
			}
		} catch {
			case NonFatal(e) =>
				println(s"Could not restart container $containerId", e)
		}
	}

	def listContainers(): Option[java.util.List[Container]] = {
		try {
			Some(docker.listContainers())
		} catch {
			case NonFatal(e) =>
				println(s"Could not list containers $containerId", e)
				None
			case e: Exception => println(e); None
		}
	}

	def checkContainer(imgName: String = imageName): Option[Container] = {
		val c = listContainers()
		if (c.isDefined) {
			val found = for(container <- c.get.asScala.toIterator; if container.image() == imgName) yield Some(container)
			if (found.hasNext) found.next else None
		} else {
			None
		}
	}

	def reuseContainer(c: Option[Container]): Unit = {
		if (c.isDefined && c.get.image() == imageName) {
			dockerIp = DockerUtils.getIp()
			containerId = c.get.id()
			//			initExisting(containerId)
		}
	}

	def reuseOrCreate(imgName:String = imageName): Unit = {
		// TODO should also probably make sure the container we are gonna reuse matches the ports specified by this instance
		val exists = checkContainer(imgName)
		println("checking for container " + imgName)
		if (exists.isEmpty) {
			println("no container count...creating")
			initContainer()
		} else {
			println("ports ", exists.get.ports())
			println("found existing...reusing")
			reuseContainer(exists)
		}
	}


	// TODO add refresh method so that it will destroy and recreate any existing containers that we could potentially use
	// TODO add class to store groups of containers
	// TODO add container tracking
	// TODO have option to sleep or delete containers
	// TODO add intelligent checking where we can see if any containers have already been started that we can use for our tests
	// TODO make dockerClient methods chainable for convenience and easier management

}
