package com.cmu.miis.capstone;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.RequestLimitingHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.util.Headers;
import io.undertow.servlet.api.DeploymentInfo;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

import org.xnio.Options;

import javax.servlet.ServletException;
import java.security.NoSuchAlgorithmException;

public class UndertowServer {
    private static Undertow instance;
    static final int HTTP_PORT = 8443;
    public static void startUndertow() throws Exception {

        Undertow.Builder builder =  Undertow.builder().addHttpListener(HTTP_PORT, "0.0.0.0")
                .setBufferSize(1024 * 16)
                .setIoThreads(Math.max(1,Runtime.getRuntime().availableProcessors() - 1))
                .setSocketOption(Options.BACKLOG, 20)
                .setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
                .setWorkerOption(Options.CONNECTION_HIGH_WATER, 7000)
                .setWorkerOption(Options.CONNECTION_LOW_WATER, 5000)
                .setHandler(new RequestLimitingHandler(200, 50, Handlers.header(Handlers.path()
                                .addPrefixPath("/", getRestEasyServlet())
                        , Headers.SERVER_STRING, "RR_WS")))
                .setWorkerThreads(200);

        instance = builder.build();
        instance.start();
    }

    private static HttpHandler getRestEasyServlet() throws ServletException, NoSuchAlgorithmException {
        ResteasyDeployment depl = new ResteasyDeployment();
        depl.setApplication(new ApplicationConfig());

        FilterInfo f;
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(UndertowServer.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("RestEasy")
                .addServlets(
                        Servlets.servlet("RestEasyServlet", HttpServlet30Dispatcher.class)
                                .setAsyncSupported(true)
                                .setLoadOnStartup(1)
                                .addMapping("/"),
                        Servlets.servlet("SwaggerServlet", SwaggerServlet.class)
                                .setLoadOnStartup(2)
                                .addMapping("/api-docs/*")
                ).addServletContextAttribute(ResteasyDeployment.class.getName(), depl);
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        return manager.start();
    }

    public static void tearDown() {
        if (instance != null) {
            instance.stop();
            instance = null;
        }
    }

    public static boolean isRunning() {
        return instance != null;
    }

    public static void main(String[] args) throws Exception{
        startUndertow();
    }
}

