package util

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ ElasticClient, ElasticProperties }

trait InitEs {

  // Environment variables should be declared in the docker compose file while creating the docker
  // image of the whole sbt play project
  def elasticHost: String
  def elasticPort: String
  def indexName: String

  // If ES is installed in local machine and want to run locally
  //  val props: ElasticProperties = ElasticProperties("http://localhost:9200")

  def dockerUrl = s"http://$elasticHost:$elasticPort"

  def props: ElasticProperties = ElasticProperties(dockerUrl)

  def client: ElasticClient = ElasticClient(JavaClient(props))
}
