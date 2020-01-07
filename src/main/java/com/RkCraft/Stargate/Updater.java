package com.RkCraft.Stargate;

import org.bukkit.plugin.*;
import org.bukkit.configuration.file.*;
import java.util.logging.*;
import java.util.zip.*;
import java.util.*;
import java.io.*;
import org.json.simple.*;
import java.net.*;
import org.bukkit.scheduler.*;

public class Updater
{
    private static final String TITLE_VALUE = "name";
    private static final String LINK_VALUE = "downloadUrl";
    private static final String TYPE_VALUE = "releaseType";
    private static final String VERSION_VALUE = "gameVersion";
    private static final String QUERY = "/servermods/files?projectIds=";
    private static final String HOST = "https://api.curseforge.com";
    private static final String USER_AGENT = "Updater (by Gravity)";
    private static final String DELIMETER = "^v|[\\s_-]v";
    private static final String[] NO_UPDATE_TAG;
    private static final int BYTE_SIZE = 1024;
    private static final String API_KEY_CONFIG_KEY = "api-key";
    private static final String DISABLE_CONFIG_KEY = "disable";
    private static final String API_KEY_DEFAULT = "PUT_API_KEY_HERE";
    private static final boolean DISABLE_DEFAULT = false;
    private final Plugin plugin;
    private final UpdateType type;
    private final boolean announce;
    private final File file;
    private final File updateFolder;
    private final UpdateCallback callback;
    private int id;
    private String apiKey;
    private String versionName;
    private String versionLink;
    private String versionType;
    private String versionGameVersion;
    private URL url;
    private Thread thread;
    private UpdateResult result;
    
    public Updater(final Plugin plugin, final int id, final File file, final UpdateType type, final boolean announce) {
        this(plugin, id, file, type, null, announce);
    }
    
    public Updater(final Plugin plugin, final int id, final File file, final UpdateType type, final UpdateCallback callback) {
        this(plugin, id, file, type, callback, false);
    }
    
    public Updater(final Plugin plugin, final int id, final File file, final UpdateType type, final UpdateCallback callback, final boolean announce) {
        this.id = -1;
        this.apiKey = null;
        this.result = UpdateResult.SUCCESS;
        this.plugin = plugin;
        this.type = type;
        this.announce = announce;
        this.file = file;
        this.id = id;
        this.updateFolder = this.plugin.getServer().getUpdateFolderFile();
        this.callback = callback;
        final File pluginFile = this.plugin.getDataFolder().getParentFile();
        final File updaterFile = new File(pluginFile, "Updater");
        final File updaterConfigFile = new File(updaterFile, "config.yml");
        final YamlConfiguration config = new YamlConfiguration();
        config.options().header("This configuration file affects all plugins using the Updater system (version 2+ - http://forums.bukkit.org/threads/96681/ )\nIf you wish to use your API key, read http://wiki.bukkit.org/ServerMods_API and place it below.\nSome updating systems will not adhere to the disabled value, but these may be turned off in their plugin's configuration.");
        config.addDefault("api-key", (Object)"PUT_API_KEY_HERE");
        config.addDefault("disable", (Object)false);
        if (!updaterFile.exists()) {
            this.fileIOOrError(updaterFile, updaterFile.mkdir(), true);
        }
        final boolean createFile = !updaterConfigFile.exists();
        try {
            if (createFile) {
                this.fileIOOrError(updaterConfigFile, updaterConfigFile.createNewFile(), true);
                config.options().copyDefaults(true);
                config.save(updaterConfigFile);
            }
            else {
                config.load(updaterConfigFile);
            }
        }
        catch (Exception e) {
            String message;
            if (createFile) {
                message = "The updater could not create configuration at " + updaterFile.getAbsolutePath();
            }
            else {
                message = "The updater could not load configuration at " + updaterFile.getAbsolutePath();
            }
            this.plugin.getLogger().log(Level.SEVERE, message, e);
        }
        if (config.getBoolean("disable")) {
            this.result = UpdateResult.DISABLED;
            return;
        }
        String key = config.getString("api-key");
        if ("PUT_API_KEY_HERE".equalsIgnoreCase(key) || "".equals(key)) {
            key = null;
        }
        this.apiKey = key;
        try {
            this.url = new URL("https://api.curseforge.com/servermods/files?projectIds=" + this.id);
        }
        catch (MalformedURLException e2) {
            this.plugin.getLogger().log(Level.SEVERE, "The project ID provided for updating, " + this.id + " is invalid.", e2);
            this.result = UpdateResult.FAIL_BADID;
        }
        if (this.result != UpdateResult.FAIL_BADID) {
            (this.thread = new Thread(new UpdateRunnable())).start();
        }
        else {
            this.runUpdater();
        }
    }
    
    public UpdateResult getResult() {
        this.waitForThread();
        return this.result;
    }
    
    public ReleaseType getLatestType() {
        this.waitForThread();
        if (this.versionType != null) {
            for (final ReleaseType type : ReleaseType.values()) {
                if (this.versionType.equalsIgnoreCase(type.name())) {
                    return type;
                }
            }
        }
        return null;
    }
    
    public String getLatestGameVersion() {
        this.waitForThread();
        return this.versionGameVersion;
    }
    
    public String getLatestName() {
        this.waitForThread();
        return this.versionName;
    }
    
    public String getLatestFileLink() {
        this.waitForThread();
        return this.versionLink;
    }
    
    private void waitForThread() {
        if (this.thread != null && this.thread.isAlive()) {
            try {
                this.thread.join();
            }
            catch (InterruptedException e) {
                this.plugin.getLogger().log(Level.SEVERE, null, e);
            }
        }
    }
    
    private void saveFile(final String file) {
        final File folder = this.updateFolder;
        this.deleteOldFiles();
        if (!folder.exists()) {
            this.fileIOOrError(folder, folder.mkdir(), true);
        }
        this.downloadFile();
        final File dFile = new File(folder.getAbsolutePath(), file);
        if (dFile.getName().endsWith(".zip")) {
            this.unzip(dFile.getAbsolutePath());
        }
        if (this.announce) {
            this.plugin.getLogger().info("Finished updating.");
        }
    }
    
    private void downloadFile() {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            final URL fileUrl = new URL(this.versionLink);
            final int fileLength = fileUrl.openConnection().getContentLength();
            in = new BufferedInputStream(fileUrl.openStream());
            fout = new FileOutputStream(new File(this.updateFolder, this.file.getName()));
            final byte[] data = new byte[1024];
            if (this.announce) {
                this.plugin.getLogger().info("About to download a new update: " + this.versionName);
            }
            long downloaded = 0L;
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                downloaded += count;
                fout.write(data, 0, count);
                final int percent = (int)(downloaded * 100L / fileLength);
                if (this.announce && percent % 10 == 0) {
                    this.plugin.getLogger().info("Downloading update: " + percent + "% of " + fileLength + " bytes.");
                }
            }
        }
        catch (Exception ex) {
            this.plugin.getLogger().log(Level.WARNING, "The auto-updater tried to download a new update, but was unsuccessful.", ex);
            this.result = UpdateResult.FAIL_DOWNLOAD;
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex2) {
                this.plugin.getLogger().log(Level.SEVERE, null, ex2);
            }
            try {
                if (fout != null) {
                    fout.close();
                }
            }
            catch (IOException ex2) {
                this.plugin.getLogger().log(Level.SEVERE, null, ex2);
            }
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ex3) {
                this.plugin.getLogger().log(Level.SEVERE, null, ex3);
            }
            try {
                if (fout != null) {
                    fout.close();
                }
            }
            catch (IOException ex3) {
                this.plugin.getLogger().log(Level.SEVERE, null, ex3);
            }
        }
    }
    
    private void deleteOldFiles() {
        final File[] arr$;
        final File[] list = arr$ = this.listFilesOrError(this.updateFolder);
        for (final File xFile : arr$) {
            if (xFile.getName().endsWith(".zip")) {
                this.fileIOOrError(xFile, xFile.mkdir(), true);
            }
        }
    }
    
    private void unzip(final String file) {
        final File fSourceZip = new File(file);
        try {
            final String zipPath = file.substring(0, file.length() - 4);
            final ZipFile zipFile = new ZipFile(fSourceZip);
            final Enumeration<? extends ZipEntry> e = zipFile.entries();
            while (e.hasMoreElements()) {
                final ZipEntry entry = (ZipEntry)e.nextElement();
                final File destinationFilePath = new File(zipPath, entry.getName());
                this.fileIOOrError(destinationFilePath.getParentFile(), destinationFilePath.getParentFile().mkdirs(), true);
                if (!entry.isDirectory()) {
                    final BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    final byte[] buffer = new byte[1024];
                    final FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    final BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
                    int b;
                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                    final String name = destinationFilePath.getName();
                    if (!name.endsWith(".jar") || !this.pluginExists(name)) {
                        continue;
                    }
                    final File output = new File(this.updateFolder, name);
                    this.fileIOOrError(output, destinationFilePath.renameTo(output), true);
                }
            }
            zipFile.close();
            this.moveNewZipFiles(zipPath);
        }
        catch (IOException e2) {
            this.plugin.getLogger().log(Level.SEVERE, "The auto-updater tried to unzip a new update file, but was unsuccessful.", e2);
            this.result = UpdateResult.FAIL_DOWNLOAD;
        }
        finally {
            this.fileIOOrError(fSourceZip, fSourceZip.delete(), false);
        }
    }
    
    private void moveNewZipFiles(final String zipPath) {
        final File[] arr$;
        final File[] list = arr$ = this.listFilesOrError(new File(zipPath));
        for (final File dFile : arr$) {
            if (dFile.isDirectory() && this.pluginExists(dFile.getName())) {
                final File oFile = new File(this.plugin.getDataFolder().getParent(), dFile.getName());
                final File[] dList = this.listFilesOrError(dFile);
                final File[] oList = this.listFilesOrError(oFile);
                for (final File cFile : dList) {
                    boolean found = false;
                    for (final File xFile : oList) {
                        if (xFile.getName().equals(cFile.getName())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        final File output = new File(oFile, cFile.getName());
                        this.fileIOOrError(output, cFile.renameTo(output), true);
                    }
                    else {
                        this.fileIOOrError(cFile, cFile.delete(), false);
                    }
                }
            }
            this.fileIOOrError(dFile, dFile.delete(), false);
        }
        final File zip = new File(zipPath);
        this.fileIOOrError(zip, zip.delete(), false);
    }
    
    private boolean pluginExists(final String name) {
        final File[] arr$;
        final File[] plugins = arr$ = this.listFilesOrError(new File("plugins"));
        for (final File file : arr$) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean versionCheck() {
        final String title = this.versionName;
        if (this.type != UpdateType.NO_VERSION_CHECK) {
            final String localVersion = this.plugin.getDescription().getVersion();
            if (title.split("^v|[\\s_-]v").length < 2) {
                final String authorInfo = this.plugin.getDescription().getAuthors().isEmpty() ? "" : (" (" + this.plugin.getDescription().getAuthors().get(0) + ")");
                this.plugin.getLogger().warning("The author of this plugin" + authorInfo + " has misconfigured their Auto Update system");
                this.plugin.getLogger().warning("File versions should follow the format 'PluginName vVERSION'");
                this.plugin.getLogger().warning("Please notify the author of this error.");
                this.result = UpdateResult.FAIL_NOVERSION;
                return false;
            }
            final String remoteVersion = title.split("^v|[\\s_-]v")[title.split("^v|[\\s_-]v").length - 1].split(" ")[0];
            if (this.hasTag(localVersion) || !this.shouldUpdate(localVersion, remoteVersion)) {
                this.result = UpdateResult.NO_UPDATE;
                return false;
            }
        }
        return true;
    }
    
    public boolean shouldUpdate(final String localVersion, final String remoteVersion) {
        return !localVersion.equalsIgnoreCase(remoteVersion);
    }
    
    private boolean hasTag(final String version) {
        for (final String string : Updater.NO_UPDATE_TAG) {
            if (version.contains(string)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean read() {
        try {
            final URLConnection conn = this.url.openConnection();
            conn.setConnectTimeout(5000);
            if (this.apiKey != null) {
                conn.addRequestProperty("X-API-Key", this.apiKey);
            }
            conn.addRequestProperty("User-Agent", "Updater (by Gravity)");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final JSONArray array = (JSONArray)JSONValue.parse(response);
            if (array.isEmpty()) {
                this.plugin.getLogger().warning("The updater could not find any files for the project id " + this.id);
                this.result = UpdateResult.FAIL_BADID;
                return false;
            }
            final JSONObject latestUpdate = (JSONObject)array.get(array.size() - 1);
            this.versionName = (String)latestUpdate.get((Object)"name");
            this.versionLink = (String)latestUpdate.get((Object)"downloadUrl");
            this.versionType = (String)latestUpdate.get((Object)"releaseType");
            this.versionGameVersion = (String)latestUpdate.get((Object)"gameVersion");
            return true;
        }
        catch (IOException e) {
            if (e.getMessage().contains("HTTP response code: 403")) {
                this.plugin.getLogger().severe("dev.bukkit.org rejected the API key provided in plugins/Updater/config.yml");
                this.plugin.getLogger().severe("Please double-check your configuration to ensure it is correct.");
                this.result = UpdateResult.FAIL_APIKEY;
            }
            else {
                this.plugin.getLogger().severe("The updater could not contact dev.bukkit.org for updating.");
                this.plugin.getLogger().severe("If you have not recently modified your configuration and this is the first time you are seeing this message, the site may be experiencing temporary downtime.");
                this.result = UpdateResult.FAIL_DBO;
            }
            this.plugin.getLogger().log(Level.SEVERE, null, e);
            return false;
        }
    }
    
    private void fileIOOrError(final File file, final boolean result, final boolean create) {
        if (!result) {
            this.plugin.getLogger().severe("The updater could not " + (create ? "create" : "delete") + " file at: " + file.getAbsolutePath());
        }
    }
    
    private File[] listFilesOrError(final File folder) {
        final File[] contents = folder.listFiles();
        if (contents == null) {
            this.plugin.getLogger().severe("The updater could not access files at: " + this.updateFolder.getAbsolutePath());
            return new File[0];
        }
        return contents;
    }
    
    private void runUpdater() {
        if (this.url != null && this.read() && this.versionCheck()) {
            if (this.versionLink != null && this.type != UpdateType.NO_DOWNLOAD) {
                String name = this.file.getName();
                if (this.versionLink.endsWith(".zip")) {
                    name = this.versionLink.substring(this.versionLink.lastIndexOf("/") + 1);
                }
                this.saveFile(name);
            }
            else {
                this.result = UpdateResult.UPDATE_AVAILABLE;
            }
        }
        if (this.callback != null) {
            new BukkitRunnable() {
                public void run() {
                    Updater.this.runCallback();
                }
            }.runTask(this.plugin);
        }
    }
    
    private void runCallback() {
        this.callback.onFinish(this);
    }
    
    static {
        NO_UPDATE_TAG = new String[] { "-DEV", "-PRE", "-SNAPSHOT" };
    }
    
    public enum UpdateResult
    {
        SUCCESS, 
        NO_UPDATE, 
        DISABLED, 
        FAIL_DOWNLOAD, 
        FAIL_DBO, 
        FAIL_NOVERSION, 
        FAIL_BADID, 
        FAIL_APIKEY, 
        UPDATE_AVAILABLE;
    }
    
    public enum UpdateType
    {
        DEFAULT, 
        NO_VERSION_CHECK, 
        NO_DOWNLOAD;
    }
    
    public enum ReleaseType
    {
        ALPHA, 
        BETA, 
        RELEASE;
    }
    
    private class UpdateRunnable implements Runnable
    {
        @Override
        public void run() {
            Updater.this.runUpdater();
        }
    }
    
    public interface UpdateCallback
    {
        void onFinish(final Updater p0);
    }
}
