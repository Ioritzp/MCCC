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

class DataBaseManager(context: Context): SQLiteAssetHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
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

        //preset mejoras
        const val TABLE_PRESET_UPGRADES = "mejora_preset"
        const val COLUMN_PRESET_UPGRADE_ID ="id_preset_mejora"
        const val COLUMN_PRESET_CAMPAIGN_ID_FK_3 ="id_preset_campana_fk"
        const val COLUMN_PRESET_UPGRADE_NAME = "nombre"
        const val COLUMN_PRESET_UPGRADE_TYPE = "tipo"

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
        const val COLUMN_INSTANCE_HERO_CREDITS = "Creditos"
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

}

    //Custom onUpgrade for keeping user data when updating the db (temporary - planning to move to 2 databases after the project)
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
                    val question = MarvelQuestion(id, text, " ")
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
                    val upgrade = Upgrade(id, name, type)
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

                    val scenario = Scenario(id, name, villainName, description, emptyList())
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

                    val question = MarvelQuestion(id, text, " ")
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
                    //TODO: check if the password is correctly hashed
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

    @SuppressLint("Range")
    fun addCampaign(campaign: InstanceCampaign): Long{
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_PRESET_CAMPAIGN_ID_FK_2, campaign.presetCampaignId)
                put(COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM, campaign.playerNum)
                put(COLUMN_INSTANCE_CAMPAIGN_STARTDATE, campaign.startDate.toString())
                put(COLUMN_INSTANCE_CAMPAIGN_ENDDATE, campaign.endDate.toString())
                put(COLUMN_INSTANCE_USER_ID_FK, campaign.userId)
                put(COLUMN_INSTANCE_CAMPAIGN_DIFFICULTY, campaign.difficulty)

            }

            val newCampaignId = db.insert(TABLE_INSTANCE_CAMPAIGNS, null, values)
            db.close()

        return newCampaignId

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun readCampaigns(): List<InstanceCampaign> {
        val campaignList = mutableListOf<InstanceCampaign>()
        val db = this.readableDatabase

        var cursor: Cursor? = null
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
    """

        try {
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    //get data from preset
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_NAME))
                    val description = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_DESCRIPTION))

                    // Get data from instance
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID))
                    val presetCampaignId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_CAMPAIGN_ID_FK_2))
                    val playerNum = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM))
                    val startDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_STARTDATE))
                    val endDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ENDDATE))
                    val userId = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_USER_ID_FK))
                    val difficulty = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_DIFFICULTY))

                    val campaign = InstanceCampaign(
                        id = id,
                        presetCampaignId = presetCampaignId,
                        name = name,
                        description = description,
                        scenarioList = readScenariosForCampaign(id),
                        userId = userId,
                        userName = " ",
                        playerNum = playerNum,
                        startDate = LocalDateTime.parse(startDateStr),
                        endDate = LocalDateTime.parse(endDateStr),
                        difficulty = difficulty
                    )

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
                    val instanceCampaignId = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID_FK_2))
                    val presetScenarioId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_ID_FK_2))
                    val startDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STARTDATE))
                    val endDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ENDDATE))
                    val status = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STATUS))

                    val scenario = InstanceScenario(
                        id = id,
                        instanceCampaignId = instanceCampaignId,
                        presetScenarioId = presetScenarioId,
                        name = name,
                        description = description,
                        questionList = readQuestionsForScenario(id), // This still calls the next level down
                        villainName = villainName,
                        startDate = LocalDateTime.parse(startDateStr),
                        endDate = LocalDateTime.parse(endDateStr),
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
            }

            db.insert(TABLE_INSTANCE_HEROES, null, values)
            db.close()

    }

    //region instanced scenarios
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun addScenario(scenario: InstanceScenario): Long{
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_INSTANCE_CAMPAIGN_ID_FK_2, scenario.instanceCampaignId)
                put(COLUMN_PRESET_SCENARIO_ID_FK_2, scenario.presetScenarioId)
                put(COLUMN_INSTANCE_SCENARIO_STARTDATE, scenario.startDate.toLocalDate().toString())
                put(COLUMN_INSTANCE_SCENARIO_ENDDATE, scenario.endDate.toLocalTime().toString())
                put(COLUMN_INSTANCE_SCENARIO_STATUS, scenario.status)
            }

            val newScenarioId = db.insert(TABLE_INSTANCE_SCENARIOS, null, values)
            db.close()

        return newScenarioId
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun readScenarios(): List<InstanceScenario> {
        val scenarioList = mutableListOf<InstanceScenario>()
        val db = this.readableDatabase

        var cursor: Cursor? = null
        //SQL query for name,
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
    """

        try {
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    //get data from preset
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_NAME))
                    val description = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_DESCRIPTION))
                    val villainName = cursor.getString(cursor.getColumnIndex((COLUMN_PRESET_VILLAIN_NAME)))

                    // Get data from instance
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ID))
                    val instanceCampaignId = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_CAMPAIGN_ID_FK_2))
                    val presetScenarioId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_SCENARIO_ID_FK_2))
                    val startDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STARTDATE))
                    val endDateStr = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ENDDATE))
                    val status = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_STATUS))


                    val scenario = InstanceScenario(
                        id = id,
                        instanceCampaignId = instanceCampaignId,
                        presetScenarioId = presetScenarioId,
                        name = name,
                        description = description,
                        questionList = readQuestionsForScenario(id),
                        villainName = villainName,
                        startDate = LocalDateTime.parse(startDateStr),
                        endDate = LocalDateTime.parse(endDateStr),
                        status = status
                    )

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

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun readQuestionsForScenario(scenarioId: Int): List<InstanceMarvelQuestion> {
        val questionList = mutableListOf<InstanceMarvelQuestion>()
        val db = this.readableDatabase
        var cursor: Cursor? = null

        // The query is the same as readQuestions, but with a WHERE clause
        val query = """
        SELECT
            iq.*,
            pq.$COLUMN_PRESET_QUESTION_TEXT
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
                    // Get data from the joined TABLE_PRESET_QUESTIONS
                    val text = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_TEXT))

                    // Get data from the TABLE_INSTANCE_QUESTIONS
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_QUESTION_ID))
                    val instanceScenarioId = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ID_FK))
                    val presetQuestionId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_ID_FK))
                    val answer = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_QUESTION_ANSWER))

                    // Create the InstanceMarvelQuestion object
                    val question = InstanceMarvelQuestion(
                        id = id,
                        instanceScenarioId = instanceScenarioId,
                        presetQuestionId = presetQuestionId,
                        text = text, // From the JOIN
                        answer = answer
                    )
                    questionList.add(question)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return questionList
    }


    //region instanced questions

    @SuppressLint("Range")
    fun addQuestion(question: InstanceMarvelQuestion){
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_PRESET_QUESTION_ID_FK, question.presetQuestionId)
                put(COLUMN_INSTANCE_SCENARIO_ID_FK, question.instanceScenarioId)
                put(COLUMN_INSTANCE_QUESTION_ANSWER, question.answer)
            }

            db.insert(TABLE_INSTANCE_QUESTIONS, null, values)
            db.close()

    }

    @SuppressLint("Range")
    fun readQuestions(): List<InstanceMarvelQuestion> {
        val questionList = mutableListOf<InstanceMarvelQuestion>()
        val db = this.readableDatabase

        var cursor: Cursor? = null

        val query = """
            SELECT
            iq.*,
            pq.$COLUMN_PRESET_QUESTION_TEXT
            FROM
            $TABLE_INSTANCE_QUESTIONS iq
            JOIN
            $TABLE_PRESET_QUESTIONS pq ON iq.$COLUMN_PRESET_QUESTION_ID_FK = pq.$COLUMN_PRESET_QUESTION_ID
        """
        try {
            cursor = db.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                do {
                    val text = cursor.getString(cursor.getColumnIndex((COLUMN_PRESET_QUESTION_TEXT)))

                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_QUESTION_ID))
                    val instanceScenarioId = cursor.getInt(cursor.getColumnIndex(COLUMN_INSTANCE_SCENARIO_ID_FK))
                    val presetQuestionId = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_QUESTION_ID_FK))
                    val answer = cursor.getString(cursor.getColumnIndex(COLUMN_INSTANCE_QUESTION_ANSWER))
                    val question = InstanceMarvelQuestion(
                        id = id,
                        instanceScenarioId = instanceScenarioId,
                        presetQuestionId = presetQuestionId,
                        text = text,
                        answer = answer
                    )
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

    //region instanced upgrades

    @SuppressLint("Range")
    fun addUpgrade(upgrade: InstanceUpgrade){
        val db = this.writableDatabase

            val values = ContentValues().apply {
                put(COLUMN_PRESET_UPGRADE_ID_FK, upgrade.presetUpgradeId)
                put(COLUMN_INSTANCE_HERO_ID_FK, upgrade.heroInstanceId)
            }

            db.insert(TABLE_INSTANCE_UPGRADES, null, values)
            db.close()

    }

}