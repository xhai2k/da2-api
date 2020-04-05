package controllers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import entities.enums.ErrorCode;
import models.User;

import org.apache.http.HttpStatus;
import play.Play;
import play.data.validation.Validation;
import play.i18n.Messages;
import play.mvc.After;
import play.mvc.Before;
import play.mvc.Controller;
import utils.ResponseUtils;
import utils.TokenUtlis;

/**
 * Class Base Controller
 *
 * @author quanna
 */
public class BaseController extends Controller {

	protected final ObjectMapper mapper;
	protected String token;
	protected User userLogin;

	public BaseController() {
		this.mapper = new ObjectMapper();
		this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * Function filter all request
	 */
	@Before(unless = { "AuthController.login", "AuthController.logout","AuthController.getUserByEmail",
			"AuthController.getUserByToken", "AuthController.changePasswordForUser" })
	public void checkToken() {

		if (Validation.hasErrors()) {
			responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
		}

		String token = TokenUtlis.getTokenFromRequest(request);
		if (token != null) {
			User user = TokenUtlis.decode(token);
			// user is null if token invalid
			this.userLogin = user;
			this.token = user != null ? token : null;
		}

		if (this.userLogin == null) {
			responseError(HttpStatus.SC_FORBIDDEN, ErrorCode.INVALID_TOKEN, Messages.get("INVALID_TOKEN"));
		}

	}

	/**
	 * Function catch all response for CORS
	 */
	@After
	public void affer() {

		response.accessControl("*");
		if (this.token != null) {
			this.token = TokenUtlis.encode(this.userLogin);
			response.setCookie(Play.configuration.getProperty("token.auth"), token);
		}
	}

	/**
	 * Handle error response
	 *
	 * @param status
	 * @param errorCode
	 * @param msg
	 */
	protected void responseError(int status, ErrorCode errorCode, String msg) {

		response.status = status;
		renderJSON(ResponseUtils.createError(errorCode, msg));
	}

	/**
	 * Handle success response
	 *
	 * @param object
	 */
	protected void responseSuccess(Object object) {

		String json = mapper.valueToTree(object).toString();
		renderJSON(json);
	}

}
