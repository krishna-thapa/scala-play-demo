package helper

import com.whisk.docker.{ DockerContainer, DockerKit, DockerPortMapping, DockerReadyChecker }

import scala.concurrent.duration.DurationInt

trait DockerElasticsearchService extends DockerKit {

  val DefaultElasticsearchHttpPort   = 9200
  val DefaultElasticsearchClientPort = 9300
  val NewElasticsearchHttpPort       = 2237 //Random port for the testing purpose

  val elasticsearchContainer: DockerContainer =
    DockerContainer("docker.elastic.co/elasticsearch/elasticsearch:7.10.1")
      .withPortMapping(
        DefaultElasticsearchHttpPort   -> DockerPortMapping(Some(NewElasticsearchHttpPort)),
        DefaultElasticsearchClientPort -> DockerPortMapping(Some(DefaultElasticsearchClientPort))
      )
      .withEnv("discovery.type=single-node")
      .withReadyChecker(
        DockerReadyChecker
          .HttpResponseCode(DefaultElasticsearchHttpPort, "/", Some("0.0.0.0"))
          .within(100.millis)
          .looped(20, 1250.millis)
      )

  abstract override def dockerContainers: List[DockerContainer] =
    elasticsearchContainer :: super.dockerContainers
}
