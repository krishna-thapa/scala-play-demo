package com.krishna.db

import com.github.tminglei.slickpg._
import com.krishna.model.auth.ProfilePictureInfo
import play.api.libs.json.{ JsValue, Json }
import slick.basic.Capability
import slick.jdbc.JdbcCapabilities

// https://github.com/tminglei/slick-pg
trait QuotesPostgresDriver
    extends ExPostgresProfile
    with PgArraySupport
    with PgDate2Support
    with PgRangeSupport
    with PgHStoreSupport
    with PgPlayJsonSupport
    with array.PgArrayJdbcTypes
    with PgSearchSupport
    with PgNetSupport
    with PgLTreeSupport {

  def pgjson = "jsonb" // jsonb support is in postgres 9.4.0 onward; for 9.3.x use "json"

  // Add back `capabilities.insertOrUpdate` to enable native `upsert` support; for postgres 9.5+
  override protected def computeCapabilities: Set[Capability] =
    super.computeCapabilities + JdbcCapabilities.insertOrUpdate

  override val api: API = new API {}
  val plainAPI = new API with PlayJsonPlainImplicits

  trait API
      extends super.API
      with ArrayImplicits
      with DateTimeImplicits
      with JsonImplicits
      with PlayJsonPlainImplicits
      with NetImplicits
      with LTreeImplicits
      with RangeImplicits
      with HStoreImplicits
      with SearchImplicits
      with SearchAssistants {

    implicit val strListTypeMapper: DriverJdbcType[List[String]] =
      new SimpleArrayJdbcType[String]("text").to(_.toList)

    implicit val playJsonArrayTypeMapper: DriverJdbcType[List[JsValue]] =
      new AdvancedArrayJdbcType[JsValue](
        pgjson,
        s => utils.SimpleArrayUtils.fromString[JsValue](Json.parse)(s).orNull,
        v => utils.SimpleArrayUtils.mkString[JsValue](_.toString())(v)
      ).to(_.toList)

    implicit val profilePicInfoJsonTypeMapper =
      MappedJdbcType.base[ProfilePictureInfo, JsValue](Json.toJson(_), _.as[ProfilePictureInfo])

    implicit val beanArrayTypeMapper =
      new AdvancedArrayJdbcType[ProfilePictureInfo](
        pgjson,
        s =>
          utils
            .SimpleArrayUtils
            .fromString[ProfilePictureInfo](Json.parse(_).as[ProfilePictureInfo])(s)
            .orNull,
        v =>
          utils
            .SimpleArrayUtils
            .mkString[ProfilePictureInfo](b => Json.stringify(Json.toJson(b)))(v)
      ).to(_.toList)

  }

}

object QuotesPostgresDriver extends QuotesPostgresDriver
