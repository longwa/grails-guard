<!doctype html>
<html>
<head>
    <style type="text/css">
    body {
        font-family: Helvetica, Arial, "Lucida Grande", sans-serif;
    }

    table {
        font-family: verdana, arial, sans-serif;
        font-size: 11px;
        color: #333333;
        border: 1px #666666;
        border-collapse: collapse;
    }

    table th {
        border: 1px solid #666666;
        padding: 6px;
        background-color: #dedede;
    }

    table td {
        border: 1px solid #666666;
        padding: 6px;
        background-color: #ffffff;
        white-space: pre;
        font-family: "Consolas", "Courier New", monospace;
    }
    </style>

    <title>Test Pattern: ${testPattern}</title>
</head>

<body>
<h2>Test Pattern: ${testPattern}</h2>
<hr/>

<div style="color: green">
    <strong>Passed: ${totalPassed}</strong>
</div>
<g:if test="${totalFailed}">
    <strong><div style="color: red">Failed: ${totalFailed}</div></strong>
</g:if>

<br />

<div>
    <table>
        <thead>
        <tr>
            <th>Class</th>
            <th>Method</th>
            <th>Message</th>
            <th>Trace</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${failures}" var="failure">
            <tr>
                <td>${failure.description.className}</td>
                <td>${failure.description.methodName}</td>
                <td>${failure.message}</td>
                <td>${failure.trace}</td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
</body>
</html>