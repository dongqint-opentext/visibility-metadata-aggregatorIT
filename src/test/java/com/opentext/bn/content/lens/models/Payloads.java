package com.opentext.bn.content.lens.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payloads {
    public List<Payload> payloads;

    public void addPayload (Payload payload) {
        if (payloads == null) {
            payloads = new ArrayList<Payload>();
        }
        payloads.add(payload);
    }

	public List<Payload> getPayloads() {
		return payloads;
	}

	public void setPayloads(List<Payload> payloads) {
		this.payloads = payloads;
	}
    
    
}