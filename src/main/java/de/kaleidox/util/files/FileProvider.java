package de.kaleidox.util.files;

import java.io.File;
import java.io.IOException;

public class FileProvider {
    private final static String UNIX_PREPATH = "/var/bots/tzero/";

    public static File getFile(String subPath) {
        if (OSValidator.isUnix()) {
            File file = new File(UNIX_PREPATH + subPath);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return file;
        }
        File file = new File(subPath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }
}
