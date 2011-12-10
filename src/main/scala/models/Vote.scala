package com.meetup.models

//import javax.jdo.annotations._

/*@PersistenceCapable(
  identityType = IdentityType.APPLICATION,
  detachable = "true"
)*/
class Vote() {
  //@PrimaryKey
  //@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  var id: java.lang.Long = _

  //@Persistent
  var entry_id: Int = _

  //@Persistent
  var member_id: Int = _
}
