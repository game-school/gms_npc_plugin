package idk.plugin.npc.dialogue;

import cn.nukkit.Player;
import idk.plugin.npc.Loader;
import idk.plugin.npc.Log;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UpdateCsv {
    public static String dkChange;
    static HashMap<String, HashMap<String, String>> s_loadedDlgs = new HashMap<>();

    public static String cleanStr(String s) {
        return s;//.replaceAll("[,¬\n\r¦]", "");
    }

    public static HashMap<String, String> loadDialogueFile(String filename) throws IOException {
        Log.debug(String.format("Loading dialogue file: %s", filename));

        if (s_loadedDlgs.containsKey(filename)) {
            Log.debug(String.format("Cached: %s", filename));
            return s_loadedDlgs.get(filename);
        }

        Log.debug(String.format("Loading: %s", filename));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "Cp1252"))) { //May need alternate encoding for other platforms - needs testing (was originally ISO-8859-1)
            HashMap<String, String> dlgs = new HashMap<>();
            String line = br.readLine();

            while (line != null) {
                if (!line.isEmpty()) {
                    String[] attrs = line.split(",");
                    if (attrs.length == 2) {
                        String key = cleanStr(attrs[0]);
                        String value = attrs[1].replace('¬', ',');
                        dlgs.put(key, value);
                        Log.debug(String.format("%s => %s", key, value));
                    }
                }

                line = br.readLine();
            }

            Log.debug(String.format("Loaded and caching: %s", filename));
            s_loadedDlgs.put(filename, dlgs);

            return dlgs;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        }
    }

    public static void saveDialogueFile(String filename, HashMap<String, String> dlgs) {
        try (FileWriter fw = new FileWriter(filename)) {
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            for (Map.Entry<String, String> entry : dlgs.entrySet()) {
                pw.println(String.format("%s,%s", entry.getKey(), entry.getValue()));
            }

            bw.close();
            pw.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     *Add dialogue to the appropriate CSV.
     *
     * @param diaKey Talk text save name
     * @param diaValue Dialogue content
     * @param user Player object to determine world name to append to the filepath
     * @return Boolean - has it worked?
     */
    public static boolean updateDialogue(String diaKey, String diaValue, Player user) {
        try {
            String worldName = Loader.getWorldName(user);
            File filepath = new File(Loader.getPath("dialogue"), worldName+".csv");
            HashMap<String, String> dlgs = loadDialogueFile(filepath.toString());

            dlgs.put(cleanStr(diaKey), cleanStr(diaValue));
            saveDialogueFile(filepath.toString(), dlgs);

            return true;
        }
        catch (IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error during data write! Please ensure the relevant CSV is not currently in use by another application.");
            return false;
        }
    }

    /**
     *Search for a dialogue entry based on the title it's stored under. Returning its value is optional.
     *
     * @param diaKey Talk text save name
     * @param user Player object to determine world name to append to the filepath
     * @param give True = Returns the text value associated with diaKey, False = Returns a keyword to confirm whether the entry exists or not
     * @return A string, either the dialogue found, or a keyword representing the search result, to be checked by Talk
     */
    public static String findDialogue(String diaKey, Player user) throws IOException {
        String dk = cleanStr(diaKey);

        String worldName = Loader.getWorldName(user);
        File filepath = new File(Loader.getPath("dialogue"), worldName + ".csv");

        Log.debug(String.format("Opening file path: %s [Key = %s]", filepath.toString(), diaKey));

        HashMap<String, String> dlgs = loadDialogueFile(filepath.toString());

        if (dlgs.containsKey(dk))
            return dlgs.get(dk);

        return null;
    }

    /**
     *Used for deleting CSV entries, but ultimately only called when overwriting.
     *
     * @param diaKey Talk text save name
     * @param user Player object to determine world name to append to the filepath
     * @return Boolean - has it worked?
     */
    public static boolean deleteDialogue(String diaKey, Player user)
    {
        String worldName = Loader.getWorldName(user);
        File filepath = new File(Loader.getPath("dialogue"), worldName + ".csv");
        String searchKey;
        String rtnDialogue;
        String tbd = diaKey;
        File tempFile = new File(Loader.getPath("dialogue"), worldName + "temp.csv");

        File oldFile = filepath;
        File newFile = tempFile;

        try
        {
            FileWriter fw = new FileWriter(tempFile,true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);
            Scanner scanner = new Scanner(filepath);
            scanner.useDelimiter("[,\n]");

            while (scanner.hasNext())
            {
                searchKey = scanner.next();
                rtnDialogue = scanner.next();

                if (!searchKey.equals(tbd))
                {
                    pw.println(searchKey+","+rtnDialogue);
                }

            }
            scanner.close();
            pw.flush();
            pw.close();
            bw.close();
            fw.close();
            oldFile.delete();
            File dump = filepath;
            newFile.renameTo(dump);

            return true;

        }
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Error during overwrite! Please ensure the relevant CSV is not currently in use by another application.");
            return false;
        }

    }

}
