package rs.raf.nwpDom3.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rs.raf.nwpDom3.repositories.UserRepository;

import java.util.Arrays;

import static rs.raf.nwpDom3.security.SecurityConstants.*;


@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {

	private CustomAuthenticationProvider customAuthenticationProvider;
	private BCryptPasswordEncoder encoder;
	private UserRepository userRepository;

	@Autowired
	public WebSecurity(CustomAuthenticationProvider customAuthenticationProvider,
					   BCryptPasswordEncoder encoder,
					   UserRepository userRepository) {
		super();
		this.customAuthenticationProvider = customAuthenticationProvider;
		this.encoder = encoder;
		this.userRepository = userRepository;

	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.cors().and().csrf().disable()
				.authorizeRequests()
				.antMatchers(LOGIN_PATH).permitAll()
				.anyRequest().authenticated().and()
				.addFilter(new JWTAuthenticationFilter(authenticationManager(),
													   userRepository))
				.addFilter(new JWTAuthorizationFilter(authenticationManager(),
						userRepository))
				.sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*")); // OVDE MORA DA IDE MOJ URL SAMO
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "PATCH"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.addExposedHeader("Authorization");
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {

		customAuthenticationProvider.setEncoder(encoder);
		auth.authenticationProvider(customAuthenticationProvider);
	}

}
