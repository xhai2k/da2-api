package models;

import javax.persistence.*;

import play.db.jpa.GenericModel;
import utils.DateUtlis;

/**
 * Class User
 *
 * @author クオン
 */
@Entity
@Table(name = "users")
public class User extends GenericModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int id;

    @Column(name = "uid")
    public String uid;

    @Column(name = "password")
    public String password;

    @Column(name = "name")
    public String name;

    @Column(name = "roles")
    public int roles;

    @Column(name = "email")
    public String email;

    @Column(name = "storage")
    public Long storage;
    @Column(name = "avatar")
    public String avatar;
    @Column(name = "phone")
    public String phone;
    @Column(name = "sex")
    public String sex;
    @Column(name = "birthday")
    public java.sql.Date birthday;


    public User() {
        super();
    }

    public User(int id, String uid, String name, int roles, String email, Long storage) {
        this.id = id;
        this.uid = uid;
        this.name = name;
        this.roles = roles;
        this.email = email;
        this.storage = storage;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int isRoles() {
        return roles;
    }

    public void setRoles(int roles) {
        this.roles = roles;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getStorage() {
        return storage;
    }

    public void setStorage(Long storage) {
        this.storage = storage;
    }
}
