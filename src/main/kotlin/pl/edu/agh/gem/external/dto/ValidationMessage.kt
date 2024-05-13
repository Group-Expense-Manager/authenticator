package pl.edu.agh.gem.external.dto

class ValidationMessage private constructor() {
    companion object {
        const val EMAIL_NOT_BLANK = "Email can not be blank"
        const val PASSWORD_NOT_BLANK = "Password can not be blank"
        const val WRONG_EMAIL_FORMAT = "Wrong email format"
        const val MIN_PASSWORD_LENGTH = "Minimum password length is 8"
        const val MAX_PASSWORD_LENGTH = "Maximum password length is 30"
        const val PASSWORD_LOWERCASE = "Password must contain at least one lowercase letter"
        const val PASSWORD_UPPERCASE = "Password must contain at least one uppercase letter"
        const val PASSWORD_DIGIT = "Password must contain at least one digit"
        const val PASSWORD_SPECIAL_CHARACTER = "Password must contain at least one special character among @#\$%^&+=!"
    }
}
