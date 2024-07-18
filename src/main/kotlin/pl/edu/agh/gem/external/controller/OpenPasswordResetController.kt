package pl.edu.agh.gem.external.controller

import org.springframework.http.MediaType.TEXT_HTML_VALUE
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import pl.edu.agh.gem.internal.service.AuthService
import pl.edu.agh.gem.paths.Paths.OPEN

@Controller
@RequestMapping("$OPEN/reset-password")
class OpenPasswordResetController(
    private val authService: AuthService,
) {
    @GetMapping(produces = [TEXT_HTML_VALUE])
    fun sendPassword(
        @RequestParam email: String,
        @RequestParam code: String,
    ): String {
        authService.sendPasswordEmail(email, code)
        return "success"
    }
}
