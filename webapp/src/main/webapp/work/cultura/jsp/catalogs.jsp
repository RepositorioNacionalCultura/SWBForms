<%-- 
    Document   : catalogs
    Created on : 23-nov-2017, 12:25:31
    Author     : juan.fernandez
--%><%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
        <title>Catálogos</title>
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/css/bootstrap.min.css" integrity="sha384-PsH8R72JQ3SOdhVi3uxftmaW6Vc51MKb0q5P2rRUpPvrszuE4W1povHYgTpBfshb" crossorigin="anonymous">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.5.2/animate.min.css">

        <style type="text/css">
            body { padding-top: 3.5rem; }
            h1 { padding-bottom: 9px; margin-bottom: 20px; border-bottom: 1px solid #eee; }
            .sidebar { position: fixed; top: 51px; bottom: 0; left: 0; z-index: 1000; padding: 20px 0; overflow-x: hidden; overflow-y: auto; border-right: 1px solid #eee; }
            .sidebar .nav { margin-bottom: 20px; }
            .sidebar .nav-item { width: 100%; }
            .sidebar .nav-item + .nav-item { margin-left: 0; }
            .sidebar .nav-link { border-radius: 0; }
            .placeholder img { padding-top: 1.5rem; padding-bottom: 1.5rem; }
        </style>
        <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
        <!--[if lt IE 9]>
        <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
        <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
        <![endif]-->

        <script src="/platform/js/eng.js" type="text/javascript"></script>
        <script type="text/javascript">
            eng.initPlatform("/work/cultura/jsp/datasources.js", false);
        </script> 
    </head>
    <body>
        <jsp:include page="includes/header.jsp" flush="true"></jsp:include>

        <div class="container-fluid">
            <div class="row">
                <nav class="col-sm-3 col-md-2 d-none d-sm-block bg-light sidebar">
                    <ul class="nav nav-pills flex-column">
                        <li class="nav-item">
                            <a class="nav-link active" href="catalogs">Catálogos <span class="sr-only">(current)</span></a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="extractors">Extractores</a>
                        </li>
                    </ul>
                </nav>

                <main role="main" class="col-sm-9 ml-sm-auto col-md-10 pt-3 animated fadeIn" style="z-index: 0">
                    <h3>Verbos OAI-PMH</h3>
                    <script type="text/javascript">
                        var verbs = eng.createGrid({
                            left: "-10",
                            margin: "10px",
                            width: "80%",
                            height: 200,
                            canEdit: true,
                            canRemove: true,
                            canAdd: true,
    //                recordClick: function (grid, record) {
    //                    var o = record._id;
    //                    isc.say(JSON.stringify(o, null, "\t"));
    //                    return false;
    //                }
                        }, "Verbs");
                    </script>
                    <h3>Prefijos OAI-PMH</h3>
                    <script type="text/javascript">
                        var metaPrefix = eng.createGrid({
                            left: "-10",
                            margin: "10px",
                            width: "80%",
                            height: 200,
                            canEdit: true,
                            canRemove: true,
                            canAdd: true,
    //                recordClick: function (grid, record) {
    //                    var o = record._id;
    //                    isc.say(JSON.stringify(o, null, "\t"));
    //                    return false;
    //                }
                        }, "MetaData_Prefix");
                    </script>
                    <h3>Reemplazo de cadenas</h3>
                    <script type="text/javascript">
                        var remplazo = eng.createGrid({
                            left: "-10",
                            margin: "10px",
                            width: "80%",
                            height: 200,
                            canEdit: true,
                            canRemove: true,
                            canAdd: true,
                            //                recordDoubleClick: function (grid, record)
                            //                {
                            //                    window.location = "endPoint.jsp?_id=" + record._id;
                            //                    return false;
                            //                }
                            //                ,
                            //                addButtonClick: function (event)
                            //                {
                            //                    window.location = "endPoint.jsp";
                            //                    return false;
                            //                },
                        }, "Replace");
                    </script>
                </main>
            </div>
        </div>

        <script src="https://code.jquery.com/jquery-3.2.1.slim.min.js" integrity="sha384-KJ3o2DKtIkvYIK3UENzmM7KCkRr/rE9/Qpg6aAZGJwFDMVNA/GpGFF93hXpG5KkN" crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.3/umd/popper.min.js" integrity="sha384-vFJXuSJphROIrBnz7yo7oB41mKfc8JzQZiCq4NCceLEaO4IHwicKwpJf9c9IpFgh" crossorigin="anonymous"></script>
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta.2/js/bootstrap.min.js" integrity="sha384-alpBpkh1PFOepccYVYDB4do5UnbKysX5WZXm3XxPqe5iKTfUKjNkCk9SaVuEZflJ" crossorigin="anonymous"></script>
    </body>
</html>