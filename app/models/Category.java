package models;

import play.db.jpa.GenericModel;

import javax.persistence.*;

@Entity
@Table(name = "categories")
public class Category extends GenericModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;
    public String name;
}
