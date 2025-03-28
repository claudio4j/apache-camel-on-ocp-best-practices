package sample;

import javax.sql.DataSource;

import org.apache.camel.builder.RouteBuilder;

import jakarta.inject.Inject;

public class JdbcLog extends RouteBuilder {

    @Inject
    DataSource datasource() {
        org.apache.commons.dbcp2.BasicDataSource ds = new org.apache.commons.dbcp2.BasicDataSource();
        ds.setUsername("{{secret:secret-basic-auth/username}}");
        ds.setPassword("{{secret:secret-basic-auth/password}}");
        ds.setUrl("jdbc:postgresql://{{secret:secret-basic-auth/host}}:5432/testdb");
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    @Override
    public void configure() throws Exception {
        from("timer:foo?period=30s")
                .setBody(simple("SELECT * FROM foo"))
                .to("jdbc:datasource")
                .log("${body}");
    }
}