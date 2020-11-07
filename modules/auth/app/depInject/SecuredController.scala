package depInject

import javax.inject.Inject
import play.api.mvc.AbstractController
import service.{ AdminActionBuilder, UserActionBuilder }

class SecuredController @Inject()(scc: SecuredControllerComponents)
    extends AbstractController(scc) {
  def UserAction: UserActionBuilder   = scc.userActionBuilder
  def AdminAction: AdminActionBuilder = scc.adminActionBuilder
}
