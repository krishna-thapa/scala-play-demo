package util

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ ElasticClient, ElasticProperties }

trait InitEs {

  // Environment variables should be declared in the docker compose file while creating the docker
  // image of the whole sbt play project
  // in this example we create a client to a local Docker container at localhost:9200
  val elasticHost: String
  val elasticPort: String

  // If ES is installed in local machine and want to run locally
  //  val props: ElasticProperties = ElasticProperties("http://localhost:9200")

  val dockerUrl = s"http://$elasticHost:$elasticPort"

  val props: ElasticProperties = ElasticProperties(dockerUrl)

  val client: ElasticClient = ElasticClient(JavaClient(props))

  val indexName: String = "quotes"
}
