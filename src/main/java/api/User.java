package api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private String email;
    private String password;
    private String name;

    //Пустой конструктор для Gson
    public User() {
    }

    //Конструктор без email
    public User(boolean email, String password, String name) {
        this.password = password;
        this.name = name;
    }

    //Конструктор без password
    public User(String email, boolean password, String name) {
        this.email = email;
        this.name = name;
    }

    //Конструктор без name
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String email) {
        this.email = email;
    }

    public User(boolean email, boolean password, String name) {
        this.name = name;
    }

    public User(boolean email, String password) {
        this.password = password;
    }
}


