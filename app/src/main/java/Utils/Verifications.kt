package Utils

import org.mindrot.jbcrypt.BCrypt

class Verifications {

    companion object {
        fun checkPassword(plainTextPassword: String, hashedPassword: String): Boolean {
            return BCrypt.checkpw(plainTextPassword, hashedPassword)

        }
    }
}