package com.opentext.bn.content.lens.models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Processes {

    public List<Process> processes;

    public void addProcess (Process process) {
        if (processes == null) {
            processes = new ArrayList<Process>();
        }
        processes.add(process);
    }

	public List<Process> getProcesses() {
		return processes;
	}

	public void setProcesses(List<Process> processes) {
		this.processes = processes;
	}
    
    
}