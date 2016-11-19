package com.cmu.miis.capstone;

import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;



public class SwaggerServlet extends HttpServlet {

    private static final String CONTENT_TYPE_HTML = "text/html";
    private static final String CONTENT_TYPE_CSS = "text/css";
    private static final String CONTENT_TYPE_JS = "application/x-javascript";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_IMAGE = "image/gif";

    protected InputStream getStream(String resource) throws ServletException {
        return this.getClass().getClassLoader().getResourceAsStream(resource);
    }

    @Override
    public void init() throws ServletException {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null) path = "";

        String contentType = CONTENT_TYPE_HTML;
        if (path.endsWith(".css")) {
            contentType = CONTENT_TYPE_CSS;
        } else if (path.endsWith(".js")) {
            contentType = CONTENT_TYPE_JS;
        } else if (path.endsWith(".html") || path.equals("") || path.equals("/")) {
            contentType = CONTENT_TYPE_HTML;
        } else if (path.endsWith(".gif")) {
            contentType = CONTENT_TYPE_IMAGE;
        }

        if (StringUtils.isEmpty(req.getPathInfo()) || "/".equals(req.getPathInfo())) {
            response.setStatus(301);
            response.addHeader("Location", req.getContextPath() + req.getServletPath() + "/index.html");
            return;
        }

        response.setStatus(200);
        response.addHeader("Access-Control-Allow-Headers", "Origin, X-Atmosphere-tracking-id, X-Atmosphere-Framework, " +
                "X-Cache-Date, Content-Type, X-Atmosphere-Transport, X-Remote, api_key, auth_token, *");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Request-Headers", "Origin, X-Atmosphere-tracking-id, X-Atmosphere-Framework, " +
                "X-Cache-Date, Content-Type, X-Atmosphere-Transport,  X-Remote, api_key, *");

        response.setContentType(contentType);
        response.setCharacterEncoding("UTF-8");

        try (ServletOutputStream stream = response.getOutputStream()) {
            if ("/index.html".equals(path)) {
                path = "dist/index.html";
            }
            if (path.startsWith("/")) {
                path = path.substring("/".length());
            }

            InputStream fis = getStream(path);
            byte[] bytes = IOUtils.toByteArray(fis);
            stream.write(bytes);
        }
    }
}

