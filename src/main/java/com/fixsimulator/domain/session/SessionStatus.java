package com.fixsimulator.domain.session;

import lombok.Getter;

@Getter
public enum SessionStatus {
    CONNECTED,
    DISCONNECTED,
    LOGGED_OUT
}
