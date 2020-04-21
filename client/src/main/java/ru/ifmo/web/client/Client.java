package ru.ifmo.web.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.ifmo.web.client.util.Command;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class Client {
    private static final ThreadLocal<Users> USERS_THREAD_LOCAL = new ThreadLocal<>();
    private static final String SERVICE_WSDL_URL = "http://localhost:8080/users?wsdl";

    public static void main(String... args) throws SQLException_Exception, IOException {
        Users usersService = getService();
        if (usersService == null) {
            System.out.println("Неправильно задан URL на котором размещен wsdl севриса");
            System.exit(-1);
        }
        UsersService userPort = usersService.getUsersServicePort();
        Thread thread = Thread.currentThread();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        int currentState = 0;
        Command command;
        UserDTO userDTO;
        Long id;

        writeHelp();
        while (true) {
            try {
                currentState = readState(reader);
                if (currentState < 0 || currentState > Command.values().length) {
                    continue;
                } else if (currentState == 0) {
                    writeHelp();
                    continue;
                }
                command = Command.values()[currentState - 1];
                switch (command) {
                    case FIND_ALL:
                        userPort.findAll().stream().map(Client::userToString).forEach(System.out::println);
                        break;
                    case FIND_BY_FILTERS:
                        System.out.println("Введите значения полей, по которым хотите производить фильтрацию.\n" +
                                "Чтобы не применять фильтр, оставьте значение пустым");
                        id = readLong(reader);
                        userDTO = readUser(reader);
                        userPort.findWithFilters(id, userDTO.getLogin(), userDTO.getPassword(),
                                userDTO.getEmail(), userDTO.getGender(), userDTO.getRegisterDate())
                                .stream().map(Client::userToString).forEach(System.out::println);
                        break;
                    case INSERT:
                        addBasicHeader(userPort);
                        System.out.println("Введите поля нового пользователя:");
                        userDTO = readUser(reader);
                        System.out.println("Пользоваетль успешно добавлен. Его id: " + userPort.insert(userDTO.getLogin(), userDTO.getPassword(),
                                userDTO.getEmail(), userDTO.getGender(), userDTO.getRegisterDate()));
                        break;
                    case UPDATE:
                        addBasicHeader(userPort);
                        System.out.print("Введите id пользователя, которого хотите изменить: ");
                        id = readLong(reader);
                        System.out.println("Введите новые поля пользователя");
                        userDTO = readUser(reader);
                        System.out.println(String.format(
                                "Обновлено %s пользователей",
                                    userPort.update(id, userDTO.getLogin(), userDTO.getPassword(),
                                        userDTO.getEmail(),
                                        userDTO.getGender(),
                                        userDTO.getRegisterDate()
                                    )
                                )
                        );
                        break;
                    case DELETE:
                        addBasicHeader(userPort);
                        System.out.print("Введите id пользователя, которого хотите удалить: ");
                        id = readLong(reader);
                        System.out.println(userPort.delete(id));
                        break;
                    case QUIT:
                        return;
                }
            } catch (UserServiceException e) {
                System.out.println(e.getFaultInfo().getMessage());
                System.out.println("Попробуй ещё раз!");
            }
        }
    }

    private static Users getService() {
        Users users = USERS_THREAD_LOCAL.get();
        if (users == null) {
            URL url = null;
            try {
                url = new URL(SERVICE_WSDL_URL);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }
            users = new Users(url);
            USERS_THREAD_LOCAL.set(users);
        }
        return users;
    }

    private static void  addBasicHeader(UsersService usersService) {
        Map<String, Object> req_ctx = ((BindingProvider)usersService).getRequestContext();
        req_ctx.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "http://localhost:8080/users?wsdl");

        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("Username", Collections.singletonList("izard"));
        headers.put("Password", Collections.singletonList("1234"));
        req_ctx.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
    }

    private static UserDTO readUser(BufferedReader reader) {
        System.out.print("login: ");
        String login = readString(reader);
        System.out.print("password: ");
        String password = readString(reader);
        System.out.print("email: ");
        String email = readString(reader);
        System.out.print("gender: ");
        Boolean gender = readBoolean(reader);
        System.out.print("registerDate(yyyy-mm-dd): ");
        XMLGregorianCalendar registerDate = readDate(reader);
        return new UserDTO(login, password, email, gender, registerDate);
    }

    private static String readString(BufferedReader reader) {
        String trim = null;
        try {
            trim = reader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (trim.isEmpty()) {
            return null;
        }
        return trim;
    }

    private static XMLGregorianCalendar readDate(BufferedReader reader) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date rd = sdf.parse(reader.readLine());

            GregorianCalendar c = new GregorianCalendar();

            if (rd != null) {
                    c.setTime(rd);
                XMLGregorianCalendar xmlGregorianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                xmlGregorianCalendar.setTimezone(0);
                return xmlGregorianCalendar;
            } else {
                return null;
            }
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    private static Long readLong(BufferedReader reader) {
        try {
            return Long.parseLong(reader.readLine());
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    private static Boolean readBoolean(BufferedReader reader) {
        try {
            String s = reader.readLine();
            if (s.equals("")) {
                return null;
            }
            return Boolean.parseBoolean(s);
        } catch (java.lang.Exception e) {
            return null;
        }
    }

    private static int readState( BufferedReader reader) {
        try {
            System.out.print("> ");
            String s = reader.readLine();
            return "help".equals(s) ? 0 : Integer.parseInt(s);
        } catch (java.lang.Exception e) {
            return -1;
        }
    }

    private static void writeHelp() {
        System.out.println("\nВыберите один из пунктов:");
        System.out.println("0. Вывести help");
        for (Command value : Command.values()) {
            System.out.println(1 + value.ordinal() + ". " + value.getHelp());
        }
    }

    private static String userToString(User user) {
        return "User{" +
                "id=" + user.getId() +
                ", login='" + user.getLogin() + '\'' +
                ", email='" + user.getEmail() + '\'' +
                ", gender=" + user.isGender() +
                ", registerDate=" + user.getRegisterDate() +
                '}';
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UserDTO {
        private String login;
        private String password;
        private String email;
        private Boolean gender;
        private XMLGregorianCalendar registerDate;

    }
}
