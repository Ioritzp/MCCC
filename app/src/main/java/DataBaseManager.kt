import Classes.Hero
import android.annotation.SuppressLint
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import android.content.Context
import android.database.Cursor
import android.content.ContentValues

class DataBaseManager(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object DBConstants{
    //DBDetails
    const val DATABASE_NAME = "Marvel Champions Campaigns.db"
    const val DATABASE_VERSION = 1

    //preset tables - prepopulated by the system, not filled by the user

        //preset heroe
        const val TABLE_PRESET_HEROES = "heroe_preset"
        const val COLUMN_PRESET_HERO_ID = "id_heroe"
        const val COLUMN_PRESET_HERO_NAME = "nombre"
        const val COLUMN_PRESET_HERO_INITIALLIFE ="vida_inicial" // FIXED

        //preset campaña
        const val TABLE_PRESET_CAMPAIGNS = "campana_preset" // FIXED
        const val COLUMN_PRESET_CAMPAIGN_ID ="id_preset_campana" // FIXED
        const val COLUMN_PRESET_CAMPAIGN_NAME = "nombre"
        const val COLUMN_PRESET_CAMPAIGN_DESCRIPTION = "descripcion"

        //preset escenarios
        const val TABLE_PRESET_SCENARIOS = "escenario_preset"
        const val COLUMN_PRESET_SCENARIO_ID = "id_preset_escenario"
        const val COLUMN_PRESET_CAMPAIGN_ID_FK ="id_preset_campana_fk" // FIXED
        const val COLUMN_PRESET_SCENARIO_NAME = "nombre"
        const val COLUMN_PRESET_VILLAIN_NAME = "nombre_villano" // FIXED
        const val COLUMN_PRESET_SCENARIO_DESCRIPTION = "descripcion"

        //preset preguntas
        const val TABLE_PRESET_QUESTIONS = "preguntas_preset"
        const val COLUMN_PRESET_QUESTION_ID ="id_preset_pregunta"
        const val COLUMN_PRESET_SCENARIO_ID_FK ="id_preset_escenario_fk" // FIXED
        const val COLUMN_PRESET_QUESTION_TEXT = "texto"

        //users table
        const val TABLE_USERS = "usuarios"
        const val COLUMN_USER_ID = "id_usuario"
        const val COLUMN_USER_NAME = "nombre"
        const val COLUMN_USER_MAIL = "email" // FIXED
        const val COLUMN_USER_PASSWORD = "contrasena" // FIXED
        const val COLUMN_USER_PHONE = "telefono"

        //heroes table
        const val TABLE_INSTANCE_HEROES = "heroe_instancia" // FIXED
        const val COLUMN_INSTANCE_HERO_ID = "id_instancia_heroe"
        const val COLUMN_INSTANCE_CAMPAIGN_ID_FK = "id_instancia_campana_fk"
        const val COLUMN_PRESET_HERO_ID_FK = "id_heroe_fk"
        const val COLUMN_INSTANCE_HERO_NAME = "nombre"
        const val COLUMN_INSTANCE_HERO_CURRENTLIFE = "vida"
        const val COLUMN_INSTANCE_HERO_MODIFICATIONDATE = "fecha_modificacion"
        const val COLUMN_INSTANCE_HERO_MODIFICATIONHOUR = "hora_modificacion" // FIXED

        //campaign table
        const val TABLE_INSTANCE_CAMPAIGNS = "campana_instancia" // FIXED
        const val COLUMN_INSTANCE_CAMPAIGN_ID ="id_instancia_campana"
        const val COLUMN_INSTANCE_USER_ID_FK ="id_usuario_fk"
        const val COLUMN_PRESET_CAMPAIGN_ID_FK_2 = "id_preset_campana_fk_2"
        const val COLUMN_INSTANCE_CAMPAIGN_STARTDATE = "fecha_inicio"
        const val COLUMN_INSTANCE_CAMPAIGN_ENDDATE = "fecha_fin"
        const val COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM = "num_jugadores"

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

}
    //create db function
    override fun onCreate(db: SQLiteDatabase?) {

        db?.execSQL(
            """
                CREATE TABLE $TABLE_PRESET_HEROES (
                    $COLUMN_PRESET_HERO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_PRESET_HERO_NAME TEXT NOT NULL,
                    $COLUMN_PRESET_HERO_INITIALLIFE INTEGER NOT NULL
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_PRESET_CAMPAIGNS (
                    $COLUMN_PRESET_CAMPAIGN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_PRESET_CAMPAIGN_NAME TEXT NOT NULL,
                    $COLUMN_PRESET_CAMPAIGN_DESCRIPTION TEXT NOT NULL
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_PRESET_SCENARIOS (
                    $COLUMN_PRESET_SCENARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_PRESET_CAMPAIGN_ID_FK INTEGER NOT NULL,
                    $COLUMN_PRESET_SCENARIO_NAME TEXT NOT NULL,
                    $COLUMN_PRESET_VILLAIN_NAME TEXT NOT NULL,
                    $COLUMN_PRESET_SCENARIO_DESCRIPTION TEXT NOT NULL,
                    
                    --foreign keys--
                    FOREIGN KEY ($COLUMN_PRESET_CAMPAIGN_ID_FK) REFERENCES $TABLE_PRESET_CAMPAIGNS($COLUMN_PRESET_CAMPAIGN_ID)
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_PRESET_QUESTIONS (
                    $COLUMN_PRESET_QUESTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_PRESET_SCENARIO_ID_FK INTEGER NOT NULL,
                    $COLUMN_PRESET_QUESTION_TEXT TEXT NOT NULL,
                    
                    --foreign keys--
                    FOREIGN KEY ($COLUMN_PRESET_SCENARIO_ID_FK) REFERENCES $TABLE_PRESET_SCENARIOS($COLUMN_PRESET_SCENARIO_ID)
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_USERS (
                    $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_USER_NAME TEXT NOT NULL,
                    $COLUMN_USER_MAIL TEXT NOT NULL,
                    $COLUMN_USER_PASSWORD TEXT NOT NULL,
                    $COLUMN_USER_PHONE INTEGER NOT NULL
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_INSTANCE_CAMPAIGNS (
                    $COLUMN_INSTANCE_CAMPAIGN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_INSTANCE_USER_ID_FK INTEGER NOT NULL,
                    $COLUMN_PRESET_CAMPAIGN_ID_FK_2 INTEGER NOT NULL,
                    $COLUMN_INSTANCE_CAMPAIGN_STARTDATE DATE NOT NULL,
                    $COLUMN_INSTANCE_CAMPAIGN_ENDDATE DATE NOT NULL,
                    $COLUMN_INSTANCE_CAMPAIGN_PLAYERNUM INT NOT NULL,
                    
                    --foreign keys--
                    FOREIGN KEY ($COLUMN_INSTANCE_USER_ID_FK) REFERENCES $TABLE_USERS($COLUMN_USER_ID),
                    FOREIGN KEY ($COLUMN_PRESET_CAMPAIGN_ID_FK_2) REFERENCES $TABLE_PRESET_CAMPAIGNS($COLUMN_PRESET_CAMPAIGN_ID)
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_INSTANCE_SCENARIOS (
                    $COLUMN_INSTANCE_SCENARIO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_INSTANCE_CAMPAIGN_ID_FK_2 INTEGER NOT NULL,
                    $COLUMN_PRESET_SCENARIO_ID_FK_2 INTEGER NOT NULL,
                    $COLUMN_INSTANCE_SCENARIO_STARTDATE DATE NOT NULL,
                    $COLUMN_INSTANCE_SCENARIO_ENDDATE DATE NOT NULL,
                    $COLUMN_INSTANCE_SCENARIO_STATUS TEXT NOT NULL,
                    
                    --foreign keys--
                    FOREIGN KEY ($COLUMN_INSTANCE_CAMPAIGN_ID_FK_2) REFERENCES $TABLE_INSTANCE_CAMPAIGNS($COLUMN_INSTANCE_CAMPAIGN_ID),
                    FOREIGN KEY ($COLUMN_PRESET_SCENARIO_ID_FK_2) REFERENCES $TABLE_PRESET_SCENARIOS($COLUMN_PRESET_SCENARIO_ID)
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_INSTANCE_QUESTIONS (
                    $COLUMN_INSTANCE_QUESTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_PRESET_QUESTION_ID_FK INTEGER NOT NULL,
                    $COLUMN_INSTANCE_SCENARIO_ID_FK INTEGER NOT NULL,
                    $COLUMN_INSTANCE_QUESTION_ANSWER TEXT NOT NULL,
                    
                    --foreign keys--
                    FOREIGN KEY ($COLUMN_PRESET_QUESTION_ID_FK) REFERENCES $TABLE_PRESET_QUESTIONS($COLUMN_PRESET_QUESTION_ID),
                    FOREIGN KEY ($COLUMN_INSTANCE_SCENARIO_ID_FK) REFERENCES $TABLE_INSTANCE_SCENARIOS($COLUMN_INSTANCE_SCENARIO_ID)
            )
        """
        )

        db?.execSQL(
            """
                CREATE TABLE $TABLE_INSTANCE_HEROES (
                    $COLUMN_INSTANCE_HERO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_INSTANCE_CAMPAIGN_ID_FK INTEGER NOT NULL,
                    $COLUMN_PRESET_HERO_ID_FK INTEGER NOT NULL,
                    $COLUMN_INSTANCE_HERO_NAME TEXT NOT NULL,
                    $COLUMN_INSTANCE_HERO_CURRENTLIFE INTEGER NOT NULL,
                    $COLUMN_INSTANCE_HERO_MODIFICATIONDATE DATE NOT NULL,
                    $COLUMN_INSTANCE_HERO_MODIFICATIONHOUR TEXT NOT NULL,
                    
                     --foreign keys--
                    FOREIGN KEY ($COLUMN_INSTANCE_CAMPAIGN_ID_FK) REFERENCES $TABLE_INSTANCE_CAMPAIGNS($COLUMN_INSTANCE_CAMPAIGN_ID),
                    FOREIGN KEY ($COLUMN_PRESET_HERO_ID_FK) REFERENCES $TABLE_PRESET_HEROES($COLUMN_PRESET_HERO_ID)
            )
        """
        )

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //POSIBLEMENTE HAYA QUE MODIFICAR ESTE METODO PARA AÑADIR ALTER, Y CREAR UN METODO REMAKE PARA EL DROP

        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRESET_HEROES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRESET_QUESTIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRESET_SCENARIOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PRESET_CAMPAIGNS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_INSTANCE_HEROES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_INSTANCE_QUESTIONS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_INSTANCE_SCENARIOS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_INSTANCE_CAMPAIGNS")

        onCreate(db)
    }

    //REGION: CRUD functions - crear, leer, actualizar y borrar.

    //tablas prepopuladas. ESTAS FUNCIONES SOLO SE DEBEN LLAMAN DESDE ADMIN.
    //heroes
    fun addPresetHero(name: String, initialLife: Int): Long{
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_PRESET_HERO_NAME, name)
            put(COLUMN_PRESET_HERO_INITIALLIFE, initialLife)
        }

        val newRowId = db.insert(TABLE_PRESET_HEROES, null, values)


        return newRowId

    }
    @SuppressLint("Range")
    fun getAllHeroes():List<Hero> {
        val heroList = mutableListOf<Hero>()
        val db = this.readableDatabase
        var cursor: Cursor? = null
        try {
            //query to db
            cursor = db.query(
                TABLE_PRESET_HEROES,
                null, // null for all columns
                null, // No WHERE clause
                null, // No values for the WHERE clause
                null, // Do not group the rows
                null, // Do not filter by row groups
                null  // The sort order

            )

            //check cursor data and move to 1st row
            if(cursor.moveToFirst()){
                do{
                    //get data
                    val id = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_HERO_ID))
                    val name = cursor.getString(cursor.getColumnIndex(COLUMN_PRESET_HERO_NAME))
                    val initialLife = cursor.getInt(cursor.getColumnIndex(COLUMN_PRESET_HERO_INITIALLIFE))

                    //create hero object to add to list
                    val hero = Hero(id, name, initialLife, initialLife) //as currentlife for preset will always be initial life

                    heroList.add(hero)

                }while(cursor.moveToNext())

            }


        } catch (e: Exception){
            e.printStackTrace()
        } finally { //ALWAYS CLOSE CURSOR  hence the finally
            cursor?.close()

        }

        return heroList

    }

}