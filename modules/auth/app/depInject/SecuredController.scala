package depInject

import play.api.mvc.{ AbstractController, DefaultActionBuilder }
import service.{ AdminActionBuilder, UserActionBuilder }

import javax.inject.Inject

class SecuredController @Inject()(scc: SecuredControllerComponents) extends AbstractController(scc) {
  def UserAction: UserActionBuilder         = scc.userActionBuilder
  def AdminAction: AdminActionBuilder       = scc.adminActionBuilder
  override def Action: DefaultActionBuilder = scc.actionBuilder
}
