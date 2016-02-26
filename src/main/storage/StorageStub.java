/**
 * 
 */
package main.storage;

/**
 * @author Quek Yang Sheng
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import main.data.Task;
import main.data.TaskBean;

public class StorageStub {

	private static StorageStub storage;

	String fileDirectory = "./";
	String fileName = "test.json";

	private StorageStub() {
	}

	public static synchronized StorageStub getStorage() {
		if (storage == null) {
			storage = new StorageStub();
		}
		return storage;
	}

	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public ArrayList<TaskBean> readTasks() {
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
		
		//stub method
		ArrayList<TaskBean> taskBeans = new ArrayList<>();
		
		System.out.println(tasks.size());
		for(int i=0; i<tasks.size(); i++){
			Task task = tasks.get(i);
			TaskBean bean = new TaskBean();
			bean.setTitle(task.getTitle());
			taskBeans.add(bean);
		}
		
		return taskBeans;
	}

	public void writeTasks(ArrayList<TaskBean> tasks) throws FileNotFoundException {
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
