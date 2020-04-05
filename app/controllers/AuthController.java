package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.enums.ErrorCode;
import entities.jsonModel.StorageAllTab;
import entities.jsonModel.UserStorage;
import entities.jsonModel.UserTab;
import models.BackupFile;
import models.User;
import play.Play;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.i18n.Messages;
import repositories.BackupfileRepository;
import repositories.UserRepository;
import repositories.impl.BackupFileRepositoryImpl;
import repositories.impl.UserRepositoryImpl;
import services.IMailService;
import services.impl.MailService;
import utils.TokenUtlis;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class Auth Controller
 *
 * @author quanna
 */
@SuppressWarnings("ALL")
public class AuthController extends BaseController {
	final static int TYPE_JOIN = 1; // User Connect to the system
	final static int TYPE_QUIT = 2; // Client Send Quit Request
	final static int TYPE_LEAVE = 3; // Server Check
	final static int TYPE_LOGIN = 4; // Message with type check Logout
	final static int TYPE_MESSAGE = 5; // Message with type Dengon
	private final BackupfileRepository backupfileRepository;
	private final UserRepository userRepository;
	private final IMailService mailService;

	public AuthController() {
		backupfileRepository = new BackupFileRepositoryImpl();
		userRepository = new UserRepositoryImpl();
		mailService = new MailService();
	}

	public void getAllUser(){
			List<User> users = userRepository.all();
			if (users == null) {
				responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
			}
			List<Object[]> storageUsedUsers = userRepository.getTotalUsedUsers();
			List<UserStorage> userStorages = new ArrayList<>();
			for(Object[] data: storageUsedUsers){
				userStorages.add(new UserStorage(data));
			}
			List<UserTab> userTabs = new ArrayList<UserTab>();
			UserStorage userStorage = new UserStorage();
			for(User user: users){
				userStorage  = !userStorages.isEmpty() ? userStorages.stream().filter(i -> i.id == user.getId()).findFirst().orElse(null) : null;
				userTabs.add(new UserTab(user, userStorage));
			}
			responseSuccess(userTabs);

	}
	public void getStorageAll(){
		List<Object[]> data = userRepository.getStorageAll();
		if (data== null){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		StorageAllTab storageAllTab = new StorageAllTab(data.get(0));
		responseSuccess(storageAllTab);
	}
	public void changePasswordForUser(){
		Logger.info("API change password called");
		Map<String, String> bodyRequest = null;
		try {
			bodyRequest = mapper.readValue(request.body, Map.class);
		} catch (Exception e) {
			Logger.error(e, "Body is null");
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		String token = Objects.requireNonNull(bodyRequest).get("token");
		String password = Objects.requireNonNull(bodyRequest).get("password");
		String passwordConfirm = Objects.requireNonNull(bodyRequest).get("password_confirm");
		if (!StringUtils.isNotEmpty(token) || !StringUtils.isNotEmpty(password) || !StringUtils.isNotEmpty(passwordConfirm)) {
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		if(!password.equalsIgnoreCase(passwordConfirm)){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PASSWORD_CONFIRM_FALSE, Messages.get("PASSWORD_CONFIRM_FALSE"));
		}
		User userSelected = new User();
		if (token != null) {
			User user = TokenUtlis.decode(token);
			// user is null if token invalid
			userSelected = user;
			token = user != null ? token : null;
		}
		if(userSelected == null){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.USER_NOT_FOUND, Messages.get("USER_NOT_FOUND"));
		}

		userSelected.setPassword(password);
		User userUpdate = userRepository.changePassword(userSelected);
		responseSuccess(userUpdate);


	}
	public void changePasswordOfUser(){
		Logger.info("API change password called");
		Map<String, String> bodyRequest = null;
		try {
			bodyRequest = mapper.readValue(request.body, Map.class);
		} catch (Exception e) {
			Logger.error(e, "Body is null");
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		String token = TokenUtlis.getTokenFromRequest(request);
		String old_password = Objects.requireNonNull(bodyRequest).get("old_password");
		String password = Objects.requireNonNull(bodyRequest).get("password");
		String passwordConfirm = Objects.requireNonNull(bodyRequest).get("password_confirm");
		if (!StringUtils.isNotEmpty(token) ||!StringUtils.isNotEmpty(old_password) || !StringUtils.isNotEmpty(password)) {
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		User userSelected = new User();
		if (token != null) {
			User user = TokenUtlis.decode(token);
			userSelected = User.findById(user.id);
			token = user != null ? token : null;
		}
		if(userSelected == null){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.USER_NOT_FOUND, Messages.get("USER_NOT_FOUND"));
		}
		if(!old_password.equals(userSelected.getPassword())){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.OLD_PASSWORD_FALSE, "古いパスワードが正しくありません。");
		}
		if(!password.equalsIgnoreCase(passwordConfirm)){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PASSWORD_CONFIRM_FALSE, Messages.get("PASSWORD_CONFIRM_FALSE"));
		}


		userSelected.setPassword(password);
		User userUpdate = userRepository.changePassword(userSelected);
		responseSuccess(userUpdate);


	}
	public void getUserById(Integer id){
		User user = userRepository.getBy(id);
		if(user == null){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.USER_NOT_FOUND, Messages.get("USER_NOT_FOUND"));
		}
		UserTab userTab = new UserTab(user, null);
		responseSuccess(userTab);
	}
	public void getUserByToken(String token){
		User userSelected = new User();
		if (token != null) {
			User user = TokenUtlis.decode(token);
			// user is null if token invalid
			userSelected = user;
			token = user != null ? token : null;
		}
		if(userSelected == null){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.USER_NOT_FOUND, Messages.get("USER_NOT_FOUND"));
		}
		UserTab userTab = new UserTab(userSelected, null);
		responseSuccess(userTab);
	}
	public void getUserByEmail(String email) throws EmailException{
		User user = User.find("byEmail",email).first();
		if(user == null){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.USER_NOT_FOUND, Messages.get("USER_NOT_FOUND"));
		}
		ObjectNode objectNode = mapper.createObjectNode();
		String token = TokenUtlis.encode(user);
		objectNode.put("token", token);
		response.setCookie(Play.configuration.getProperty("token.auth"), token);
		String title = "[SmileBackup+] パスワードリセット";
		StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("パスワードを忘れた場合は以下から再設定することが可能です:\n%s");
		String hostDefault = Play.configuration.getProperty("host.default");
		String msg = String.format(stringBuilder.toString(),hostDefault+"/"+"reset_password/"+token);
		mailService.sentMailToUser(user, title,msg);
		UserTab userTab = new UserTab(user, null);
		responseSuccess(userTab);
	}
	public void createUser() throws EmailException {
		Logger.info("API create user called");
		Map<String, String> bodyRequest = null;
		try {
			bodyRequest = mapper.readValue(request.body, Map.class);
		} catch (Exception e) {
			Logger.error(e, "Body is null");
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		String uid = Objects.requireNonNull(bodyRequest).get("uid");
		String name = Objects.requireNonNull(bodyRequest).get("name");
		String email = Objects.requireNonNull(bodyRequest).get("email");
		String storage = Objects.requireNonNull(bodyRequest).get("storage");
		if (!StringUtils.isNotEmpty(uid) || !StringUtils.isNotEmpty(name) || !StringUtils.isNotEmpty(email)) {
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		String regex = "[0-9]*\\.?[0-9]*";
		String max = Play.configuration.getProperty("maximum.storage");
		if(!storage.matches(regex))
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, "0~"+max+"の間の数値を入力して下さい。");
		Double storageParamGb = Double.parseDouble(storage);
		if(storageParamGb > Integer.parseInt(max) || storageParamGb<0){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, "0~"+max+"の間の数値を入力して下さい。");
		}
		List<User> users = User.findAll();
		if(!users.isEmpty()){
			for(User user : users){
				if(user.getUid().equalsIgnoreCase(uid))
					responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.UID_DUPLICATE, Messages.get("UID_DUPLICATE"));
				if(user.getEmail().equalsIgnoreCase(email))
					responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.EMAIL_DUPPLICATE, Messages.get("EMAIL_DUPPLICATE"));
			}
		}
		User user = new User();
		user.setUid(uid);
		user.setName(name);
		user.setEmail(email);
		user.setStorage(Math.round(storageParamGb*1024*1024*1024));
		user.setPassword("admin@123");
		user.setRoles(1);
		User userSaved = userRepository.create(user);
		String hostDefault = Play.configuration.getProperty("host.default");
		String token = TokenUtlis.encode(userSaved);
		String msg = String.format("アカウント情報\nユーザー名:\t%s\nアクセス: %s",user.getUid(), hostDefault+"/"+"reset_password"+"/"+token);
		mailService.sentMailToUser(userSaved, "[SmileBackup+] ユーザ情報",msg);
		UserTab userTab = new UserTab(userSaved, null);
		responseSuccess(userTab);
	}
	public void deleteUser(Integer id) {
		Logger.info("API delete User called");
		if (id == null) {
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		List<BackupFile> backupFiles = backupfileRepository.listFileByUser(id);
		if(backupFiles.size()>0){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.DELETE_ERROR, "クラウドへファイルをアップロードしたことがあるユーザーであるため、削除できません。");
		}
		User user = userRepository.delete(id);
		if (user == null) {
			Logger.error("Delete user Error");
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.DELETE_ERROR, Messages.get("DELETE_ERROR"));
		} else {
			Logger.error("Delete user Success");
			UserTab userTab = new UserTab(user,null);
			responseSuccess(userTab);
		}
	}
	public void updateUser(){
		Logger.info("API update user called");
		Map<String, String> bodyRequest = null;
		try {
			bodyRequest = mapper.readValue(request.body, Map.class);
		} catch (Exception e) {
			Logger.error(e, "Body is null");
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}

        Integer id =  Integer.parseInt(Objects.requireNonNull(bodyRequest).get("id"));
		String uid = Objects.requireNonNull(bodyRequest).get("uid");
		String name = Objects.requireNonNull(bodyRequest).get("name");
		String email = Objects.requireNonNull(bodyRequest).get("email");
		String storage = Objects.requireNonNull(bodyRequest).get("storage");
		if (!StringUtils.isNotEmpty(uid) || !StringUtils.isNotEmpty(name) || !StringUtils.isNotEmpty(email)) {
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}
		String regex = "[0-9]*\\.?[0-9]*";
		String max = Play.configuration.getProperty("maximum.storage");
		if(!storage.matches(regex))
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, "0~"+max+"の間の数値を入力して下さい。");
		Double storageParamGb = Double.parseDouble(storage);
		if(storageParamGb != 0 && (storageParamGb > 1024 || storageParamGb<0)){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, "0~"+max+"の間の数値を入力して下さい。");
		}
		List<User> users = User.findAll();
		if(!users.isEmpty()){
			for(User user : users){
				if(user.getId() != id){
					if(user.getUid().equalsIgnoreCase(uid))
						responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.UID_DUPLICATE, Messages.get("UID_DUPLICATE"));
					if(user.getEmail().equalsIgnoreCase(email))
						responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.EMAIL_DUPPLICATE, Messages.get("EMAIL_DUPPLICATE"));
				}
			}

		}

		List<BackupFile> backupFiles = backupfileRepository.listFileByUser(id);
		Long sumSize = backupFiles.stream().mapToLong(x -> x.fileSize).sum();
		if(Math.round(storageParamGb*1024*1024*1024) < sumSize){
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, "容量制限は使用済みの容量より小さいです。 ");
		}
		User user = new User();
		user.setId(id);
		user.setUid(uid);
		user.setName(name);
		user.setEmail(email);
		user.setStorage(Math.round(storageParamGb*1024*1024*1024));
		User userSaved = userRepository.update(user);
		UserTab userTab = new UserTab(userSaved, null);
		responseSuccess(userTab);
	}

	/**
	 * Controller api login
	 * 
	 * @throws JSONException
	 */
	public void login() throws JSONException {
		Logger.info("API Login called");

		Map<String, String> bodyRequest = null;
		try {
			bodyRequest = mapper.readValue(request.body, Map.class);
		} catch (Exception e) {
			Logger.error(e, "Body is null");
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}

		String email = Objects.requireNonNull(bodyRequest).get("email");
		String password = bodyRequest.get("password");
		Logger.info("User login: %s", email);
		if (!StringUtils.isNotEmpty(email) || !StringUtils.isNotEmpty(password)) {
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));

		}

		ObjectNode objectNode = mapper.createObjectNode();
		User user;
		String adminEmail = Play.configuration.getProperty("admin.email");
		String adminPassword = Play.configuration.getProperty("admin.password");
		if (email.equalsIgnoreCase(adminEmail) && password.equals(adminPassword)) {
			user = new User();
			user.setId(0);
			user.setRoles(0);
			user.setUid("admin");
			user.setName("kanri");
			//user.setTidCopy(0);
			objectNode.put("jwtTokenType", "SuperAdmin");
			objectNode.put("role", 0);
		} else {
			user = userRepository.findByEmailAndPassword(email, password);
			objectNode.put("role", 1);
			if (user == null) {
				responseError(HttpStatus.SC_FORBIDDEN, ErrorCode.LOGIN_FAIL, Messages.get("LOGIN_FAIL"));
			}
			// Socket Prevent Duplicated Login
			//Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			JSONObject data = new JSONObject();
			data.put("data", "DOUBLE_LOGIN");
			data.put("type", TYPE_LOGIN);
			//objectNode.put("storage", user.getStorage());
			//room.say(Integer.toString(user.getTantoMaster().getId()) + "-" + timestamp.getTime(), data.toString());
		}

		String token = TokenUtlis.encode(user);
		objectNode.put("token", token);
		response.setCookie(Play.configuration.getProperty("token.auth"), token);

		// Setting Info
		objectNode.put("sessionTimeout", Play.configuration.getProperty("session.timeout"));
        objectNode.put("id", user.getId());
		objectNode.put("role", user.isRoles());
		objectNode.put("fullName", user.getName());
		objectNode.put("phoneNumber", user.phone);
		objectNode.put("sex", user.sex);
		objectNode.put("dateOfBirth", user.birthday.toString());

		objectNode.put("uid", user.getUid());
		renderJSON(mapper.convertValue(objectNode, Map.class));
	}

	/**
	 * Controller api detect token
	 */
	public void detectToken() {
		Logger.info("Detect token");
		renderJSON(userLogin);
	}

	/**
	 * Controller api logout
	 */
	public void logout() {
		response.removeCookie(Play.configuration.getProperty("token.auth"));
		renderJSON("ok");
	}

}
