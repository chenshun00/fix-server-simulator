package com.fixsimulator.domain.autoresponse;

import com.fixsimulator.domain.message.FixMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Rule {
    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("condition")
    private Condition condition;

    @JsonProperty("actions")
    private List<Action> actions;

    public boolean matches(FixMessage message) {
        return condition.matches(message);
    }
}
