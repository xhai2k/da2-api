package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entities.enums.ErrorCode;

import java.util.Map;

/**
 * Class ResponseUtils
 *
 * @author quanna
 */
public class ResponseUtils {
    /**
     * Method create response error
     *
     * @param code
     * @param message
     * @return Map Object
     */
    public static Map createError(ErrorCode code, String message) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode errorObject = mapper.createObjectNode();
        errorObject.put("code", code.toString());
        errorObject.put("message", message);
        ObjectNode response = mapper.createObjectNode();
        response.putPOJO("error", errorObject);
        return mapper.convertValue(response, Map.class);
    }
}
