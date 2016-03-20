/**
 * @author Bevin Seetoh Jia Jin
 *
 */

package test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.storage.Storage;

public class TestStorage {
    
    Storage storage;
    
    @Test
    public void readUserSettingsTest() {
        storage.setFileLocation("?", new ArrayList<Task>());
        storage = null;
        storage = Storage.getStorage();
        storage.setFileLocation("storage.txt", new ArrayList<Task>());
        storage = null;
        storage = Storage.getStorage();
        storage.setFileLocation("<><>///\\:?||", new ArrayList<Task>());
        storage = null;
        storage = Storage.getStorage();
    }
    
    @Test
    public void storageCloneTest() {
        try {
            storage.clone();
        } catch (CloneNotSupportedException e) {
        }
    }
    
    @Test
    public void readTest() {
        assertNotNull(storage.readTasks());
        storage.writeTasks(null);
        assertNotNull(storage.readTasks());
        storage.setFileLocation("storage.txt", new ArrayList<Task>());
        ArrayList<Task> tasks = new ArrayList<Task>();
        storage.writeTasks(tasks);
        assertNotNull(storage.readTasks());
        tasks.add(new Task("test"));
        storage.writeTasks(tasks);
        assertNotNull(storage.readTasks());
        
        try(PrintWriter out = new PrintWriter("storage.txt")){
            out.print("!");
            out.close();
        } catch (Exception e) {
        }
        assertNotNull(storage.readTasks());
        storage.setFileLocation("<><>///\\:?||", new ArrayList<Task>());
        storage = null;
        storage = Storage.getStorage();
        
        File file = new File("settings.txt");
        file.delete();
        storage = null;
        storage = Storage.getStorage();
    }
    
    @Test
    public void writeTest() {
        assertNotNull(storage.getFilePath());
    }
    
    @Before
    public void initialize() {
        storage = Storage.getStorage();
    }

}
