package com.opentext.bn.content.lens.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

    private String id;

    private String adminonly;

    private long version;

    private String publishedtime;

    private String arrivaltime;

    private String packagemessageid;

    private String messagedetailsavailable;

    private String msgtype;

    private String msgtypecode;

    private String inagent;

    private String outagent;

    private String sendername;

    private String senderid;

    private String senderparentid;

    private String senderchildid;

    private String receivername;

    private String receiverid;

    private String receiverparentid;

    private String receiverchildid;

    private String sendermsgid;

    private String status;

    private String statusdate;

    private Integer processingtime;

    private String fastatus;

    private String fastatusdate;

    private String priority;

    private String pairwiseid;

    private String category;

    private String productionstatus;

    private String informat;

    private String outformat;

    private Integer insize;

    private Integer outsize;

    private String senderresellerid;

    private String receiverresellerid;

    private String messagelevel;

    private List<Map<String, String>> additionalinformation = new ArrayList<Map<String, String>>();

    private List<String> relatedmsg = new ArrayList<>();
    
    private List<String> owners = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAdminonly() {
		return adminonly;
	}

	public void setAdminonly(String adminonly) {
		this.adminonly = adminonly;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getPublishedtime() {
		return publishedtime;
	}

	public void setPublishedtime(String publishedtime) {
		this.publishedtime = publishedtime;
	}

	public String getArrivaltime() {
		return arrivaltime;
	}

	public void setArrivaltime(String arrivaltime) {
		this.arrivaltime = arrivaltime;
	}

	public String getPackagemessageid() {
		return packagemessageid;
	}

	public void setPackagemessageid(String packagemessageid) {
		this.packagemessageid = packagemessageid;
	}

	public String getMessagedetailsavailable() {
		return messagedetailsavailable;
	}

	public void setMessagedetailsavailable(String messagedetailsavailable) {
		this.messagedetailsavailable = messagedetailsavailable;
	}

	public String getMsgtype() {
		return msgtype;
	}

	public void setMsgtype(String msgtype) {
		this.msgtype = msgtype;
	}

	public String getMsgtypecode() {
		return msgtypecode;
	}

	public void setMsgtypecode(String msgtypecode) {
		this.msgtypecode = msgtypecode;
	}

	public String getInagent() {
		return inagent;
	}

	public void setInagent(String inagent) {
		this.inagent = inagent;
	}

	public String getOutagent() {
		return outagent;
	}

	public void setOutagent(String outagent) {
		this.outagent = outagent;
	}

	public String getSendername() {
		return sendername;
	}

	public void setSendername(String sendername) {
		this.sendername = sendername;
	}

	public String getSenderid() {
		return senderid;
	}

	public void setSenderid(String senderid) {
		this.senderid = senderid;
	}

	public String getSenderparentid() {
		return senderparentid;
	}

	public void setSenderparentid(String senderparentid) {
		this.senderparentid = senderparentid;
	}

	public String getSenderchildid() {
		return senderchildid;
	}

	public void setSenderchildid(String senderchildid) {
		this.senderchildid = senderchildid;
	}

	public String getReceivername() {
		return receivername;
	}

	public void setReceivername(String receivername) {
		this.receivername = receivername;
	}

	public String getReceiverid() {
		return receiverid;
	}

	public void setReceiverid(String receiverid) {
		this.receiverid = receiverid;
	}

	public String getReceiverparentid() {
		return receiverparentid;
	}

	public void setReceiverparentid(String receiverparentid) {
		this.receiverparentid = receiverparentid;
	}

	public String getReceiverchildid() {
		return receiverchildid;
	}

	public void setReceiverchildid(String receiverchildid) {
		this.receiverchildid = receiverchildid;
	}

	public String getSendermsgid() {
		return sendermsgid;
	}

	public void setSendermsgid(String sendermsgid) {
		this.sendermsgid = sendermsgid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatusdate() {
		return statusdate;
	}

	public void setStatusdate(String statusdate) {
		this.statusdate = statusdate;
	}

	public Integer getProcessingtime() {
		return processingtime;
	}

	public void setProcessingtime(Integer processingtime) {
		this.processingtime = processingtime;
	}

	public String getFastatus() {
		return fastatus;
	}

	public void setFastatus(String fastatus) {
		this.fastatus = fastatus;
	}

	public String getFastatusdate() {
		return fastatusdate;
	}

	public void setFastatusdate(String fastatusdate) {
		this.fastatusdate = fastatusdate;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getPairwiseid() {
		return pairwiseid;
	}

	public void setPairwiseid(String pairwiseid) {
		this.pairwiseid = pairwiseid;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getProductionstatus() {
		return productionstatus;
	}

	public void setProductionstatus(String productionstatus) {
		this.productionstatus = productionstatus;
	}

	public String getInformat() {
		return informat;
	}

	public void setInformat(String informat) {
		this.informat = informat;
	}

	public String getOutformat() {
		return outformat;
	}

	public void setOutformat(String outformat) {
		this.outformat = outformat;
	}

	public Integer getInsize() {
		return insize;
	}

	public void setInsize(Integer insize) {
		this.insize = insize;
	}

	public Integer getOutsize() {
		return outsize;
	}

	public void setOutsize(Integer outsize) {
		this.outsize = outsize;
	}

	public String getSenderresellerid() {
		return senderresellerid;
	}

	public void setSenderresellerid(String senderresellerid) {
		this.senderresellerid = senderresellerid;
	}

	public String getReceiverresellerid() {
		return receiverresellerid;
	}

	public void setReceiverresellerid(String receiverresellerid) {
		this.receiverresellerid = receiverresellerid;
	}

	public String getMessagelevel() {
		return messagelevel;
	}

	public void setMessagelevel(String messagelevel) {
		this.messagelevel = messagelevel;
	}

	public List<Map<String, String>> getAdditionalinformation() {
		return additionalinformation;
	}

	public void setAdditionalinformation(List<Map<String, String>> additionalinformation) {
		this.additionalinformation = additionalinformation;
	}

	public List<String> getRelatedmsg() {
		return relatedmsg;
	}

	public void setRelatedmsg(List<String> relatedmsg) {
		this.relatedmsg = relatedmsg;
	}

	public List<String> getOwners() {
		return owners;
	}

	public void setOwners(List<String> owners) {
		this.owners = owners;
	}
    
    

}