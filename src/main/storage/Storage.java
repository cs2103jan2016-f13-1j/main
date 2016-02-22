/**
 * 
 */
package main.storage;

/**
 * @author Bevin Seetoh Jia Jin
 *
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;

import main.data.Task;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class Storage {

	private static Storage storage;

	String fileDirectory = "C:\\Users\\Admin1\\Desktop\\Workspace\\Dooleh\\";
	String fileName = "test.json";

	private Storage() {
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

	public ArrayList<Task> readTasks() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(fileDirectory + fileName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Gson gson = new GsonBuilder().create();
		ArrayList<Task> tasks = gson.fromJson(reader,
				new TypeToken<ArrayList<Task>>() {
				}.getType());
		return tasks;
	}

	public void writeTasks(ArrayList<Task> tasks) throws FileNotFoundException {
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(
				fileDirectory + fileName), "UTF-8")) {
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
