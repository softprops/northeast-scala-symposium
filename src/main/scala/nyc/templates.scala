package nescala.nyc

trait Templates extends nescala.Templates {

  def nycLayout(head: xml.NodeSeq)
    (bodyScripts: xml.NodeSeq)
    (body: xml.NodeSeq) = unfiltered.response.Html(
    <html>
      <head>
        <meta http-equiv="Content-Type" content="text/html;charset=utf-8" />
        <title>&#8663;northeast scala symposium / nyc</title>
        <link href="http://fonts.googleapis.com/css?family=Arvo:regular,bold" rel="stylesheet" type="text/css"/>
        <link rel="stylesheet" type="text/css" href="/css/tipsy.css" />
        <link rel="stylesheet" type="text/css" href="/css/app.css" />
        <script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.4.4/jquery.min.js"></script>
        { head }
      </head>
      <body>
        <div id="container">
        <h1>Symposium Talk Selection</h1>
        { body }
        </div>
        { bodyScripts }
      </body>
    </html>
  )

}
