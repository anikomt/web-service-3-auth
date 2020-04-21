package ru.ifmo.web.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.ifmo.web.database.dao.UserDAO;
import ru.ifmo.web.database.entity.User;
import ru.ifmo.web.service.util.UserServiceException;
import ru.ifmo.web.service.util.UserServiceFault;

import javax.annotation.Resource;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@WebService(serviceName = "users", targetNamespace = "users_namespace")
@NoArgsConstructor
public class UsersService {
    private UserDAO userDAO;
    @Resource
    private WebServiceContext wsctx;

    public UsersService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @WebMethod
    public List<User> findAll() throws SQLException {
        return userDAO.findAll();
    }

    @WebMethod
    public List<User> findWithFilters(@WebParam(name = "id") Long id, @WebParam(name = "login") String login,
                                      @WebParam(name = "password") String password, @WebParam(name = "email") String email,
                                      @WebParam(name = "gender") Boolean gender, @WebParam(name = "registerDate") XMLGregorianCalendar registerDate) throws SQLException {
        return userDAO.findWithFilters(id, login, password, email, gender, registerDate);
    }

    @WebMethod
    public int delete(@WebParam(name = "id") Long id) throws UserServiceException {
        try {
            checkCredentials();
            if (id == null ) {
                String message = "Id can't be null";
                throw new UserServiceException(message, new UserServiceFault(message));
            }
            int delete = userDAO.delete(id);
            if (delete <= 0) {
                String message = String.format("Can't delete User. User with specified id: %s not found ", id);
                throw new UserServiceException(message, new UserServiceFault(message));
            }
            return delete;
        } catch (SQLException e) {
            String message = "SQL exception: " + e.getMessage() + ". State: " + e.getSQLState();
            throw new UserServiceException(message, e, new UserServiceFault(message));
        }
    }

    @WebMethod
    public Long insert(@WebParam(name = "login") String login, @WebParam(name = "password") String password,
                       @WebParam(name = "email") String email, @WebParam(name = "gender") Boolean gender,
                       @WebParam(name = "registerDate") XMLGregorianCalendar registerDate) throws UserServiceException {
        try {
            checkCredentials();
            return userDAO.insert(login, password, email, gender, registerDate);
        } catch (SQLException e) {
            String message = "SQL exception: " + e.getMessage() + ". State: " + e.getSQLState();
            throw new UserServiceException(message, e, new UserServiceFault(message));
        }
    }

    @WebMethod
    public int update(@WebParam(name = "id") Long id, @WebParam(name = "login") String login,
                                      @WebParam(name = "password") String password, @WebParam(name = "email") String email,
                                      @WebParam(name = "gender") Boolean gender,
                      @WebParam(name = "registerDate") XMLGregorianCalendar registerDate) throws UserServiceException {
        int update = 0;
        try {
            checkCredentials();
            update = userDAO.update(id, login, password, email, gender, registerDate);
            if (update <= 0) {
                String message = String.format("Can't update User. User with specified id: %s not found ", id);
                throw new UserServiceException(message, new UserServiceFault(message));
            }
        } catch (SQLException e) {
            String message = "SQL exception: " + e.getMessage() + ". State: " + e.getSQLState();
            throw new UserServiceException(message, e, new UserServiceFault(message));
        }
        return update;
    }

    public void checkCredentials() throws UserServiceException, SQLException {

        MessageContext mctx = wsctx.getMessageContext();

        Map http_headers = (Map) mctx.get(MessageContext.HTTP_REQUEST_HEADERS);
        List userList = (List) http_headers.get("Username");
        List passList = (List) http_headers.get("Password");

        String username = "";
        String password = "";

        if(userList!=null){
            //get username
            username = userList.get(0).toString();
        }

        if(passList!=null){
            //get password
            password = passList.get(0).toString();
        }
        User user = userDAO.findByCredentials(username, password);
        if (user == null) {
            String message = "Unknown user!";
            throw new UserServiceException(message, new UserServiceFault(message));
        }
    }
}
