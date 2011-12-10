package com.meetup

trait Config {
  private lazy val props = {
    val file = getClass.getResourceAsStream("/meetup.properties")
    val props = new java.util.Properties
    props.load(file)
    file.close()
    props
  }

  def property(name: String) = props.getProperty(name) match {
    case null => sys.error("missing property %s" format name)
    case value => value
  }

  def intProperty(name: String) =
    try {
      property(name).toInt
    } catch { case nfe: NumberFormatException =>
      sys.error("%s was not an int" format property(name))
    }
}
