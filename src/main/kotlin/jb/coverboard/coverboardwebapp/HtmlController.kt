package jb.coverboard.coverboardwebapp

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import org.springframework.web.servlet.view.RedirectView
import java.util.*
import javax.servlet.http.HttpServletRequest


@Component
@RestController
class HtmlController(val userService: UserService,
                     val spotifyService: SpotifyService,
                     val httpServletRequest: HttpServletRequest,
                     val authManager: AuthenticationManager) {

    val clientId: String = System.getenv("CLIENT_ID")
    val clientSecret: String = System.getenv("CLIENT_SECRET")
    val callbackUrl: String = System.getenv("CALLBACK_URL")

    data class TokenResponse(val access_token: String, val token_type: String, val scope: String,
                             val expires_in: Int, val refresh_token: String)

    @GetMapping("/test")
    fun test(): String {
        return "test"
    }

    @CrossOrigin
    @GetMapping("/list")
    fun list(): Any? {
        return spotifyService.getCoverUrls()
//        val coverUrls = ArrayList<String>()
//        for (tokens in userService.getTokens()) {
//            val currentlyPlayingResult = spotifyService.getCurrentlyPlaying(tokens.token)
//            when (currentlyPlayingResult) {
//                is SpotifyService.CurrentlyPlayingResult.Playing -> {
//                    if (currentlyPlayingResult.is_playing) {
//                        coverUrls.add(currentlyPlayingResult.item.album.images[0].url)
//                    }
//                }
//                is SpotifyService.CurrentlyPlayingResult.NeedsRefresh -> {
//                    spotifyService.refreshAccessToken(tokens.refresh_token)
//                }
//            }
//        }
//        return coverUrls
    }

    @GetMapping("/sp_callback")
    fun login(@RequestParam("code") code: String, @RequestParam("state") state: String): RedirectView {
        if (state.equals(httpServletRequest.session.getAttribute("spotify_auth_state"))) {
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
                    val userName = spotifyService.getMyName(tokens.access_token)
                    if (userName != null) {
                        userService.registerToken(userName, tokens.access_token, tokens.refresh_token)
                        this.httpServletRequest.session.setAttribute("spotify_user_id", userName)
                        val authReq = UsernamePasswordAuthenticationToken(userName, tokens.access_token)
                        val auth = authManager.authenticate(authReq)
                        val sc = SecurityContextHolder.getContext()
                        sc.authentication = auth
                        httpServletRequest.getSession(true).setAttribute(SPRING_SECURITY_CONTEXT_KEY, sc)
                    }
                }
            }
            return RedirectView("/")
        } else {
            return RedirectView("/?error=authfail")
        }
    }

    @GetMapping("/reg")
    fun redirectWithUsingRedirectPrefix(attributes: RedirectAttributes): RedirectView {
        val state = UUID.randomUUID().toString()
        httpServletRequest.session.setAttribute("spotify_auth_state", state)
        attributes.addAttribute("response_type", "code")
        attributes.addAttribute("client_id", clientId)
        attributes.addAttribute("scope", "user-read-private user-read-email user-read-currently-playing user-read-playback-state")
        attributes.addAttribute("state", state)
        attributes.addAttribute("redirect_uri", callbackUrl)
        return RedirectView("https://accounts.spotify.com/authorize")
    }
}