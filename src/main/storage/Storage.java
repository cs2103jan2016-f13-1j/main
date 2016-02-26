/**
 * 
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

	String fileDirectory = System.getProperty("user.dir");
	String fileName = "storage.txt";
	String filePath = fileDirectory + "\\" + fileName;

	private Storage() {
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
	public void createFile() {
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
	
	public String getFileDirectory() {
		return fileDirectory;
	}
	
	public void setFileDirectory(String directory) {
		this.fileDirectory = directory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
