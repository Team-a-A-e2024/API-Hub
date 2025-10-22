package dat.security.token;

import com.nimbusds.jose.JOSEException;
import dat.security.dtos.UserDTO;
import dat.security.exceptions.TokenCreationException;

import java.text.ParseException;

public interface ITokenSecurity {

    UserDTO getUserWithRolesFromToken(String token) throws ParseException;
    boolean tokenIsValid(String token, String secret) throws ParseException, JOSEException;
    boolean tokenNotExpired(String token) throws ParseException;
    int timeToExpire(String token) throws ParseException;

    String createToken(UserDTO user, String ISSUER, String TOKEN_EXPIRE_TIME, String SECRET_KEY) throws TokenCreationException;

}