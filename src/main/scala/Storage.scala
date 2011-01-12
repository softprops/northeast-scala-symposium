package com.meetup

object Storage {
  def apply[T](block: javax.jdo.PersistenceManager => T): T = {
    val mgr = factory.getPersistenceManager
    try { block(mgr) }
    finally { mgr.close() }
  }

  lazy val factory =
    javax.jdo.JDOHelper.getPersistenceManagerFactory("transactions-optional")
}
