package jb.coverboard.coverboardwebapp

import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class RequestLogger : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, o: Any): Boolean {
        println("Request: " + request.requestURL)
        return true
    }
}