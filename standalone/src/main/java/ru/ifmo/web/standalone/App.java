package ru.ifmo.web.standalone;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.ifmo.web.database.dao.UserDAO;
import ru.ifmo.web.service.UsersService;

import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Endpoint;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

@Slf4j
public class App {
    public static void main(String... args) throws DatatypeConfigurationException, SQLException {
        String url = "http://0.0.0.0:8080/users";

        DataSource dataSource = initDataSource();
        System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace",
                "false");
        Endpoint.publish(url, new UsersService(new UserDAO(dataSource)));
        log.info("Application started");
    }

    @SneakyThrows
    private static DataSource initDataSource() {
        InputStream dsPropsStream = App.class.getClassLoader().getResourceAsStream("application.properties");
        Properties dsProps = new Properties();
        dsProps.load(dsPropsStream);
        HikariConfig hikariConfig = new HikariConfig(dsProps);
        return new HikariDataSource(hikariConfig);
    }
}
