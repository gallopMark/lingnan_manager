package com.haoyu.app.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.haoyu.app.base.BaseResponseResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AnnouncementListResult extends BaseResponseResult<AnnouncementListResult.AnnouncementListResponseData>{
    public class AnnouncementListResponseData implements Serializable {
        @Expose
        @SerializedName("announcements")
        private List<AnnouncementEntity> announcements = new ArrayList<>();
        @Expose
        @SerializedName("paginator")
        private Paginator paginator;

        public List<AnnouncementEntity> getAnnouncements() {
            return announcements;
        }

        public void setAnnouncements(List<AnnouncementEntity> announcements) {
            this.announcements = announcements;
        }

        public Paginator getPaginator() {
            return paginator;
        }

        public void setPaginator(Paginator paginator) {
            this.paginator = paginator;
        }
    }
}
