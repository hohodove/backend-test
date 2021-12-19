package com.test.config;



import com.test.Handler.SimpleAccessDeniedHandler;
import com.test.Handler.SimpleAuthenticationEntryPoint;
import com.test.Handler.SimpleAuthenticationFailureHandler;
import com.test.Handler.SimpleAuthenticationSuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;


@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  };

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/swagger-ui/**",
                               "/v2/api-docs/**",
                               "/configuration/ui",
                               "/swagger-resources/**",
                               "/configuration/security",
                               "/swagger-ui.html",
                               "/webjars/**");
  }

  @Autowired
  public void configureGlocal(AuthenticationManagerBuilder auth, 
      @Qualifier("simpleUserDetailsService") UserDetailsService userDetailsService,
      PasswordEncoder passwordEncoder) throws Exception {
  
      auth.eraseCredentials(true)
          .userDetailsService(userDetailsService)
          .passwordEncoder(passwordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .mvcMatchers("/prelogin", "/greeting")
            .permitAll()
        .anyRequest()
            .authenticated()
        .and()
        .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler())
        .and()
            .formLogin()
            .loginProcessingUrl("/login").permitAll()
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
        .and()
        .logout()
            .logoutUrl("/logout")
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .logoutSuccessHandler(logoutSuccessHandler())
        .and()
        .csrf()
            //.ignoringAntMatchers("/login")
            .csrfTokenRepository(new CookieCsrfTokenRepository())
        ;
  }

  AuthenticationEntryPoint authenticationEntryPoint() {
    return new SimpleAuthenticationEntryPoint();
  }

  AccessDeniedHandler accessDeniedHandler() {
    return new SimpleAccessDeniedHandler();
  }

  AuthenticationSuccessHandler authenticationSuccessHandler() {
    return new SimpleAuthenticationSuccessHandler();
  }

  AuthenticationFailureHandler authenticationFailureHandler() {
    return new SimpleAuthenticationFailureHandler();
  }

  LogoutSuccessHandler logoutSuccessHandler() {
    return new HttpStatusReturningLogoutSuccessHandler();
  }

}