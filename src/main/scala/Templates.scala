package com.meetup

import unfiltered.response.Html

trait Templates {
  def layout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) =
      boston.Templates.bostonLayout(head)(bodyScripts)(body)
}
