package vn.tts.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.tts.model.UserPrincipal;
import vn.tts.model.dto.UserPrincipalDto;
import vn.tts.repository.UserRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String userId = null;

        // Check if the header starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Extract token
            userId = jwtService.extractAccountId(token); // Extract username from token
        }

        // If the token is valid and no authentication is set in the context, then validate token and set authentication
        if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtService.validateAccessToken(token)) {
                UserPrincipalDto dto = userRepository.findUserInfoDetailById(UUID.fromString(userId))
                        .orElseThrow(()-> new UsernameNotFoundException("{message.user.not.found}"));

                List<SimpleGrantedAuthority> authorities;
                if (dto.getRole() != null) {
                    authorities = new ArrayList<>(
                            Stream.of(dto.getPermissions().split(","))
                                    .map(SimpleGrantedAuthority::new)
                                    .toList()
                    );
                    authorities.add(new SimpleGrantedAuthority(dto.getRole()));
                } else {
                    authorities = new ArrayList<>();
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        new UserPrincipal(
                                dto.getUserId(),
                                dto.getUsername(),
                                dto.getFullName(),
                                dto.getPassword(),
                                authorities
                        ),
                        null,
                        authorities
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}


