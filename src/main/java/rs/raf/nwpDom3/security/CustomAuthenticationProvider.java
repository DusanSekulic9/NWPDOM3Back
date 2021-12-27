package rs.raf.nwpDom3.security;

import rs.raf.nwpDom3.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rs.raf.nwpDom3.repositories.UserRepository;

import static java.util.Collections.emptyList;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private PasswordEncoder encoder;
	private UserRepository userRepository;

	@Autowired
	public CustomAuthenticationProvider(UserRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
	public Authentication authenticate(Authentication auth) throws AuthenticationException {

		String email = auth.getName();
		String password = auth.getCredentials().toString();

		User user = userRepository.findByEmail(email);


		if(user==null)
			throw new BadCredentialsException("Authentication failed, no such username!");

		if(user!=null){

//			System.out.println("enc null? :"+encoder);
//			System.out.println("pass null? :"+password);
//			System.out.println("distPass null? :"+user.getPassword() );
//			System.out.println("POKUSA SAM");

			// proveri sifru
			if (encoder.matches(password, user.getPassword())) {

				return new UsernamePasswordAuthenticationToken(email, password, emptyList());
			}

			throw new BadCredentialsException("Authentication failed, incorrect password!");

		}

		throw new BadCredentialsException("Authentication failed, end of method!");
	}

	@Override
	public boolean supports(Class<?> auth) {
		return auth.equals(UsernamePasswordAuthenticationToken.class);
	}

	public void setEncoder(PasswordEncoder encoder) {
		this.encoder = encoder;
	}
}
