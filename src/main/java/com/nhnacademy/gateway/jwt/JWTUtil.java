package com.nhnacademy.gateway.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;

/**
 * JWT Utility Class
 *
 * @author 오연수
 */
@Component
public class JWTUtil {
	private SecretKey secretKey;

	public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
		// 양방향 암호화 알고리즘 사용
		this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
			Jwts.SIG.HS256.key().build().getAlgorithm());
	}

	/**
	 * JWT 에서 Email 정보를 가져온다.
	 *
	 * @param token access token
	 * @return email
	 */
	public String getUsername(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("username", String.class);
	}

	/**
	 * JWT 에서 멤버의 권한을 가져온다.
	 *
	 * @param token access token
	 * @return auth
	 */
	public List<String> getAuth(String token) {
		String authoritiesString = Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("auth", String.class);

		return Arrays.stream(authoritiesString.split(","))
			.collect(Collectors.toList());
	}

	/**
	 * JWT 에서 멤버의 Id를 가져온다.
	 *
	 * @param token access token
	 * @return the member id
	 */
	public Long getMemberId(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("memberId", Long.class);
	}

	/**
	 * JWT 에서 멤버의 uuid 를 가져온다.
	 *
	 * @param token 토큰
	 * @return the uuid
	 */
	public String getUuid(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.get("uuid", String.class);
	}

	/**
	 * JWT 유효 기간(만료 기간) 체크한다.
	 *
	 * @param token access token
	 * @return 유효성
	 */
	public Boolean isExpired(String token) {

		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload()
			.getExpiration()
			.before(new Date());
	}
}
