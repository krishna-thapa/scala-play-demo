package config

import com.typesafe.config.Config
import play.api.ConfigLoader

case class ElasticsearchConfig(
  ESHOST: String,
  ESPORT: String,
  ESINDEXNAME: String
)

// https://www.baeldung.com/scala/access-play-configuration
object ElasticsearchConfig {

  implicit val elasticsearchConfigLoader: ConfigLoader[ElasticsearchConfig] =
    (rootConfig: Config, path: String) => {
      val config = rootConfig.getConfig(path)
      new ElasticsearchConfig(
        config.getString("ESHOST"),
        config.getString("ESPORT"),
        config.getString("ESINDEXNAME")
      )
    }

}
