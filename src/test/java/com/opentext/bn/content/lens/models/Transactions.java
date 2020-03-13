package com.opentext.bn.content.lens.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Transactions {

    boolean partial;
    List<Message> messages;
	public boolean isPartial() {
		return partial;
	}
	public void setPartial(boolean partial) {
		this.partial = partial;
	}
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
    
    

}