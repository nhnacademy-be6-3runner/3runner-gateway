package com.nhnacademy.gateway.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT Utility class 에 대한 테스트입니다.
 *
 * @author 오연수
 */
public class JwtTest {

	private JWTUtil jwtUtil;
	private SecretKey secretKey;

	/**
	 * Sets up.
	 */
	@BeforeEach
	public void setUp() {
		String secret = "my-very-secure-secret-key-111111jdasdlfjqwlefjxlzvmqwefdsfcxmvoiwfjp";
		secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),
			SignatureAlgorithm.HS256.getJcaName());
		jwtUtil = new JWTUtil(secret);
	}

	private String createTestToken(String username, String auth, Long memberId, Date expiration) {
		return Jwts.builder()
			.claim("username", username)
			.claim("auth", auth)
			.claim("memberId", memberId)
			.expiration(expiration)
			.signWith(secretKey)
			.compact();
	}


	@DisplayName("토큰에서 유저 이름 검사")
	@Test
	public void testGetUsername() {
		String token = createTestToken("testUser", "ROLE_USER", 1L, new Date(System.currentTimeMillis() + 10000));
		String username = jwtUtil.getUsername(token);
		assertEquals("testUser", username);
	}

	@DisplayName("토큰에서 권한 검사")
	@Test
	public void testGetAuth() {
		String token = createTestToken("testUser", "ROLE_USER", 1L, new Date(System.currentTimeMillis() + 10000));
		List<String> auth = jwtUtil.getAuth(token);
		assertEquals(1, auth.size());
		assertEquals("ROLE_USER", auth.get(0));
	}

	@DisplayName("토큰에서 복수 권한 검사")
	@Test
	public void testGetAuths() {
		String token = createTestToken("testUser", "ROLE_USER,ROLE_ADMIN", 1L, new Date(System.currentTimeMillis() + 10000));
		List<String> auth = jwtUtil.getAuth(token);
		assertEquals(2, auth.size());
		assertEquals("ROLE_USER", auth.getFirst());
		assertEquals("ROLE_ADMIN", auth.getLast());
	}

	@DisplayName("토큰에서 멤버 아이디 검사")
	@Test
	public void testGetMemberId() {
		String token = createTestToken("testUser", "ROLE_USER", 1L, new Date(System.currentTimeMillis() + 10000));
		Long memberId = jwtUtil.getMemberId(token);
		assertEquals(1L, memberId);
	}

	@DisplayName("토큰에서 유효 기간 검사")
	@Test
	public void testIsExpired() {
		assertThrows(io.jsonwebtoken.ExpiredJwtException.class, () -> {
			jwtUtil.isExpired(createTestToken("testUser", "ROLE_USER", 1L, new Date(System.currentTimeMillis() - 10000)));
		});

		String token = createTestToken("testUser", "ROLE_USER", 1L, new Date(System.currentTimeMillis() + 10000));
		assertFalse(jwtUtil.isExpired(token));
	}
}
