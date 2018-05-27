package com.ffssabcloud.file_system;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
            .passwordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder())
            .withUser("admin")
            .password("{bcrypt}$2a$10$IRzhcAEwJ9c3FSJ/l7gLzOHk7hts7grSefkOXDCiU4OHZBubz95kS")
            .roles("ADMIN");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers(HttpMethod.POST).hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE).hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()
            .formLogin()
                .successHandler(new LoginSuccessHandler())
                .and()
            .csrf().disable();
    }
    
    private class PlainPasswordEncoder implements PasswordEncoder{

        @Override
        public String encode(CharSequence raw) {
            return raw.toString();
        }

        @Override
        public boolean matches(CharSequence raw, String other) {
            return other.equals(raw.toString());
        }
        
    }
    
    private class LoginSuccessHandler implements AuthenticationSuccessHandler {

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication auth)
                throws IOException, ServletException {
            request.getSession().setAttribute("authenticated", true);
            response.sendRedirect("/");
        }
        
    }

}
