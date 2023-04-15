package com.example.hibernate.listeners.event_listener;

import com.example.hibernate.entity.RevInfo;
import org.hibernate.envers.RevisionListener;

public class CustomRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        ((RevInfo) revisionEntity).setUserName("SYSTEM");
    }
}
