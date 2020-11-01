package search.util

import com.sksamuel.elastic4s.{ ElasticClient, ElasticProperties }
import com.sksamuel.elastic4s.http.JavaClient
import com.typesafe.config.{ Config, ConfigFactory }

trait InitEs {

  def config: Config = ConfigFactory.load("application")

  // Environment variables should be declared in the docker compose file while creating the docker
  // image of the whole sbt play project
  // in this example we create a client to a local Docker container at localhost:9200
  val elasticHost: String = sys.env.getOrElse("ES_HOST", config.getString("ES.ES_HOST"))
  val elasticPort: String = sys.env.getOrElse("ES_PORT", config.getString("ES.ES_PORT"))

  // If ES is installed in local machine and want to run locally
  //  val props: ElasticProperties = ElasticProperties("http://localhost:9200")

  val dockerUrl = s"http://$elasticHost:$elasticPort"

  val props: ElasticProperties = ElasticProperties(dockerUrl)

  val client: ElasticClient = ElasticClient(JavaClient(props))

  val indexName: String = "quotes"
}
