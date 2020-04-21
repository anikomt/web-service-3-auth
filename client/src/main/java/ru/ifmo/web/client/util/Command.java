package ru.ifmo.web.client.util;

import lombok.Getter;
import lombok.Setter;

public enum Command {
    FIND_ALL("Вывести всех пользователей"),
    FIND_BY_FILTERS("Применить фильтры"),
    INSERT("Добавить пользователя"),
    UPDATE("Обновить информацию о пользователе"),
    DELETE("Удалить пользователя"),
    QUIT("Выйти");

    @Getter
    @Setter
    private String help;

    Command(String help) {
        this.help = help;
    }
}
