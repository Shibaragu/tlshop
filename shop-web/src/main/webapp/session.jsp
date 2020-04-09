<%@ pagelanguage="java" %>

<html>

  <head><title>TomcatA</title></head>

  <body>

   <h1style="color: red;">Tomcat A</h1>

    <tablealign="centre"border="1">

      <tr>

        <td>Session ID</td>

        <td><%= session.getId() %></td>

      </tr>

      <tr>

        <td>Created on</td>

        <td><%= session.getCreationTime() %></td>

     </tr>

    </table>

  </body>

</html>

sessionID:<%=session.getId()%>

<br>

SessionIP:<%=request.getServerName()%>

<br>

SessionPort:<%=request.getServerPort()%>

<%

out.println("This is Tomcat Server A");

%>