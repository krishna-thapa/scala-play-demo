package config

import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ ElasticClient, ElasticProperties }

trait InitEs {

  // Environment variables should be declared in the docker compose file while creating the docker
  // image of the whole sbt play project
  def esConfig: ElasticsearchConfig
  def indexName: String = esConfig.ESINDEXNAME

  // If ES is installed in local machine and want to run locally
  //  val props: ElasticProperties = ElasticProperties("http://localhost:9200")

  def dockerUrl = s"http://${ esConfig.ESHOST }:${ esConfig.ESPORT }"

  def props: ElasticProperties = ElasticProperties(dockerUrl)

  def client: ElasticClient = ElasticClient(JavaClient(props))
}
