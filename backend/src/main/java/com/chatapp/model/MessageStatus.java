package com.chatapp.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the delivery status of a message.
 * <p>
 * Status lifecycle:
 * 1. SENT - Message has been sent by the sender
 * 2. DELIVERED - Message has been delivered to the recipient's client
 * 3. READ - Message has been read/viewed by the recipient
 */
@Schema(description = "Status of message delivery and reading")
public enum MessageStatus {

    @Schema(description = "Message has been sent but not yet delivered to recipient")
    SENT,

    @Schema(description = "Message has been delivered to recipient but not yet read")
    DELIVERED,

    @Schema(description = "Message has been read by the recipient")
    READ
}
