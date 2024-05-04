package pl.edu.agh.gem.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import pl.edu.agh.gem.internal.persistence.NotVerifiedUserRepository
import pl.edu.agh.gem.internal.persistence.VerifiedUserRepository
import pl.edu.agh.gem.security.UserDetailsServiceImpl

@Configuration
@EnableWebSecurity
class AuthConfig {

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    fun userDetailsService(
        notVerifiedUserRepository: NotVerifiedUserRepository,
        verifiedUserRepository: VerifiedUserRepository,
    ): UserDetailsService {
        return UserDetailsServiceImpl(notVerifiedUserRepository, verifiedUserRepository)
    }

    @Bean
    fun authenticationProvider(
        verifiedUserRepository: VerifiedUserRepository,
        notVerifiedUserRepository: NotVerifiedUserRepository,
    ): AuthenticationProvider {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService(notVerifiedUserRepository, verifiedUserRepository))
        authenticationProvider.setPasswordEncoder(encoder())

        return authenticationProvider
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .authorizeHttpRequests { auth -> auth.anyRequest().permitAll() }
        return http.build()
    }
}
