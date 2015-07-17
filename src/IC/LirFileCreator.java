package IC;

import java.io.*;

/**
 * LirFileCreator class to generate the .lir file
 * for the microLir emulator
 */
public class LirFileCreator {

    private final String lirFileName;
    private final String lirOutput;

    public LirFileCreator(String fileName, String output) {
        String path = fileName.substring(0, fileName.lastIndexOf("\\") + 1);
        String name = fileName.substring(fileName.lastIndexOf("\\") + 1, fileName.lastIndexOf("."));
        File file = new File(path + "output\\");

        final boolean b = !file.exists() && file.mkdirs();

        this.lirFileName = path + "output\\" + name + ".lir";
        this.lirOutput = output;
    }

    public String getFilePath() {
        return this.lirFileName;
    }

    public void createFile() {
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lirFileName), "utf-8"));
            writer.write(lirOutput);
        } catch (IOException ex) {
            System.err.println("Error writing to file");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch(Exception ex){
                    /* ignore */
                }
            }
        }
    }
}