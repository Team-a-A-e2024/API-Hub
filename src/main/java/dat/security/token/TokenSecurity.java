package dat.security.token;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import dat.security.token.ITokenSecurity;
import dat.security.exceptions.TokenCreationException;
import dat.security.dtos.UserDTO;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class TokenSecurity implements ITokenSecurity {

    @Override
    public UserDTO getUserWithRolesFromToken(String token) throws ParseException {
        // Return a user with Set of roles as strings
        SignedJWT jwt = SignedJWT.parse(token);
        String roles = jwt.getJWTClaimsSet().getClaim("roles").toString();
        String username = jwt.getJWTClaimsSet().getClaim("username").toString();

        Set<String> rolesSet = Arrays
                .stream(roles.split(","))
                .collect(Collectors.toSet());
        return new UserDTO(username, rolesSet);
    }
    @Override
    public boolean tokenIsValid(String token, String secret) throws ParseException, JOSEException{
        SignedJWT jwt = SignedJWT.parse(token);
        if (jwt.verify(new MACVerifier(secret)))
            return true;
        else
            return false;
    }

    @Override
    public boolean tokenNotExpired(String token) throws ParseException{
        if (timeToExpire(token) > 0)
            return true;
        else
            return false;
    }
    @Override
    public int timeToExpire(String token) throws ParseException{
        SignedJWT jwt = SignedJWT.parse(token);
        return (int) (jwt.getJWTClaimsSet().getExpirationTime().getTime() - new Date().getTime());
    }

    @Override
    public String createToken(UserDTO user, String ISSUER, String TOKEN_EXPIRE_TIME, String SECRET_KEY) throws TokenCreationException {
        // https://codecurated.com/blog/introduction-to-jwt-jws-jwe-jwa-jwk/
        try {
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .issuer(ISSUER)
                    .claim("UserId", user.getId())
                    .claim("username", user.getUsername())
                    .claim("roles", user.getRoles().stream().reduce((s1, s2) -> s1 + "," + s2).get())
                    .expirationTime(new Date(new Date().getTime() + Integer.parseInt(TOKEN_EXPIRE_TIME)))
                    .build();
            Payload payload = new Payload(claimsSet.toJSONObject());
            System.out.println(SECRET_KEY);
            JWSSigner signer = new MACSigner(SECRET_KEY);
            System.out.println(signer);
            JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
            JWSObject jwsObject = new JWSObject(jwsHeader, payload);
            jwsObject.sign(signer);
            return jwsObject.serialize();

        } catch (JOSEException e) {
            e.printStackTrace();
            throw new TokenCreationException("Could not create token", e);
        }
    }
}

