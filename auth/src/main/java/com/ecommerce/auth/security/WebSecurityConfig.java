package com.ecommerce.auth.security;


import com.ecommerce.auth.security.jwt.AuthEntryPointJwt;
import com.ecommerce.auth.security.jwt.AuthTokenFilter;
import com.ecommerce.auth.security.services.UserDetailsServiceIml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
//@EnableMethodSecurity
@EnableWebSecurity
public class WebSecurityConfig {
    /*
Tôi muốn xác thực bằng JWT, dùng user trong database, mật khẩu mã hóa, filter riêng của mình
Spring Security hỏi: Ai load user? Ai xử lý JWT filter? Ai mã hóa password?
Ai xử lý lỗi khi không có quyền? Ai xác thực đăng nhập username/password?
Ai điều phối tất cả?”
   */
    //Ai xử lý việc load thông tin người dùng? → UserDetailsServiceImpl
    @Autowired
    UserDetailsServiceIml userDetailsService;

    //Ai xử lý JWT filter? → AuthTokenFilter
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    //Nếu không có quyền thì ai trả lỗi? → AuthEntryPointJwt
    @Bean
    public AuthTokenFilter authTokenFilter(){
        return new AuthTokenFilter();
    }

    //Ai xác thực username + password? → DaoAuthenticationProvider (dùng UserDetailsService + PasswordEncoder)
    @Bean
   public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }
    // Dùng để mã hóa và kiểm tra mật khẩu người dùng → BCryptPasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    //  Ai điều phối quá trình xác thực? → AuthenticationManager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    //FitterChain của riêng tôi
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/products/**",
                                "/error",
                                "/images/**",
                                "/api/payments/sepapy-callback",
                                "/api/orders/status/**",
                                "/calljson"
                        ).permitAll()
                        .requestMatchers("/api/payments/qr").authenticated()
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/deliver/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_DELIVER")
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }



}
