package repositories;

import java.util.List;

import models.User;

/**
 * @author quanna
 */
public interface UserRepository {
    User findByUidAndPassword(String username, String password);
    User findByEmailAndPassword(String mail, String password);
    List<User> all();
    User getBy(Integer id);
    User update(User user);
    User create(User user);
    User delete(Integer id);
    User changePassword(User user);
    List<Object[]> getTotalUsedUsers();
    List<Object[]> getStorageAll();
    
}
