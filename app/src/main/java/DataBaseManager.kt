import Classes.*
import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper


import android.content.Context
import android.database.Cursor
import android.content.ContentValues
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.database.sqlite.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime
import android.util.Log

class DataBaseManager(context: Context?): SQLiteAssetHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object DBConstants{
    //DBDetails
    const val DATABASE_NAME = "marvel_champions_preset.db"
    const val DATABASE_VERSION = 1

    //preset tables - prepopulated by the system, not filled by the user

        //preset heroe
        const val TABLE_PRESET_HEROES = "heroe_preset"
        const val COLUMN_PRESET_HERO_ID = "id_heroe"
        const val COLUMN_PRESET_HERO_NAME = "nombre"
        const val COLUMN_PRESET_HERO_INITIALLIFE ="vida_inicial"

        //preset campaña
        const val TABLE_PRESET_CAMPAIGNS = "campana_preset"
        const val COLUMN_PRESET_CAMPAIGN_ID ="id_preset_campana"
        const val COLUMN_PRESET_CAMPAIGN_NAME = "nombre"
        const val COLUMN_PRESET_CAMPAIGN_DESCRIPTION = "descripcion"

        //preset escenarios
        const val TABLE_PRESET_SCENARIOS = "escenario_preset"
        const val COLUMN_PRESET_SCENARIO_ID = "id_preset_escenario"
        const val COLUMN_PRESET_CAMPAIGN_ID_FK ="id_preset_campana_fk"
        const val COLUMN_PRESET_SCENARIO_NAME = "nombre"
        const val COLUMN_PRESET_VILLAIN_NAME = "nombre_villano"
        const val COLUMN_PRESET_SCENARIO_DESCRIPTION = "descripcion"

        //preset preguntas
        const val TABLE_PRESET_QUESTIONS = "preguntas_preset"
        const val COLUMN_PRESET_QUESTION_ID ="id_preset_pregunta"
        const val COLUMN_PRESET_SCENARIO_ID_FK ="id_preset_escenario_fk"
        const val COLUMN_PRESET_QUESTION_TEXT = "texto"
        const val COLUMN_PRESET_QUESTION_TYPE = "tipo_pregunta"

        //preset mejoras
        const val TABLE_PRESET_UPGRADES = "mejora_preset"
        const val COLUMN_PRESET_UPGRADE_ID ="id_preset_mejora"
        const val COLUMN_PRESET_CAMPAIGN_ID_FK_3 ="id_preset_campana_fk"
        const val COLUMN_PRESET_UPGRADE_NAME = "nombre"
        const val COLUMN_PRESET_UPGRADE_TYPE = "tipo"
        const val COLUMN_PRESET_UPGRADE_IS_DISPOSABLE = "consumible"
        const val COLUMN_PRESET_SCENARIO_ID_FK_3 = "id_preset_escenario_fk"

        //users table
        const val TABLE_USERS = "usuarios"
        const val COLUMN_USER_ID = "id_usuario"
        const val COLUMN_USER_NAME = "nombre"
        const val COLUMN_USER_MAIL = "email"
        const val COLUMN_USER_PASSWORD = "password"
        const val COLUMN_USER_PHONE = "telefono"

        //heroes table
        const val TABLE_INSTANCE_HEROES = "heroe_instancia"
        const val COLUMN_INSTANCE_HERO_ID = "id_instancia_heroe"
        const val COLUMN_INSTANCE_CAMPAIGN_ID_FK = "id_instancia_campana_fk"
        const val COLUMN_PRESET_HERO_ID_FK = "id_heroe_fk"
        const val COLUMN_INSTANCE_HERO_CREDITS = "creditos"
        const val COLUMN_INSTANCE_HERO_NAME = "nombre"
        const val COLUMN_INSTANCE_HERO_CURRENTLIFE = "vida"
        const val COLUMN_INSTANCE_HERO_MODIFICATIONDATE = "fecha_modificacion"
        const val COLUMN_INSTANCE_HERO_MODIFICATIONHOUR = "hora_modificacion"

        //campaign table
        const val TABLE_INSTANCE_CAMPAIGNS = "campana_instancia"
        const val COLUMN_INSTANCE_CAMPAIGN_ID ="id_instancia_campana"
        const val COLUMN_INSTANCE_USER_ID_FK ="id_usuario_fk"
        const val COLUMN_PRESET_CAMPAIGN_ID_FK_2 = "id_preset_campana_fk_2"
        const val COLUMN_INSTANCE_CAMPAIGN_STARTDATE = "fecha_inicio"
        const val COLUMN_INSTANCE_CAMPAIGN_ENDDATE = "fecha_fin"
        const val COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM = "num_jugadores"
        const val COLUMN_INSTANCE_CAMPAIGN_DIFFICULTY = "dificultad"

        //scenario table
        const val TABLE_INSTANCE_SCENARIOS = "escenario_instancia"
        const val COLUMN_INSTANCE_SCENARIO_ID ="id_instancia_escenario"
        const val COLUMN_INSTANCE_CAMPAIGN_ID_FK_2 ="id_instancia_campana_fk_2"
        const val COLUMN_PRESET_SCENARIO_ID_FK_2 = "id_preset_escenario_fk_2"
        const val COLUMN_INSTANCE_SCENARIO_STARTDATE = "fecha_inicio"
        const val COLUMN_INSTANCE_SCENARIO_ENDDATE = "fecha_fin"
        const val COLUMN_INSTANCE_SCENARIO_STATUS = "estado"

        //question table
        const val TABLE_INSTANCE_QUESTIONS = "pregunta_instancia"
        const val COLUMN_INSTANCE_QUESTION_ID ="id_instancia_pregunta"
        const val COLUMN_INSTANCE_SCENARIO_ID_FK ="id_instancia_escenario_fk"
        const val COLUMN_PRESET_QUESTION_ID_FK = "id_preset_pregunta_fk"
        const val COLUMN_INSTANCE_QUESTION_ANSWER = "respuesta"

        //upgrade table
        const val TABLE_INSTANCE_UPGRADES = "mejora_instancia"
        const val COLUMN_INSTANCE_UPGRADE_ID ="id_instancia_mejora"
        const val COLUMN_PRESET_UPGRADE_ID_FK ="id_preset_mejora_fk"
        const val COLUMN_INSTANCE_HERO_ID_FK = "id_instancia_heroe_fk"
        const val COLUMN_INSTANCE_CAMPAIGN_ID_FK_3 = "id_instancia_campana_fk"
        const val COLUMN_INSTANCE_UPGRADE_STATUS = "status"

}

    //Custom onUpgrade for keeping user data when updating the db (temporary - planning to move to 2 databases after the project)
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        //The override checks if the version has changed before doing anything
        if (newVersion > oldVersion){

            //fetch all the user data on the values
            val userCampaigns = fetchAllCampaignInstances(db)
            val userUsers = fetchAllUsers(db)
            val userHeroes = fetchAllHeroInstances(db)
            val userScenarios = fetchAllScenarioInstances(db)
            val userQuestions = fetchAllQuestionInstances(db)
            val userUpgrades = fetchAllUpgradesInstances(db)

            //temporarily put the new version to the old version to prevet default upgrade (deleteall).
            setForcedUpgrade(oldVersion)
            //upgrade happens
            super.onUpgrade(db, oldVersion, newVersion)
            setForcedUpgrade(newVersion)

            //reinsert data.
            reinsertUserInstances(db, userUsers)
            reinsertCampaignInstances(db, userCampaigns)
            reinsertHeroInstances(db, userHeroes)
            reinsertScenarioInstances(db, userScenarios)
            reinsertQuestionInstances(db, userQuestions)
            reinsertUpgradeInstances(db, userUpgrades)

        }


    }

    //fetch methods for custom onUpgrade

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("Range")
    private fun fetchAllUsers(db: SQLiteDatabase): List<ContentValues>{
        val list = mutableListOf<ContentValues>()
        val cursor = db.query(TABLE_USERS, null, null, null, null, null, null)
        cursor.use{
            if(it.moveToFirst()){
                do{
                    val values = ContentValues()
                    values.put(COLUMN_USER_ID, it.getInt(it.getColumnIndex(COLUMN_USER_ID)))
                    values.put(COLUMN_USER_NAME, it.getInt(it.getColumnIndex(COLUMN_USER_NAME)))
                    values.put(COLUMN_USER_MAIL, it.getInt(it.getColumnIndex(COLUMN_USER_MAIL)))
                    values.put(COLUMN_USER_PASSWORD, it.getInt(it.getColumnIndex(COLUMN_USER_PASSWORD)))
                    values.put(COLUMN_USER_PHONE, it.getInt(it.getColumnIndex(COLUMN_USER_PHONE)))
                    list.add(values)
                }while(it.moveToNext())
            }
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("Range")
    private fun fetchAllCampaignInstances(db: SQLiteDatabase): List<ContentValues>{
        val list = mutableListOf<ContentValues>()
        val cursor = db.query(TABLE_INSTANCE_CAMPAIGNS, null, null, null, null, null, null)
        cursor.use{
            if(it.moveToFirst()){
                do{
                    val values = ContentValues()
                    values.put(COLUMN_INSTANCE_CAMPAIGN_ID, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID)))
                    values.put(COLUMN_INSTANCE_USER_ID_FK, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_USER_ID_FK)))
                    values.put(COLUMN_PRESET_CAMPAIGN_ID_FK_2, it.getInt(it.getColumnIndex(COLUMN_PRESET_CAMPAIGN_ID_FK_2)))
                    values.put(COLUMN_INSTANCE_CAMPAIGN_STARTDATE, it.getString(it.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_STARTDATE)))
                    values.put(COLUMN_INSTANCE_CAMPAIGN_ENDDATE, it.getString(it.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ENDDATE)))
                    values.put(COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM)))
                    list.add(values)
                }while(it.moveToNext())
            }
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("Range")
    private fun fetchAllHeroInstances(db: SQLiteDatabase): List<ContentValues>{
        val list = mutableListOf<ContentValues>()
        val cursor = db.query(TABLE_INSTANCE_HEROES, null, null, null, null, null, null)
        cursor.use{
            if(it.moveToFirst()){
                do{
                    val values = ContentValues()
                    values.put(COLUMN_INSTANCE_HERO_ID, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_HERO_ID)))
                    values.put(COLUMN_INSTANCE_CAMPAIGN_ID_FK, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID_FK)))
                    values.put(COLUMN_PRESET_HERO_ID_FK, it.getInt(it.getColumnIndex(COLUMN_PRESET_HERO_ID_FK)))
                    values.put(COLUMN_INSTANCE_HERO_NAME, it.getString(it.getColumnIndex(COLUMN_INSTANCE_HERO_NAME)))
                    values.put(COLUMN_INSTANCE_HERO_CURRENTLIFE, it.getString(it.getColumnIndex(COLUMN_INSTANCE_HERO_CURRENTLIFE)))
                    values.put(COLUMN_INSTANCE_HERO_CREDITS, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_HERO_CREDITS)))
                    values.put(COLUMN_INSTANCE_HERO_MODIFICATIONDATE, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_HERO_MODIFICATIONDATE)))
                    values.put(COLUMN_INSTANCE_HERO_MODIFICATIONHOUR, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_HERO_MODIFICATIONHOUR)))
                    list.add(values)
                }while(it.moveToNext())
            }
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("Range")
    private fun fetchAllScenarioInstances(db: SQLiteDatabase): List<ContentValues>{
        val list = mutableListOf<ContentValues>()
        val cursor = db.query(TABLE_INSTANCE_SCENARIOS, null, null, null, null, null, null)
        cursor.use{
            if(it.moveToFirst()){
                do{
                    val values = ContentValues()
                    values.put(COLUMN_INSTANCE_SCENARIO_ID, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ID)))
                    values.put(COLUMN_INSTANCE_CAMPAIGN_ID_FK_2, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ID)))
                    values.put(COLUMN_PRESET_SCENARIO_ID_FK_2, it.getInt(it.getColumnIndex(COLUMN_PRESET_SCENARIO_ID_FK_2)))
                    values.put(COLUMN_INSTANCE_SCENARIO_STARTDATE, it.getString(it.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STARTDATE)))
                    values.put(COLUMN_INSTANCE_SCENARIO_ENDDATE, it.getString(it.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ENDDATE)))
                    values.put(COLUMN_INSTANCE_SCENARIO_STATUS, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STATUS)))
                    list.add(values)
                }while(it.moveToNext())
            }
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("Range")
    private fun fetchAllQuestionInstances(db: SQLiteDatabase): List<ContentValues>{
        val list = mutableListOf<ContentValues>()
        val cursor = db.query(TABLE_INSTANCE_QUESTIONS, null, null, null, null, null, null)
        cursor.use{
            if(it.moveToFirst()){
                do{
                    val values = ContentValues()
                    values.put(COLUMN_INSTANCE_QUESTION_ID, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_QUESTION_ID)))
                    values.put(COLUMN_PRESET_QUESTION_ID_FK, it.getInt(it.getColumnIndex(COLUMN_PRESET_QUESTION_ID_FK)))
                    values.put(COLUMN_INSTANCE_SCENARIO_ID, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ID)))
                    values.put(COLUMN_INSTANCE_QUESTION_ANSWER, it.getString(it.getColumnIndex(COLUMN_INSTANCE_QUESTION_ANSWER)))
                    list.add(values)
                }while(it.moveToNext())
            }
        }
        return list
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("Range")
    private fun fetchAllUpgradesInstances(db: SQLiteDatabase): List<ContentValues>{
        val list = mutableListOf<ContentValues>()
        val cursor = db.query(TABLE_INSTANCE_UPGRADES, null, null, null, null, null, null)
        cursor.use{
            if(it.moveToFirst()){
                do{
                    val values = ContentValues()
                    values.put(COLUMN_INSTANCE_UPGRADE_ID, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_UPGRADE_ID)))
                    values.put(COLUMN_INSTANCE_HERO_ID_FK, it.getInt(it.getColumnIndex(COLUMN_INSTANCE_HERO_ID_FK)))
                    values.put(COLUMN_PRESET_UPGRADE_ID_FK, it.getInt(it.getColumnIndex(COLUMN_PRESET_UPGRADE_ID_FK)))

                    list.add(values)
                }while(it.moveToNext())
            }
        }
        return list
    }

    //REinsert the data - methods

    private fun reinsertUserInstances(db: SQLiteDatabase, list: List<ContentValues>){
        db.beginTransaction()
        try {
            list.forEach { values ->
                //remmove the PK so the db auto-generates a new one.
                values.remove(COLUMN_USER_ID)
                db.insert(TABLE_USERS, null, values)

            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    private fun reinsertCampaignInstances(db: SQLiteDatabase, list: List<ContentValues>){
        db.beginTransaction()
        try {
            list.forEach { values ->
                //remmove the PK so the db auto-generates a new one.
                values.remove(COLUMN_INSTANCE_CAMPAIGN_ID)
                db.insert(TABLE_INSTANCE_CAMPAIGNS, null, values)

            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    private fun reinsertHeroInstances(db: SQLiteDatabase, list: List<ContentValues>){
        db.beginTransaction()
        try {
            list.forEach { values ->
                //remmove the PK so the db auto-generates a new one.
                values.remove(COLUMN_INSTANCE_HERO_ID)
                db.insert(TABLE_INSTANCE_HEROES, null, values)

            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    private fun reinsertScenarioInstances(db: SQLiteDatabase, list: List<ContentValues>){
        db.beginTransaction()
        try {
            list.forEach { values ->
                //remmove the PK so the db auto-generates a new one.
                values.remove(COLUMN_INSTANCE_SCENARIO_ID)
                db.insert(TABLE_INSTANCE_SCENARIOS, null, values)

            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    private fun reinsertQuestionInstances(db: SQLiteDatabase, list: List<ContentValues>){
        db.beginTransaction()
        try {
            list.forEach { values ->
                //remmove the PK so the db auto-generates a new one.
                values.remove(COLUMN_INSTANCE_QUESTION_ID)
                db.insert(TABLE_INSTANCE_QUESTIONS, null, values)

            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    private fun reinsertUpgradeInstances(db: SQLiteDatabase, list: List<ContentValues>){
        db.beginTransaction()
        try {
            list.forEach { values ->
                //remmove the PK so the db auto-generates a new one.
                values.remove(COLUMN_INSTANCE_UPGRADE_ID)
                db.insert(TABLE_INSTANCE_UPGRADES, null, values)

            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    //end of onUpgrade methods


    //REGION add the preset info

    @SuppressLint("Range")
    fun getAllPresetHeroes(): List<Hero> {
        val heroList = mutableListOf<Hero>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_PRESET_HEROES, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_HERO_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_HERO_NAME))
                    val initialLife = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_HERO_INITIALLIFE))
                    heroList.add(Hero(id, name, initialLife, initialLife))
                } while (cursor.moveToNext())
        }
    } catch (e: Exception){
        e.printStackTrace()
    } finally {
        cursor?.close()
    }

        return heroList

    }

    @SuppressLint("Range")
    fun getAllPresetCampaigns(): List<Campaign> {
        val campaignList = mutableListOf<Campaign>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_PRESET_CAMPAIGNS, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_NAME))
                    val description = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_DESCRIPTION))
                    val scenariosForCampaign = getScenariosForCampaign(db, id)
                    val campaign = Campaign(id, name, description, scenariosForCampaign)
                    campaignList.add(campaign)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return campaignList

    }



    @SuppressLint("Range")
    fun getAllPresetScenarios(): List<Scenario> {
        val scenarioList = mutableListOf<Scenario>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_PRESET_SCENARIOS, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_NAME))
                    val villainName = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_VILLAIN_NAME))
                    val description = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_DESCRIPTION))
                    val questionsForScenario = getQuestionsForScenario(db, id)
                    val scenario = Scenario(id, name, villainName, description, questionsForScenario)
                    scenarioList.add(scenario)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return scenarioList
    }

    @SuppressLint("Range")
    fun getAllPresetQuestions(): List<MarvelQuestion> {
        val questionList = mutableListOf<MarvelQuestion>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_PRESET_QUESTIONS, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_ID))
                    val text = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_TEXT))
                    val type = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_TYPE))
                    val question = MarvelQuestion(id, text, " ", type)
                    questionList.add(question)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return questionList

    }


    @SuppressLint("Range")
    fun getAllPresetUpgrades(): List<Upgrade> {
        val upgradeList = mutableListOf<Upgrade>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_PRESET_UPGRADES, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_NAME))
                    val type = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_TYPE))
                    val isDisposable = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_IS_DISPOSABLE))
                    val upgrade = Upgrade(id, name, type, isDisposable)
                    upgradeList.add(upgrade)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return upgradeList

    }


    //REGION Helpers

    @SuppressLint("Range")
    private fun getScenariosForCampaign(db: SQLiteDatabase, campaignId: Int): List<Scenario>{
        val scenarioList = mutableListOf<Scenario>()
        var cursor: Cursor? = null

        try {
            // Define the 'where' clause to filter by campaign ID
            val selection = "$COLUMN_PRESET_CAMPAIGN_ID_FK = ?"
            val selectionArgs = arrayOf(campaignId.toString())

            cursor = db.query(
                TABLE_PRESET_SCENARIOS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_NAME))
                    val villainName =
                        cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_VILLAIN_NAME))
                    val description =
                        cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_DESCRIPTION))

                    val questionsForThisScenario = getQuestionsForScenario(db, id)

                    val scenario = Scenario(id, name, villainName, description, questionsForThisScenario)
                    scenarioList.add(scenario)

                } while (cursor.moveToNext())
            }

        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return scenarioList
    }

    @SuppressLint("Range")
    private fun getQuestionsForScenario(db: SQLiteDatabase, scenarioId: Int): List<MarvelQuestion>{
        val questionList = mutableListOf<MarvelQuestion>()
        var cursor: Cursor? = null

        try {
            // Define the 'where' clause to filter by campaign ID
            val selection = "$COLUMN_PRESET_SCENARIO_ID_FK = ?"
            val selectionArgs = arrayOf(scenarioId.toString())

            cursor = db.query(
                TABLE_PRESET_QUESTIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_ID))
                    val text = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_TEXT))
                    val type = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_TYPE))

                    val question = MarvelQuestion(id, text, " ", type)
                    questionList.add(question)
                } while (cursor.moveToNext())
            }

        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return questionList
    }


    //Supra region: CRUD methods for user data.


    //region: User table interactions
    //Insert new user to the user table - check verifications class for the password check
    @SuppressLint("Range")
     fun addUser(user: User){
         val db = this.writableDatabase
        db.transaction {
                //generate a salt and hashed password for security
                val hashedPassword = BCrypt.hashpw(user.password, BCrypt.gensalt())

                val values = ContentValues().apply {
                    put(COLUMN_USER_NAME, user.name)
                    put(COLUMN_USER_MAIL, user.email)
                    put(COLUMN_USER_PHONE, user.phone)
                    put(COLUMN_USER_PASSWORD, hashedPassword)
                }
                //insert new row. insert method returns row ID of the new row, or -1 if failed
                db.insert(TABLE_USERS, null, values)
                //transaction successful

        }
    }

    //read the user table

    @SuppressLint("Range")
    fun readUsers(): List<User> {
        val userList = mutableListOf<User>()
        val db = this.readableDatabase

        var cursor: Cursor? = null
        try {
            cursor = db.query(TABLE_USERS, null, null, null, null, null, null)
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_USER_NAME))
                    val email = cursor.getString(cursor.getColumnIndex(COLUMN_USER_MAIL))
                    val phone = cursor.getInt(cursor.getColumnIndex(COLUMN_USER_PHONE))
                    val password = cursor.getString(cursor.getColumnIndex(COLUMN_USER_PASSWORD))
                    val user = User(id, name, email, password, phone)
                    userList.add(user)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return userList
    }


    //region instanced campaigns

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun addCampaign(campaign: InstanceCampaign): Long{
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_PRESET_CAMPAIGN_ID_FK_2, campaign.presetCampaignId)
                put(COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM, campaign.playerNum)
                put(COLUMN_INSTANCE_CAMPAIGN_STARTDATE, campaign.startDate.toLocalDate().toString())
                put(COLUMN_INSTANCE_CAMPAIGN_ENDDATE, campaign.endDate?.toLocalDate().toString())
                put(COLUMN_INSTANCE_USER_ID_FK, campaign.userId)
                put(COLUMN_INSTANCE_CAMPAIGN_DIFFICULTY, campaign.difficulty)

            }

            val newCampaignId = db.insert(TABLE_INSTANCE_CAMPAIGNS, null, values)


        return newCampaignId

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun readCampaigns(userId: Int): List<InstanceCampaign> {
        val campaignList = mutableListOf<InstanceCampaign>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {

            //SQL query for name,
            val query = """
            SELECT
                ic.*,
                pc.$COLUMN_PRESET_CAMPAIGN_NAME,
                pc.$COLUMN_PRESET_CAMPAIGN_DESCRIPTION
           FROM
                $TABLE_INSTANCE_CAMPAIGNS ic
            JOIN
                $TABLE_PRESET_CAMPAIGNS pc ON ic.$COLUMN_PRESET_CAMPAIGN_ID_FK_2 = pc.$COLUMN_PRESET_CAMPAIGN_ID
            WHERE
                ic.$COLUMN_INSTANCE_USER_ID_FK = ?
    """


            cursor = db.rawQuery(query, arrayOf(userId.toString()))
            if (cursor.moveToFirst()) {
                do {
                    //get data from preset
                    val name =
                        cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_NAME))
                    val description = cursor.getString(
                        cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_DESCRIPTION)
                    )

                    // Get data from instance
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID))
                    val presetCampaignId =
                        cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_ID_FK_2))
                    val playerNum =
                        cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM))
                    val startDateStr = cursor.getString(
                        cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_STARTDATE)
                    )
                    val endDateStr =
                        cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ENDDATE))
                    val userId =
                        cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_USER_ID_FK))
                    val difficulty = cursor.getString(
                        cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_DIFFICULTY)
                    )

                    val endDate = if(endDateStr != null && endDateStr != "null"){
                        LocalDateTime.parse(endDateStr + "T00:00:00")
                    } else{
                        null
                    }

                    val campaign = InstanceCampaign(
                        id = id,
                        presetCampaignId = presetCampaignId,
                        name = name,
                        description = description,
                        scenarioList = readScenariosForCampaign(id),
                        userId = userId,
                        userName = " ",
                        playerNum = playerNum,
                        startDate = LocalDateTime.parse(startDateStr + "T00:00:00"),
                        endDate = endDate,
                        difficulty = difficulty
                    )

                    campaignList.add(campaign)
                } while (cursor.moveToNext())
            }
        }catch (e: Exception){
            e.printStackTrace()
        }finally {

            cursor?.close()

        }
        return campaignList

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun readScenariosForCampaign(campaignId: Int): List<InstanceScenario> {
        val scenarioList = mutableListOf<InstanceScenario>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        // The query is the same as readScenarios, but with a WHERE clause
        val query = """
        SELECT
            ic.*,
            pc.$COLUMN_PRESET_SCENARIO_NAME,
            pc.$COLUMN_PRESET_SCENARIO_DESCRIPTION,
            pc.$COLUMN_PRESET_VILLAIN_NAME
        FROM
            $TABLE_INSTANCE_SCENARIOS ic
        JOIN
            $TABLE_PRESET_SCENARIOS pc ON ic.$COLUMN_PRESET_SCENARIO_ID_FK_2 = pc.$COLUMN_PRESET_SCENARIO_ID
        WHERE
            ic.$COLUMN_INSTANCE_CAMPAIGN_ID_FK_2 = ?
    """

        try {
            Log.d("DB_DEBUG", "entering readscenarios")
            // The '?' in the query is replaced by the campaignId here.
            // This is the safe way to prevent SQL injection.
            cursor = db.rawQuery(query, arrayOf(campaignId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    // (The logic inside the loop is identical to your readScenarios function)
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_NAME))
                    val description = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_DESCRIPTION))
                    val villainName = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_VILLAIN_NAME))

                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ID))
                    val presetScenarioId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_ID_FK_2))
                    Log.d("DB_DEBUG", "getting data from readscenarios")
                    Log.d("DB_DEBUG", "getting data from a")
                    val status = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STATUS))
                    Log.d("DB_DEBUG", "getting $status")
                    val startDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STARTDATE))
                    val endDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ENDDATE))
                    Log.d("DB_DEBUG", "getting $endDateStr")

                    val endDate = if (endDateStr != null && endDateStr != "null") {
                        LocalDateTime.parse(endDateStr + "T00:00:00")
                    } else {
                        null
                    }
                    Log.d("DB_DEBUG", "readed readscenarios, showing dates: $startDateStr, $endDateStr")
                    val scenario = InstanceScenario(
                        id = id,
                        instanceCampaignId = campaignId,
                        presetScenarioId = presetScenarioId,
                        name = name,
                        description = description,
                        questionList = readQuestionsForScenario(id), // This still calls the next level down
                        villainName = villainName,
                        startDate = LocalDateTime.parse(startDateStr + "T00:00:00"),
                        endDate = endDate,
                        status = status
                    )
                    scenarioList.add(scenario)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return scenarioList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateCampaign(campaign: InstanceCampaign) {
        val db = this.writableDatabase
        val values = ContentValues().apply {

            put(COLUMN_INSTANCE_CAMPAIGN_ENDDATE, campaign.endDate?.toLocalDate().toString())
            Log.d("LOCALDATECHECK", "local date: ${campaign.endDate?.toLocalDate().toString()}")
        }
        // Update the row in the database where the scenario ID matches
        db.update(
            TABLE_INSTANCE_CAMPAIGNS,
            values, "$COLUMN_INSTANCE_CAMPAIGN_ID = ?",
            arrayOf(campaign.id.toString()))

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun getCampaignById(campaignId: Int): InstanceCampaign? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var campaign: InstanceCampaign? = null

        // Query to join the instance table with the preset table
        val query = """
        SELECT
            ic.*,
            pc.$COLUMN_PRESET_CAMPAIGN_NAME,
            pc.$COLUMN_PRESET_CAMPAIGN_DESCRIPTION
        FROM
            $TABLE_INSTANCE_CAMPAIGNS ic
        JOIN
            $TABLE_PRESET_CAMPAIGNS pc ON ic.$COLUMN_PRESET_CAMPAIGN_ID_FK_2 = pc.$COLUMN_PRESET_CAMPAIGN_ID
        WHERE
            ic.$COLUMN_INSTANCE_CAMPAIGN_ID = ?
    """

        try {
            cursor = db.rawQuery(query, arrayOf(campaignId.toString()))

            if (cursor.moveToFirst()) {
                // Get all the real data from the cursor
                val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID))
                val presetCampaignId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_ID_FK_2))
                val playerNum = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM))
                Log.d("CAMPAIGNGETDEBUG", "PLAYERS: $playerNum")
                val startDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_STARTDATE))
                Log.d("CAMPAIGNGETDEBUG", "startdat: $startDateStr")
                val endDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ENDDATE))
                val userId = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_USER_ID_FK))
                val difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_DIFFICULTY))
                Log.d("CAMPAIGNGETDEBUG", "$endDateStr")
                // Get the name and description from the JOINED preset table
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_NAME))
                val description = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_DESCRIPTION))

                val endDate = if (endDateStr != null && endDateStr != "null") {
                    LocalDateTime.parse(endDateStr + "T00:00:00")
                } else{
                    null
                }
                Log.d("CAMPAIGNGETDEBUG", "$name, $endDate")
                // Reconstruct the full InstanceCampaign object with real data
                campaign = InstanceCampaign(
                    id = id,
                    presetCampaignId = presetCampaignId,
                    name = name,
                    description = description,
                    playerNum = playerNum,
                    startDate = LocalDateTime.parse(startDateStr + "T00:00:00"),
                    endDate = endDate,
                    userId = userId.toInt(),
                    difficulty = difficulty,
                    // These are fetched separately to avoid circular dependencies
                    scenarioList = arrayListOf(),
                    userName = "" // This can be fetched with another JOIN if needed
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return campaign
    }

    @SuppressLint("Range")
    fun getPresetCampaignIdByInstanceId(instanceCampaignId: Int): Int{
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var presetCampaignId = -1 //Preset value just in case

        val query = "SELECT $COLUMN_PRESET_CAMPAIGN_ID_FK_2 FROM $TABLE_INSTANCE_CAMPAIGNS WHERE $COLUMN_INSTANCE_CAMPAIGN_ID = ?"

        try {
            cursor = db.rawQuery(query, arrayOf(instanceCampaignId.toString()))
            if(cursor.moveToFirst()){
                presetCampaignId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_ID_FK_2))
            }
        } catch (err: Exception){
            Log.e("DB_ERROR", "Error getting campaign id")
            err.printStackTrace()
        } finally {
            cursor?.close()
        }

        return presetCampaignId

    }

    //region instanced heroes
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun addHero(hero: InstanceHero){
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_INSTANCE_CAMPAIGN_ID_FK, hero.instanceCampaignId)
                put(COLUMN_PRESET_HERO_ID_FK, hero.presetHeroId)
                put(COLUMN_INSTANCE_HERO_NAME, hero.name)
                put(COLUMN_INSTANCE_HERO_CREDITS, hero.credits)
                put(COLUMN_INSTANCE_HERO_MODIFICATIONDATE, hero.modDate.toLocalDate().toString())
                put(COLUMN_INSTANCE_HERO_MODIFICATIONHOUR, hero.modDate.toLocalTime().toString())
                put(COLUMN_INSTANCE_HERO_CURRENTLIFE, hero.currentLife)
            }

            db.insert(TABLE_INSTANCE_HEROES, null, values)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun getHeroesByCampaignId(campaignId: Int): List<InstanceHero>{
        Log.d("DB_DEBUG", "Fetching heroes for campaignId: $campaignId")
        val db = this.readableDatabase
        var cursor: Cursor? = null
        val heroList = mutableListOf<InstanceHero>()

        // Query to join the instance table with the preset table
        val query = """
        SELECT * FROM $TABLE_INSTANCE_HEROES
        WHERE $COLUMN_INSTANCE_CAMPAIGN_ID_FK = ?
    """

        try {
            cursor = db.rawQuery(query, arrayOf(campaignId.toString()))
            Log.d("DB_DEBUG", "Hero cursor found ${cursor.count} rows.")

            if (cursor.moveToFirst()) {
                do {
                    // Get all the real data from the cursor
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_HERO_ID))
                    val presetHeroId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_HERO_ID_FK))
                    val credits = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_HERO_CREDITS))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_HERO_NAME))
                    val currentLife = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_HERO_CURRENTLIFE))
                    val modData = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_HERO_MODIFICATIONDATE))
                    val modTime = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_HERO_MODIFICATIONHOUR))
                    val heroUpgrades = getUpgradesForHero(id)


                    val hero = InstanceHero(
                        id = id,
                        presetHeroId = presetHeroId,
                        instanceCampaignId = campaignId,
                        credits = credits,
                        name = name,
                        currentLife = currentLife,
                        upgrades = heroUpgrades,
                        modDate = LocalDateTime.parse("${modData}T${modTime}")
                    )
                    heroList.add(hero)
                }while(cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("DB_CRITICAL_ERROR", "Failed to parse heroes for campaignId: $campaignId. Exception: ${e.message}")
            Log.e("HERO_FETCH_FAILURE", "failed to parse heroes for campId: $campaignId. Exception: ${e.javaClass.simpleName} -> ${e.message}")
            e.printStackTrace() // Keep this to see the full stack trace
        } finally {
            cursor?.close()
        }
        return heroList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateHero(hero: InstanceHero) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INSTANCE_CAMPAIGN_ID_FK, hero.instanceCampaignId)
            put(COLUMN_PRESET_HERO_ID_FK, hero.presetHeroId)
            put(COLUMN_INSTANCE_HERO_NAME, hero.name)
            put(COLUMN_INSTANCE_HERO_CREDITS, hero.credits)
            put(COLUMN_INSTANCE_HERO_MODIFICATIONDATE, hero.modDate.toLocalDate().toString())
            put(COLUMN_INSTANCE_HERO_MODIFICATIONHOUR, hero.modDate.toLocalTime().toString())
            put(COLUMN_INSTANCE_HERO_CURRENTLIFE, hero.currentLife)
        }
        // Update the row in the database where the scenario ID matches
        db.update(
            TABLE_INSTANCE_HEROES,
            values, "$COLUMN_INSTANCE_HERO_ID = ?",
            arrayOf(hero.id.toString()))

    }

    //region instanced scenarios
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun addScenario(scenario: InstanceScenario): Long{
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_INSTANCE_CAMPAIGN_ID_FK_2, scenario.instanceCampaignId)
                put(COLUMN_PRESET_SCENARIO_ID_FK_2, scenario.presetScenarioId)
                put(COLUMN_INSTANCE_SCENARIO_STARTDATE, scenario.startDate?.toLocalDate().toString())
                Log.d("DEBUG_SCENARIO_ON_CAMPAIGN_CREATION","In dbmanager: "+ scenario.startDate?.toLocalDate().toString())
                put(COLUMN_INSTANCE_SCENARIO_ENDDATE, scenario.endDate?.toLocalDate().toString())
                put(COLUMN_INSTANCE_SCENARIO_STATUS, scenario.status)
            }

            val newScenarioId = db.insert(TABLE_INSTANCE_SCENARIOS, null, values)

        return newScenarioId
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun readQuestionsForScenario(scenarioId: Int): List<InstanceMarvelQuestion> {
        val questionList = mutableListOf<InstanceMarvelQuestion>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var counter = 0

        // The query is the same as readQuestions, but with a WHERE clause
        val query = """
        SELECT
            iq.$COLUMN_INSTANCE_QUESTION_ID,
            iq.$COLUMN_INSTANCE_SCENARIO_ID_FK,
            iq.$COLUMN_PRESET_QUESTION_ID_FK,
            iq.$COLUMN_INSTANCE_QUESTION_ANSWER,
            pq.$COLUMN_PRESET_QUESTION_TEXT,
            pq.$COLUMN_PRESET_QUESTION_TYPE
        FROM
            $TABLE_INSTANCE_QUESTIONS iq
        JOIN
            $TABLE_PRESET_QUESTIONS pq ON iq.$COLUMN_PRESET_QUESTION_ID_FK = pq.$COLUMN_PRESET_QUESTION_ID
        WHERE
            iq.$COLUMN_INSTANCE_SCENARIO_ID_FK = ?
    """
        try {
            // The '?' in the query is replaced by the scenarioId.
            // This is the safe way to prevent SQL injection attacks.
            cursor = db.rawQuery(query, arrayOf(scenarioId.toString()))

            if (cursor.moveToFirst()) {
                do {
                    Log.d("DEBUG_QUESTIONS", "question updated: $counter times")
                    counter++
                    // Get data from the INSTANCE table columns
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_QUESTION_ID))
                    val presetQuestionId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_ID_FK))
                    val answer = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_QUESTION_ANSWER))

                    // Get data from the PRESET table columns via the JOIN
                    val text = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_TEXT))
                    val typeStr = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_TYPE))

                    // Build the complete object
                    val instancedQuestion = InstanceMarvelQuestion(
                        id = id,
                        instanceScenarioId = scenarioId, // We already have this from the function parameter
                        presetQuestionId = presetQuestionId,
                        text = text, // Now we have the text
                        answer = answer,
                        questionType = QuestionType.valueOf(typeStr) // And now we have the type
                    )
                    questionList.add(instancedQuestion)
                    Log.d("DEBUG_QUESTIONS", "$instancedQuestion")
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("DB_QUESTION_FAIL", "Failed to read questions for scenario $scenarioId. Error: ${e.message}", e)
        } finally {
            cursor?.close()
        }
        Log.d("DB_QUESTION_FETCH", "Found ${questionList.size} questions for scenario $scenarioId")
        return questionList
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    fun getScenarioById(scenarioId: Int): InstanceScenario? {
        val db = this.readableDatabase
        var cursor: Cursor? = null
        var scenario: InstanceScenario? = null

        val query = """
            SELECT
                ic.*,
                pc.$COLUMN_PRESET_SCENARIO_NAME,
                pc.$COLUMN_PRESET_SCENARIO_DESCRIPTION,
                pc.$COLUMN_PRESET_VILLAIN_NAME
            FROM
                $TABLE_INSTANCE_SCENARIOS ic
            JOIN
                $TABLE_PRESET_SCENARIOS pc ON ic.$COLUMN_PRESET_SCENARIO_ID_FK_2 = pc.${COLUMN_PRESET_SCENARIO_ID}
            WHERE
                ic.$COLUMN_INSTANCE_SCENARIO_ID = ?
        """
        try{
            cursor = db.rawQuery(query, arrayOf(scenarioId.toString()))
            if (cursor.moveToFirst()) {
                // Get data from the preset table join
                val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_NAME))
                val description =
                    cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_DESCRIPTION))
                val villainName =
                    cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_VILLAIN_NAME))

                // Get data from the instance table
                val instanceCampaignId =
                    cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID_FK_2))
                val presetScenarioId =
                    cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_ID_FK_2))
                val startDateStr =
                    cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STARTDATE))
                val endDateStr =
                    cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ENDDATE))
                val status =
                    cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STATUS))

                // Fetch the associated questions for this specific scenario
                Log.d("GET_SCENARIO_DEBUG", "Successfully parsed scenario $scenarioId. Now fetching questions...")
                val questions = readQuestionsForScenario(scenarioId)
                Log.d("GET_SCENARIO_DEBUG", "Successfully fetched ${questions.size} questions for scenario $scenarioId.")
                Log.d("GET_SCENARIO_DEBUG", "Questions: $questions")

                val endDate = if (endDateStr != null && endDateStr != "null") {
                    LocalDateTime.parse(endDateStr + "T00:00:00")
                } else {
                    null
                }


                // Build the complete scenario object
                scenario = InstanceScenario(
                    id = scenarioId,
                    instanceCampaignId = instanceCampaignId,
                    presetScenarioId = presetScenarioId,
                    name = name,
                    description = description,
                    questionList = ArrayList(questions), // Use the correctly fetched questions
                    villainName = villainName,
                    startDate = LocalDateTime.parse("${startDateStr}T00:00:00"),
                    endDate = endDate,
                    status = status
                )
            }
        } catch (error: Exception){
            Log.e("DB_ERROR", "Failed to read scenario by id: $scenarioId")
            error.printStackTrace()
        } finally {
            cursor?.close()
        }
       return scenario
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateScenarioStatus(scenarioId: Int, newStatus: String, setEndDate: Boolean) {
        val db = this.writableDatabase
        val endDate = LocalDateTime.now()
        val values = ContentValues().apply {
            put(COLUMN_INSTANCE_SCENARIO_STATUS, newStatus)
            if(setEndDate) {
                put(COLUMN_INSTANCE_SCENARIO_ENDDATE, LocalDateTime.now().toLocalDate().toString())
                Log.d("DEBUG_SCENARIO_ON_CAMPAIGN_CREATION","finished scenario date:"+ LocalDateTime.now().toString())
            }
        }
        // Update the row where the scenario ID matchesLog.d("End date check", "end date: ${campaign.id} and ${campaign.endDate}")
        db.update(
            TABLE_INSTANCE_SCENARIOS,
            values,
            "$COLUMN_INSTANCE_SCENARIO_ID = ?",
            arrayOf(scenarioId.toString())
        )

    }

    //region instanced questions

    @SuppressLint("Range")
    fun addQuestion(question: InstanceMarvelQuestion): Long{
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_PRESET_QUESTION_ID_FK, question.presetQuestionId)
                put(COLUMN_INSTANCE_SCENARIO_ID_FK, question.instanceScenarioId)
                put(COLUMN_INSTANCE_QUESTION_ANSWER, question.answer)
            }

            return db.insert(TABLE_INSTANCE_QUESTIONS, null, values)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateQuestion(question: InstanceMarvelQuestion) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRESET_QUESTION_ID_FK, question.presetQuestionId)
            put(COLUMN_INSTANCE_SCENARIO_ID_FK, question.instanceScenarioId)
            put(COLUMN_INSTANCE_QUESTION_ANSWER, question.answer)
        }
        // Update the row in the database where the scenario ID matches
        db.update(
            TABLE_INSTANCE_QUESTIONS,
            values, "$COLUMN_INSTANCE_QUESTION_ID = ?",
            arrayOf(question.id.toString()))

    }

    //region instanced upgrades


    @SuppressLint("Range")
    fun getUpgradesForHero(heroId: Int): List<InstanceUpgrade> {
        val assignedUpgrades = mutableListOf<InstanceUpgrade>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        // This query joins the instance upgrades table with the preset upgrades table
        // to get the full details of each upgrade assigned to the specified hero.
        val query = """
            SELECT
                iu.$COLUMN_INSTANCE_UPGRADE_ID,
                iu.$COLUMN_INSTANCE_CAMPAIGN_ID_FK,
                iu.$COLUMN_PRESET_UPGRADE_ID_FK,
                iu.$COLUMN_INSTANCE_HERO_ID_FK,
                pu.$COLUMN_PRESET_UPGRADE_NAME,
                pu.$COLUMN_PRESET_UPGRADE_IS_DISPOSABLE
            FROM
                $TABLE_INSTANCE_UPGRADES iu
            JOIN
                $TABLE_PRESET_UPGRADES pu ON iu.$COLUMN_PRESET_UPGRADE_ID_FK = pu.$COLUMN_PRESET_UPGRADE_ID
            WHERE
                iu.$COLUMN_INSTANCE_HERO_ID_FK = ?
        """

        try {
            cursor = db.rawQuery(query, arrayOf(heroId.toString()))
            if (cursor.moveToFirst()) {
                do {
                    val instanceUpgrade = InstanceUpgrade(
                        id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_UPGRADE_ID)),
                        instanceCampaignId = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID_FK)),
                        presetUpgradeId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_ID_FK)),
                        instanceHeroId = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_HERO_ID_FK)),
                        name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_NAME)),
                        isDisposable = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_IS_DISPOSABLE)) == 1
                    )
                    assignedUpgrades.add(instanceUpgrade)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("DB_ERROR", "Failed to get upgrades for heroId: $heroId", e)
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return assignedUpgrades
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    @SuppressLint("Range")
    fun getAllUsersForVerification(): List<User>{
        val db = this.readableDatabase
        val userList = mutableListOf<User>()
        val cursor = db.query(TABLE_USERS, null, null, null, null, null, null)
        cursor.use{
            if(it.moveToFirst()){
                do{
                    val id = it.getInt(it.getColumnIndex(COLUMN_USER_ID))
                    val name = it.getString(it.getColumnIndex(COLUMN_USER_NAME))
                    val phone = it.getInt(it.getColumnIndex(COLUMN_USER_PHONE))
                    val mail = it.getString(it.getColumnIndex(COLUMN_USER_MAIL))

                    val user = User(
                        id = id,
                        name = name,
                        phone = phone,
                        email = mail,
                        password = "null"
                    )

                    userList.add(user)

                }while(it.moveToNext())
            }
        }
        return userList
    }

    //delete user. Deletes campaigns related to it, also.
    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteUser(userId: Int){
        val db = this.writableDatabase
        db.beginTransaction()
        try{
            val campaignIds = readCampaigns(userId).map{it.id}
            for (id in campaignIds){
                deleteCampaignAndAllRelatedData(id)
            }

            val deletedusers = db.delete(
                TABLE_USERS,
                "$COLUMN_USER_ID = ?",
                arrayOf(userId.toString())
            )

            db.setTransactionSuccessful()

        } catch (err: Exception){
            err.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    //delete campaigns
    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteCampaignAndAllRelatedData(instanceCampaignId: Int) {
        val db = this.writableDatabase
        Log.d("DB_DELETE", "Starting deletion process for campaign ID: $instanceCampaignId")

        // Use a transaction to ensure all deletions succeed or none do.
        db.beginTransaction()
        try {

            val scenarioIds = readScenariosForCampaign(instanceCampaignId).map { it.id }
            Log.d("DB_DELETE", "Found scenarios to delete: $scenarioIds")

            if (scenarioIds.isNotEmpty()) {

                // We create a string of placeholders (?,?,?) for the query.
                val questionPlaceholders = scenarioIds.joinToString(separator = ", ") { "?" }
                val questionsDeleted = db.delete(
                    TABLE_INSTANCE_QUESTIONS,
                    "$COLUMN_INSTANCE_SCENARIO_ID_FK IN ($questionPlaceholders)",
                    scenarioIds.map { it.toString() }.toTypedArray()
                )
                Log.d("DB_DELETE", "Deleted $questionsDeleted questions for scenarios: $scenarioIds")


                val scenarioPlaceholders = scenarioIds.joinToString(separator = ", ") { "?" }
                val scenariosDeleted = db.delete(
                    TABLE_INSTANCE_SCENARIOS,
                    "$COLUMN_INSTANCE_SCENARIO_ID IN ($scenarioPlaceholders)",
                    scenarioIds.map { it.toString() }.toTypedArray()
                )
                Log.d("DB_DELETE", "Deleted $scenariosDeleted scenarios.")
            }


            val upgradesDeleted = db.delete(
                TABLE_INSTANCE_UPGRADES,
                "$COLUMN_INSTANCE_CAMPAIGN_ID_FK_3 = ?",
                arrayOf(instanceCampaignId.toString())
            )
            Log.d("DB_DELETE", "Deleted $upgradesDeleted upgrades for campaign.")

            val heroesDeleted = db.delete(
                TABLE_INSTANCE_HEROES,
                "$COLUMN_INSTANCE_CAMPAIGN_ID_FK = ?",
                arrayOf(instanceCampaignId.toString())
            )
            Log.d("DB_DELETE", "Deleted $heroesDeleted heroes for campaign.")


            val campaignsDeleted = db.delete(
                TABLE_INSTANCE_CAMPAIGNS,
                "$COLUMN_INSTANCE_CAMPAIGN_ID = ?",
                arrayOf(instanceCampaignId.toString())
            )
            Log.d("DB_DELETE", "Deleted $campaignsDeleted campaign entry.")

            // If all deletions were successful, mark the transaction as successful.
            db.setTransactionSuccessful()
            Log.d("DB_DELETE", "Transaction successful for campaign ID: $instanceCampaignId")

        } catch (e: Exception) {
            Log.e("DB_DELETE", "Error during campaign deletion transaction. Rolling back.", e)
            e.printStackTrace()
        } finally {
            // End the transaction. If setTransactionSuccessful was not called, this will roll back the changes.
            db.endTransaction()
        }
    }


    @SuppressLint("Range")
    fun getAvailableUpgrades(instanceCampaignId: Int, presetCampaignId: Int, presetScenarioId: Int): List<Upgrade> {
        val db = this.readableDatabase
        val availableUpgrades = mutableListOf<Upgrade>()

        val assignedUpgrades = mutableSetOf<Int>()
        val assignedCursor = db.query(
            TABLE_INSTANCE_UPGRADES,
            arrayOf(COLUMN_PRESET_UPGRADE_ID_FK),
            "$COLUMN_INSTANCE_CAMPAIGN_ID_FK_3 = ?",
            arrayOf(instanceCampaignId.toString()),
            null, null, null
        )
        while (assignedCursor.moveToNext()) {
            assignedUpgrades.add(assignedCursor.getInt(assignedCursor.getColumnIndex(COLUMN_PRESET_UPGRADE_ID_FK)))
        }
        assignedCursor.close()
        val query = "SELECT * FROM $TABLE_PRESET_UPGRADES " +
                "WHERE " +
                "$COLUMN_PRESET_CAMPAIGN_ID_FK_3 = ?" +
                " AND " +
                "($COLUMN_PRESET_SCENARIO_ID_FK_3 = ? " +
                "OR" +
                " $COLUMN_PRESET_SCENARIO_ID_FK = 0)"
        val cursor = db.rawQuery(query, arrayOf(presetCampaignId.toString(), presetScenarioId.toString()))

        while (cursor.moveToNext()) {
            val presetId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_ID))


            if (presetId !in assignedUpgrades) {
                availableUpgrades.add(
                    Upgrade(
                        id = presetId,
                        name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_NAME)),
                        type = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_TYPE)),
                        isDisposable = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_UPGRADE_IS_DISPOSABLE))
                    )
                )
            }
        }
        cursor.close()
        return availableUpgrades
    }


    fun assignUpgradeToHero(campaignId: Int, presetUpgradeId: Int, heroId: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INSTANCE_CAMPAIGN_ID_FK_3, campaignId)
            put(COLUMN_PRESET_UPGRADE_ID_FK, presetUpgradeId)
            put(COLUMN_INSTANCE_HERO_ID_FK, heroId)
        }
        return db.insert(TABLE_INSTANCE_UPGRADES, null, values)
    }

    fun deleteUpgradesFromHeroByScenario(heroId: Int, scenarioId: Int) {
        val db = this.writableDatabase
        // We need a JOIN to find the correct upgrades to delete.
        // We want to delete from instance_upgrades...
        // ...where the hero ID matches...
        // ...AND where the preset_upgrade's scenario ID matches the one we are editing.
        val query = """
        DELETE FROM $TABLE_INSTANCE_UPGRADES
        WHERE $COLUMN_INSTANCE_HERO_ID_FK = ? AND $COLUMN_PRESET_UPGRADE_ID_FK IN (
            SELECT $COLUMN_PRESET_UPGRADE_ID FROM $TABLE_PRESET_UPGRADES
            WHERE $COLUMN_PRESET_SCENARIO_ID_FK_3 = ?
        )
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(heroId.toString(), scenarioId.toString()))
        // rawQuery for DELETE doesn't move the cursor, but executing it performs the deletion.
        // We must call moveToFirst() or a similar method to execute it.
        cursor.moveToFirst()
        cursor.close()
        db.close()
    }

}