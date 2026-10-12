package org.qifu.fm.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.qifu.base.exception.ServiceException;
import org.qifu.fm.entity.FmFormSnapshot;
import org.qifu.fm.entity.FmTaskAction;
import org.qifu.fm.mapper.FmFormSnapshotMapper;
import org.qifu.fm.mapper.FmTaskActionMapper;

class FmImmutableAuditServiceTest {

    @Test
    void snapshotCannotBeUpdatedOrDeleted() {
        FmFormSnapshotMapper mapper = mock(FmFormSnapshotMapper.class);
        FmFormSnapshotServiceImpl service = new FmFormSnapshotServiceImpl(mapper);
        FmFormSnapshot snapshot = new FmFormSnapshot();

        assertThrows(ServiceException.class, () -> service.update(snapshot));
        assertThrows(ServiceException.class, () -> service.delete(snapshot));
        verifyNoInteractions(mapper);
    }

    @Test
    void actionCannotBeUpdatedOrDeleted() {
        FmTaskActionMapper mapper = mock(FmTaskActionMapper.class);
        FmTaskActionServiceImpl service = new FmTaskActionServiceImpl(mapper);
        FmTaskAction action = new FmTaskAction();

        assertThrows(ServiceException.class, () -> service.update(action));
        assertThrows(ServiceException.class, () -> service.delete(action));
        verifyNoInteractions(mapper);
    }
}
