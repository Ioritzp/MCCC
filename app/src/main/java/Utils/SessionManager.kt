package Utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)

    companion object{
        private const val USER_ID_KEY = "USER_ID"
    }

    fun saveUserId(userId: Int){
        preferences.edit {
            putInt(USER_ID_KEY, userId)
        }
    }

    fun getUserId():Int?{
        return if(preferences.contains(USER_ID_KEY)) preferences.getInt(USER_ID_KEY, -1) else null
    }

    fun clearSession(){
        preferences.edit {
            clear()
        }
    }

}