/**
 * Summary of public methods that can be called:
 * 
 * getStorage();
 * readTasks();
 * writeTasks(ArrayList<ArrayList<Task>> tasks);
 * 
 * getFileDirectory();
 * getFileName();
 * 
 * setFileDirectory(String fileDirectory);
 * setFileName(String fileName);
 */
package main.storage;

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
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
	
	String fileName = null;
	String filePath = null;

	private Storage() {
	    readUserSettings();
	}

	public static synchronized Storage getStorage() {
		if (storage == null) {
			storage = new Storage();
		}
		return storage;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	
	public ArrayList<ArrayList<Task>> readTasks() {
	    ArrayList<ArrayList<Task>> tasks = null;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			Gson gson = new GsonBuilder().create();
	        tasks = gson.fromJson(reader,
	                new TypeToken<ArrayList<ArrayList<Task>>>() {
	                }.getType());
	        logger.log(Level.INFO,"Successfully read tasks from: " + fileName);
		} catch (Exception e) {
		    ArrayList<ArrayList<Task>> allTasks = new ArrayList<ArrayList<Task>>();
            allTasks.add(new ArrayList<Task>());
            allTasks.add(new ArrayList<Task>());
			tasks = allTasks;
			logger.log(Level.INFO,"Application clean start. Tasks will be saved in: " + DEFAULT_FILE_NAME);
		}
		assert(tasks != null);
		return tasks;
	}

	public void writeTasks(ArrayList<ArrayList<Task>> tasks) throws FileNotFoundException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(
				filePath), "UTF-8")) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(tasks, writer);
			logger.log(Level.INFO,"Successfully saved tasks to: " + fileName);
		} catch (Exception e) {
			logger.log(Level.INFO,"Error while saving tasks to: " + fileName);
			setFileLocation(DEFAULT_FILE_NAME);
			writeTasks(tasks);
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
	
	public void setFileLocation(String path) {
	    filePath = path;
	    updateFileName(path);
	    try(PrintWriter out = new PrintWriter(USER_SETTINGS)){
            out.print(path);
            out.close();
            logger.log(Level.INFO,"Updated user settings with: " + path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	    assert((new File(USER_SETTINGS)).exists());
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
	
	public String getFileLocation() {
	    return filePath;
	}
	
	public void readUserSettings() {
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
                fileName = DEFAULT_FILE_NAME;
                setFileLocation(getDefaultFilePath());
                logger.log(Level.INFO,"Corrupted file location from settings.txt. Set as default location.");
            }
        } catch (Exception e) {
            fileName = DEFAULT_FILE_NAME;
            setFileLocation(getDefaultFilePath());
            logger.log(Level.INFO,"Corrupted file location from settings.txt. Set as default location.");
        }
	    assert(fileName != null);
	}
	
	private boolean validFilePath(String path) {
        File file = new File(path);
        if (file.exists()) {
            return true;
        } else {
            return false;
        }
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
