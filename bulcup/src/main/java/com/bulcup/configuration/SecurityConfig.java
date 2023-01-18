package com.bulcup.configuration;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bulcup.service.ManagerService;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@RequiredArgsConstructor
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private ManagerService managerService;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
		.authorizeRequests()
		.antMatchers("/user/**/","/manager/**/*").permitAll()

		.anyRequest().authenticated()

		.and()
		.formLogin()
		.loginPage("/manager/manager_login")
		//.loginProcessingUrl("/loginProcess")
		//.defaultSuccessUrl("/loginSuccess")  		
		.permitAll()
		.and()
		.csrf().disable()
		.logout()
		.permitAll();

	}

	@Bean
	public PasswordEncoder getPasswordEncoder() {
		return new BCryptPasswordEncoder();
	}
}