package com.intelliservice.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── 完全放行 ──
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/mcp/**").permitAll()
                // 商品列表/详情无需登录
                .requestMatchers("/api/products/**").permitAll()
                // 判罚规则供 Python Agent 内部调用
                .requestMatchers("/api/penalty-rules/**").permitAll()
                // Python Agent 拉取配置
                .requestMatchers("/api/agent-configs/enabled").permitAll()
                .requestMatchers("/api/tool-configs/enabled").permitAll()
                // Python Agent 回调
                .requestMatchers("/api/knowledge/documents/*/status").permitAll()
                .requestMatchers("/api/tickets/*/ai-suggest").permitAll()

                // ── 仅 admin ──
                .requestMatchers("/api/knowledge/**").hasRole("ADMIN")
                .requestMatchers("/api/monitor/**").hasRole("ADMIN")
                .requestMatchers("/api/agent-configs/**").hasRole("ADMIN")
                .requestMatchers("/api/tool-configs/**").hasRole("ADMIN")

                // ── agent 或 admin ──
                .requestMatchers("/api/tickets/**").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/transfer/waiting").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/transfer/serving").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/transfer/accept").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/transfer/reply").hasAnyRole("AGENT", "ADMIN")
                .requestMatchers("/api/transfer/close").hasAnyRole("AGENT", "ADMIN")

                // ── 普通用户（buyer） ──
                .requestMatchers("/api/orders/**").authenticated()
                .requestMatchers("/api/transfer/request").hasRole("USER")
                .requestMatchers("/api/ratings/**").hasRole("USER")

                // ── 其他已登录均可 ──
                .requestMatchers("/api/chat/**").authenticated()
                .requestMatchers("/api/sessions/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
