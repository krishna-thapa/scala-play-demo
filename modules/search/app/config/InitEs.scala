package config

import com.krishna.model.base.WithCSCVIdResource
import com.sksamuel.elastic4s.ElasticDsl.indexInto
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.playjson.playJsonIndexable
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.streams.RequestBuilder
import com.sksamuel.elastic4s.{ ElasticClient, ElasticProperties }
import play.api.libs.json.OFormat

trait InitEs {

  // Environment variables should be declared in the docker compose file while creating the docker
  // image of the whole sbt play project
  def esConfig: ElasticsearchConfig
  def indexName: String = esConfig.ESINDEXNAME

  // If ES is installed in local machine and want to run locally
  //  val props: ElasticProperties = ElasticProperties("http://localhost:9200")

  def dockerUrl = s"http://${esConfig.ESHOST}:${esConfig.ESPORT}"

  def props: ElasticProperties = ElasticProperties(dockerUrl)

  def client: ElasticClient = ElasticClient(JavaClient(props))

  /*
    An implementation of RequestBuilder to load stream in ElasticSearch
   */
  def builder[T <: WithCSCVIdResource](
      indexName: String
  )(implicit conv: OFormat[T]): RequestBuilder[T] =
    (q: T) => indexInto(indexName).id(q.csvId).doc(q).refresh(RefreshPolicy.Immediate)

}
