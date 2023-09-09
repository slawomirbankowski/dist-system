package com.distsystem.api.enums;

/** Callbacks on messages - all events that can happen to single message */
public enum DistCallbackType {
    onResponse,
    onTimeout,
    onError,
    onClientNotFound,
    onSend
}
