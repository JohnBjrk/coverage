package jb.coverboard.coverboardwebapp

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails
import org.springframework.stereotype.Component
import org.springframework.util.ErrorHandler
import org.springframework.web.client.RestTemplate
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView


@Component
@RestController
class HtmlController(val spotService: SpotService) {

    val clientId: String = System.getenv("CLIENT_ID")
    val clientSecret: String = System.getenv("CLIENT_SECRET")
    val callbackUrl: String = System.getenv("CALLBACK_URL")

    data class UserInfo(val id: String)

    data class Image(val url: String)
    data class Album(val images: Array<Image>)
    data class Item(val album: Album)
    data class Playing(val item: Item)

    data class TokenResponse(val access_token: String, val token_type: String, val scope: String,
                             val expires_in: Int, val refresh_token: String)

    data class RefreshTokenResponse(val access_token: String, val token_type: String, val scope: String,
                             val expires_in: Int)

    @GetMapping("/user")
    fun user(principal: Principal): Principal {
        return principal
    }

    @GetMapping("/u")
    fun u(principal: Principal): Any {
        return when (principal) {
            is OAuth2Authentication ->
                when (principal.details) {
                    is OAuth2AuthenticationDetails -> (principal.details as OAuth2AuthenticationDetails).tokenValue
                    else -> principal.details.toString()
                }
            else -> principal.name
        }
    }

//    @GetMapping("/reg")
//    fun reg(principal: Principal): Any? {
//        return when (principal) {
//            is OAuth2Authentication ->
//                when (principal.details) {
//                    is OAuth2AuthenticationDetails -> {
//                        val tok = (principal.details as OAuth2AuthenticationDetails).tokenValue
//                        spotService.registerToken(principal.name, tok)
//                        spotService.store()
//                    }
//                    else -> "Err"
//                }
//            else -> "Err"
//        }
//    }

    @CrossOrigin
    @GetMapping("/list")
    fun list(): Any? {
        spotService.restore()
        val coverUrls = ArrayList<String>()
        for (tokens in spotService.getTokens()) {
            var httpHeaders = HttpHeaders()
            httpHeaders.set("Authorization", "Bearer " + tokens.token)
            val httpEntity = HttpEntity("", httpHeaders)
            val restTemplate = RestTemplate()
            try {
                val playing = restTemplate.exchange("https://api.spotify.com/v1/me/player/currently-playing", HttpMethod.GET, httpEntity, Playing::class.java)
                if (playing.statusCode.is2xxSuccessful) {
                    if (playing.body != null && playing.body is Playing) {
                        coverUrls.add((playing.body as Playing).item.album.images[0].url)
                    }
                }
            } catch (e: HttpClientErrorException) {
                if (e.statusCode.value() == 401) {
                    var httpHeaders = HttpHeaders()
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    httpHeaders.setBasicAuth(clientId, clientSecret)
                    val map: MultiValueMap<String, String> = LinkedMultiValueMap()
                    map.add("refresh_token", tokens.refresh_token)
                    map.add("grant_type", "refresh_token")
                    val request: HttpEntity<MultiValueMap<String, String>> = HttpEntity(map, httpHeaders)
                    val restTemplate = RestTemplate()
                    val tokenRespose = restTemplate.exchange("https://accounts.spotify.com/api/token", HttpMethod.POST, request, RefreshTokenResponse::class.java)
                    if (tokenRespose.statusCode.is2xxSuccessful) {
                        if (tokenRespose.body is RefreshTokenResponse) {
                            val refreshed_tokens = tokenRespose.body as RefreshTokenResponse
                            val userName = getMyName(refreshed_tokens.access_token)
                            if (userName != null) {
                                spotService.registerToken(userName, refreshed_tokens.access_token, tokens.refresh_token)
                                spotService.store()
                            }
                        }
                    }
                    println("Refreshed token")
                }
            }
        }
        //coverUrls.add("https://i.scdn.co/image/b99b0f2f799051d4acb1edcab07838ab15442b33")
        return coverUrls
    }

    @GetMapping("/sp_callback")
    fun login(@RequestParam("code") code: String, @RequestParam("state") state: String): RedirectView {
        var httpHeaders = HttpHeaders()
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        httpHeaders.setBasicAuth(clientId, clientSecret)
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("code", code)
        map.add("redirect_uri", callbackUrl)
        map.add("grant_type", "authorization_code")
        val request: HttpEntity<MultiValueMap<String, String>> = HttpEntity(map, httpHeaders)
        val restTemplate = RestTemplate()
        val tokenRespose = restTemplate.exchange("https://accounts.spotify.com/api/token", HttpMethod.POST, request, TokenResponse::class.java)
        if (tokenRespose.statusCode.is2xxSuccessful) {
            if (tokenRespose.body is TokenResponse) {
                val tokens = tokenRespose.body as TokenResponse
                val userName = getMyName(tokens.access_token)
                if (userName != null) {
                    spotService.registerToken(userName, tokens.access_token, tokens.refresh_token)
                    spotService.store()
                }
            }
        }
        return RedirectView("/")
    }

    @GetMapping("/reg")
    fun redirectWithUsingRedirectPrefix(attributes: RedirectAttributes): RedirectView {
        attributes.addAttribute("response_type", "code")
        attributes.addAttribute("client_id", clientId)
        attributes.addAttribute("scope", "user-read-private user-read-email user-read-currently-playing user-read-playback-state")
        attributes.addAttribute("state", "st4te")
        attributes.addAttribute("redirect_uri", callbackUrl)
        return RedirectView("https://accounts.spotify.com/authorize")
    }

    fun getMyName(token: String): String? {
        var httpHeaders = HttpHeaders()
        httpHeaders.setBearerAuth(token)
        val httpEntity = HttpEntity("", httpHeaders)
        val restTemplate = RestTemplate()
        val me = restTemplate.exchange("https://api.spotify.com/v1/me", HttpMethod.GET, httpEntity, UserInfo::class.java)
        return if (me.statusCode.is2xxSuccessful) {
            if (me.body is UserInfo) {
                (me.body as UserInfo).id
            } else {
                null
            }
        } else {
            null
        }
    }

//    @GetMapping("/")
//    fun apabepa(): String {
//        val restTemplate = restTemplate()
//        val me = restTemplate.getForEntity("https://api.spotify.com/v1/me", String.javaClass)
//        return "ApaBepa"
//    }
}