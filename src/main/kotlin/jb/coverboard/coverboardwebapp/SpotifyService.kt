package jb.coverboard.coverboardwebapp

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.ArrayList

@Service
class SpotifyService(val userService: UserService) {

    data class PlayingData(val imageUrl: String, val spotifyUrl: String)

    var hasCurrentlyPlayingRequest: Boolean = false

    var globalPlayingData = emptyList<PlayingData>()

    val clientId: String = System.getenv("CLIENT_ID")
    val clientSecret: String = System.getenv("CLIENT_SECRET")

    data class Image(val url: String)
    data class Album(val images: Array<Image>)
    data class ExternalUrls(val spotify: String)
    data class Item(val album: Album, val external_urls: ExternalUrls)

    sealed class CurrentlyPlayingResult {
        data class Playing(val is_playing: Boolean, val item: Item) : CurrentlyPlayingResult()
        object NeedsRefresh : CurrentlyPlayingResult()
        object Error : CurrentlyPlayingResult()
    }

    data class RefreshTokenResponse(val access_token: String, val token_type: String, val scope: String,
                                    val expires_in: Int)

    data class UserInfo(val id: String)

    fun requestDataRefresh() {
        hasCurrentlyPlayingRequest = true
    }

    fun getCoverUrls(): List<String> {
        return globalPlayingData.map { it.imageUrl }
    }

    fun getPlayingData(): List<PlayingData> {
        return globalPlayingData
    }

    fun getCurrentlyPlaying(accessToken: String): CurrentlyPlayingResult {
        var httpHeaders = HttpHeaders()
        httpHeaders.set("Authorization", "Bearer " + accessToken)
        val httpEntity = HttpEntity("", httpHeaders)
        val restTemplate = RestTemplate()
        try {
            val playing = restTemplate.exchange("https://api.spotify.com/v1/me/player/currently-playing", HttpMethod.GET, httpEntity, CurrentlyPlayingResult.Playing::class.java)
            //println("Got resp: " + playing.statusCodeValue)
            if (playing.statusCode.is2xxSuccessful) {
                if (playing.body != null && playing.body is CurrentlyPlayingResult.Playing) {
                    val p = playing.body as CurrentlyPlayingResult.Playing
                    if (p.is_playing) {
                        return p
                    }
                }
            }
        } catch (e: HttpClientErrorException) {
            if (e.statusCode.value() == 401) {
                return CurrentlyPlayingResult.NeedsRefresh
            }
        }
        return CurrentlyPlayingResult.Error
    }

    fun refreshAccessToken(refreshToken: String) {
        val httpHeaders = HttpHeaders()
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED)
        httpHeaders.setBasicAuth(clientId, clientSecret)
        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        map.add("refresh_token", refreshToken)
        map.add("grant_type", "refresh_token")
        val request: HttpEntity<MultiValueMap<String, String>> = HttpEntity(map, httpHeaders)
        val restTemplate = RestTemplate()
        val tokenRespose = restTemplate.exchange("https://accounts.spotify.com/api/token", HttpMethod.POST, request, RefreshTokenResponse::class.java)
        if (tokenRespose.statusCode.is2xxSuccessful) {
            if (tokenRespose.body is RefreshTokenResponse) {
                val refreshed_tokens = tokenRespose.body as RefreshTokenResponse
                val userName = getMyName(refreshed_tokens.access_token)
                if (userName != null) {
                    userService.registerToken(userName, refreshed_tokens.access_token, refreshToken)
                }
            }
        }
        println("Refreshed token")
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

    @Scheduled(fixedDelay = 5000)
    fun updateCurrentPlaying() {
        val playingDataList = ArrayList<PlayingData>()
        if (hasCurrentlyPlayingRequest) {
            println("Update currently playing")
            for (tokens in userService.getTokens()) {
                val currentlyPlayingResult = getCurrentlyPlaying(tokens.token)
                when (currentlyPlayingResult) {
                    is SpotifyService.CurrentlyPlayingResult.Playing -> {
                        if (currentlyPlayingResult.is_playing) {
                            val playingData = PlayingData(currentlyPlayingResult.item.album.images[0].url,
                                    currentlyPlayingResult.item.external_urls.spotify)
                            playingDataList.add(playingData)
                        }
                    }
                    is SpotifyService.CurrentlyPlayingResult.NeedsRefresh -> {
                        refreshAccessToken(tokens.refresh_token)
                    }
                }
            }
            hasCurrentlyPlayingRequest = false
        }
        globalPlayingData = playingDataList
    }
}