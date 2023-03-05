/*
 * Homework 08
 * Message 
 * Group12_HW08
 * Samba Diagne
 * Chris Overcash
 */

package edu.uncc.hw08;

import java.io.Serializable;


public class Message implements Serializable {
    public String messageBody, receiver, sender, messageID;
    public String sendAt;
    public Message() {

    }


    public Message(String message, String receiver, String sender, String sendAt, String id) {
        this.messageID = id;
        this.messageBody = message;
        this.receiver = receiver;
        this.sender = sender;
        this.sendAt = sendAt;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getMessage() {
        return messageBody;
    }

    public void setMessage(String message) {
        this.messageBody = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSendAt() {
        return sendAt;
    }

    public void setSendAt(String sendAt) {
        this.sendAt = sendAt;
    }


}
