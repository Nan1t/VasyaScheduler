package ru.nanit.vasyascheduler;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import ru.nanit.vasyascheduler.api.storage.Configuration;
import ru.nanit.vasyascheduler.api.storage.Language;
import ru.nanit.vasyascheduler.api.storage.properties.DBProperties;
import ru.nanit.vasyascheduler.api.storage.properties.FileProperties;
import ru.nanit.vasyascheduler.api.storage.database.*;
import ru.nanit.vasyascheduler.api.storage.properties.Properties;
import ru.nanit.vasyascheduler.api.util.FileUtil;
import ru.nanit.vasyascheduler.api.util.Logger;
import ru.nanit.vasyascheduler.api.util.Patterns;
import ru.nanit.vasyascheduler.bot.commands.*;
import ru.nanit.vasyascheduler.bot.console.CommandRestart;
import ru.nanit.vasyascheduler.bot.console.CommandStats;
import ru.nanit.vasyascheduler.bot.console.CommandStop;
import ru.nanit.vasyascheduler.bot.types.BotTelegram;
import ru.nanit.vasyascheduler.data.datetime.Days;
import ru.nanit.vasyascheduler.data.schedule.ConsultationSchedule;
import ru.nanit.vasyascheduler.data.schedule.StudentSchedule;
import ru.nanit.vasyascheduler.data.schedule.TeacherSchedule;
import ru.nanit.vasyascheduler.services.*;
import ru.nanit.vasyascheduler.bot.Bot;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Scanner;

public class VasyaScheduler {

    private Path root;
    private Configuration conf;
    private Language lang;
    private Database database;

    private CommandManager commandManager;
    private WebHookManager webhookManager;
    private SubscribesManager subscribesManager;
    private ScheduleManager scheduleManager;
    private ScheduleTimer scheduleTimer;
    private ConsoleManager consoleManager;
    private PointsManager pointsManager;

    public VasyaScheduler(Path root){
        this.root = root;
    }

    /**
     * Main method
     */
    public void start() throws Exception {
        Logger.info("Starting bot...");
        Path confFolder = Paths.get(root.toString(), "configuration");

        conf = new Configuration("config.conf", confFolder, this);

        try{
            Logger.info("Connecting to database...");
            setupDatabase();
        } catch (SQLException e){
            Logger.error("Error while connecting to database. You should to check connection data in config.conf. " + e.getMessage());
            exit();
            return;
        }

        registerSerializers();
        setupProxy();

        Properties properties;
        String propMethod = conf.get().getNode("properties").getString();

        if(propMethod.equalsIgnoreCase("FILE")){
            properties = new FileProperties(root, "hash.properties");
        } else {
            String type = conf.get().getNode("database", "type").getString();
            database.executeSQL(getSQLString("/tables/" + type.toLowerCase() + "/hash.sql"));
            properties = new DBProperties(database);
        }

        Configuration scheduleConf = new Configuration("schedule.conf", confFolder, this);

        FileUtil.setRootPath(root);
        Days.parse(scheduleConf);
        Patterns.setTeacherDefault(conf.get().getNode("expressions", "teacherDefault").getString());
        Patterns.setTeacherDefaultSeparate(conf.get().getNode("expressions", "teacherDefaultGroups").getString());
        Patterns.setTeacherInline(conf.get().getNode("expressions", "teacherInline").getString());

        lang = new Language("lang.conf", confFolder, this);
        scheduleManager = new ScheduleManager(scheduleConf, properties);
        subscribesManager = new SubscribesManager(database, scheduleManager);

        scheduleManager.loadTeacherSchedule();
        scheduleManager.loadConsultationSchedule();
        scheduleManager.loadStudentsSchedule();
        scheduleManager.loadProperties();

        subscribesManager.loadAll();

        Configuration pointsConf = new Configuration("points.conf", confFolder, this);
        pointsManager = new PointsManager(pointsConf);

        registerCommands();
        registerBots();
        //registerWebHooks();
        registerConsoleCommands();

        scheduleTimer = new ScheduleTimer(conf, lang, properties, scheduleManager, subscribesManager);
        scheduleTimer.start();

        /*Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try{
                VasyaScheduler.this.stop();
            } catch (Exception e){
                e.printStackTrace();
            }
        }));*/

        Logger.info("Done! For help, type \"help\" or \"?\"");
    }

    public void stop() throws Exception {
        Logger.info("Stopping bot...");

        if(consoleManager != null){
            consoleManager.stopListening();
        }

        if(database != null){
            database.closeConnection();
        }

        if(webhookManager != null){
            webhookManager.stop();
        }

        if(scheduleTimer != null){
            scheduleTimer.stop();
        }

        for (Bot bot : BotManager.getBots().values()){
            bot.disable();
        }
    }

    public void exit() throws Exception {
        stop();
        Logger.info("Good bye!");
        System.exit(0);
    }

    private void setupProxy(){
        boolean isEnable = conf.get().getNode("proxy", "enable").getBoolean();

        if(isEnable){
            String user = conf.get().getNode("proxy", "user").getString();
            String password = conf.get().getNode("proxy", "password").getString();

            Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, password.toCharArray());
                    }
                }
            );

            System.setProperty("http.proxyUser", user);
            System.setProperty("http.proxyPassword", password);
            Logger.info("Proxy successfully authorized");
        }
    }

    private void registerBots(){
        Bot telegram = new BotTelegram(conf.get().getNode("telegram", "botName").getString(),
                conf.get().getNode("telegram", "token").getString(), commandManager);

        BotManager.registerBot(Bot.Type.TELEGRAM, telegram);

        telegram.enable();
    }

    private void registerCommands(){
        commandManager = new CommandManager();

        commandManager.registerCommand(new CommandHelp(lang), "help", "start", "h");

        commandManager.registerCommand(new CommandTeacherSubscribe(lang, subscribesManager, scheduleManager), "teachersubscribe", "ts");
        commandManager.registerCommand(new CommandTeacher(lang, subscribesManager, scheduleManager), "teacher",  "t");
        commandManager.registerCommand(new CommandTeacherDeny(lang, subscribesManager), "teacherdeny", "tdeny");

        commandManager.registerCommand(new CommandConsultations(lang, subscribesManager, scheduleManager), "consultations", "consultation", "c");
        commandManager.registerCommand(new CommandConsultationsAll(lang, scheduleManager), "consultationsall", "consall");

        commandManager.registerCommand(new CommandStudentsSubscribe(lang, subscribesManager, scheduleManager), "studentsubscribe", "ss");
        commandManager.registerCommand(new CommandStudents(lang, subscribesManager, scheduleManager), "students", "student", "s");
        commandManager.registerCommand(new CommandStudentsDeny(lang, subscribesManager), "studentsdeny", "sdeny");

        commandManager.registerCommand(new CommandPoints(lang, pointsManager, subscribesManager), "points", "point", "p");
    }

    private void registerConsoleCommands(){
        consoleManager = new ConsoleManager();
        consoleManager.registerCommand(new ru.nanit.vasyascheduler.bot.console.CommandHelp(), "help", "?");
        consoleManager.registerCommand(new CommandStop(this), "stop", "exit", "quit");
        consoleManager.registerCommand(new CommandRestart(this), "restart", "reload");
        consoleManager.registerCommand(new CommandStats(), "status", "stats", "info");
        consoleManager.startListening();
    }

    private void registerWebHooks() throws Exception {
        webhookManager = new WebHookManager(conf);
        webhookManager.start();
    }

    private void registerSerializers(){
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(TeacherSchedule.class), new TeacherSchedule.Serializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(StudentSchedule.class), new StudentSchedule.Serializer());
        TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ConsultationSchedule.class), new ConsultationSchedule.Serializer());
    }

    private void setupDatabase() throws SQLException {
        String type = conf.get().getNode("database", "type").getString();
        String host = conf.get().getNode("database", "host").getString();
        String dbName = conf.get().getNode("database", "database").getString();
        String user = conf.get().getNode("database", "user").getString();
        String password = conf.get().getNode("database", "password").getString();
        int port = conf.get().getNode("database", "port").getInt();

        switch (type.toLowerCase()){
            default:
                Logger.error("Unsupported database type '" + type + "'. Check supported types in config.conf comment.");
                throw new SQLException("Unsupported database type");
            case "mysql":
                database = new MySQLDatabase(host, port, dbName, user, password);
                break;
            case "sqlite":
                database = new SQLiteDatabase(root, dbName, user, password);
                break;
            case "h2":
                database = new H2Database(root, dbName, user, password);
                break;
            case "postgresql":
                database = new PostgreSQLDatabase(host, port, dbName, user, password);
                break;
        }

        Logger.info("Connected successfully to " + database.toString());

        database.executeSQL(getSQLString("/tables/" + type.toLowerCase() + "/teachers.sql"));
        database.executeSQL(getSQLString("/tables/" + type.toLowerCase() + "/students.sql"));
        database.executeSQL(getSQLString("/tables/" + type.toLowerCase() + "/points.sql"));
    }

    private String getSQLString(String sqlFile){
        InputStream in = getClass().getResourceAsStream(sqlFile);
        Scanner scanner = new Scanner(in);
        StringBuilder builder = new StringBuilder();

        while (scanner.hasNextLine()){
            builder.append(scanner.nextLine());
        }

        return builder.toString();
    }
}
