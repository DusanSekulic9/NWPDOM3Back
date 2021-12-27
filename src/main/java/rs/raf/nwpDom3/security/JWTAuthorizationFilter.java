package rs.raf.nwpDom3.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import rs.raf.nwpDom3.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import rs.raf.nwpDom3.repositories.UserRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import static rs.raf.nwpDom3.security.SecurityConstants.*;



/**
 * Autorizacioja sluzi da proveri validnost JSON Web Tokena koji se nalazi u
 * poslatom requestu
 */
public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

	private UserRepository userRepository;

	@Autowired
	public JWTAuthorizationFilter(AuthenticationManager authManager,
								  UserRepository userRepository) {
		super(authManager);
		this.userRepository = userRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		String token = req.getHeader(HEADER_STRING);

		UsernamePasswordAuthenticationToken authentication = getAuthentication(req, token);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request, String token) {

		if(token==null) return null;

		// parsiranje tokena
		DecodedJWT jwt = JWT.require(Algorithm.HMAC512(SECRET.getBytes())).build()
				.verify(token.replace(TOKEN_PREFIX, ""));

		String email = jwt.getSubject();
		if (email == null) return null;

		User user = userRepository.findByEmail(email);

		// Provera da li se nalazi user u bazi
		if (user == null)	return null;

		return new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());

	}
}