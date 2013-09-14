package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.PaiStudy;

public interface Notifier {

	public abstract void updateNotification(List<PaiStudy> studies);
	public abstract void sendNotice(long securityId, String title, String text);
	public abstract void sendServiceNotice(int notifyId, String title, String text, int numMessages);

}