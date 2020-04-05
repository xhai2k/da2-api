package entities.jsonModel;

import models.Post;

import java.sql.Date;
import java.sql.Timestamp;

public class PostDto {
    public Integer id;
    public String province;
    public String district;
    public String ward;
    public String street;
    public String apartmentNumber;
    public String describe;
    public String images;
    public Integer acreage;
    public String direction;
    public Integer numberOfRooms;
    public Integer numberOfToliets;
    public String tolietType;
    public String status;
    public String rentalObject;
    public Double closingTime;
    public Integer price;
    public java.util.Date dateSubmitted;
    public java.util.Date dateExpiration;
    public Boolean isActive;
    public Double lat;
    public Double lng;

    public int accountId;
    public String fullName;
    public String avatar;
    public String email;
    public String phoneNumber;
    public String sex;
    public Date dateOfBirth;
    public Integer role;

    public Integer categoryId;
    public String categoryName;

    public PostDto(Post post) {
        this.id = post.id;
        this.province = post.province;
        this.district = post.district;
        this.ward = post.ward;
        this.street = post.street;
        this.apartmentNumber = post.apartment_number;
        this.describe = post.describe;
        this.images = post.images;
        this.acreage = post.acreage;
        this.direction = post.direction;
        this.numberOfRooms = post.number_of_rooms;
        this.numberOfToliets = post.number_of_toliets;
        this.tolietType = post.toliet_type;
        this.status = post.status;
        this.rentalObject = post.rental_object;
        this.closingTime = post.closing_time;
        this.price = post.price;
        this.dateSubmitted = post.date_submitted != null ? new Date(post.date_submitted.getTime()) : null;
        this.dateExpiration = post.date_expiration != null ? new Date(post.date_expiration.getTime()) : null;
        this.lat = post.lat;
        this.lng = post.lng;

        if(post.user != null){
            this.accountId = post.user.id;
            this.fullName = post.user.name;
            this.avatar = post.user.avatar;
            this.phoneNumber = post.user.phone;
            this.sex = post.user.sex;
            this.dateOfBirth = post.user.birthday;
            this.role = post.user.roles;
        }
        if(post.category != null){
            this.categoryId = post.category.id;
            this.categoryName = post.category.name;
        }
    }
}
