package com.sitepen.issuetracker.model;

    import jakarta.validation.constraints.Email;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.Size;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.data.annotation.Id;
    import org.springframework.data.mongodb.core.index.Indexed;
    import org.springframework.data.mongodb.core.mapping.Document;
    import org.springframework.security.core.GrantedAuthority;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;

    import java.util.Collection;
    import java.util.Collections;

    @Document(collection = "users")
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class User implements UserDetails {
        @Id
        private String id;

        @NotBlank
        @Size(max = 50)
        private String name;

        @NotBlank
        @Email
        @Indexed(unique = true)
        private String email;

        @NotBlank
        private String password;

        @Builder.Default
        private String role = "USER";

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
