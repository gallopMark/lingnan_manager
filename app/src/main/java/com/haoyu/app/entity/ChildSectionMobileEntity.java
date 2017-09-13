package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChildSectionMobileEntity implements Serializable {
	@Expose
	@SerializedName("activities")
	private List<CourseSectionActivity> activities = new ArrayList<CourseSectionActivity>();
	@Expose
	@SerializedName("id")
	private String id;
	@Expose
	@SerializedName("state")
	private String state;
	@Expose
	@SerializedName("completeState")
	private String completeState;
	@Expose
	@SerializedName("title")
	private String title;

	public List<CourseSectionActivity> getActivities() {
		return activities;
	}

	public void setActivities(List<CourseSectionActivity> activities) {
		this.activities = activities;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCompleteState() {
		return completeState;
	}

	public void setCompleteState(String completeState) {
		this.completeState = completeState;
	}
}