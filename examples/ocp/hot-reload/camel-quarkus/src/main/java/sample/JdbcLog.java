package sample;

import java.security.Principal;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.dbcp2.BasicDataSource;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.agroal.api.AgroalDataSource;
import io.agroal.api.security.NamePrincipal;
import io.agroal.api.security.SimplePassword;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

public class JdbcLog extends RouteBuilder {

    // @Inject
    // Logger LOG;
/*
    @jakarta.inject.Inject
    public DataSource datasource(
        // @PropertyInject("{{secret:secret-basic-auth/username}}") String username,
        // @PropertyInject("{{secret:secret-basic-auth/password}}") String password,
        // @PropertyInject("{{secret:secret-basic-auth/host}}") String host,
        // @PropertyInject("{{secret:secret-basic-auth/port:5432}}") int port) {
        // quarkus way 1
        @ConfigProperty(name = "{{secret:secret-basic-auth/username}}") Provider<String> username,
        @ConfigProperty(name = "{{secret:secret-basic-auth/password}}") Provider<String> password,
        @ConfigProperty(name = "{{secret:secret-basic-auth/host}}") Provider<String> host,
        @ConfigProperty(name = "{{secret:secret-basic-auth/port:5432}}") Provider<Integer> port) {
        // quarkus way 2
        // @ConfigProperty(name = "${username}") Provider<String> username,
        // @ConfigProperty(name = "${password}") Provider<String> password,
        // @ConfigProperty(name = "${host}") Provider<String> host,
        // @ConfigProperty(name = "${port:5432}") Provider<Integer> port) {
        org.apache.commons.dbcp2.BasicDataSource ds = new org.apache.commons.dbcp2.BasicDataSource();
        LOG.info(">>> teste");
        LOG.info(">>> teste");
        LOG.info(">>> teste");
        LOG.info(">>> teste");
        ds.setUsername(username.get());
        ds.setPassword(password.get());
        ds.setUrl("jdbc:postgresql://" + host.get() + ":" + port.get() + "/testdb");
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }
*/
    @Override
    public void configure() throws Exception {
        from("timer:JAVA?period=30s")
            .setBody(simple("SELECT * FROM foo"))
            .to("jdbc:my-camel")
            .process(e -> e.getContext().getRegistry().findByType(DataSource.class).forEach(b -> {
                AgroalDataSource ds = (AgroalDataSource) b;
                Principal princ = ds.getConfiguration().connectionPoolConfiguration().connectionFactoryConfiguration().principal();
                log.info("> agroal user: " + princ.getName());
            }))
            .log("resolving username from camel kubernetes function: {{secret:secret-basic-auth/username}}")
            .log("body: ${body}");
    }
}