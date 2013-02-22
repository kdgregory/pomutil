<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@page import="com.kdgregory.pomutil.cleaner.CommandLine"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!doctype html>
<html>
<head>
    <title>POMUtil Cleaner</title>
    <link type="text/css" rel="stylesheet" href="<c:url value='/resources/css/main.css'/>"/>
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
                        <table class="optionSelect">
                        <c:forEach var="option" items="${options}">
                            <tr><td> <input type="checkbox" name="${option.definition.key}" value="1"
                                     ${option.value ? "checked" : ""}/> </td>
                                <td> <c:out value="${option.definition.description}" escapeXml="true"/> </td>
                            </tr>
                        </c:forEach>
                        </table>
                     </td>
                </tr>
                <tr><td> <input id="submitPom" type="submit" value="Submit"> </td> </tr>
            </table>
        </form>
    </fieldset>

    <fieldset id="response">
        <legend>Cleaned POM</legend>
        <textarea name="cleanPOM" rows="32" cols="120">${cleanedPom}</textarea>
    </fieldset>
    
    <p id="copyright"> Copyright 2013, Keith D Gregory
        <br/>
        Source available from <a href="http://www.github.com/kdgregory/pomutil" target="_blank">GitHub</a>;
        licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License, version 2.0</a>.
        </p>

</body>
</html>
