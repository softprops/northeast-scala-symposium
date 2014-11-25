package nescala

import scala.util.control.Exception.allCatch
import org.apache.commons.codec.digest.DigestUtils

object Hashing extends Config {
  def apply(values: String*) =
    allCatch.opt(DigestUtils.md5Hex((values + siteSecret).mkString(":")))
      .getOrElse("")

  def authentic(sig: String, values: String*) =
    apply(values:_*) == sig
}
