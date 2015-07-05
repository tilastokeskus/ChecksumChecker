package checksumchecker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChecksumChecker {
    
    public static final Logger LOGGER = Logger.getLogger(ChecksumChecker.class.getName());

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        String algorithm = "MD5";
        boolean recursive = false;
        String outputFilePath = "checksum.txt";
        
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-")) {
                if (arg.equals("-o")) outputFilePath = args[++i];
                if (arg.equals("-r")) recursive = true;
                if (arg.equals("-MD5")) algorithm = "MD5";
                if (arg.equals("-SHA1")) algorithm = "SHA1";
            } else {
                paths.add(arg);
            }
        }
        
        List<File> files = new ArrayList<>();
        for (String path : paths) {
            File file = new File(path);
            if (!file.exists()) {
                LOGGER.log(Level.SEVERE, null, "Path does not exist: " + path);
            } else if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                files.addAll(getFilesInFolder(file, recursive));
            }
        }
        
        printChecksums(files, algorithm, new File(outputFilePath));
    }

    private static void printChecksums(List<File> files, String algorithm, File output) {
        try {
            PrintWriter writer = new PrintWriter(output);
        
            for (File file : files) {
                try {
                    writer.println(file.getName() + ": " + getChecksum(file, algorithm));
                    System.out.println(file.getName() + ": " + getChecksum(file, algorithm));
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }
            
            writer.close();
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    private static List<File> getFilesInFolder(final File root, boolean recursive) {
        List<File> files = new ArrayList<>();
        List<File> folders = new ArrayList<>();
        folders.add(root);
        
        for (int i = 0; i < folders.size(); i++) {
            File folder = folders.get(i);
            for (final File fileEntry : folder.listFiles()) {
                if (fileEntry.isDirectory() && recursive) {
                    folders.add(fileEntry);
                } else if (fileEntry.isFile()) {
                    files.add(fileEntry);
                }
            }
        }
        
        return files;
    }
    
    private static String getChecksum(File file, String algorithm) throws Exception {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        FileInputStream fis = new FileInputStream(file);
        byte[] dataBytes = new byte[1024];

        int nread = 0; 

        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }

        byte[] mdbytes = md.digest();

        //convert the byte to hex format
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
    }
    
}
