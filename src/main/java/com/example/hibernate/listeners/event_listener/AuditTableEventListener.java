package com.example.hibernate.listeners.event_listener;

import org.hibernate.event.spi.*;

import static com.example.hibernate.listeners.event_listener.Audit.Operation.DELETE;
import static com.example.hibernate.listeners.event_listener.Audit.Operation.INSERT;

public class AuditTableEventListener implements PreInsertEventListener, PreDeleteEventListener  {
    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        auditEntity(event, INSERT);
        return false;
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        auditEntity(event, DELETE);
        return false;
    }

    private void auditEntity(AbstractPreDatabaseOperationEvent event, Audit.Operation delete) {
        if (!event.getEntity().getClass().equals(Audit.class)) {
            var entityId = event.getId() == null ? null : event.getId().toString();
            Audit audit = Audit.builder()
                    .entityId(entityId)
                    .entityClass(event.getEntity().getClass().getSimpleName())
                    .entityContent(event.getEntity().toString())
                    .operation(delete)
                    .build();
            event.getSession().persist(audit);
        }
    }
}
