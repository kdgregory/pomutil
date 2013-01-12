<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>

<!doctype html>
<html>
<head>
    <title>POMUtil Cleaner</title>
    <link type="text/css" rel="stylesheet" href="<c:url value='/css/main.css'/>"/>
</head>
<body>

    <div id="heading">
        <p class="title"> PomUtil Cleaner </p>
    </div>

    <fieldset id="request">
        <legend></legend>
        <form action="" method="post" enctype="multipart/form-data">
            <table>
                <tr><th> Select POM: </th>
                    <td> <input name="file" type="file"/> </td> </tr>
                <tr><th> Options: </th>
                    <td>
                    </td> </tr>
                <tr><td> &nbsp; </td>
                    <td align="right"> <input id="submitPom" type="submit" value="Submit"> </td>
            </table>
        </form>
    </fieldset>

    <fieldset id="response">
        <legend>Cleaned POM</legend>
        <textarea name="cleanPOM" rows="36" cols="120">${cleanedPom}</textarea>
    </fieldset>

</body>
</html>
