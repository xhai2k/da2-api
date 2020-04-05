package repositories.impl;

import java.util.List;

import models.BackupFile;
import models.User;
import play.db.jpa.JPA;
import repositories.UserRepository;
import utils.EncoderUtils;

import javax.persistence.Query;

/*
 *Class UserRepository implements
 * @author quanna
 */
public class UserRepositoryImpl implements UserRepository {
	
	/**
	 * getImageByTid
	 * @param
	 * 
	 * @return
	 * 
	 */
	@Override
	public User getBy(Integer id) {
		User user = new User();
		if(id== null){
			return null;
		}
		return user.find("byId", id).first();
	}
	@Override
	public User changePassword(User user){
		User modelUpdate = User.findById(user.getId());
		modelUpdate.setPassword(user.getPassword());
		return modelUpdate.save();
	}
	@Override
	public User update(User user){
		User userUpdate = User.findById(user.getId());
		userUpdate.setUid(user.getUid());
		userUpdate.setName(user.getName());
		userUpdate.setEmail(user.getEmail());
		userUpdate.setStorage(user.getStorage());
		return userUpdate.save();
	}
	@Override
	public User create(User user){
		return user.save();
	}
	@Override
	public User delete(Integer id){
		User modelDelete = User.findById(id);
		return modelDelete.delete();
	}
    /**
     * Method find user for login
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public User findByUidAndPassword(String username, String password) {
        //String passwordEncode = EncoderUtils.generateAuthenticationKey(password);
        return User.find("byUidAndPassword", username, password).first();
    }
	public User findByEmailAndPassword(String mail, String password) {
		//String passwordEncode = EncoderUtils.generateAuthenticationKey(password);
		return User.find("byEmailAndPassword", mail, password).first();
	}
    /**
     * Method GET
     *
     * @return
     */
    @Override
    public List<User> all() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("SELECT u FROM User u ORDER BY u.id ASC");
		Query query  = JPA.em().createQuery(stringBuilder.toString(), User.class);
		return query.getResultList();
    }
    @Override
	public List<Object[]> getTotalUsedUsers(){
    	StringBuilder stringBuilder = new StringBuilder();
    	stringBuilder.append("SELECT u.id, sum(file_size) as total");
    	stringBuilder.append(" FROM backup_file b inner join users u on b.user_id = u.id group by u.id");
		Query query1 = JPA.em().createNativeQuery(stringBuilder.toString());
		return query1.getResultList();
	}
	@Override
	public List<Object[]> getStorageAll(){
    	StringBuilder stringBuilder = new StringBuilder();
    	stringBuilder.append(" SELECT sum(filesize) as used, sum(storageAll) as all FROM ");
    	stringBuilder.append(" (SELECT sum(b.file_size) as filesize, 0 as storageAll from backup_file b right join users u on b.user_id = u.id ");
    	stringBuilder.append(" union all select  0 as filesize, sum(u.storage) as storageAll from users u ) as result");
		Query query1 = JPA.em().createNativeQuery(stringBuilder.toString());
		return query1.getResultList();
	}
}
