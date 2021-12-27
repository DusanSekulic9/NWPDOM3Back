package rs.raf.nwpDom3.security;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import rs.raf.nwpDom3.entities.Permission;
import rs.raf.nwpDom3.entities.User;
import rs.raf.nwpDom3.forms.LogInForm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rs.raf.nwpDom3.repositories.UserRepository;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static rs.raf.nwpDom3.security.SecurityConstants.*;


/**
 * Sluzi da da JSON Web Token user-u koji pokusava da pristupi (user salje
 * username i password).
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private AuthenticationManager authenticationManager;
	private UserRepository userRepository;
	private Gson gson = new Gson();

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager,
								   UserRepository userRepository) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res)
			throws AuthenticationException {

		try {
			LogInForm user = new ObjectMapper().readValue(req.getInputStream(), LogInForm.class);
			UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getUsername(),
																							    user.getPassword(),
																							    Collections.emptyList());
			return authenticationManager.authenticate(token);

		}catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
											Authentication auth) throws IOException {

		String email = auth.getName();
		String token;

		User user = userRepository.findByEmail(email);

		Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		Instant expiration = issuedAt.plus(30, ChronoUnit.DAYS);

		if(user==null)
			return;

		if(user!=null){
			token = JWT.create()
				.withSubject(email)
				.withClaim("can_create_users", user.isCan_create_users())
				.withClaim("can_read_users", user.isCan_read_users())
				.withClaim("can_update_users", user.isCan_update_users())
				.withClaim("can_delete_users", user.isCan_delete_users())
				.withExpiresAt(Date.from(expiration))
				.sign(HMAC512(SECRET.getBytes()));

			res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
			res.getWriter().write(gson.toJson(user));

		}

	}
}