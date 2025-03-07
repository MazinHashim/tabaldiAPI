package com.tabaldi.api.security;

import com.tabaldi.api.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

        private final JwtAuthenticationFilter jwtAuthFilter;
        private final AuthenticationProvider authenticationProvider;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                .csrf(httpSecurityCsrfConfigurer -> httpSecurityCsrfConfigurer.disable())
                // .exceptionHandling(httpSecurityExceptionHandlingConfigurer ->
                // httpSecurityExceptionHandlingConfigurer
                //// .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                // .accessDeniedHandler(customAccessDeniedHandler))

                .authorizeHttpRequests(
                        authorizationManagerRequestMatcherRegistry -> authorizationManagerRequestMatcherRegistry
                                .requestMatchers("/api/v1/users/profile")
                                .authenticated()
                                .requestMatchers("/api/v1/users/verify/changePhone/**")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/users/admin")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/users/add")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/users/delete/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/users/**", "/api/v1/files/**").permitAll()
                                .requestMatchers("/api/v1/details/admin/home")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/details/vendor/home/*")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name())
                                .requestMatchers("/api/v1/customers/profile")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/vendors/profile")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name())
                                .requestMatchers("/api/v1/cartItems/**")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/cartItems/update/quantity")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/customers/save")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/customers/**")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/orders/pending/*")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/orders/pending")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/orders/create/**")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/orders/history")
                                .hasAnyRole(Role.CUSTOMER.name(), Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/orders/cancel/**")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/orders/*/cartItems")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/orders/change/status/**")
                                .hasAnyRole(Role.SUPERADMIN.name(),
                                                Role.CUSTOMER.name(),
                                                Role.VENDOR_USER.name(),
                                                Role.VENDOR.name())
                                .requestMatchers("/api/v1/orders/will/pass/*")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/orders/save/note/*")
                                .hasAnyRole(Role.SUPERADMIN.name(),
                                        Role.VENDOR_USER.name(),
                                        Role.VENDOR.name())
                                .requestMatchers("/api/v1/orders/*")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/invoices/order/*")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR_USER.name(),Role.VENDOR.name(),
                                                Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/invoices/*/download")
                                .permitAll()
                                .requestMatchers("/api/v1/addresses/**")
                                .hasAnyRole(Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/categories/save")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name(), Role.VENDOR_USER.name())
                                .requestMatchers("/api/v1/categories/delete/*")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name(), Role.VENDOR_USER.name())
                                .requestMatchers("/api/v1/categories/toggle/publish/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/categories/**")
                                .hasAnyRole(Role.CUSTOMER.name(), Role.VENDOR.name(), Role.VENDOR_USER.name())
                                .requestMatchers("/api/v1/options/**")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name(), Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/products/toggle/publish/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/products/search").permitAll()
                                .requestMatchers("/api/v1/products/*").permitAll()
                                .requestMatchers("/api/v1/products/save")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name(), Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/products/*/add-image")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name(), Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/products/*/remove-image")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name(), Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/products/delete/*")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name(), Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/products/*/cart-items")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name(), Role.CUSTOMER.name(),
                                                Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/vendors/*/products")
                                .permitAll()
                                .requestMatchers("/api/v1/vendors/toggle/working/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/vendors/*/categories")
                                .permitAll()
                                .requestMatchers("/api/v1/vendors/*/advertisements")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name(), Role.VENDOR_USER.name())
                                .requestMatchers("/api/v1/vendors/*/users")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name())
                                .requestMatchers("/api/v1/vendors/*/orders")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name(), Role.CUSTOMER.name())
                                .requestMatchers("/api/v1/vendors/*/invoices")
                                .hasAnyRole(Role.VENDOR.name(), Role.VENDOR_USER.name())
                                .requestMatchers("/api/v1/vendors/save")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/vendors/delete/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/vendors/add/user")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name())
                                .requestMatchers("/api/v1/vendors/search/location/*")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name())
                                .requestMatchers("/api/v1/vendors/place/details/*")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name())
                                .requestMatchers("/api/v1/vendors/delete/user/*")
                                .hasAnyRole(Role.SUPERADMIN.name(), Role.VENDOR.name())
                                .requestMatchers("/api/v1/vendors").permitAll()
                                .requestMatchers("/api/v1/vendors/*").permitAll()
                                .requestMatchers("/api/v1/advertisements")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/advertisements/active")
                                .permitAll()
                                .requestMatchers(
                                                "/api/v1/advertisements/toggle/showing/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/advertisements/save")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/advertisements/delete/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .requestMatchers("/api/v1/advertisements/*")
                                .hasAnyRole(Role.SUPERADMIN.name())
                                .anyRequest().denyAll())
                .sessionManagement(
                                httpSecuritySessionManagementConfigurer -> httpSecuritySessionManagementConfigurer
                                                // this will create new session for each request
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
        }
}
