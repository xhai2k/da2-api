package entities.jsonModel;

import models.User;
import utils.CommonUtils;

public class UserTab {
    public int id;
    public String uid;
    public String name;
    public int roles;
    public String email;
    public Long storage;
    public double storageGb;
    public double storageUsedGb;
    public UserTab(User user, UserStorage userStorage){
        this.id = user.id;
        this.uid = user.uid;
        this.name = user.name;
        this.roles = user.roles;
        this.email = user.email;
        this.storage = user.storage;
        this.storageGb = CommonUtils.bytesToGB(user.storage);
        if(userStorage != null){
            storageUsedGb = CommonUtils.bytesToGB(userStorage.totalUsed);
        }
    }
}
