package Utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val preferences = context.getSharedPreferences("AppSession", Context.MODE_PRIVATE)

    companion object{
        private const val USER_ID_KEY = "USER_ID"
    }

    fun saveUserId(userId: Int){
        val editor = preferences.edit()
        editor.putInt(USER_ID_KEY, userId)
        editor.apply()
    }

    fun getUserId():Int?{
        return if(preferences.contains(USER_ID_KEY)) preferences.getInt(USER_ID_KEY, -1) else null
    }

    fun clearSession(){
        val editor = preferences.edit()
        editor.clear()
        editor.apply()
    }

}