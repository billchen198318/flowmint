-- Run with the MariaDB/MySQL CLI before deploying the matching application.
-- The application inserts these audit rows but must never rewrite or remove them.
DELIMITER $$

CREATE TRIGGER fm_form_snapshot_no_update
BEFORE UPDATE ON fm_form_snapshot
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'fm_form_snapshot is immutable';
END$$

CREATE TRIGGER fm_form_snapshot_no_delete
BEFORE DELETE ON fm_form_snapshot
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'fm_form_snapshot is immutable';
END$$

CREATE TRIGGER fm_task_action_no_update
BEFORE UPDATE ON fm_task_action
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'fm_task_action is immutable';
END$$

CREATE TRIGGER fm_task_action_no_delete
BEFORE DELETE ON fm_task_action
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'fm_task_action is immutable';
END$$

DELIMITER ;
