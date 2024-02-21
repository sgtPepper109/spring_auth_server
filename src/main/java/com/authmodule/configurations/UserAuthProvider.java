package com.authmodule.configurations;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.authmodule.dto.UserDto;
import com.authmodule.entities.User;
import com.authmodule.exceptions.AppException;
import com.authmodule.mappers.UserMapper;
import com.authmodule.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {
    Logger logger = LoggerFactory.getLogger(UserAuthProvider.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(UserDto dto) {
        Date now = new Date(), validity = new Date(now.getTime() + 3_600_000); // One hour
        return JWT.create()
                .withIssuer(dto.getLogin())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .withClaim("firstName", dto.getFirstName())
                .withClaim("lastName", dto.getLastName())
                .sign(Algorithm.HMAC256(secretKey));
    }

    public Authentication validateToken(String token){
        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT;
        try {
            decodedJWT = verifier.verify(token);
        } catch (Exception e) {
            logger.error("ERROR: Token invalid");
            throw new AppException("Error while validating the JWT", HttpStatus.NOT_FOUND);
        }
        Date now = new Date();
        if (now.after(decodedJWT.getIssuedAt()) && now.before(decodedJWT.getExpiresAt())) {
            UserDto user = UserDto.builder()
                    .login(decodedJWT.getIssuer())
                    .firstName(decodedJWT.getClaim("firstName").asString())
                    .lastName(decodedJWT.getClaim("lastName").asString())
                    .build();
            User oUser = userRepository.findByLogin(user.getLogin()).orElseThrow(() -> {
                logger.error("ERROR: Token invalid -> Issuer not found");
                throw new AppException("Unknown user", HttpStatus.NOT_FOUND);
            });
            if (!oUser.getFirstName().equals(user.getFirstName()) || !oUser.getLastName().equals(user.getLastName())) {
                logger.error("ERROR: Token Invalid -> Firstname and Lastname invalid");
                throw new AppException("Unknown user", HttpStatus.NOT_FOUND);
            }
            logger.info("INFO: Token VALID");
            return new UsernamePasswordAuthenticationToken(userMapper.userDto(oUser), null, Collections.emptyList());
        }
        logger.error("ERROR: Token Invalid");
        throw new AppException("Token Invalid", HttpStatus.NOT_FOUND);
    }
}
