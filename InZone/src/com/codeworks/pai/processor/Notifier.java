package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.PaiStudy;

public interface Notifier {

	public abstract void updateNotification(List<PaiStudy> studies);

}