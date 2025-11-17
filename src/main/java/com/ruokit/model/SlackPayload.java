package com.ruokit.model;

public class SlackPayload {

    private final String text;

    public SlackPayload(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
