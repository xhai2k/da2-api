package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import models.User;
import play.Logger;
import play.Play;
import play.mvc.Http;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Class TokenUtlis
 *
 * @author quanna
 */
public class TokenUtlis {
    /**
     * Method encode token
     *
     * @param user
     * @return token
     */
    public static String encode(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("uid", user.getUid());
        userInfo.put("name", user.getName());
        userInfo.put("storage", user.getStorage());
        //userInfo.put("email", user.getEmail());
        //userInfo.put("tidCopy", user.getTidCopy());
        //userInfo.put("userImage", user.getUserImage());

        Map<String, Object> claimsUser = new HashMap<String, Object>();
        claimsUser.put("userInfo", userInfo);

        long expireTime = Calendar.getInstance().getTimeInMillis() +
                Long.parseLong(Play.configuration.getProperty("token.expireTime"));
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(Calendar.getInstance().getTime())
                .setClaims(claimsUser)
                .signWith(SignatureAlgorithm.HS512,
                        Play.configuration.getProperty("token.key"))
                .setExpiration(new Date(expireTime));
        return builder.compact();
    }

    /**
     * Method decode token
     *
     * @param token
     * @return User
     */
    public static User decode(String token) {
        Map userInfo;
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(Play.configuration.getProperty("token.key"))
                    .parseClaimsJws(token).getBody();
            userInfo = claims.get("userInfo", Map.class);
        } catch (Exception e) {
            Logger.error(e, e.getMessage());
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(userInfo, User.class);
    }

    /**
     * Method get token from request http
     *
     * @param request
     * @return token
     */
    public static String getTokenFromRequest(Http.Request request) {
        String token = null;
//        Http.Header headerToken = request.headers
//                .get(Play.configuration.getProperty("token.auth"));
//        if (headerToken != null) {
//            token = headerToken.value();
//        }
        Http.Cookie cookie = request.cookies.get(Play.configuration.getProperty("token.auth"));
        if(cookie != null){
            token = cookie.value;
        }
        return token;
    }
}