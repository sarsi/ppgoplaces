/*
 * file browser object for directory path and name
 */

package com.liseem.android.travel.items;

public class FileOption implements Comparable<FileOption> {

	    private String name;
	    private String data;
	    private String path;
	    
	    public FileOption(String name,String data,String path) {
	        this.name = name;
	        this.data = data;
	        this.path = path;
	    }
	    
	    public String getName() {
	        return name;
	    }
	    
	    public String getData() {
	        return data;
	    }
	    
	    public String getPath() {
	        return path;
	    }
	    
	    @Override
	    public int compareTo(FileOption option) {
	        if (this.name != null)
	            return this.name.toLowerCase().compareTo(option.getName().toLowerCase()); 
	        else 
	            throw new IllegalArgumentException();
	    }
	
}
