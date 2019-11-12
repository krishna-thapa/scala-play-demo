// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/santosh/gitHome/play-scala-starter-example/conf/routes
// @DATE:Tue Nov 12 20:39:46 GMT 2019


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
