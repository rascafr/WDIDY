package com.wdidy.app.drawer;

// Model for our sidelist
public class NavDrawerItem {

    private String title;
    private String id;
	private String moreData;
    private boolean isProfile;
	private int icon;
	private String count = "0";
	// boolean to set visiblity of the counter
	private boolean isCounterVisible = false;
	
	public NavDrawerItem(){}

	public NavDrawerItem(String title, int icon){
		this.title = title;
		this.icon = icon;
        isProfile = false;
	}

    public NavDrawerItem(String title, String id){
        this.title = title;
        this.id = id;
        isProfile = true;
    }

	public NavDrawerItem(String title, int icon, boolean isCounterVisible, String count){
		this.title = title;
		this.icon = icon;
		this.isCounterVisible = isCounterVisible;
		this.count = count;
	}

	public String getMoreData() {
		return moreData;
	}

	public void setMoreData(String moreData) {
		this.moreData = moreData;
	}

	public boolean isEmpty() {
		return title.length() == 0;
	}
	
	public String getTitle(){
		return this.title;
	}



    public String getId() { return this.id; }
	
	public int getIcon(){
		return this.icon;
	}
	
	public String getCount(){
        return this.count;
	}

    public boolean isProfile() { return this.isProfile; }
	
	public boolean getCounterVisibility(){
		return this.isCounterVisible;
	}
	
	public void setTitle(String title){
		this.title = title;
	}

    public void setId(String id){
        this.id = id;
    }

	public void setIcon(int icon){
		this.icon = icon;
	}
	
	public void setCount(String count){
		this.count = count;
		if (!count.equals("0"))
			isCounterVisible = true;
		else
			isCounterVisible = false;
	}
	
	public void setCounterVisibility(boolean isCounterVisible){
		this.isCounterVisible = isCounterVisible;
	}
}