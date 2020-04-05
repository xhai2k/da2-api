package models;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "post")
public class Post extends GenericModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;
//    public Integer category_id;
    public String province;
    public String district;
    public String ward;
    public String street;
    public String apartment_number;
    public String describe;
    public String images;
    public Integer acreage;
    public String direction;
    public Integer number_of_rooms;
    public Integer number_of_toliets;
    public String toliet_type;
    public String status;
    public String rental_object;
    public Double closing_time;
    public Integer price;
    public Timestamp date_submitted;
    public Timestamp date_expiration;
    public Double lat;
    public Double lng;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(nullable = true, name = "account_id")
    public User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(nullable = true, name = "category_id")
    public Category category;










}
