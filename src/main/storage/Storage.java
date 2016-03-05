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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import main.data.Task;

public class Storage {

	private static Storage storage;
	private String OS = System.getProperty("os.name").toLowerCase();
	
	private final String WINDOWS_DIR_SYMBOL = "\\";
	private final String MAC_DIR_SYMBOL = "/";
	private final String UNIX_DIR_SYMBOL = "/";
	private final String SOLARIS_DIR_SYMBOL = "/";
	
	private final String FILE_PATH_FORMAT = "%s%s%s";
	
	String fileDirectory = System.getProperty("user.dir");
	String fileName = "storage.txt";
	String filePath = null;

	private Storage() {
		buildFilePath();
		createFile();
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
	private void createFile() {
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
	
	private void buildFilePath() {
		if (isWindows()) {
			filePath = String.format(FILE_PATH_FORMAT,fileDirectory,WINDOWS_DIR_SYMBOL,fileName);
        } else if (isMac()) {
        	filePath = String.format(FILE_PATH_FORMAT,fileDirectory,MAC_DIR_SYMBOL,fileName);
        } else if (isUnix()) {
        	filePath = String.format(FILE_PATH_FORMAT,fileDirectory,UNIX_DIR_SYMBOL,fileName);
        } else if (isSolaris()) {
        	filePath = String.format(FILE_PATH_FORMAT,fileDirectory,SOLARIS_DIR_SYMBOL,fileName);
        } 
        else {
        	System.out.println("OS is not supported");
        }
	}
	
	public String getFileDirectory() {
		return fileDirectory;
	}
	
	public void setFileDirectory(String directory) {
		this.fileDirectory = directory;
		buildFilePath();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
		buildFilePath();
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
