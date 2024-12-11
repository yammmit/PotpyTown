package com.example.potpytown.models;

public class Mission {
    private String mission_id;
    private String mission_type;
    private String description;
    private int mission_value;
    private int receive_reward;

    // 기본 생성자 (Firestore 매핑에 필수)
    public Mission() {}

    // Getter와 Setter
    public String getMissionId() {
        return mission_id;
    }

    public void setMissionId(String mission_id) {
        this.mission_id = mission_id;
    }

    public String getMission_type() {
        return mission_type;
    }

    public void setMission_type(String mission_type) {
        this.mission_type = mission_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMission_value() {
        return mission_value;
    }

    public void setMission_value(int mission_value) {
        this.mission_value = mission_value;
    }

    public int getReceive_reward() {
        return receive_reward;
    }

    public void setReceive_reward(int receive_reward) {
        this.receive_reward = receive_reward;
    }
}
