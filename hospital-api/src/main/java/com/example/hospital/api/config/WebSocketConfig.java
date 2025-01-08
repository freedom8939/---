package com.example.hospital.api.config;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.webapp.AbstractConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

/*    int httpsPort = 443;

    @Bean
    public ServletWebServerFactory servletWebServerFactory() {
        JettyServletWebServerFactory jetty = new JettyServletWebServerFactory();
        jetty.addConfigurations(new AbstractConfiguration() {

            @Override
            public void configure(WebAppContext context) {
                Constraint constraint = new Constraint();
                constraint.setDataConstraint(2);

                ConstraintMapping constraintMapping = new ConstraintMapping();
                constraintMapping.setPathSpec("/*");
                constraintMapping.setConstraint(constraint);

                ConstraintSecurityHandler constraintSecurityHandler = new ConstraintSecurityHandler();
                constraintSecurityHandler.addConstraintMapping(constraintMapping);
                context.setSecurityHandler(constraintSecurityHandler);
            }
        });

        jetty.addServerCustomizers((Server server) -> {
            HttpConfiguration http = new HttpConfiguration();
            http.setSecurePort(httpsPort);
            ServerConnector connector = new ServerConnector(server);
            connector.addConnectionFactory(new HttpConnectionFactory(http));
            server.addConnector(connector);
        });
        return jetty;
    }*/
}
