package ru.ifmo.web.database.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import ru.ifmo.web.database.entity.User;
import ru.ifmo.web.database.util.CriteriaBuilder;
import ru.ifmo.web.database.util.Predicate;

import javax.sql.DataSource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
public class UserDAO {
    private final DataSource dataSource;

    private final String TABLE_NAME = "users";
    private final String ID = "id";
    private final String LOGIN = "login";
    private final String PASSWORD = "password";
    private final String EMAIL = "email";
    private final String GENDER = "gender";
    private final String REGISTER_DATE = "register_date";

    private final String[] columnNames = {ID, LOGIN, PASSWORD, EMAIL, GENDER, REGISTER_DATE};

    public List<User> findAll() throws SQLException {
        log.info("Find all");
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            CriteriaBuilder cb = new CriteriaBuilder();
            String query = cb.select()
                    .selectors(columnNames)
                    .from(TABLE_NAME).toString();
            log.debug("Query string {}", query);
            statement.execute(query);
            return resultSetToList(statement.getResultSet());
        }
    }

    public List<User> findWithFilters(Long id, String login, String password, String email, Boolean gender, XMLGregorianCalendar registerDate) throws SQLException {
        Date regDate = registerDate == null ? null : registerDate.toGregorianCalendar().getTime();
        log.debug("Find with filters: {} {} {} {} {} {}", id, login, password, email, gender, regDate);
        Stream<? extends Serializable> params = Stream.of(id, login, password, email, gender, regDate);
        if (params.allMatch(Objects::isNull)) {
            return findAll();
        }

        CriteriaBuilder cb = new CriteriaBuilder();
        cb = cb.select()
                .selectors(columnNames)
                .from(TABLE_NAME);



        Predicate where = new Predicate();
        Predicate predicate = addEqualsPredicate(where, ID, id);
        predicate = addEqualsPredicate(predicate, LOGIN, login);
        predicate = addEqualsPredicate(predicate, PASSWORD, password);
        predicate = addEqualsPredicate(predicate, EMAIL, email);
        predicate = addEqualsPredicate(predicate, GENDER, gender);
        if (registerDate != null) {
            predicate = addEqualsPredicate(predicate, REGISTER_DATE, new SimpleDateFormat("yyyy.MM.dd")
                    .format(regDate));
        }

        cb = cb.where(predicate);

        String c = cb.toString();
        log.debug("Query string {}", cb);
        try (Connection connection = dataSource.getConnection()) {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(c);
            return resultSetToList(rs);
        }

    }

    public int delete(Long id) throws SQLException  {
        CriteriaBuilder cb = new CriteriaBuilder();
        cb = cb.delete()
                .from(TABLE_NAME);
        Predicate where = new Predicate();
        Predicate predicate = addEqualsPredicate(where, ID, id);
        cb = cb.where(predicate);

        String c = cb.toString();
        log.debug("Query string {}", cb);
        Connection connection = dataSource.getConnection();
        Statement s = connection.createStatement();
        return s.executeUpdate(c);
    }


    private Predicate addEqualsPredicate(Predicate where, String columnName, Object value) {
        if (value != null) {
                where.and(columnName + " = '" + value.toString() + "'");
        }
        return where;
    }
    private List<User> resultSetToList(ResultSet rs) throws SQLException {
        List<User> result = new ArrayList<>();
        while (rs.next()) {
            result.add(toEntity(rs));
        }
        return result;
    }
    private User toEntity(ResultSet rs) throws SQLException {
        long id = rs.getLong(ID);
        String login = rs.getString(LOGIN);
        String password = rs.getString(PASSWORD);
        String email = rs.getString(EMAIL);
        Boolean gender = rs.getBoolean(GENDER);
        Date registerDate = rs.getDate(REGISTER_DATE);
        return new User(id, login, password, email, gender, registerDate);
    }

    public Long insert(String login, String password, String email, Boolean gender, XMLGregorianCalendar registerDate)
            throws SQLException {
        Date regDate = registerDate == null ? null : registerDate.toGregorianCalendar().getTime();
        log.debug("Insert new entity: {} {} {} {} {} ", login, password, email, gender, regDate);

        CriteriaBuilder cb = new CriteriaBuilder();
        cb = cb.insert(TABLE_NAME);

        cb.columns(LOGIN, PASSWORD, EMAIL, GENDER, REGISTER_DATE);

        Predicate where = new Predicate();
        where = where.comma(login);
        where = where.comma(password);
        where = where.comma(email);
        where = where.comma(gender.toString());
        if (registerDate != null) {
            where = where.comma(new SimpleDateFormat("yyyy.MM.dd")
                    .format(regDate));
        }

        cb.values(where);

        String c = cb.toString();
        log.debug("Query string {}", cb);
        Connection connection = dataSource.getConnection();
        PreparedStatement s = connection.prepareStatement(c,
                Statement.RETURN_GENERATED_KEYS);
        int update = s.executeUpdate();
        ResultSet rs = s.getGeneratedKeys();
        if (rs != null && rs.next()) {
            return rs.getLong(1);
        }

        return -1L;
    }

    public int update(Long id, String login, String password, String email,
                          Boolean gender, XMLGregorianCalendar registerDate) throws SQLException {
        Date regDate = registerDate == null ? null : registerDate.toGregorianCalendar().getTime();
        log.debug("Update entity with id: {}. New values: {} {} {} {} {} ", id, login, password, email, gender, regDate);

        CriteriaBuilder cb = new CriteriaBuilder();
        cb = cb.update(TABLE_NAME);

        var loginColumn = new AbstractMap.SimpleImmutableEntry<String,String>(LOGIN, login);
        var passwordColumn = new AbstractMap.SimpleImmutableEntry<String,String>(PASSWORD, password);
        var emailColumn = new AbstractMap.SimpleImmutableEntry<String,String>(EMAIL, email);
        var genderColumn = new AbstractMap.SimpleImmutableEntry<String,String>(GENDER, gender.toString());
        var regiserDateColumn = new AbstractMap.SimpleImmutableEntry<String,String>(REGISTER_DATE, new SimpleDateFormat("yyyy.MM.dd")
                .format(regDate));
        cb.setColumns(loginColumn, passwordColumn, emailColumn, genderColumn, regiserDateColumn);


        Predicate predicate = new Predicate();
        cb.where(predicate.and(ID + " = " + id.toString()));

        String c = cb.toString();
        log.debug("Query string {}", cb);
        Connection connection = dataSource.getConnection();
        PreparedStatement s = connection.prepareStatement(c);
        return s.executeUpdate();
    }

    public User findByCredentials(String username, String password) throws SQLException {
        CriteriaBuilder cb = new CriteriaBuilder();
        cb = cb.select()
                .selectors(columnNames)
                .from(TABLE_NAME);
        Predicate where = new Predicate();
        Predicate predicate = addEqualsPredicate(where, LOGIN, username);
        predicate = addEqualsPredicate(predicate, PASSWORD, password);
        cb = cb.where(predicate);

        String c = cb.toString();
        log.debug("Query string {}", cb);
        try (Connection connection = dataSource.getConnection()) {
            Statement s = connection.createStatement();
            ResultSet rs = s.executeQuery(c);
            return resultSetToEntity(rs);
        }
    }

    private User resultSetToEntity(ResultSet rs) throws SQLException {
        User result = null;
        if (rs.next()) {
            result = toEntity(rs);
        }
        return result;
    }

}
