import com.google.inject.AbstractModule
import com.krishna.httpService.{ HttpService, WebClient }
import com.krishna.util.Logging
import httpService.MockWikiMediaApi
import scheduler.ApplicationStart

import java.util.{ Calendar, Date }

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  *
  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */
class Module extends AbstractModule with Logging {

  val projectEnv: String = sys.env.getOrElse("PROJECT_ENV", "local")
  val now: Date          = Calendar.getInstance().getTime

  log.info(s"Project is started in $projectEnv environment at $now")

  override def configure(): Unit = {
    bind(classOf[ApplicationStart]).asEagerSingleton()
    if (projectEnv.equalsIgnoreCase("develop")) {
      // DI the mock API response for Wiki Media if the env is running at develop
      bind(classOf[WebClient]).to(classOf[MockWikiMediaApi])
    } else {
      bind(classOf[WebClient]).to(classOf[HttpService])
    }
  }
}
