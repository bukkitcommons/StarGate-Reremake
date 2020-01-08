package com.RkCraft.Stargate;

import lombok.Cleanup;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.logging.*;
import java.io.*;
import java.util.*;

public class LangLoader
{
    private final String UTF8_BOM = "\ufeff";
    private final File datFolder;
    private String lang;
    private HashMap<String, String> strList;
    private final HashMap<String, String> defList;
    
    public LangLoader(final File datFolder, final String lang) {
        this.lang = lang;
        this.datFolder = datFolder;
        final File tmp = new File(datFolder, lang + ".txt");
        if (!tmp.exists()) {
            tmp.getParentFile().mkdirs();
        }
        this.updateLanguage(lang);
        this.strList = this.load(lang);
        final InputStream is = Stargate.class.getResourceAsStream("resources/" + lang + ".txt");
        if (is != null) {
            this.defList = this.load("en", is);
        }
        else {
            this.defList = null;
            Stargate.log.severe("[Stargate] Error loading backup language. There may be missing text ingame");
        }
    }
    
    public boolean reload() {
        this.updateLanguage(this.lang);
        this.strList = this.load(this.lang);
        return true;
    }
    
    public String getString(final String name) {
        String val = this.strList.get(name);
        if (val == null && this.defList != null) {
            val = this.defList.get(name);
        }
        if (val == null) {
            return "";
        }
        return val;
    }
    
    public void setLang(final String lang) {
        this.lang = lang;
    }
    
    private void updateLanguage(final String lang) {
        final ArrayList<String> keyList = new ArrayList<>();
        final ArrayList<String> valList = new ArrayList<>();
        final HashMap<String, String> curLang = this.load(lang);
        final InputStream is = Stargate.class.getResourceAsStream("resources/" + lang + ".txt");
        if (is == null) {
            return;
        }
        boolean updated = false;
        FileOutputStream fos = null;
        try {
            final InputStreamReader isr = new InputStreamReader(is);
            try (final BufferedReader br = new BufferedReader(isr)) {
                String line = br.readLine();
                boolean firstLine = true;
                while (line != null) {
                    if (firstLine) {
                        line = this.removeUTF8BOM(line);
                    }
                    firstLine = false;
                    final int eq = line.indexOf(61);
                    if (eq == -1) {
                        keyList.add("");
                        valList.add("");
                        line = br.readLine();
                    }
                    else {
                        final String key = line.substring(0, eq);
                        final String val = line.substring(eq);
                        if (curLang.get(key) == null) {
                            keyList.add(key);
                            valList.add(val);
                            updated = true;
                        }
                        else {
                            keyList.add(key);
                            valList.add("=" + curLang.get(key));
                            curLang.remove(key);
                        }
                        line = br.readLine();
                    }
                }
            }
            fos = new FileOutputStream(this.datFolder + lang + ".txt");
            final OutputStreamWriter out = new OutputStreamWriter(fos,  StandardCharsets.UTF_8);
            try (final BufferedWriter bw = new BufferedWriter(out)) {
                for (int i = 0; i < keyList.size(); ++i) {
                    bw.write(keyList.get(i) + valList.get(i));
                    bw.newLine();
                }
                bw.newLine();
                for (final String key2 : curLang.keySet()) {
                    bw.write(key2 + "=" + curLang.get(key2));
                    bw.newLine();
                }
            }
        }
        catch (Exception ignored) {}
        finally {
            if (fos != null) {
                try {
                    fos.close();
                }
                catch (Exception ignored) {}
            }
        }
        if (updated) {
            Stargate.log.log(Level.INFO, "[Stargate] Your language file ({0}.txt) has been updated", lang);
        }
    }
    
    private HashMap<String, String> load(final String lang) {
        return this.load(lang, null);
    }
    @SneakyThrows
    private HashMap<String, String> load(final String lang, final InputStream is) {
        final HashMap<String, String> strings = new HashMap<>();
        @Cleanup
        InputStreamReader isr = null;
        if(is == null){
            //Load from file
            @Cleanup
            FileInputStream fileInputStream = new FileInputStream(new File(this.datFolder , lang + ".txt"));
            isr = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
        }else{
            isr = new InputStreamReader(is, StandardCharsets.UTF_8);
        }
        final BufferedReader br = new BufferedReader(isr);
        String line = br.readLine();
        int cursorLine = 1;
        while (line != null) {
            if(cursorLine == 1) {
                line = this.removeUTF8BOM(line);
            }
            final int eq = line.indexOf(61);
            if (eq == -1) {
                line = br.readLine();
            }else {
                final String key = line.substring(0, eq);
                final String val = line.substring(eq + 1);
                strings.put(key, val);
                line = br.readLine();
            }
            cursorLine++;
        }
        return strings;
    }
    
    public void debug() {
        Set<String> keys = this.strList.keySet();
        for (final String key : keys) {
            Stargate.debug("LangLoader::Debug::strList", key + " => " + this.strList.get(key));
        }
        if (this.defList == null) {
            return;
        }
        keys = this.defList.keySet();
        for (final String key : keys) {
            Stargate.debug("LangLoader::Debug::defList", key + " => " + this.defList.get(key));
        }
    }
    
    private String removeUTF8BOM(String s) {
        if (s.startsWith("\ufeff")) {
            s = s.substring(1);
        }
        return s;
    }
}
