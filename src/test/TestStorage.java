/**
 * @author Bevin Seetoh Jia Jin
 *
 */

package test;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import main.data.Task;
import main.storage.Storage;

public class TestStorage {
    
    Storage storage;
    
    /*
     * Tests settings.txt exists but saved path is corrupted
     */
    @Test
    public void readUserSettingsTest() {
        storage.setFileLocation("<><>///\\:?||");
        assertNotNull(storage.readTasks());
    }
    
    /*
     * Test if CloneNotSupportedException is thrown.
     * Singleton classes do not support cloning.
     */
    @Test
    public void storageCloneTest() {
        try {
            storage.clone();
        } catch (CloneNotSupportedException e) {
        }
    }
    
    /*
     * This is a boundary case for writing task.
     * Add zero task.
     * Add one task.
     */
    @Test
    public void readWriteTest() {
        ArrayList<Task> tasks = new ArrayList<Task>();
        storage.writeTasks(tasks);
        assertNotNull(storage.readTasks());
        
        tasks.add(new Task("test"));
        storage.writeTasks(tasks);
        assertNotNull(storage.readTasks());
        storage.writeTasks(new ArrayList<Task>());
    }
    
    /*
     * Tests if there is a file path even if settings.txt 
     * is deleted in @Before.
     */
    @Test
    public void readFilePath() {
        assertNotNull(storage.getFileDir());
    }
    
    /*
     * Tests if settings.txt does not exist
     */
    @Before
    public void initialize() {
        File file = new File("settings.txt");
        if (file.exists()) {
            file.delete();
        }
        storage = Storage.getInstance();
    }

}
