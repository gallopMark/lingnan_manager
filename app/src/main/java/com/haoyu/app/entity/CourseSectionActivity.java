package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CourseSectionActivity implements Serializable {
    private static final long serialVersionUID = 1L;
    @Expose
    @SerializedName("id")
    private String id;
    @Expose
    @SerializedName("relation")
    private Relation relation;
    @Expose
    @SerializedName("state")
    private String state;
    @Expose
    @SerializedName("title")
    private String title;
    @Expose
    @SerializedName("type")
    private String type;
    @Expose
    @SerializedName("completeState")
    private String completeState;
    @Expose
    @SerializedName("lastViewTime")
    private double lastViewTime;
    @Expose
    @SerializedName("timing")
    private boolean timing;
    @Expose
    @SerializedName("mVideo")
    private VideoMobileEntity mVideo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Relation getRelation() {
        return relation;
    }

    public void setRelation(Relation relation) {
        this.relation = relation;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCompleteState() {
        return completeState;
    }

    public void setCompleteState(String completeState) {
        this.completeState = completeState;
    }

    public double getLastViewTime() {
        return lastViewTime;
    }

    public void setLastViewTime(double lastViewTime) {
        this.lastViewTime = lastViewTime;
    }

    public boolean isTiming() {
        return timing;
    }

    public void setTiming(boolean timing) {
        this.timing = timing;
    }

    public VideoMobileEntity getmVideo() {
        return mVideo;
    }

    public void setmVideo(VideoMobileEntity mVideo) {
        this.mVideo = mVideo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CourseSectionActivity) {
            CourseSectionActivity activity = (CourseSectionActivity) obj;
            if (activity.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }
}