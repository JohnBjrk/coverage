package jb.coverboard.coverboardwebapp

import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

@Component
class CustomAuthProvider: AuthenticationProvider {
    override fun authenticate(auth: Authentication?): Authentication {
        return if (auth != null) {
            UsernamePasswordAuthenticationToken(auth.name, auth.credentials, emptyList())
        } else {
            throw BadCredentialsException("Got null auth")
        }
    }

    override fun supports(auth: Class<*>?): Boolean {
        return auth != null && auth.equals(UsernamePasswordAuthenticationToken::class.java)
    }
}