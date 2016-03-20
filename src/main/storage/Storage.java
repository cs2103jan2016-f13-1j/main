package main.storage;

/**
 * This is a singleton class. This class is used to read/write to
 * an output file. 
 * 
 * You can expect to see two files upon initializing this class:
 * 1. settings.txt (Stores the path of the output file - Default: storage.txt)
 * 2. storage.txt (Default file name)
 */

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import main.data.Task;

public class Storage {
    
    private static final Logger logger = Logger.getLogger(Storage.class.getName());
    
	private static Storage storage;
	private String OS = System.getProperty("os.name").toLowerCase();
	
	private final String USER_SETTINGS = "settings.txt";
	private final String USER_DIR = System.getProperty("user.dir");
	private final String DEFAULT_FILE_NAME = "storage.txt";
	
	private final String WINDOWS_DIR_SYMBOL = "\\";
	private final String MAC_DIR_SYMBOL = "/";
	private final String UNIX_DIR_SYMBOL = "/";
	private final String SOLARIS_DIR_SYMBOL = "/";
	
	private final String FILE_PATH_FORMAT = "%s%s%s";
	
	private String fileName = null;
	private String filePath = null;

	private Storage() {
	    readUserSettings();
	}
	
	/**
	 * A static method to initialize an instance of the {@code Storage} class.
	 * 
	 * @return   An instance of the {@code Storage} class
	 */
	public static synchronized Storage getStorage() {
		if (storage == null) {
			storage = new Storage();
		}
		return storage;
	}
	
	/**
	 * Prevents attempts to clone this singleton class.
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	/**
	 * Accesses the output file, read and build the data into {@code Task} objects.
	 * 
	 * @return   A {@code ArrayList} of {@code Tasks}
	 */
	public ArrayList<Task> readTasks() {
        ArrayList<Task> tasks = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            Gson gson = new GsonBuilder().create();
            tasks = gson.fromJson(reader,
                    new TypeToken<ArrayList<Task>>() {
                    }.getType());
            reader.close();
            
            if (tasks.isEmpty()) {
                logger.log(Level.WARNING,"ERROR READING FILE: " + fileName);
                throw new Exception("ERROR READING FILE");
            }
            logger.log(Level.INFO,"Successfully read tasks from: " + fileName);
        } catch (Exception e) {
            tasks = new ArrayList<Task>();
            logger.log(Level.INFO,"Application clean start.");
        }
        assert(tasks != null);
        return tasks;
    }
	
	/**
	 * This method takes in an {@code ArrayList} of {@code Tasks} and writes
	 * them into the output file.
	 * 
	 * @param  tasks
	 *         The {@code ArrayList} of {@code Tasks} to write
	 */
    public void writeTasks(ArrayList<Task> tasks) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(
                filePath), "UTF-8")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(tasks, writer);
            logger.log(Level.INFO,"Saved tasks to: " + fileName);
        } catch (Exception e) {
            logger.log(Level.INFO,"Corrupted file location: " + filePath);
            setFileLocation(DEFAULT_FILE_NAME, tasks);
        }
        assert((new File(filePath)).exists());
    }
	
	private String getDefaultFilePath() {
	    String path = null;
		if (isWindows()) {
			path = String.format(FILE_PATH_FORMAT,USER_DIR,WINDOWS_DIR_SYMBOL,DEFAULT_FILE_NAME);
        } else if (isMac()) {
        	path = String.format(FILE_PATH_FORMAT,USER_DIR,MAC_DIR_SYMBOL,DEFAULT_FILE_NAME);
        } else if (isUnix()) {
        	path = String.format(FILE_PATH_FORMAT,USER_DIR,UNIX_DIR_SYMBOL,DEFAULT_FILE_NAME);
        } else if (isSolaris()) {
        	path = String.format(FILE_PATH_FORMAT,USER_DIR,SOLARIS_DIR_SYMBOL,DEFAULT_FILE_NAME);
        } else {
            path = String.format(FILE_PATH_FORMAT,USER_DIR,UNIX_DIR_SYMBOL,DEFAULT_FILE_NAME);
            logger.log(Level.INFO,"OS not detected, assume UNIX OS");
        }
		assert(path != null);
		return path;
	}
	
	/**
	 * This methods updates file path and stores it in the settings file.
	 * The settings file can be found in the application folder, settings.txt.
	 * 
	 * @param  path
	 *         The path to save the output file
	 * @param  tasks
	 *         The {@code ArrayList} of {@code Task} to write to the new path
	 */
	public void setFileLocation(String path, ArrayList<Task> tasks) {
	    filePath = path;
	    updateFileName(path);
	    try(PrintWriter out = new PrintWriter(USER_SETTINGS)){
            out.print(path);
            out.close();
            logger.log(Level.INFO,"Updated user settings with: " + path);
        } catch (Exception e) {
        	logger.log(Level.WARNING,"Failed to write to: " + path);
        }
	    assert((new File(USER_SETTINGS)).exists());
	    writeTasks(tasks);
	}
	
	private void updateFileName(String path) {
	    if (isWindows()) {
	        fileName = path.substring(path.lastIndexOf(WINDOWS_DIR_SYMBOL) + 1);
	    } else if (isMac()) {
	        fileName = path.substring(path.lastIndexOf(MAC_DIR_SYMBOL) + 1);
        } else if (isUnix()) {
            fileName = path.substring(path.lastIndexOf(UNIX_DIR_SYMBOL) + 1);
        } else if (isSolaris()) {
            fileName = path.substring(path.lastIndexOf(SOLARIS_DIR_SYMBOL) + 1);
        } else {
            fileName = DEFAULT_FILE_NAME;
        }
	    assert(fileName != null);
	}
	
	/**
	 * This method returns the current path of the output file.
	 * 
	 * @return   A {@code String} indicating the file path
	 */
	public String getFilePath() {
	    return filePath;
	}
	
	private void readUserSettings() {
	    try {
            Scanner sc = new Scanner(new File(USER_SETTINGS));
            String path = sc.nextLine().trim();
            sc.close();
            logger.log(Level.INFO,"Read file location from settings.txt: " + path);
            if (validFilePath(path)) {
                filePath = path;
                updateFileName(path);
                logger.log(Level.INFO,"Valid file location: " + path);
            } else {
                logger.log(Level.INFO,"Invalid file location: " + path);
                throw new InvalidPathException(path, "Invalid file location");
            }
        } catch (Exception e) {
            fileName = DEFAULT_FILE_NAME;
            setFileLocation(getDefaultFilePath(), new ArrayList<Task>());
            logger.log(Level.INFO,"Corrupted file location from settings.txt. Set as default location.");
        }
	    assert(fileName != null);
	}
	
	private boolean validFilePath(String path) {
        File file = new File(path);
        return file.exists();
	}
	
	private boolean isWindows() {
		return (OS.contains("win"));
	}
	
	private boolean isMac() {
		return (OS.contains("mac"));
	}
	
	private boolean isUnix() {
		return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
	}
	
	private boolean isSolaris() {
		return (OS.contains("sunos"));
	}

}
