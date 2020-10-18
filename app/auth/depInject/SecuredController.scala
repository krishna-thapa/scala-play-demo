package auth.depInject

import auth.service.{ AdminActionBuilder, UserActionBuilder }
import javax.inject.Inject
import play.api.mvc.AbstractController

class SecuredController @Inject()(scc: SecuredControllerComponents)
    extends AbstractController(scc) {
  def UserAction: UserActionBuilder   = scc.userActionBuilder
  def AdminAction: AdminActionBuilder = scc.adminActionBuilder
}
