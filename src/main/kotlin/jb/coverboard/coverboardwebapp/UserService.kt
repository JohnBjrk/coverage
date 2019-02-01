package jb.coverboard.coverboardwebapp

import com.google.gson.Gson
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.security.oauth2.client.OAuth2ClientContext
//import org.springframework.security.oauth2.client.OAuth2RestTemplate
//import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails
import org.springframework.stereotype.Service
import java.io.File

@Service
class UserService(val userRepository: UserRepository) {

    data class TokenDBJSON(val db: Map<String, Tokens>)

    data class Tokens(val token: String, val refresh_token: String)

    var tokenDB: MutableMap<String, Tokens> = HashMap()

    fun registerToken(user: String, token: String, refreshToken: String) {
        tokenDB[user] = Tokens(token, refreshToken)

        val byUserId = userRepository.getByUserId(user)
        if (byUserId != null) {
            val updatedUser = byUserId.copy(accessToken = token, refreshToken = refreshToken)
            userRepository.save(updatedUser)
        } else {
            val newUser = RegisteredUser(0, user, token, refreshToken)
            userRepository.save(newUser)
        }
    }

    fun getTokens(): List<Tokens> {
        return userRepository.findAll().map { Tokens(it.accessToken, it.refreshToken) }

        //return tokenDB.values.toTypedArray()
    }

    @Scheduled(fixedDelay = 5000)
    fun updateCurrentPlaying() {
        println("Update currently playing")
    }
}
