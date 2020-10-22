package search.util

import com.sksamuel.elastic4s.{ ElasticClient, ElasticProperties }
import com.sksamuel.elastic4s.http.JavaClient

trait InitEs {

  val props: ElasticProperties = ElasticProperties("http://localhost:9200")
  val client: ElasticClient    = ElasticClient(JavaClient(props))

  val indexName: String = "quotes"
}
