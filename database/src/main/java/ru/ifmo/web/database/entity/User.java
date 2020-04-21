package ru.ifmo.web.database.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String login;
    private String password;
    private String email;
    private Boolean gender = true; //True - man, false - woman
    private Date registerDate;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", email='" + email + '\'' +
                ", gender=" + gender +
                ", registerDate=" + registerDate +
                '}';
    }
}
