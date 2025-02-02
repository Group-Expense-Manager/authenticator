package pl.edu.agh.gem.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl(private val email: String, private val password: String) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return mutableListOf()
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return email
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}
