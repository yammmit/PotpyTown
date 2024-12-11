package com.example.potpytown.models;

public class UserMission {
    private String mission_id;
    private String mission_type;
    private int mission_value;
    private int progress; // 사용자 진행 상황 (0으로 초기화)
    private int receive_reward;
    private String description;
    private String status; // 상태 필드
    private String user_id; // 사용자 ID

    // 기본 생성자
    public UserMission() {}

    // Getter와 Setter 추가
    public String getMission_id() {
        return mission_id;
    }

    public void setMission_id(String mission_id) {
        this.mission_id = mission_id;
    }

    public String getMission_type() {
        return mission_type;
    }

    public void setMission_type(String mission_type) {
        this.mission_type = mission_type;
    }

    public int getMission_value() {
        return mission_value;
    }

    public void setMission_value(int mission_value) {
        this.mission_value = mission_value;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getReceive_reward() {
        return receive_reward;
    }

    public void setReceive_reward(int receive_reward) {
        this.receive_reward = receive_reward;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
