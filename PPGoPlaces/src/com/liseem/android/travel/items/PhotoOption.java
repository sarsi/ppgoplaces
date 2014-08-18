/*
 * file browser object for directory path and name
 */

package com.liseem.android.travel.items;

import java.io.File;
import java.io.Serializable;

public class PhotoOption implements Serializable, Comparable<PhotoOption> {

	    private String 		name;
	    private String 		path;
	    private int	  		scale;
	    private int	  		rotate;
	    private boolean	mark;
	    private long		id;
	    
	    
	    //--constructors------------------------------
	    public PhotoOption(File file) {
	        this.name = file.getName();
	        this.path = file.getAbsolutePath();
	    }
	    
	    public PhotoOption() {
	    	
	    }
	    
	   
	    
	    //--Getters and Setters---------------------
	    
	    public boolean isMark() {
	    	return mark;
	    }
	    
	    public void setMark(boolean checked) {
	    	mark = checked;
	    }
	    
	    public long getId() {
	    	return id;
	    }
	    
	    public void setId(long rowid) {
	    	id = rowid;
	    }
	    
	    public String getName() {
	        return name;
	    }
	    
    
	    public String getPath() {
	        return path;
	    }
	    
	    
	    public int getScale() {
			return scale;
		}

		public void setScale(int scale) {
			this.scale = scale;
		}

		public int getRotate() {
			return rotate;
		}

		public void setRotate(int rotate) {
			this.rotate = rotate;
		}

		public int getOrient() {
			String scaleStr=Integer.toString(scale);
			String orientStr=Integer.toString(rotate);
			StringBuilder orientText=new StringBuilder().append(orientStr.toString()).append(scaleStr.toString());
			int orient=Integer.parseInt(orientText.toString());
			if (scale==0 || rotate ==0) {
				return 0;
			} else {
				return orient;
			}			
		}
		
		@Override
	    public int compareTo(PhotoOption option) {
	        if (this.name != null)
	            return this.name.toLowerCase().compareTo(option.getName().toLowerCase()); 
	        else 
	            throw new IllegalArgumentException();
	    }
	
}
