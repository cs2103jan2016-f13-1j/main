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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import main.data.Task;

public class Storage {

	private static Storage storage;
	private String OS = System.getProperty("os.name").toLowerCase();
	
	private final String USER_SETTINGS = "settings.txt";
	private final String USER_DIR = System.getProperty("user.dir");
	
	private final String WINDOWS_DIR_SYMBOL = "\\";
	private final String MAC_DIR_SYMBOL = "/";
	private final String UNIX_DIR_SYMBOL = "/";
	private final String SOLARIS_DIR_SYMBOL = "/";
	
	private final String FILE_PATH_FORMAT = "%s%s%s";
	
	String fileName = "storage.txt";
	String filePath = null;

	private Storage() {
	    readUserSettings();
		prepareFile();
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
	
	//Open file if it exists, otherwise create a new file
	private void prepareFile() {
		File file = new File(filePath);
		
		try {
			if (!file.exists()) {
				ArrayList<ArrayList<Task>> allTasks = new ArrayList<ArrayList<Task>>();
				ArrayList<Task> emptyTaskList = new ArrayList<Task>();
				allTasks.add(emptyTaskList);
				allTasks.add(emptyTaskList);
				writeTasks(allTasks);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<ArrayList<Task>> readTasks() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Gson gson = new GsonBuilder().create();
		ArrayList<ArrayList<Task>> tasks = gson.fromJson(reader,
				new TypeToken<ArrayList<ArrayList<Task>>>() {
				}.getType());
		return tasks;
	}

	public void writeTasks(ArrayList<ArrayList<Task>> tasks) throws FileNotFoundException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(
				filePath), "UTF-8")) {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			gson.toJson(tasks, writer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getDefaultFilePath() {
	    String path = null;
		if (isWindows()) {
			path = String.format(FILE_PATH_FORMAT,USER_DIR,WINDOWS_DIR_SYMBOL,fileName);
        } else if (isMac()) {
        	path = String.format(FILE_PATH_FORMAT,USER_DIR,MAC_DIR_SYMBOL,fileName);
        } else if (isUnix()) {
        	path = String.format(FILE_PATH_FORMAT,USER_DIR,UNIX_DIR_SYMBOL,fileName);
        } else if (isSolaris()) {
        	path = String.format(FILE_PATH_FORMAT,USER_DIR,SOLARIS_DIR_SYMBOL,fileName);
        } 
        else {
        	System.out.println("OS is not supported");
        }
		return path;
	}
	
	public void setFileLocation(String filePath) {
	    this.filePath = filePath;
	    
	    try(PrintWriter out = new PrintWriter(USER_SETTINGS)){
            out.print(filePath);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}
	
	public String getFileLocation() {
	    return filePath;
	}
	
	public void readUserSettings() {
	    try {
            Scanner sc = new Scanner(new File(USER_SETTINGS));
            String path = sc.nextLine().trim();
            sc.close();
            if (validFilePath(path)) {
                filePath = path;
            } else {
                setFileLocation(getDefaultFilePath());
            }
        } catch (Exception e) {
            setFileLocation(getDefaultFilePath());
        }
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
