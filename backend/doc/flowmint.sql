/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-12.3.2-MariaDB, for Win64 (AMD64)
--
-- Host: localhost    Database: flowmint
-- ------------------------------------------------------
-- Server version	12.3.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `act_evt_log`
--

DROP TABLE IF EXISTS `act_evt_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_evt_log` (
  `LOG_NR_` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `TIME_STAMP_` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  `USER_ID_` varchar(255) DEFAULT NULL,
  `DATA_` longblob DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) DEFAULT NULL,
  `LOCK_TIME_` timestamp(3) NULL DEFAULT NULL,
  `IS_PROCESSED_` tinyint(4) DEFAULT 0,
  PRIMARY KEY (`LOG_NR_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_evt_log`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_evt_log` WRITE;
/*!40000 ALTER TABLE `act_evt_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_evt_log` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ge_bytearray`
--

DROP TABLE IF EXISTS `act_ge_bytearray`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ge_bytearray` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(64) DEFAULT NULL,
  `BYTES_` longblob DEFAULT NULL,
  `GENERATED_` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_BYTEAR_DEPL` (`DEPLOYMENT_ID_`),
  CONSTRAINT `ACT_FK_BYTEARR_DEPL` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `act_re_deployment` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ge_bytearray`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ge_bytearray` WRITE;
/*!40000 ALTER TABLE `act_ge_bytearray` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ge_bytearray` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ge_property`
--

DROP TABLE IF EXISTS `act_ge_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ge_property` (
  `NAME_` varchar(64) NOT NULL,
  `VALUE_` varchar(300) DEFAULT NULL,
  `REV_` int(11) DEFAULT NULL,
  PRIMARY KEY (`NAME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ge_property`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ge_property` WRITE;
/*!40000 ALTER TABLE `act_ge_property` DISABLE KEYS */;
INSERT INTO `act_ge_property` VALUES
('cfg.execution-related-entities-count','true',1),
('cfg.task-related-entities-count','true',1),
('common.schema.version','8.0.0.0',1),
('eventregistry.schema.version','8.0.0.0',1),
('next.dbid','1',1),
('schema.history','create(8.0.0.0)',1),
('schema.version','8.0.0.0',1);
/*!40000 ALTER TABLE `act_ge_property` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_actinst`
--

DROP TABLE IF EXISTS `act_hi_actinst`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_actinst` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT 1,
  `PROC_DEF_ID_` varchar(64) NOT NULL,
  `PROC_INST_ID_` varchar(64) NOT NULL,
  `EXECUTION_ID_` varchar(64) NOT NULL,
  `ACT_ID_` varchar(255) NOT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `CALL_PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `ACT_NAME_` varchar(255) DEFAULT NULL,
  `ACT_TYPE_` varchar(255) NOT NULL,
  `ASSIGNEE_` varchar(255) DEFAULT NULL,
  `COMPLETED_BY_` varchar(255) DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `TRANSACTION_ORDER_` int(11) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_ACT_INST_START` (`START_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_ACT_INST_PROCINST` (`PROC_INST_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_HI_ACT_INST_EXEC` (`EXECUTION_ID_`,`ACT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_actinst`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_actinst` WRITE;
/*!40000 ALTER TABLE `act_hi_actinst` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_actinst` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_attachment`
--

DROP TABLE IF EXISTS `act_hi_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_attachment` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `USER_ID_` varchar(255) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) DEFAULT NULL,
  `TYPE_` varchar(255) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `URL_` varchar(4000) DEFAULT NULL,
  `CONTENT_ID_` varchar(64) DEFAULT NULL,
  `TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_attachment`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_attachment` WRITE;
/*!40000 ALTER TABLE `act_hi_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_attachment` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_comment`
--

DROP TABLE IF EXISTS `act_hi_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_comment` (
  `ID_` varchar(64) NOT NULL,
  `TYPE_` varchar(255) DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `USER_ID_` varchar(255) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `ACTION_` varchar(255) DEFAULT NULL,
  `MESSAGE_` varchar(4000) DEFAULT NULL,
  `FULL_MSG_` longblob DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_comment`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_comment` WRITE;
/*!40000 ALTER TABLE `act_hi_comment` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_comment` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_detail`
--

DROP TABLE IF EXISTS `act_hi_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_detail` (
  `ID_` varchar(64) NOT NULL,
  `TYPE_` varchar(255) NOT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `ACT_INST_ID_` varchar(64) DEFAULT NULL,
  `NAME_` varchar(255) NOT NULL,
  `VAR_TYPE_` varchar(255) DEFAULT NULL,
  `REV_` int(11) DEFAULT NULL,
  `TIME_` datetime(3) NOT NULL,
  `BYTEARRAY_ID_` varchar(64) DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) DEFAULT NULL,
  `TEXT2_` varchar(4000) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_DETAIL_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_ACT_INST` (`ACT_INST_ID_`),
  KEY `ACT_IDX_HI_DETAIL_TIME` (`TIME_`),
  KEY `ACT_IDX_HI_DETAIL_NAME` (`NAME_`),
  KEY `ACT_IDX_HI_DETAIL_TASK_ID` (`TASK_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_detail`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_detail` WRITE;
/*!40000 ALTER TABLE `act_hi_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_detail` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_entitylink`
--

DROP TABLE IF EXISTS `act_hi_entitylink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_entitylink` (
  `ID_` varchar(64) NOT NULL,
  `LINK_TYPE_` varchar(255) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `PARENT_ELEMENT_ID_` varchar(255) DEFAULT NULL,
  `REF_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `REF_SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `REF_SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `ROOT_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `ROOT_SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `HIERARCHY_TYPE_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_ENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_HI_ENT_LNK_REF_SCOPE` (`REF_SCOPE_ID_`,`REF_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_HI_ENT_LNK_ROOT_SCOPE` (`ROOT_SCOPE_ID_`,`ROOT_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_HI_ENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_entitylink`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_entitylink` WRITE;
/*!40000 ALTER TABLE `act_hi_entitylink` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_entitylink` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_identitylink`
--

DROP TABLE IF EXISTS `act_hi_identitylink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_identitylink` (
  `ID_` varchar(64) NOT NULL,
  `GROUP_ID_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) DEFAULT NULL,
  `USER_ID_` varchar(255) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_USER` (`USER_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_IDENT_LNK_TASK` (`TASK_ID_`),
  KEY `ACT_IDX_HI_IDENT_LNK_PROCINST` (`PROC_INST_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_identitylink`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_identitylink` WRITE;
/*!40000 ALTER TABLE `act_hi_identitylink` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_identitylink` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_procinst`
--

DROP TABLE IF EXISTS `act_hi_procinst`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_procinst` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT 1,
  `PROC_INST_ID_` varchar(64) NOT NULL,
  `BUSINESS_KEY_` varchar(255) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) NOT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `START_USER_ID_` varchar(255) DEFAULT NULL,
  `START_ACT_ID_` varchar(255) DEFAULT NULL,
  `END_ACT_ID_` varchar(255) DEFAULT NULL,
  `SUPER_PROCESS_INSTANCE_ID_` varchar(64) DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  `NAME_` varchar(255) DEFAULT NULL,
  `CALLBACK_ID_` varchar(255) DEFAULT NULL,
  `CALLBACK_TYPE_` varchar(255) DEFAULT NULL,
  `REFERENCE_ID_` varchar(255) DEFAULT NULL,
  `REFERENCE_TYPE_` varchar(255) DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) DEFAULT NULL,
  `BUSINESS_STATUS_` varchar(255) DEFAULT NULL,
  `END_USER_ID_` varchar(255) DEFAULT NULL,
  `STATE_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `PROC_INST_ID_` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PRO_INST_END` (`END_TIME_`),
  KEY `ACT_IDX_HI_PRO_I_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDX_HI_PRO_SUPER_PROCINST` (`SUPER_PROCESS_INSTANCE_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_procinst`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_procinst` WRITE;
/*!40000 ALTER TABLE `act_hi_procinst` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_procinst` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_taskinst`
--

DROP TABLE IF EXISTS `act_hi_taskinst`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_taskinst` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT 1,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `TASK_DEF_ID_` varchar(64) DEFAULT NULL,
  `TASK_DEF_KEY_` varchar(255) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) DEFAULT NULL,
  `STATE_` varchar(255) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `PARENT_TASK_ID_` varchar(64) DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) DEFAULT NULL,
  `OWNER_` varchar(255) DEFAULT NULL,
  `ASSIGNEE_` varchar(255) DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `IN_PROGRESS_TIME_` datetime(3) DEFAULT NULL,
  `IN_PROGRESS_STARTED_BY_` varchar(255) DEFAULT NULL,
  `CLAIM_TIME_` datetime(3) DEFAULT NULL,
  `CLAIMED_BY_` varchar(255) DEFAULT NULL,
  `SUSPENDED_TIME_` datetime(3) DEFAULT NULL,
  `SUSPENDED_BY_` varchar(255) DEFAULT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `COMPLETED_BY_` varchar(255) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) DEFAULT NULL,
  `PRIORITY_` int(11) DEFAULT NULL,
  `IN_PROGRESS_DUE_DATE_` datetime(3) DEFAULT NULL,
  `DUE_DATE_` datetime(3) DEFAULT NULL,
  `FORM_KEY_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  `LAST_UPDATED_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_TASK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_TASK_INST_PROCINST` (`PROC_INST_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_taskinst`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_taskinst` WRITE;
/*!40000 ALTER TABLE `act_hi_taskinst` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_taskinst` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_tsk_log`
--

DROP TABLE IF EXISTS `act_hi_tsk_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_tsk_log` (
  `ID_` bigint(20) NOT NULL AUTO_INCREMENT,
  `TYPE_` varchar(64) DEFAULT NULL,
  `TASK_ID_` varchar(64) NOT NULL,
  `TIME_STAMP_` timestamp(3) NOT NULL,
  `USER_ID_` varchar(255) DEFAULT NULL,
  `DATA_` varchar(4000) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_ACT_HI_TSK_LOG_TASK` (`TASK_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_tsk_log`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_tsk_log` WRITE;
/*!40000 ALTER TABLE `act_hi_tsk_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_tsk_log` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_hi_varinst`
--

DROP TABLE IF EXISTS `act_hi_varinst`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_hi_varinst` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT 1,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `NAME_` varchar(255) NOT NULL,
  `VAR_TYPE_` varchar(100) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) DEFAULT NULL,
  `TEXT2_` varchar(4000) DEFAULT NULL,
  `META_INFO_` varchar(4000) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `LAST_UPDATED_TIME_` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_HI_PROCVAR_NAME_TYPE` (`NAME_`,`VAR_TYPE_`),
  KEY `ACT_IDX_HI_VAR_SCOPE_ID_TYPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_VAR_SUB_ID_TYPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_HI_PROCVAR_PROC_INST` (`PROC_INST_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_TASK_ID` (`TASK_ID_`),
  KEY `ACT_IDX_HI_PROCVAR_EXE` (`EXECUTION_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_hi_varinst`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_hi_varinst` WRITE;
/*!40000 ALTER TABLE `act_hi_varinst` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_hi_varinst` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_bytearray`
--

DROP TABLE IF EXISTS `act_id_bytearray`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_bytearray` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `BYTES_` longblob DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_bytearray`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_bytearray` WRITE;
/*!40000 ALTER TABLE `act_id_bytearray` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_bytearray` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_group`
--

DROP TABLE IF EXISTS `act_id_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_group` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_group`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_group` WRITE;
/*!40000 ALTER TABLE `act_id_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_group` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_info`
--

DROP TABLE IF EXISTS `act_id_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_info` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `USER_ID_` varchar(64) DEFAULT NULL,
  `TYPE_` varchar(64) DEFAULT NULL,
  `KEY_` varchar(255) DEFAULT NULL,
  `VALUE_` varchar(255) DEFAULT NULL,
  `PASSWORD_` longblob DEFAULT NULL,
  `PARENT_ID_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_info`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_info` WRITE;
/*!40000 ALTER TABLE `act_id_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_info` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_membership`
--

DROP TABLE IF EXISTS `act_id_membership`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_membership` (
  `USER_ID_` varchar(64) NOT NULL,
  `GROUP_ID_` varchar(64) NOT NULL,
  PRIMARY KEY (`USER_ID_`,`GROUP_ID_`),
  KEY `ACT_FK_MEMB_GROUP` (`GROUP_ID_`),
  CONSTRAINT `ACT_FK_MEMB_GROUP` FOREIGN KEY (`GROUP_ID_`) REFERENCES `act_id_group` (`ID_`),
  CONSTRAINT `ACT_FK_MEMB_USER` FOREIGN KEY (`USER_ID_`) REFERENCES `act_id_user` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_membership`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_membership` WRITE;
/*!40000 ALTER TABLE `act_id_membership` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_membership` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_priv`
--

DROP TABLE IF EXISTS `act_id_priv`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_priv` (
  `ID_` varchar(64) NOT NULL,
  `NAME_` varchar(255) NOT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_PRIV_NAME` (`NAME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_priv`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_priv` WRITE;
/*!40000 ALTER TABLE `act_id_priv` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_priv` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_priv_mapping`
--

DROP TABLE IF EXISTS `act_id_priv_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_priv_mapping` (
  `ID_` varchar(64) NOT NULL,
  `PRIV_ID_` varchar(64) NOT NULL,
  `USER_ID_` varchar(255) DEFAULT NULL,
  `GROUP_ID_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_FK_PRIV_MAPPING` (`PRIV_ID_`),
  KEY `ACT_IDX_PRIV_USER` (`USER_ID_`),
  KEY `ACT_IDX_PRIV_GROUP` (`GROUP_ID_`),
  CONSTRAINT `ACT_FK_PRIV_MAPPING` FOREIGN KEY (`PRIV_ID_`) REFERENCES `act_id_priv` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_priv_mapping`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_priv_mapping` WRITE;
/*!40000 ALTER TABLE `act_id_priv_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_priv_mapping` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_property`
--

DROP TABLE IF EXISTS `act_id_property`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_property` (
  `NAME_` varchar(64) NOT NULL,
  `VALUE_` varchar(300) DEFAULT NULL,
  `REV_` int(11) DEFAULT NULL,
  PRIMARY KEY (`NAME_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_property`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_property` WRITE;
/*!40000 ALTER TABLE `act_id_property` DISABLE KEYS */;
INSERT INTO `act_id_property` VALUES
('schema.version','8.0.0.0',1);
/*!40000 ALTER TABLE `act_id_property` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_token`
--

DROP TABLE IF EXISTS `act_id_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_token` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `TOKEN_VALUE_` varchar(255) DEFAULT NULL,
  `TOKEN_DATE_` timestamp(3) NULL DEFAULT NULL,
  `IP_ADDRESS_` varchar(255) DEFAULT NULL,
  `USER_AGENT_` varchar(255) DEFAULT NULL,
  `USER_ID_` varchar(255) DEFAULT NULL,
  `TOKEN_DATA_` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_token`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_token` WRITE;
/*!40000 ALTER TABLE `act_id_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_token` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_id_user`
--

DROP TABLE IF EXISTS `act_id_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_id_user` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `FIRST_` varchar(255) DEFAULT NULL,
  `LAST_` varchar(255) DEFAULT NULL,
  `DISPLAY_NAME_` varchar(255) DEFAULT NULL,
  `EMAIL_` varchar(255) DEFAULT NULL,
  `PWD_` varchar(255) DEFAULT NULL,
  `PICTURE_ID_` varchar(64) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_id_user`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_id_user` WRITE;
/*!40000 ALTER TABLE `act_id_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_id_user` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_procdef_info`
--

DROP TABLE IF EXISTS `act_procdef_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_procdef_info` (
  `ID_` varchar(64) NOT NULL,
  `PROC_DEF_ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `INFO_JSON_ID_` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_INFO_PROCDEF` (`PROC_DEF_ID_`),
  KEY `ACT_IDX_INFO_PROCDEF` (`PROC_DEF_ID_`),
  KEY `ACT_FK_INFO_JSON_BA` (`INFO_JSON_ID_`),
  CONSTRAINT `ACT_FK_INFO_JSON_BA` FOREIGN KEY (`INFO_JSON_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_INFO_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_procdef_info`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_procdef_info` WRITE;
/*!40000 ALTER TABLE `act_procdef_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_procdef_info` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_re_deployment`
--

DROP TABLE IF EXISTS `act_re_deployment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_re_deployment` (
  `ID_` varchar(64) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `KEY_` varchar(255) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  `DEPLOY_TIME_` timestamp(3) NULL DEFAULT NULL,
  `DERIVED_FROM_` varchar(64) DEFAULT NULL,
  `DERIVED_FROM_ROOT_` varchar(64) DEFAULT NULL,
  `PARENT_DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  `ENGINE_VERSION_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_re_deployment`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_re_deployment` WRITE;
/*!40000 ALTER TABLE `act_re_deployment` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_re_deployment` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_re_model`
--

DROP TABLE IF EXISTS `act_re_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_re_model` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `KEY_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LAST_UPDATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `VERSION_` int(11) DEFAULT NULL,
  `META_INFO_` varchar(4000) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(64) DEFAULT NULL,
  `EDITOR_SOURCE_VALUE_ID_` varchar(64) DEFAULT NULL,
  `EDITOR_SOURCE_EXTRA_VALUE_ID_` varchar(64) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_FK_MODEL_SOURCE` (`EDITOR_SOURCE_VALUE_ID_`),
  KEY `ACT_FK_MODEL_SOURCE_EXTRA` (`EDITOR_SOURCE_EXTRA_VALUE_ID_`),
  KEY `ACT_FK_MODEL_DEPLOYMENT` (`DEPLOYMENT_ID_`),
  CONSTRAINT `ACT_FK_MODEL_DEPLOYMENT` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `act_re_deployment` (`ID_`),
  CONSTRAINT `ACT_FK_MODEL_SOURCE` FOREIGN KEY (`EDITOR_SOURCE_VALUE_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_MODEL_SOURCE_EXTRA` FOREIGN KEY (`EDITOR_SOURCE_EXTRA_VALUE_ID_`) REFERENCES `act_ge_bytearray` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_re_model`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_re_model` WRITE;
/*!40000 ALTER TABLE `act_re_model` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_re_model` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_re_procdef`
--

DROP TABLE IF EXISTS `act_re_procdef`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_re_procdef` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `KEY_` varchar(255) NOT NULL,
  `VERSION_` int(11) NOT NULL,
  `DEPLOYMENT_ID_` varchar(64) DEFAULT NULL,
  `RESOURCE_NAME_` varchar(4000) DEFAULT NULL,
  `DGRM_RESOURCE_NAME_` varchar(4000) DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) DEFAULT NULL,
  `HAS_START_FORM_KEY_` tinyint(4) DEFAULT NULL,
  `HAS_GRAPHICAL_NOTATION_` tinyint(4) DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  `ENGINE_VERSION_` varchar(255) DEFAULT NULL,
  `DERIVED_FROM_` varchar(64) DEFAULT NULL,
  `DERIVED_FROM_ROOT_` varchar(64) DEFAULT NULL,
  `DERIVED_VERSION_` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_UNIQ_PROCDEF` (`KEY_`,`VERSION_`,`DERIVED_VERSION_`,`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_re_procdef`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_re_procdef` WRITE;
/*!40000 ALTER TABLE `act_re_procdef` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_re_procdef` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_actinst`
--

DROP TABLE IF EXISTS `act_ru_actinst`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_actinst` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT 1,
  `PROC_DEF_ID_` varchar(64) NOT NULL,
  `PROC_INST_ID_` varchar(64) NOT NULL,
  `EXECUTION_ID_` varchar(64) NOT NULL,
  `ACT_ID_` varchar(255) NOT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `CALL_PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `ACT_NAME_` varchar(255) DEFAULT NULL,
  `ACT_TYPE_` varchar(255) NOT NULL,
  `ASSIGNEE_` varchar(255) DEFAULT NULL,
  `COMPLETED_BY_` varchar(255) DEFAULT NULL,
  `START_TIME_` datetime(3) NOT NULL,
  `END_TIME_` datetime(3) DEFAULT NULL,
  `DURATION_` bigint(20) DEFAULT NULL,
  `TRANSACTION_ORDER_` int(11) DEFAULT NULL,
  `DELETE_REASON_` varchar(4000) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_RU_ACTI_START` (`START_TIME_`),
  KEY `ACT_IDX_RU_ACTI_END` (`END_TIME_`),
  KEY `ACT_IDX_RU_ACTI_PROC` (`PROC_INST_ID_`),
  KEY `ACT_IDX_RU_ACTI_PROC_ACT` (`PROC_INST_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_RU_ACTI_EXEC` (`EXECUTION_ID_`),
  KEY `ACT_IDX_RU_ACTI_EXEC_ACT` (`EXECUTION_ID_`,`ACT_ID_`),
  KEY `ACT_IDX_RU_ACTI_TASK` (`TASK_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_actinst`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_actinst` WRITE;
/*!40000 ALTER TABLE `act_ru_actinst` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_actinst` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_deadletter_job`
--

DROP TABLE IF EXISTS `act_ru_deadletter_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_deadletter_job` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) NOT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_DEADLETTER_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_DEADLETTER_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_DEADLETTER_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_DJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_DJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_DJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_DEADLETTER_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_DEADLETTER_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_DEADLETTER_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_deadletter_job`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_deadletter_job` WRITE;
/*!40000 ALTER TABLE `act_ru_deadletter_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_deadletter_job` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_entitylink`
--

DROP TABLE IF EXISTS `act_ru_entitylink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_entitylink` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `LINK_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `PARENT_ELEMENT_ID_` varchar(255) DEFAULT NULL,
  `REF_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `REF_SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `REF_SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `ROOT_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `ROOT_SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `HIERARCHY_TYPE_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_ENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_ENT_LNK_REF_SCOPE` (`REF_SCOPE_ID_`,`REF_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_ENT_LNK_ROOT_SCOPE` (`ROOT_SCOPE_ID_`,`ROOT_SCOPE_TYPE_`,`LINK_TYPE_`),
  KEY `ACT_IDX_ENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`,`LINK_TYPE_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_entitylink`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_entitylink` WRITE;
/*!40000 ALTER TABLE `act_ru_entitylink` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_entitylink` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_event_subscr`
--

DROP TABLE IF EXISTS `act_ru_event_subscr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_event_subscr` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `EVENT_TYPE_` varchar(255) NOT NULL,
  `EVENT_NAME_` varchar(255) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `ACTIVITY_ID_` varchar(64) DEFAULT NULL,
  `CONFIGURATION_` varchar(255) DEFAULT NULL,
  `CREATED_` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_DEFINITION_KEY_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(64) DEFAULT NULL,
  `LOCK_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EVENT_SUBSCR_CONFIG_` (`CONFIGURATION_`),
  KEY `ACT_IDX_EVENT_SUBSCR_EXEC_ID` (`EXECUTION_ID_`),
  KEY `ACT_IDX_EVENT_SUBSCR_PROC_ID` (`PROC_INST_ID_`),
  KEY `ACT_IDX_EVENT_SUBSCR_SCOPEREF_` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  CONSTRAINT `ACT_FK_EVENT_EXEC` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_event_subscr`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_event_subscr` WRITE;
/*!40000 ALTER TABLE `act_ru_event_subscr` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_event_subscr` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_execution`
--

DROP TABLE IF EXISTS `act_ru_execution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_execution` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `BUSINESS_KEY_` varchar(255) DEFAULT NULL,
  `PARENT_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `SUPER_EXEC_` varchar(64) DEFAULT NULL,
  `ROOT_PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `ACT_ID_` varchar(255) DEFAULT NULL,
  `IS_ACTIVE_` tinyint(4) DEFAULT NULL,
  `IS_CONCURRENT_` tinyint(4) DEFAULT NULL,
  `IS_SCOPE_` tinyint(4) DEFAULT NULL,
  `IS_EVENT_SCOPE_` tinyint(4) DEFAULT NULL,
  `IS_MI_ROOT_` tinyint(4) DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `CACHED_ENT_STATE_` int(11) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  `NAME_` varchar(255) DEFAULT NULL,
  `START_ACT_ID_` varchar(255) DEFAULT NULL,
  `START_TIME_` datetime(3) DEFAULT NULL,
  `START_USER_ID_` varchar(255) DEFAULT NULL,
  `LOCK_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) DEFAULT NULL,
  `IS_COUNT_ENABLED_` tinyint(4) DEFAULT NULL,
  `EVT_SUBSCR_COUNT_` int(11) DEFAULT NULL,
  `TASK_COUNT_` int(11) DEFAULT NULL,
  `JOB_COUNT_` int(11) DEFAULT NULL,
  `TIMER_JOB_COUNT_` int(11) DEFAULT NULL,
  `SUSP_JOB_COUNT_` int(11) DEFAULT NULL,
  `DEADLETTER_JOB_COUNT_` int(11) DEFAULT NULL,
  `EXTERNAL_WORKER_JOB_COUNT_` int(11) DEFAULT NULL,
  `VAR_COUNT_` int(11) DEFAULT NULL,
  `ID_LINK_COUNT_` int(11) DEFAULT NULL,
  `CALLBACK_ID_` varchar(255) DEFAULT NULL,
  `CALLBACK_TYPE_` varchar(255) DEFAULT NULL,
  `REFERENCE_ID_` varchar(255) DEFAULT NULL,
  `REFERENCE_TYPE_` varchar(255) DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) DEFAULT NULL,
  `BUSINESS_STATUS_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EXEC_BUSKEY` (`BUSINESS_KEY_`),
  KEY `ACT_IDC_EXEC_ROOT` (`ROOT_PROC_INST_ID_`),
  KEY `ACT_IDX_EXEC_REF_ID_` (`REFERENCE_ID_`),
  KEY `ACT_FK_EXE_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_EXE_PARENT` (`PARENT_ID_`),
  KEY `ACT_FK_EXE_SUPER` (`SUPER_EXEC_`),
  KEY `ACT_FK_EXE_PROCDEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_EXE_PARENT` FOREIGN KEY (`PARENT_ID_`) REFERENCES `act_ru_execution` (`ID_`) ON DELETE CASCADE,
  CONSTRAINT `ACT_FK_EXE_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`),
  CONSTRAINT `ACT_FK_EXE_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `ACT_FK_EXE_SUPER` FOREIGN KEY (`SUPER_EXEC_`) REFERENCES `act_ru_execution` (`ID_`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_execution`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_execution` WRITE;
/*!40000 ALTER TABLE `act_ru_execution` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_execution` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_external_job`
--

DROP TABLE IF EXISTS `act_ru_external_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_external_job` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) NOT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) DEFAULT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) DEFAULT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_EXTERNAL_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_EXTERNAL_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_EXTERNAL_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_EJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_EJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_EJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  CONSTRAINT `ACT_FK_EXTERNAL_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_EXTERNAL_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `act_ge_bytearray` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_external_job`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_external_job` WRITE;
/*!40000 ALTER TABLE `act_ru_external_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_external_job` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_history_job`
--

DROP TABLE IF EXISTS `act_ru_history_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_history_job` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) DEFAULT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) DEFAULT NULL,
  `ADV_HANDLER_CFG_ID_` varchar(64) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_history_job`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_history_job` WRITE;
/*!40000 ALTER TABLE `act_ru_history_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_history_job` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_identitylink`
--

DROP TABLE IF EXISTS `act_ru_identitylink`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_identitylink` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `GROUP_ID_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) DEFAULT NULL,
  `USER_ID_` varchar(255) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_IDENT_LNK_USER` (`USER_ID_`),
  KEY `ACT_IDX_IDENT_LNK_GROUP` (`GROUP_ID_`),
  KEY `ACT_IDX_IDENT_LNK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_IDENT_LNK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_IDENT_LNK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_ATHRZ_PROCEDEF` (`PROC_DEF_ID_`),
  KEY `ACT_FK_TSKASS_TASK` (`TASK_ID_`),
  KEY `ACT_FK_IDL_PROCINST` (`PROC_INST_ID_`),
  CONSTRAINT `ACT_FK_ATHRZ_PROCEDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`),
  CONSTRAINT `ACT_FK_IDL_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_TSKASS_TASK` FOREIGN KEY (`TASK_ID_`) REFERENCES `act_ru_task` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_identitylink`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_identitylink` WRITE;
/*!40000 ALTER TABLE `act_ru_identitylink` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_identitylink` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_job`
--

DROP TABLE IF EXISTS `act_ru_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_job` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) NOT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) DEFAULT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) DEFAULT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_JOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_JOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_JOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_job`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_job` WRITE;
/*!40000 ALTER TABLE `act_ru_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_job` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_suspended_job`
--

DROP TABLE IF EXISTS `act_ru_suspended_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_suspended_job` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) NOT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) DEFAULT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_SUSPENDED_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_SUSPENDED_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_SUSPENDED_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_SJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_SJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_SJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_SUSPENDED_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_SUSPENDED_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_SUSPENDED_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_suspended_job`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_suspended_job` WRITE;
/*!40000 ALTER TABLE `act_ru_suspended_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_suspended_job` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_task`
--

DROP TABLE IF EXISTS `act_ru_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_task` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `TASK_DEF_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `PROPAGATED_STAGE_INST_ID_` varchar(255) DEFAULT NULL,
  `STATE_` varchar(255) DEFAULT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `PARENT_TASK_ID_` varchar(64) DEFAULT NULL,
  `DESCRIPTION_` varchar(4000) DEFAULT NULL,
  `TASK_DEF_KEY_` varchar(255) DEFAULT NULL,
  `OWNER_` varchar(255) DEFAULT NULL,
  `ASSIGNEE_` varchar(255) DEFAULT NULL,
  `DELEGATION_` varchar(64) DEFAULT NULL,
  `PRIORITY_` int(11) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `IN_PROGRESS_TIME_` datetime(3) DEFAULT NULL,
  `IN_PROGRESS_STARTED_BY_` varchar(255) DEFAULT NULL,
  `CLAIM_TIME_` datetime(3) DEFAULT NULL,
  `CLAIMED_BY_` varchar(255) DEFAULT NULL,
  `SUSPENDED_TIME_` datetime(3) DEFAULT NULL,
  `SUSPENDED_BY_` varchar(255) DEFAULT NULL,
  `IN_PROGRESS_DUE_DATE_` datetime(3) DEFAULT NULL,
  `DUE_DATE_` datetime(3) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `SUSPENSION_STATE_` int(11) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  `FORM_KEY_` varchar(255) DEFAULT NULL,
  `IS_COUNT_ENABLED_` tinyint(4) DEFAULT NULL,
  `VAR_COUNT_` int(11) DEFAULT NULL,
  `ID_LINK_COUNT_` int(11) DEFAULT NULL,
  `SUB_TASK_COUNT_` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_TASK_CREATE` (`CREATE_TIME_`),
  KEY `ACT_IDX_TASK_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TASK_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TASK_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_TASK_EXE` (`EXECUTION_ID_`),
  KEY `ACT_FK_TASK_PROCINST` (`PROC_INST_ID_`),
  KEY `ACT_FK_TASK_PROCDEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_TASK_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_PROCDEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`),
  CONSTRAINT `ACT_FK_TASK_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_task`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_task` WRITE;
/*!40000 ALTER TABLE `act_ru_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_task` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_timer_job`
--

DROP TABLE IF EXISTS `act_ru_timer_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_timer_job` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) NOT NULL,
  `LOCK_EXP_TIME_` timestamp(3) NULL DEFAULT NULL,
  `LOCK_OWNER_` varchar(255) DEFAULT NULL,
  `EXCLUSIVE_` tinyint(1) DEFAULT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROCESS_INSTANCE_ID_` varchar(64) DEFAULT NULL,
  `PROC_DEF_ID_` varchar(64) DEFAULT NULL,
  `ELEMENT_ID_` varchar(255) DEFAULT NULL,
  `ELEMENT_NAME_` varchar(255) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `SCOPE_DEFINITION_ID_` varchar(255) DEFAULT NULL,
  `CORRELATION_ID_` varchar(255) DEFAULT NULL,
  `RETRIES_` int(11) DEFAULT NULL,
  `EXCEPTION_STACK_ID_` varchar(64) DEFAULT NULL,
  `EXCEPTION_MSG_` varchar(4000) DEFAULT NULL,
  `DUEDATE_` timestamp(3) NULL DEFAULT NULL,
  `REPEAT_` varchar(255) DEFAULT NULL,
  `HANDLER_TYPE_` varchar(255) DEFAULT NULL,
  `HANDLER_CFG_` varchar(4000) DEFAULT NULL,
  `CUSTOM_VALUES_ID_` varchar(64) DEFAULT NULL,
  `CREATE_TIME_` timestamp(3) NULL DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_TIMER_JOB_EXCEPTION_STACK_ID` (`EXCEPTION_STACK_ID_`),
  KEY `ACT_IDX_TIMER_JOB_CUSTOM_VALUES_ID` (`CUSTOM_VALUES_ID_`),
  KEY `ACT_IDX_TIMER_JOB_CORRELATION_ID` (`CORRELATION_ID_`),
  KEY `ACT_IDX_TIMER_JOB_DUEDATE` (`DUEDATE_`),
  KEY `ACT_IDX_TJOB_SCOPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TJOB_SUB_SCOPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_TJOB_SCOPE_DEF` (`SCOPE_DEFINITION_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_TIMER_JOB_EXECUTION` (`EXECUTION_ID_`),
  KEY `ACT_FK_TIMER_JOB_PROCESS_INSTANCE` (`PROCESS_INSTANCE_ID_`),
  KEY `ACT_FK_TIMER_JOB_PROC_DEF` (`PROC_DEF_ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_CUSTOM_VALUES` FOREIGN KEY (`CUSTOM_VALUES_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_EXCEPTION` FOREIGN KEY (`EXCEPTION_STACK_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_EXECUTION` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_PROCESS_INSTANCE` FOREIGN KEY (`PROCESS_INSTANCE_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_TIMER_JOB_PROC_DEF` FOREIGN KEY (`PROC_DEF_ID_`) REFERENCES `act_re_procdef` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_timer_job`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_timer_job` WRITE;
/*!40000 ALTER TABLE `act_ru_timer_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_timer_job` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `act_ru_variable`
--

DROP TABLE IF EXISTS `act_ru_variable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `act_ru_variable` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `TYPE_` varchar(255) NOT NULL,
  `NAME_` varchar(255) NOT NULL,
  `EXECUTION_ID_` varchar(64) DEFAULT NULL,
  `PROC_INST_ID_` varchar(64) DEFAULT NULL,
  `TASK_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(255) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(255) DEFAULT NULL,
  `BYTEARRAY_ID_` varchar(64) DEFAULT NULL,
  `DOUBLE_` double DEFAULT NULL,
  `LONG_` bigint(20) DEFAULT NULL,
  `TEXT_` varchar(4000) DEFAULT NULL,
  `TEXT2_` varchar(4000) DEFAULT NULL,
  `META_INFO_` varchar(4000) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `ACT_IDX_RU_VAR_SCOPE_ID_TYPE` (`SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_IDX_RU_VAR_SUB_ID_TYPE` (`SUB_SCOPE_ID_`,`SCOPE_TYPE_`),
  KEY `ACT_FK_VAR_BYTEARRAY` (`BYTEARRAY_ID_`),
  KEY `ACT_IDX_VARIABLE_TASK_ID` (`TASK_ID_`),
  KEY `ACT_FK_VAR_EXE` (`EXECUTION_ID_`),
  KEY `ACT_FK_VAR_PROCINST` (`PROC_INST_ID_`),
  CONSTRAINT `ACT_FK_VAR_BYTEARRAY` FOREIGN KEY (`BYTEARRAY_ID_`) REFERENCES `act_ge_bytearray` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_EXE` FOREIGN KEY (`EXECUTION_ID_`) REFERENCES `act_ru_execution` (`ID_`),
  CONSTRAINT `ACT_FK_VAR_PROCINST` FOREIGN KEY (`PROC_INST_ID_`) REFERENCES `act_ru_execution` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `act_ru_variable`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `act_ru_variable` WRITE;
/*!40000 ALTER TABLE `act_ru_variable` DISABLE KEYS */;
/*!40000 ALTER TABLE `act_ru_variable` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `flw_channel_definition`
--

DROP TABLE IF EXISTS `flw_channel_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `flw_channel_definition` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `VERSION_` int(11) DEFAULT NULL,
  `KEY_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `TYPE_` varchar(255) DEFAULT NULL,
  `IMPLEMENTATION_` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT NULL,
  `RESOURCE_NAME_` varchar(255) DEFAULT NULL,
  `DESCRIPTION_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_IDX_CHANNEL_DEF_UNIQ` (`KEY_`,`VERSION_`,`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flw_channel_definition`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `flw_channel_definition` WRITE;
/*!40000 ALTER TABLE `flw_channel_definition` DISABLE KEYS */;
/*!40000 ALTER TABLE `flw_channel_definition` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `flw_event_definition`
--

DROP TABLE IF EXISTS `flw_event_definition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `flw_event_definition` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `VERSION_` int(11) DEFAULT NULL,
  `KEY_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT NULL,
  `RESOURCE_NAME_` varchar(255) DEFAULT NULL,
  `DESCRIPTION_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  UNIQUE KEY `ACT_IDX_EVENT_DEF_UNIQ` (`KEY_`,`VERSION_`,`TENANT_ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flw_event_definition`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `flw_event_definition` WRITE;
/*!40000 ALTER TABLE `flw_event_definition` DISABLE KEYS */;
/*!40000 ALTER TABLE `flw_event_definition` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `flw_event_deployment`
--

DROP TABLE IF EXISTS `flw_event_deployment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `flw_event_deployment` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `CATEGORY_` varchar(255) DEFAULT NULL,
  `DEPLOY_TIME_` datetime(3) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT NULL,
  `PARENT_DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flw_event_deployment`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `flw_event_deployment` WRITE;
/*!40000 ALTER TABLE `flw_event_deployment` DISABLE KEYS */;
/*!40000 ALTER TABLE `flw_event_deployment` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `flw_event_resource`
--

DROP TABLE IF EXISTS `flw_event_resource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `flw_event_resource` (
  `ID_` varchar(255) NOT NULL,
  `NAME_` varchar(255) DEFAULT NULL,
  `DEPLOYMENT_ID_` varchar(255) DEFAULT NULL,
  `RESOURCE_BYTES_` longblob DEFAULT NULL,
  PRIMARY KEY (`ID_`),
  KEY `FLW_IDX_EVENT_RSRC_DPL` (`DEPLOYMENT_ID_`),
  CONSTRAINT `FLW_FK_EVENT_RSRC_DPL` FOREIGN KEY (`DEPLOYMENT_ID_`) REFERENCES `flw_event_deployment` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flw_event_resource`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `flw_event_resource` WRITE;
/*!40000 ALTER TABLE `flw_event_resource` DISABLE KEYS */;
/*!40000 ALTER TABLE `flw_event_resource` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `flw_ru_batch`
--

DROP TABLE IF EXISTS `flw_ru_batch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `flw_ru_batch` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `TYPE_` varchar(64) NOT NULL,
  `SEARCH_KEY_` varchar(255) DEFAULT NULL,
  `SEARCH_KEY2_` varchar(255) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) NOT NULL,
  `COMPLETE_TIME_` datetime(3) DEFAULT NULL,
  `STATUS_` varchar(255) DEFAULT NULL,
  `BATCH_DOC_ID_` varchar(64) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flw_ru_batch`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `flw_ru_batch` WRITE;
/*!40000 ALTER TABLE `flw_ru_batch` DISABLE KEYS */;
/*!40000 ALTER TABLE `flw_ru_batch` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `flw_ru_batch_part`
--

DROP TABLE IF EXISTS `flw_ru_batch_part`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `flw_ru_batch_part` (
  `ID_` varchar(64) NOT NULL,
  `REV_` int(11) DEFAULT NULL,
  `BATCH_ID_` varchar(64) DEFAULT NULL,
  `TYPE_` varchar(64) NOT NULL,
  `SCOPE_ID_` varchar(64) DEFAULT NULL,
  `SUB_SCOPE_ID_` varchar(64) DEFAULT NULL,
  `SCOPE_TYPE_` varchar(64) DEFAULT NULL,
  `SEARCH_KEY_` varchar(255) DEFAULT NULL,
  `SEARCH_KEY2_` varchar(255) DEFAULT NULL,
  `CREATE_TIME_` datetime(3) NOT NULL,
  `COMPLETE_TIME_` datetime(3) DEFAULT NULL,
  `STATUS_` varchar(255) DEFAULT NULL,
  `RESULT_DOC_ID_` varchar(64) DEFAULT NULL,
  `TENANT_ID_` varchar(255) DEFAULT '',
  PRIMARY KEY (`ID_`),
  KEY `FLW_IDX_BATCH_PART` (`BATCH_ID_`),
  CONSTRAINT `FLW_FK_BATCH_PART_PARENT` FOREIGN KEY (`BATCH_ID_`) REFERENCES `flw_ru_batch` (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flw_ru_batch_part`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `flw_ru_batch_part` WRITE;
/*!40000 ALTER TABLE `flw_ru_batch_part` DISABLE KEYS */;
/*!40000 ALTER TABLE `flw_ru_batch_part` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_approval_authority`
--

DROP TABLE IF EXISTS `fm_approval_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_approval_authority` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `APPROVAL_AUTHORITY_ID` varchar(36) NOT NULL,
  `AUTHORITY_CODE` varchar(30) NOT NULL,
  `AUTHORITY_NAME` varchar(100) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) DEFAULT NULL,
  `FORM_ID` varchar(36) DEFAULT NULL,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_AA_ID` (`TENANT_ID`,`APPROVAL_AUTHORITY_ID`),
  UNIQUE KEY `UK_FM_AA_CODE` (`TENANT_ID`,`AUTHORITY_CODE`),
  CONSTRAINT `CK_FM_AA_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_AA_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_approval_authority`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_approval_authority` WRITE;
/*!40000 ALTER TABLE `fm_approval_authority` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_approval_authority` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_approval_authority_rule`
--

DROP TABLE IF EXISTS `fm_approval_authority_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_approval_authority_rule` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `APPROVAL_AUTHORITY_RULE_ID` varchar(36) NOT NULL,
  `APPROVAL_AUTHORITY_ID` varchar(36) NOT NULL,
  `RULE_SEQ` int(11) NOT NULL,
  `CONDITION_CONFIG` longtext NOT NULL,
  `TARGET_TYPE` varchar(30) NOT NULL,
  `TARGET_REF_ID` varchar(36) DEFAULT NULL,
  `RESOLVER_CONFIG` longtext DEFAULT NULL,
  `STOP_AFTER_APPROVAL` char(1) NOT NULL DEFAULT 'Y',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_AAR_ID` (`TENANT_ID`,`APPROVAL_AUTHORITY_RULE_ID`),
  UNIQUE KEY `UK_FM_AAR_SEQ` (`TENANT_ID`,`APPROVAL_AUTHORITY_ID`,`RULE_SEQ`),
  CONSTRAINT `CK_FM_AAR_TARGET` CHECK (`TARGET_TYPE` in ('APPROVAL_LEVEL','ORG_TITLE','ORG_DUTY','APPROVAL_GROUP','FIXED_ACCOUNT')),
  CONSTRAINT `CK_FM_AAR_STOP` CHECK (`STOP_AFTER_APPROVAL` in ('Y','N')),
  CONSTRAINT `CK_FM_AAR_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_approval_authority_rule`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_approval_authority_rule` WRITE;
/*!40000 ALTER TABLE `fm_approval_authority_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_approval_authority_rule` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_approval_group`
--

DROP TABLE IF EXISTS `fm_approval_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_approval_group` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `APPROVAL_GROUP_ID` varchar(36) NOT NULL,
  `GROUP_CODE` varchar(30) NOT NULL,
  `GROUP_NAME` varchar(100) NOT NULL,
  `ASSIGNMENT_MODE` varchar(20) NOT NULL DEFAULT 'CANDIDATE',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_AG_ID` (`TENANT_ID`,`APPROVAL_GROUP_ID`),
  UNIQUE KEY `UK_FM_AG_CODE` (`TENANT_ID`,`GROUP_CODE`),
  CONSTRAINT `CK_FM_AG_MODE` CHECK (`ASSIGNMENT_MODE` in ('CANDIDATE','ALL','SEQUENTIAL')),
  CONSTRAINT `CK_FM_AG_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_approval_group`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_approval_group` WRITE;
/*!40000 ALTER TABLE `fm_approval_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_approval_group` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_approval_group_member`
--

DROP TABLE IF EXISTS `fm_approval_group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_approval_group_member` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `APPROVAL_GROUP_MEMBER_ID` varchar(36) NOT NULL,
  `APPROVAL_GROUP_ID` varchar(36) NOT NULL,
  `EMPLOYEE_ID` varchar(36) NOT NULL,
  `PRIORITY` int(11) NOT NULL DEFAULT 100,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_AGM_ID` (`TENANT_ID`,`APPROVAL_GROUP_MEMBER_ID`),
  UNIQUE KEY `UK_FM_AGM_REL` (`TENANT_ID`,`APPROVAL_GROUP_ID`,`EMPLOYEE_ID`,`EFFECTIVE_FROM`),
  KEY `IDX_FM_AGM_ACTIVE` (`TENANT_ID`,`APPROVAL_GROUP_ID`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  CONSTRAINT `CK_FM_AGM_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_AGM_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_approval_group_member`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_approval_group_member` WRITE;
/*!40000 ALTER TABLE `fm_approval_group_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_approval_group_member` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_assignment_incident`
--

DROP TABLE IF EXISTS `fm_assignment_incident`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_assignment_incident` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `INCIDENT_ID` varchar(36) NOT NULL,
  `PROCESS_INSTANCE_ID` varchar(64) NOT NULL,
  `TASK_ID` varchar(64) DEFAULT NULL,
  `TASK_DEF_KEY` varchar(100) DEFAULT NULL,
  `INCIDENT_TYPE` varchar(50) NOT NULL,
  `ERROR_CODE` varchar(50) NOT NULL,
  `ERROR_MESSAGE` varchar(2000) NOT NULL,
  `CONTEXT_DATA` longtext DEFAULT NULL,
  `INCIDENT_STATUS` varchar(20) NOT NULL DEFAULT 'OPEN',
  `RESOLVED_BY` varchar(24) DEFAULT NULL,
  `RESOLVED_DATE` datetime(3) DEFAULT NULL,
  `RESOLUTION_NOTE` varchar(2000) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_AI_ID` (`TENANT_ID`,`INCIDENT_ID`),
  KEY `IDX_FM_AI_OPEN` (`TENANT_ID`,`INCIDENT_STATUS`,`CDATE`),
  KEY `IDX_FM_AI_INSTANCE` (`TENANT_ID`,`PROCESS_INSTANCE_ID`,`TASK_ID`),
  CONSTRAINT `CK_FM_AI_STATUS` CHECK (`INCIDENT_STATUS` in ('OPEN','RESOLVED','IGNORED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_assignment_incident`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_assignment_incident` WRITE;
/*!40000 ALTER TABLE `fm_assignment_incident` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_assignment_incident` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_attachment`
--

DROP TABLE IF EXISTS `fm_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_attachment` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ATTACHMENT_ID` varchar(36) NOT NULL,
  `FORM_DATA_ID` varchar(36) NOT NULL,
  `FIELD_KEY` varchar(100) DEFAULT NULL,
  `FILE_OID` char(36) NOT NULL,
  `FILE_NAME` varchar(255) NOT NULL,
  `CONTENT_TYPE` varchar(100) NOT NULL,
  `FILE_SIZE` bigint(20) NOT NULL,
  `CONTENT_SHA256` char(64) NOT NULL,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_ATTACHMENT_ID` (`TENANT_ID`,`ATTACHMENT_ID`),
  KEY `IDX_FM_ATTACHMENT_FORM` (`TENANT_ID`,`FORM_DATA_ID`,`STATUS`),
  CONSTRAINT `CK_FM_ATTACHMENT_SIZE` CHECK (`FILE_SIZE` >= 0),
  CONSTRAINT `CK_FM_ATTACHMENT_STATUS` CHECK (`STATUS` in ('ACTIVE','DELETED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_attachment`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_attachment` WRITE;
/*!40000 ALTER TABLE `fm_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_attachment` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_employee`
--

DROP TABLE IF EXISTS `fm_employee`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_employee` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `EMPLOYEE_ID` varchar(36) NOT NULL,
  `EMPLOYEE_NO` varchar(50) NOT NULL,
  `ACCOUNT` varchar(24) NOT NULL,
  `DISPLAY_NAME` varchar(100) NOT NULL,
  `EMAIL` varchar(255) DEFAULT NULL,
  `MOBILE` varchar(30) DEFAULT NULL,
  `LOCALE` varchar(15) DEFAULT NULL,
  `TIMEZONE` varchar(50) DEFAULT NULL,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_EMPLOYEE_ID` (`TENANT_ID`,`EMPLOYEE_ID`),
  UNIQUE KEY `UK_FM_EMPLOYEE_NO` (`TENANT_ID`,`EMPLOYEE_NO`),
  UNIQUE KEY `UK_FM_EMPLOYEE_ACCOUNT` (`TENANT_ID`,`ACCOUNT`),
  KEY `IDX_FM_EMPLOYEE_ACTIVE` (`TENANT_ID`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  CONSTRAINT `CK_FM_EMP_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_EMP_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_employee`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_employee` WRITE;
/*!40000 ALTER TABLE `fm_employee` DISABLE KEYS */;
INSERT INTO `fm_employee` VALUES
('2250e92c-8cab-11f1-bf0e-4b61e379b27b','A01','a3bb53cb-fbb1-4c9d-870b-9d2e39d17cba','A0004','tester','林永山','aaa@aaa.org','0800888222','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 14:43:26.823',NULL,NULL),
('925cf380-8caa-11f1-bf0e-3b1efcd0c2a7','A01','b5a80859-2382-44ee-967c-5969ed3457d4','A0001','admin','Administrator','chen.xin.nien@gmail.com','0800956956','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2199-12-31 23:59:00.000','','admin','2026-07-31 14:39:25.310',NULL,NULL),
('c657aefb-8caa-11f1-bf0e-e7465f4ccf97','A01','8bf5c5c3-809c-45c4-9a43-9e97696a3845','A0002','tiffany','王美女','aaa@aaa.org','0800999444','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 14:40:52.516',NULL,NULL),
('f3594a46-8caa-11f1-bf0e-91b42c41b1a5','A01','a770ca51-0137-409f-af3a-af895f22ee50','A0003','steven','張大山','aaa@aaa.org','0800999111','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 14:42:08.024',NULL,NULL);
/*!40000 ALTER TABLE `fm_employee` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_employee_duty`
--

DROP TABLE IF EXISTS `fm_employee_duty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_employee_duty` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `EMPLOYEE_DUTY_ID` varchar(36) NOT NULL,
  `EMPLOYEE_ORG_ASSIGNMENT_ID` varchar(36) NOT NULL,
  `DUTY_ID` varchar(36) NOT NULL,
  `IS_PRIMARY` char(1) NOT NULL DEFAULT 'N',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_ED_ID` (`TENANT_ID`,`EMPLOYEE_DUTY_ID`),
  UNIQUE KEY `UK_FM_ED_REL` (`TENANT_ID`,`EMPLOYEE_ORG_ASSIGNMENT_ID`,`DUTY_ID`,`EFFECTIVE_FROM`),
  KEY `IDX_FM_ED_RESOLVE` (`TENANT_ID`,`DUTY_ID`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  CONSTRAINT `CK_FM_ED_PRIMARY` CHECK (`IS_PRIMARY` in ('Y','N')),
  CONSTRAINT `CK_FM_ED_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_ED_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_employee_duty`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_employee_duty` WRITE;
/*!40000 ALTER TABLE `fm_employee_duty` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_employee_duty` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_employee_org_assignment`
--

DROP TABLE IF EXISTS `fm_employee_org_assignment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_employee_org_assignment` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `EMPLOYEE_ORG_ASSIGNMENT_ID` varchar(36) NOT NULL,
  `EMPLOYEE_ID` varchar(36) NOT NULL,
  `ORG_UNIT_ID` varchar(36) NOT NULL,
  `TITLE_ID` varchar(36) NOT NULL,
  `MANAGER_SOURCE` varchar(30) NOT NULL DEFAULT 'ORG_HEAD',
  `DIRECT_MANAGER_ASSIGNMENT_ID` varchar(36) DEFAULT NULL,
  `IS_PRIMARY` char(1) NOT NULL DEFAULT 'N',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_EOA_ID` (`TENANT_ID`,`EMPLOYEE_ORG_ASSIGNMENT_ID`),
  UNIQUE KEY `UK_FM_EOA_REL` (`TENANT_ID`,`EMPLOYEE_ID`,`ORG_UNIT_ID`,`EFFECTIVE_FROM`),
  KEY `IDX_FM_EOA_PRIMARY` (`TENANT_ID`,`EMPLOYEE_ID`,`IS_PRIMARY`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  KEY `IDX_FM_EOA_MANAGER` (`TENANT_ID`,`DIRECT_MANAGER_ASSIGNMENT_ID`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  CONSTRAINT `CK_FM_EOA_MANAGER` CHECK (`MANAGER_SOURCE` in ('ORG_HEAD','PARENT_HEAD','EXPLICIT','NONE')),
  CONSTRAINT `CK_FM_EOA_MANAGER_REF` CHECK (`MANAGER_SOURCE` = 'EXPLICIT' and `DIRECT_MANAGER_ASSIGNMENT_ID` is not null or `MANAGER_SOURCE` <> 'EXPLICIT' and `DIRECT_MANAGER_ASSIGNMENT_ID` is null),
  CONSTRAINT `CK_FM_EOA_PRIMARY` CHECK (`IS_PRIMARY` in ('Y','N')),
  CONSTRAINT `CK_FM_EOA_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_EOA_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_employee_org_assignment`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_employee_org_assignment` WRITE;
/*!40000 ALTER TABLE `fm_employee_org_assignment` DISABLE KEYS */;
INSERT INTO `fm_employee_org_assignment` VALUES
('32b18c97-8e3a-11f1-b073-cd1c8aecc952','A01','d15a5922-9db1-47b1-bb5c-071351e6eef5','8bf5c5c3-809c-45c4-9a43-9e97696a3845','121bbf1f-9fde-499d-95a6-b4d59645ccb2','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-08-02 14:20:03.494',NULL,NULL),
('fee13c78-8e39-11f1-b073-ab427b7f5058','A01','1f453b21-fe51-4834-90e8-a068ef63b636','b5a80859-2382-44ee-967c-5969ed3457d4','e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-08-02 14:18:36.566',NULL,NULL);
/*!40000 ALTER TABLE `fm_employee_org_assignment` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_form_data`
--

DROP TABLE IF EXISTS `fm_form_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_form_data` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `FORM_DATA_ID` varchar(36) NOT NULL,
  `FORM_ID` varchar(36) NOT NULL,
  `FORM_VERSION_NO` int(11) NOT NULL,
  `BUSINESS_KEY` varchar(100) NOT NULL,
  `OWNER_ACCOUNT` varchar(24) NOT NULL,
  `OWNER_ORG_UNIT_ID` varchar(36) NOT NULL,
  `DATA_CONTENT` longtext NOT NULL,
  `DATA_STATUS` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `REVISION_NO` int(11) NOT NULL DEFAULT 1,
  `LOCK_VERSION` int(11) NOT NULL DEFAULT 0,
  `SUBMITTED_DATE` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_FORM_DATA_ID` (`TENANT_ID`,`FORM_DATA_ID`),
  UNIQUE KEY `UK_FM_FORM_DATA_BK` (`TENANT_ID`,`BUSINESS_KEY`),
  KEY `IDX_FM_FORM_DATA_OWNER` (`TENANT_ID`,`OWNER_ACCOUNT`,`DATA_STATUS`,`CDATE`),
  CONSTRAINT `CK_FM_FORM_DATA_STATUS` CHECK (`DATA_STATUS` in ('DRAFT','SUBMITTED','RETURNED','COMPLETED','REJECTED','CANCELLED')),
  CONSTRAINT `CK_FM_FORM_DATA_REV` CHECK (`REVISION_NO` > 0 and `LOCK_VERSION` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_form_data`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_form_data` WRITE;
/*!40000 ALTER TABLE `fm_form_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_form_data` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_form_def`
--

DROP TABLE IF EXISTS `fm_form_def`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_form_def` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `FORM_ID` varchar(36) NOT NULL,
  `FORM_CODE` varchar(50) NOT NULL,
  `FORM_NAME` varchar(150) NOT NULL,
  `CURRENT_VERSION_NO` int(11) NOT NULL DEFAULT 0,
  `STATUS` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_FD_ID` (`TENANT_ID`,`FORM_ID`),
  UNIQUE KEY `UK_FM_FD_CODE` (`TENANT_ID`,`FORM_CODE`),
  CONSTRAINT `CK_FM_FD_STATUS` CHECK (`STATUS` in ('DRAFT','PUBLISHED','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_form_def`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_form_def` WRITE;
/*!40000 ALTER TABLE `fm_form_def` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_form_def` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_form_snapshot`
--

DROP TABLE IF EXISTS `fm_form_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_form_snapshot` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `FORM_SNAPSHOT_ID` varchar(36) NOT NULL,
  `FORM_DATA_ID` varchar(36) NOT NULL,
  `PROCESS_INSTANCE_ID` varchar(64) NOT NULL,
  `TASK_ID` varchar(64) DEFAULT NULL,
  `ACTION_TYPE` varchar(30) NOT NULL,
  `FORM_VERSION_NO` int(11) NOT NULL,
  `REVISION_NO` int(11) NOT NULL,
  `DATA_CONTENT` longtext NOT NULL,
  `CONTENT_SHA256` char(64) NOT NULL,
  `SNAPSHOT_DATE` datetime(3) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_FS_ID` (`TENANT_ID`,`FORM_SNAPSHOT_ID`),
  KEY `IDX_FM_FS_INSTANCE` (`TENANT_ID`,`PROCESS_INSTANCE_ID`,`TASK_ID`,`SNAPSHOT_DATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_form_snapshot`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_form_snapshot` WRITE;
/*!40000 ALTER TABLE `fm_form_snapshot` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_form_snapshot` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_form_version`
--

DROP TABLE IF EXISTS `fm_form_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_form_version` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `FORM_ID` varchar(36) NOT NULL,
  `VERSION_NO` int(11) NOT NULL,
  `VERSION_STATUS` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `SCHEMA_CONTENT` longtext NOT NULL,
  `UI_SCHEMA_CONTENT` longtext NOT NULL,
  `CONTENT_SHA256` char(64) NOT NULL,
  `PUBLISHED_BY` varchar(24) DEFAULT NULL,
  `PUBLISHED_DATE` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_FV_VERSION` (`TENANT_ID`,`FORM_ID`,`VERSION_NO`),
  CONSTRAINT `CK_FM_FV_STATUS` CHECK (`VERSION_STATUS` in ('DRAFT','PUBLISHED','RETIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_form_version`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_form_version` WRITE;
/*!40000 ALTER TABLE `fm_form_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_form_version` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_notification`
--

DROP TABLE IF EXISTS `fm_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_notification` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `NOTIFICATION_ID` varchar(36) NOT NULL,
  `RECIPIENT_ACCOUNT` varchar(24) NOT NULL,
  `CHANNEL_TYPE` varchar(20) NOT NULL DEFAULT 'IN_APP',
  `EVENT_TYPE` varchar(50) NOT NULL,
  `SUBJECT` varchar(255) NOT NULL,
  `CONTENT_TEXT` longtext NOT NULL,
  `REFERENCE_TYPE` varchar(30) DEFAULT NULL,
  `REFERENCE_ID` varchar(64) DEFAULT NULL,
  `DELIVERY_STATUS` varchar(20) NOT NULL DEFAULT 'PENDING',
  `RETRY_COUNT` int(11) NOT NULL DEFAULT 0,
  `NEXT_RETRY_DATE` datetime(3) DEFAULT NULL,
  `SENT_DATE` datetime(3) DEFAULT NULL,
  `READ_DATE` datetime(3) DEFAULT NULL,
  `LAST_ERROR` varchar(2000) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_NOTIFICATION_ID` (`TENANT_ID`,`NOTIFICATION_ID`),
  KEY `IDX_FM_NOTIFICATION_DELIVERY` (`DELIVERY_STATUS`,`NEXT_RETRY_DATE`),
  KEY `IDX_FM_NOTIFICATION_USER` (`TENANT_ID`,`RECIPIENT_ACCOUNT`,`READ_DATE`,`CDATE`),
  CONSTRAINT `CK_FM_NOTIFICATION_CHANNEL` CHECK (`CHANNEL_TYPE` in ('IN_APP','EMAIL')),
  CONSTRAINT `CK_FM_NOTIFICATION_STATUS` CHECK (`DELIVERY_STATUS` in ('PENDING','SENT','FAILED','READ')),
  CONSTRAINT `CK_FM_NOTIFICATION_RETRY` CHECK (`RETRY_COUNT` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_notification`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_notification` WRITE;
/*!40000 ALTER TABLE `fm_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_notification` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_org_approval_level`
--

DROP TABLE IF EXISTS `fm_org_approval_level`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_org_approval_level` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `APPROVAL_LEVEL_ID` varchar(36) NOT NULL,
  `LEVEL_SCHEME_ID` varchar(36) NOT NULL,
  `LEVEL_CODE` varchar(30) NOT NULL,
  `LEVEL_NAME` varchar(100) NOT NULL,
  `LEVEL_ORDER` int(11) NOT NULL,
  `IS_HIGHEST_LEVEL` char(1) NOT NULL DEFAULT 'N',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_LEVEL_ID` (`TENANT_ID`,`APPROVAL_LEVEL_ID`),
  UNIQUE KEY `UK_FM_LEVEL_CODE` (`TENANT_ID`,`LEVEL_SCHEME_ID`,`LEVEL_CODE`),
  UNIQUE KEY `UK_FM_LEVEL_ORDER` (`TENANT_ID`,`LEVEL_SCHEME_ID`,`LEVEL_ORDER`),
  CONSTRAINT `CK_FM_LEVEL_ORDER` CHECK (`LEVEL_ORDER` >= 0),
  CONSTRAINT `CK_FM_LEVEL_HIGHEST` CHECK (`IS_HIGHEST_LEVEL` in ('Y','N')),
  CONSTRAINT `CK_FM_LEVEL_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_LEVEL_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_org_approval_level`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_org_approval_level` WRITE;
/*!40000 ALTER TABLE `fm_org_approval_level` DISABLE KEYS */;
INSERT INTO `fm_org_approval_level` VALUES
('62ed3626-8cb8-11f1-83f5-fd81193cc310','A01','7b51d9c8-0767-48fb-924a-0884974c070a','5bb2348e-e89b-46b1-8f06-03e53808b467','L8','一般員工級',80,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:18:18.678',NULL,NULL),
('7fe30dd1-8cb2-11f1-b7ad-891a41bc6c37','A01','f5021aa7-a826-4915-8f8a-194e7502daf5','5bb2348e-e89b-46b1-8f06-03e53808b467','L1','董事層級',10,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.286','admin','2026-07-31 16:18:18.662'),
('7fe3d122-8cb2-11f1-b7ad-8b7b9b21d98a','A01','974d1f9b-960f-4458-8b6c-baa59c5b2362','5bb2348e-e89b-46b1-8f06-03e53808b467','L2','總經理層級',20,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.291','admin','2026-07-31 16:18:18.665'),
('7fe44653-8cb2-11f1-b7ad-45a1fed05baa','A01','73cbfd4e-0b39-4b86-8585-b495cc490b56','5bb2348e-e89b-46b1-8f06-03e53808b467','L3','副總層級',30,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.293','admin','2026-07-31 16:18:18.667'),
('7fe49474-8cb2-11f1-b7ad-63913f0ae4d2','A01','6bd2f834-e0bb-4d3e-b788-ad558c658aab','5bb2348e-e89b-46b1-8f06-03e53808b467','L4','處長層級',40,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.295','admin','2026-07-31 16:18:18.669'),
('7fe4e295-8cb2-11f1-b7ad-0967d0d314fc','A01','64dd152e-e547-43c3-b661-0db2221e5d03','5bb2348e-e89b-46b1-8f06-03e53808b467','L5','經理層級',50,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.298','admin','2026-07-31 16:18:18.672'),
('7fe530b6-8cb2-11f1-b7ad-93a37833f35a','A01','e6529131-b94f-456e-add8-16a2eaa2e662','5bb2348e-e89b-46b1-8f06-03e53808b467','L6','副理層級',60,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.300','admin','2026-07-31 16:18:18.673'),
('7fe5a5e7-8cb2-11f1-b7ad-ad8fe04eba18','A01','e1e62bd5-6d95-4d86-817a-f9f734a2d81d','5bb2348e-e89b-46b1-8f06-03e53808b467','L7','課組層級',70,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.302','admin','2026-07-31 16:18:18.676');
/*!40000 ALTER TABLE `fm_org_approval_level` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_org_duty`
--

DROP TABLE IF EXISTS `fm_org_duty`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_org_duty` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `DUTY_ID` varchar(36) NOT NULL,
  `ORG_UNIT_ID` varchar(36) NOT NULL,
  `DUTY_CODE` varchar(30) NOT NULL,
  `DUTY_NAME` varchar(100) NOT NULL,
  `DUTY_TYPE` varchar(30) NOT NULL DEFAULT 'APPROVAL',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_DUTY_ID` (`TENANT_ID`,`DUTY_ID`),
  UNIQUE KEY `UK_FM_DUTY_CODE` (`TENANT_ID`,`ORG_UNIT_ID`,`DUTY_CODE`),
  CONSTRAINT `CK_FM_DUTY_TYPE` CHECK (`DUTY_TYPE` in ('APPROVAL','REVIEW','NOTIFY')),
  CONSTRAINT `CK_FM_DUTY_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_DUTY_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_org_duty`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_org_duty` WRITE;
/*!40000 ALTER TABLE `fm_org_duty` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_org_duty` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_org_level_scheme`
--

DROP TABLE IF EXISTS `fm_org_level_scheme`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_org_level_scheme` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `LEVEL_SCHEME_ID` varchar(36) NOT NULL,
  `SCHEME_CODE` varchar(30) NOT NULL,
  `SCHEME_NAME` varchar(100) NOT NULL,
  `IS_DEFAULT` char(1) NOT NULL DEFAULT 'N',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_LEVEL_SCHEME_ID` (`TENANT_ID`,`LEVEL_SCHEME_ID`),
  UNIQUE KEY `UK_FM_LEVEL_SCHEME_CODE` (`TENANT_ID`,`SCHEME_CODE`),
  CONSTRAINT `CK_FM_LS_DEFAULT` CHECK (`IS_DEFAULT` in ('Y','N')),
  CONSTRAINT `CK_FM_LS_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_LS_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_org_level_scheme`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_org_level_scheme` WRITE;
/*!40000 ALTER TABLE `fm_org_level_scheme` DISABLE KEYS */;
INSERT INTO `fm_org_level_scheme` VALUES
('7fe27190-8cb2-11f1-b7ad-97571f6a751e','A01','5bb2348e-e89b-46b1-8f06-03e53808b467','A01_ORG_LEVEL','台灣廠區部門層級','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 15:36:10.282','admin','2026-07-31 16:18:18.655');
/*!40000 ALTER TABLE `fm_org_level_scheme` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_org_title`
--

DROP TABLE IF EXISTS `fm_org_title`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_org_title` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `TITLE_ID` varchar(36) NOT NULL,
  `TITLE_CODE` varchar(30) NOT NULL,
  `TITLE_NAME` varchar(100) NOT NULL,
  `APPROVAL_LEVEL_ID` varchar(36) NOT NULL,
  `IS_MANAGER_TITLE` char(1) NOT NULL DEFAULT 'N',
  `SORT_NO` int(11) NOT NULL DEFAULT 0,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TITLE_ID` (`TENANT_ID`,`TITLE_ID`),
  UNIQUE KEY `UK_FM_TITLE_CODE` (`TENANT_ID`,`TITLE_CODE`),
  KEY `IDX_FM_TITLE_LEVEL` (`TENANT_ID`,`APPROVAL_LEVEL_ID`,`STATUS`),
  CONSTRAINT `CK_FM_TITLE_MANAGER` CHECK (`IS_MANAGER_TITLE` in ('Y','N')),
  CONSTRAINT `CK_FM_TITLE_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_TITLE_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_org_title`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_org_title` WRITE;
/*!40000 ALTER TABLE `fm_org_title` DISABLE KEYS */;
INSERT INTO `fm_org_title` VALUES
('01ecf9fd-8e33-11f1-b1f6-fb482084edb9','A01','869206f0-661f-4f9b-8a06-d3a35385e2de','J04','處長','6bd2f834-e0bb-4d3e-b788-ad558c658aab','Y',4,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:28:35.198',NULL,NULL),
('3160a6b0-8e33-11f1-b1f6-6f6e821352ba','A01','8e6ac20e-1015-442b-a731-9d1408cbfbc2','J05','經理','64dd152e-e547-43c3-b661-0db2221e5d03','Y',5,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:29:54.810',NULL,NULL),
('41be8fe3-8e33-11f1-b1f6-d36711d482a8','A01','2176a3a7-e085-4b41-8d26-c4ec08a44573','J06','副理','e6529131-b94f-456e-add8-16a2eaa2e662','Y',6,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:30:22.268','admin','2026-08-02 13:34:37.736'),
('5c7b5986-8e33-11f1-b1f6-2f6a0d96b04c','A01','3fe52a9d-86a9-42ab-9736-3c6a47e77436','J07','課長','e1e62bd5-6d95-4d86-817a-f9f734a2d81d','Y',70,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:31:07.126','admin','2026-08-02 13:35:16.948'),
('80f7b659-8e33-11f1-b1f6-637ef6423a2f','A01','71005fe7-c620-4f4d-a21c-2bf8cdd67ef4','J0701','組長','e1e62bd5-6d95-4d86-817a-f9f734a2d81d','Y',71,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:32:08.339','admin','2026-08-02 13:35:24.055'),
('9cd5805c-8e33-11f1-b1f6-f3140eb47b7b','A01','86751be1-e9e9-4908-b6f2-e264d105f4e0','J0801','專員','7b51d9c8-0767-48fb-924a-0884974c070a','N',80,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:32:55.091','admin','2026-08-02 13:34:57.198'),
('af8a9fff-8e33-11f1-b1f6-9bd600474ec0','A01','440aeddb-f2e4-485d-a3ed-7e92cb52380b','J0802','一般','7b51d9c8-0767-48fb-924a-0884974c070a','N',81,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:33:26.478','admin','2026-08-02 13:35:06.189'),
('b1305614-8e32-11f1-b1f6-c95c3d3e22f5','A01','6e3b108b-6cc3-4648-8412-ccf7bbd96691','J01','董事長','f5021aa7-a826-4915-8f8a-194e7502daf5','Y',0,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:26:19.745',NULL,NULL),
('d0590557-8e32-11f1-b1f6-bb3f4510e0a2','A01','55fcf5e9-5fb0-4de7-b17d-a28b9e326eae','J02','總經理','974d1f9b-960f-4458-8b6c-baa59c5b2362','Y',1,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-08-02 13:27:12.020',NULL,NULL),
('e86491aa-8e32-11f1-b1f6-a73a4f16cc7c','A01','8e95bf09-ebac-42e4-b933-a9fd25f5d96c','J03','副總經理','73cbfd4e-0b39-4b86-8585-b495cc490b56','Y',2,'ACTIVE','2026-01-01 00:00:00.000','2099-01-01 00:00:00.000','','admin','2026-08-02 13:27:52.361','admin','2026-08-02 13:33:50.916');
/*!40000 ALTER TABLE `fm_org_title` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_org_unit`
--

DROP TABLE IF EXISTS `fm_org_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_org_unit` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ORG_UNIT_ID` varchar(36) NOT NULL,
  `UNIT_CODE` varchar(30) NOT NULL,
  `CURRENT_VERSION_NO` int(11) NOT NULL DEFAULT 1,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_ORG_UNIT_ID` (`TENANT_ID`,`ORG_UNIT_ID`),
  UNIQUE KEY `UK_FM_ORG_UNIT_CODE` (`TENANT_ID`,`UNIT_CODE`),
  CONSTRAINT `CK_FM_OU_VERSION` CHECK (`CURRENT_VERSION_NO` > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_org_unit`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_org_unit` WRITE;
/*!40000 ALTER TABLE `fm_org_unit` DISABLE KEYS */;
INSERT INTO `fm_org_unit` VALUES
('012de068-8cbc-11f1-83f5-492af8f82375','A01','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','00530',1,'admin','2026-07-31 16:44:12.672',NULL,NULL),
('06be29a4-8cb9-11f1-83f5-fb0c5d4c8f0d','A01','b7ff38ab-98a8-44d0-8318-85145e9e5b57','00110',1,'admin','2026-07-31 16:22:53.516',NULL,NULL),
('1173ca18-8cbb-11f1-83f5-6134e30c8ca7','A01','db43606f-64d2-4304-8850-fbd9d8956d1f','00430',1,'admin','2026-07-31 16:37:30.477',NULL,NULL),
('1a47d758-8cb9-11f1-83f5-f93cda5f89a4','A01','11e2a4e7-30a4-4ced-8454-9753ce4a1b60','00120',1,'admin','2026-07-31 16:23:26.295',NULL,NULL),
('2a7755af-8cbc-11f1-83f5-d531ec020c2e','A01','92f87544-d491-4a96-a8d3-425795376136','00600',1,'admin','2026-07-31 16:45:21.941',NULL,NULL),
('2df5b1b5-8cbd-11f1-83f5-69c22fe17cea','A01','2e50d437-f16d-4a63-8a6b-69e6bf564e7e','00800',1,'admin','2026-07-31 16:52:37.298',NULL,NULL),
('35361af5-8cba-11f1-83f5-81431cd9bbea','A01','f092bb40-d87f-407b-9b12-2221470489a3','00300',1,'admin','2026-07-31 16:31:20.974',NULL,NULL),
('3f84e443-8cbc-11f1-83f5-0d8445e009e0','A01','e2043fd6-978c-4341-a1fe-7751fecb9b2b','00610',1,'admin','2026-07-31 16:45:57.262',NULL,NULL),
('49abaf49-8cba-11f1-83f5-210ca386e45e','A01','4f4a3de9-3b13-44f5-a783-19506696d7d7','00310',1,'admin','2026-07-31 16:31:55.299',NULL,NULL),
('4cb761ca-8cbd-11f1-83f5-872b016d32fe','A01','121bbf1f-9fde-499d-95a6-b4d59645ccb2','00810',1,'admin','2026-07-31 16:53:28.899',NULL,NULL),
('5ee4e1bd-8cba-11f1-83f5-07d2deb9158e','A01','9c2c3ed6-2c5a-4678-a585-d8d6aef6d2c7','00320',1,'admin','2026-07-31 16:32:30.907',NULL,NULL),
('607255df-8cb9-11f1-83f5-b7667bf0e834','A01','c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e','00200',1,'admin','2026-07-31 16:25:24.015',NULL,NULL),
('64c94c0b-8cbc-11f1-83f5-a1652c4a9471','A01','84ada4ab-426a-4b15-a2c0-e31676c5b183','00620',1,'admin','2026-07-31 16:46:59.785',NULL,NULL),
('6c13560e-8cbd-11f1-83f5-532f53ff46bf','A01','210f59af-a6a8-4133-adda-c927af8d69c2','00820',1,'admin','2026-07-31 16:54:21.511',NULL,NULL),
('7cf586a3-8cb9-11f1-83f5-a390809b6349','A01','ada00d6d-df68-4c9b-84d0-28b8ee8c0f58','00210',1,'admin','2026-07-31 16:26:11.850',NULL,NULL),
('7e0af8f2-8cba-11f1-83f5-09d42cb9a484','A01','c7db44c2-4d03-4eb0-b3be-2d569c3965bb','00330',1,'admin','2026-07-31 16:33:23.165',NULL,NULL),
('81d7ffa2-8cbd-11f1-83f5-0360b630536f','A01','e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d','00830',1,'admin','2026-07-31 16:54:58.033',NULL,NULL),
('8304b51f-8cbc-11f1-83f5-4d58b4b190f7','A01','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f','00630',1,'admin','2026-07-31 16:47:50.506',NULL,NULL),
('8a91f43f-8cbb-11f1-83f5-bbff3e527378','A01','60f6bfc3-2436-4002-a152-db891073629d','00500',1,'admin','2026-07-31 16:40:53.679',NULL,NULL),
('a317a43a-8cb9-11f1-83f5-077b1c31b631','A01','75b2cf0e-c377-4261-b0e6-74295af900e1','00220',1,'admin','2026-07-31 16:27:15.828',NULL,NULL),
('a618a575-8cb8-11f1-83f5-bd3ded83084d','A01','7f07391d-fa5a-4acc-9e85-627099a4ef6f','00001',1,'admin','2026-07-31 16:20:11.370',NULL,NULL),
('b2b1a4f9-8cbd-11f1-83f5-a1316a183ea9','A01','087af37e-77ff-4480-8f3e-0abf9c844804','00900',1,'admin','2026-07-31 16:56:19.990',NULL,NULL),
('ba8060de-8cb9-11f1-83f5-e58a2ffa9c55','A01','650841c6-d104-46da-804d-b7eb45cfbc67','00230',1,'admin','2026-07-31 16:27:55.101',NULL,NULL),
('bd5c1439-8cba-11f1-83f5-f525856d3529','A01','f27a7a23-bea4-4722-8be7-59beb4939d1a','00400',1,'admin','2026-07-31 16:35:09.394',NULL,NULL),
('c68f5889-8cb8-11f1-83f5-e593951b61f5','A01','8560d228-540d-4e8a-8901-e21a0e3769cd','00002',1,'admin','2026-07-31 16:21:05.835',NULL,NULL),
('cadadd10-8cbb-11f1-83f5-87cf4fd3a95a','A01','d4e31f21-45fd-4a05-8272-d60e7a0688cb','00510',1,'admin','2026-07-31 16:42:41.531',NULL,NULL),
('cb5aa9cd-8cbd-11f1-83f5-f93dfd11a74f','A01','e42fa17a-18a6-41e2-b15e-a7f77ad24e03','00910',1,'admin','2026-07-31 16:57:01.363',NULL,NULL),
('d0e7d336-8cbc-11f1-83f5-01d9cf86ebad','A01','bb772e13-7d41-41fc-88a0-a7dbc9b6f743','00700',1,'admin','2026-07-31 16:50:01.179',NULL,NULL),
('e2302e10-8cba-11f1-83f5-d7a06663b7d3','A01','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','00410',1,'admin','2026-07-31 16:36:11.181',NULL,NULL),
('e56f1014-8cbb-11f1-83f5-09dadc0b52b7','A01','f5c52e35-64a9-451b-978f-fe8410daecc8','00520',1,'admin','2026-07-31 16:43:26.123',NULL,NULL),
('e8275cfa-8cbc-11f1-83f5-3fbd2e60192c','A01','6dfafa1c-4602-41bf-ac74-0ce1eebb63c1','00710',1,'admin','2026-07-31 16:50:40.183',NULL,NULL),
('e88bf5e1-8cbd-11f1-83f5-875d62173ec6','A01','cd35e7e0-8d1f-4f0b-bac9-2fd7e66777b8','00920',1,'admin','2026-07-31 16:57:50.339',NULL,NULL),
('ec5636c0-8cb8-11f1-83f5-35469b9db05f','A01','e0d58477-af58-4cb0-a2c7-ea9429410572','00100',1,'admin','2026-07-31 16:22:09.215',NULL,NULL),
('f7ae58c4-8cba-11f1-83f5-5fa95477e4c1','A01','7f41e8b3-cace-419e-93bf-c2b78da807be','00420',1,'admin','2026-07-31 16:36:47.240',NULL,NULL),
('fee337ce-8cbc-11f1-83f5-adf85c1337f1','A01','5bf5579b-bf06-4e05-a039-dab24dd48846','00720',1,'admin','2026-07-31 16:51:18.324',NULL,NULL);
/*!40000 ALTER TABLE `fm_org_unit` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_org_unit_head`
--

DROP TABLE IF EXISTS `fm_org_unit_head`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_org_unit_head` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ORG_UNIT_HEAD_ID` varchar(36) NOT NULL,
  `ORG_UNIT_ID` varchar(36) NOT NULL,
  `EMPLOYEE_ID` varchar(36) NOT NULL,
  `HEAD_TYPE` varchar(30) NOT NULL,
  `PRIORITY` int(11) NOT NULL DEFAULT 100,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_OUH_ID` (`TENANT_ID`,`ORG_UNIT_HEAD_ID`),
  UNIQUE KEY `UK_FM_OUH_REL` (`TENANT_ID`,`ORG_UNIT_ID`,`EMPLOYEE_ID`,`HEAD_TYPE`,`EFFECTIVE_FROM`),
  KEY `IDX_FM_OUH_RESOLVE` (`TENANT_ID`,`ORG_UNIT_ID`,`HEAD_TYPE`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`,`PRIORITY`),
  CONSTRAINT `CK_FM_OUH_TYPE` CHECK (`HEAD_TYPE` in ('HEAD','DEPUTY_HEAD','ACTING_HEAD')),
  CONSTRAINT `CK_FM_OUH_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_OUH_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_org_unit_head`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_org_unit_head` WRITE;
/*!40000 ALTER TABLE `fm_org_unit_head` DISABLE KEYS */;
INSERT INTO `fm_org_unit_head` VALUES
('ce2a816d-8e3c-11f1-a6ca-8b1822d4d94c','A01','d85ece41-c37e-44a6-8d3f-b16ebbecce01','121bbf1f-9fde-499d-95a6-b4d59645ccb2','8bf5c5c3-809c-45c4-9a43-9e97696a3845','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000',NULL,'','admin','2026-08-02 14:38:43.328',NULL,NULL);
/*!40000 ALTER TABLE `fm_org_unit_head` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_org_unit_version`
--

DROP TABLE IF EXISTS `fm_org_unit_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_org_unit_version` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ORG_UNIT_ID` varchar(36) NOT NULL,
  `VERSION_NO` int(11) NOT NULL,
  `PARENT_ORG_UNIT_ID` varchar(36) DEFAULT NULL,
  `UNIT_NAME` varchar(150) NOT NULL,
  `SHORT_NAME` varchar(80) DEFAULT NULL,
  `UNIT_TYPE` varchar(30) NOT NULL DEFAULT 'DEPARTMENT',
  `TREE_DEPTH` int(11) NOT NULL,
  `PATH` varchar(2000) NOT NULL,
  `SORT_NO` int(11) NOT NULL DEFAULT 0,
  `IS_VIRTUAL` char(1) NOT NULL DEFAULT 'N',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_OUV_VERSION` (`TENANT_ID`,`ORG_UNIT_ID`,`VERSION_NO`),
  KEY `IDX_FM_OUV_TREE` (`TENANT_ID`,`PARENT_ORG_UNIT_ID`,`STATUS`,`SORT_NO`),
  CONSTRAINT `CK_FM_OUV_VERSION` CHECK (`VERSION_NO` > 0),
  CONSTRAINT `CK_FM_OUV_DEPTH` CHECK (`TREE_DEPTH` >= 0),
  CONSTRAINT `CK_FM_OUV_VIRTUAL` CHECK (`IS_VIRTUAL` in ('Y','N')),
  CONSTRAINT `CK_FM_OUV_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_OUV_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_org_unit_version`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_org_unit_version` WRITE;
/*!40000 ALTER TABLE `fm_org_unit_version` DISABLE KEYS */;
INSERT INTO `fm_org_unit_version` VALUES
('012e0779-8cbc-11f1-83f5-11f03095dcee','A01','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749',1,'d4e31f21-45fd-4a05-8272-d60e7a0688cb','出貨檢驗課','出貨檢','DEPARTMENT',5,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/60f6bfc3-2436-4002-a152-db891073629d/d4e31f21-45fd-4a05-8272-d60e7a0688cb/3e5f4331-6ed9-4fe4-80ae-6aebc9b27749/',502,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:44:12.674',NULL,NULL),
('06be77c5-8cb9-11f1-83f5-053cf2da936a','A01','b7ff38ab-98a8-44d0-8318-85145e9e5b57',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','經營企劃室','經營企劃','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/b7ff38ab-98a8-44d0-8318-85145e9e5b57/',0,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:22:53.518',NULL,NULL),
('1173f129-8cbb-11f1-83f5-bdccbcb348dd','A01','db43606f-64d2-4304-8850-fbd9d8956d1f',1,'f27a7a23-bea4-4722-8be7-59beb4939d1a','設備維護部','設備維護','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f27a7a23-bea4-4722-8be7-59beb4939d1a/db43606f-64d2-4304-8850-fbd9d8956d1f/',42,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:37:30.479',NULL,NULL),
('1a47fe69-8cb9-11f1-83f5-7144a438811b','A01','11e2a4e7-30a4-4ced-8454-9753ce4a1b60',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','稽核室','稽核','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/11e2a4e7-30a4-4ced-8454-9753ce4a1b60/',1,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:23:26.297',NULL,NULL),
('2a77a3d0-8cbc-11f1-83f5-5562119697fe','A01','92f87544-d491-4a96-a8d3-425795376136',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','供應鏈管理處','供應鏈處','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/92f87544-d491-4a96-a8d3-425795376136/',6,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:45:21.942',NULL,NULL),
('2df5ffd6-8cbd-11f1-83f5-d3c7d4f9d7b8','A01','2e50d437-f16d-4a63-8a6b-69e6bf564e7e',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','行政管理處','行政管理','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/2e50d437-f16d-4a63-8a6b-69e6bf564e7e/',8,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:52:37.300',NULL,NULL),
('35369026-8cba-11f1-83f5-410323ebd99e','A01','f092bb40-d87f-407b-9b12-2221470489a3',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','研發工程處','研發處','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f092bb40-d87f-407b-9b12-2221470489a3/',3,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:31:20.977',NULL,NULL),
('3f853264-8cbc-11f1-83f5-75e58e2bc4b3','A01','e2043fd6-978c-4341-a1fe-7751fecb9b2b',1,'92f87544-d491-4a96-a8d3-425795376136','採購部','採購','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/92f87544-d491-4a96-a8d3-425795376136/e2043fd6-978c-4341-a1fe-7751fecb9b2b/',60,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:45:57.263',NULL,NULL),
('49abfd6a-8cba-11f1-83f5-bfb6c5316543','A01','4f4a3de9-3b13-44f5-a783-19506696d7d7',1,'f092bb40-d87f-407b-9b12-2221470489a3','產品研發部','產品研發','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f092bb40-d87f-407b-9b12-2221470489a3/4f4a3de9-3b13-44f5-a783-19506696d7d7/',30,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:31:55.301',NULL,NULL),
('4cb788db-8cbd-11f1-83f5-c9c911894898','A01','121bbf1f-9fde-499d-95a6-b4d59645ccb2',1,'2e50d437-f16d-4a63-8a6b-69e6bf564e7e','人力資源部','人資','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/2e50d437-f16d-4a63-8a6b-69e6bf564e7e/121bbf1f-9fde-499d-95a6-b4d59645ccb2/',80,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:53:28.900',NULL,NULL),
('5ee52fde-8cba-11f1-83f5-9987d57c90d1','A01','9c2c3ed6-2c5a-4678-a585-d8d6aef6d2c7',1,'f092bb40-d87f-407b-9b12-2221470489a3','製程工程部','製程工程','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f092bb40-d87f-407b-9b12-2221470489a3/9c2c3ed6-2c5a-4678-a585-d8d6aef6d2c7/',31,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:32:30.908',NULL,NULL),
('6072a300-8cb9-11f1-83f5-21abbaa4403c','A01','c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','業務處','業務處','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e/',2,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:25:24.017',NULL,NULL),
('64c9731c-8cbc-11f1-83f5-8d04d46d6828','A01','84ada4ab-426a-4b15-a2c0-e31676c5b183',1,'92f87544-d491-4a96-a8d3-425795376136','物料管理部','物料管理','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/92f87544-d491-4a96-a8d3-425795376136/84ada4ab-426a-4b15-a2c0-e31676c5b183/',61,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:46:59.786',NULL,NULL),
('6c137d1f-8cbd-11f1-83f5-796993c1cdb6','A01','210f59af-a6a8-4133-adda-c927af8d69c2',1,'2e50d437-f16d-4a63-8a6b-69e6bf564e7e','總務部','總務','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/2e50d437-f16d-4a63-8a6b-69e6bf564e7e/210f59af-a6a8-4133-adda-c927af8d69c2/',81,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:54:21.512',NULL,NULL),
('7cf5d4c4-8cb9-11f1-83f5-2d82d581da32','A01','ada00d6d-df68-4c9b-84d0-28b8ee8c0f58',1,'c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e','國內業務部','國內業務','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e/ada00d6d-df68-4c9b-84d0-28b8ee8c0f58/',0,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:26:11.852',NULL,NULL),
('7e0b4713-8cba-11f1-83f5-99e3871895fc','A01','c7db44c2-4d03-4eb0-b3be-2d569c3965bb',1,'f092bb40-d87f-407b-9b12-2221470489a3','設備工程部','設備工程','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f092bb40-d87f-407b-9b12-2221470489a3/c7db44c2-4d03-4eb0-b3be-2d569c3965bb/',32,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:33:23.167',NULL,NULL),
('81d84dc3-8cbd-11f1-83f5-0752e5ee7b33','A01','e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d',1,'2e50d437-f16d-4a63-8a6b-69e6bf564e7e','資訊部','資訊','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/2e50d437-f16d-4a63-8a6b-69e6bf564e7e/e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d/',82,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:54:58.034',NULL,NULL),
('8304dc30-8cbc-11f1-83f5-a5116538cf65','A01','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f',1,'92f87544-d491-4a96-a8d3-425795376136','倉儲部','倉儲','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/92f87544-d491-4a96-a8d3-425795376136/5b95eafe-ce3d-4b83-82d5-4ac49808bf0f/',62,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:47:50.507',NULL,NULL),
('8a921b50-8cbb-11f1-83f5-1fc72f96f706','A01','60f6bfc3-2436-4002-a152-db891073629d',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','品質保證處','品保處','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/60f6bfc3-2436-4002-a152-db891073629d/',5,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:40:53.680',NULL,NULL),
('a317f25b-8cb9-11f1-83f5-8f2d01d56cab','A01','75b2cf0e-c377-4261-b0e6-74295af900e1',1,'c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e','國外業務部','國外業務','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e/75b2cf0e-c377-4261-b0e6-74295af900e1/',1,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:27:15.830',NULL,NULL),
('a618f396-8cb8-11f1-83f5-976bd93974f2','A01','7f07391d-fa5a-4acc-9e85-627099a4ef6f',1,NULL,'阿成金屬工業','阿成金屬','COMPANY',0,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/',0,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:20:11.373',NULL,NULL),
('b2b1f31a-8cbd-11f1-83f5-f51e82af9608','A01','087af37e-77ff-4480-8f3e-0abf9c844804',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','環安衛管理處','環安處','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/087af37e-77ff-4480-8f3e-0abf9c844804/',9,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:56:19.991',NULL,NULL),
('ba80aeff-8cb9-11f1-83f5-2ff606e276c5','A01','650841c6-d104-46da-804d-b7eb45cfbc67',1,'c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e','客戶服務部','客服部','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e/650841c6-d104-46da-804d-b7eb45cfbc67/',2,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:27:55.103',NULL,NULL),
('bd5c625a-8cba-11f1-83f5-27992cdc1583','A01','f27a7a23-bea4-4722-8be7-59beb4939d1a',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','生產製造處','製造處','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f27a7a23-bea4-4722-8be7-59beb4939d1a/',4,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:35:09.395',NULL,NULL),
('c68fa6aa-8cb8-11f1-83f5-f94fb30a6c13','A01','8560d228-540d-4e8a-8901-e21a0e3769cd',1,'7f07391d-fa5a-4acc-9e85-627099a4ef6f','董事長室','董事長室','DEPARTMENT',1,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/',0,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:21:05.837',NULL,NULL),
('cadb0421-8cbb-11f1-83f5-47985e1c3cf0','A01','d4e31f21-45fd-4a05-8272-d60e7a0688cb',1,'60f6bfc3-2436-4002-a152-db891073629d','品質管理部','品管部','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/60f6bfc3-2436-4002-a152-db891073629d/d4e31f21-45fd-4a05-8272-d60e7a0688cb/',50,'N','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','','admin','2026-07-31 16:42:41.532',NULL,NULL),
('cb5af7ee-8cbd-11f1-83f5-bf6feea13541','A01','e42fa17a-18a6-41e2-b15e-a7f77ad24e03',1,'087af37e-77ff-4480-8f3e-0abf9c844804','工業安全部','工安','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/087af37e-77ff-4480-8f3e-0abf9c844804/e42fa17a-18a6-41e2-b15e-a7f77ad24e03/',90,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:57:01.364',NULL,NULL),
('d0e7fa47-8cbc-11f1-83f5-83bcf8d1b672','A01','bb772e13-7d41-41fc-88a0-a7dbc9b6f743',1,'e0d58477-af58-4cb0-a2c7-ea9429410572','財務管理處','財務管理','DEPARTMENT',3,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/bb772e13-7d41-41fc-88a0-a7dbc9b6f743/',7,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:50:01.181',NULL,NULL),
('e2305521-8cba-11f1-83f5-85d49d25f08f','A01','92a905e6-4212-4aa4-ba07-1a2db0f4e39d',1,'f27a7a23-bea4-4722-8be7-59beb4939d1a','生產部','生產','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f27a7a23-bea4-4722-8be7-59beb4939d1a/92a905e6-4212-4aa4-ba07-1a2db0f4e39d/',40,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:36:11.183',NULL,NULL),
('e56f3725-8cbb-11f1-83f5-e542efce8267','A01','f5c52e35-64a9-451b-978f-fe8410daecc8',1,'d4e31f21-45fd-4a05-8272-d60e7a0688cb','進料檢驗課','進料課','DEPARTMENT',5,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/60f6bfc3-2436-4002-a152-db891073629d/d4e31f21-45fd-4a05-8272-d60e7a0688cb/f5c52e35-64a9-451b-978f-fe8410daecc8/',501,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:43:26.124',NULL,NULL),
('e827840b-8cbc-11f1-83f5-9d2a009cbcb7','A01','6dfafa1c-4602-41bf-ac74-0ce1eebb63c1',1,'bb772e13-7d41-41fc-88a0-a7dbc9b6f743','財務部','財務','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/bb772e13-7d41-41fc-88a0-a7dbc9b6f743/6dfafa1c-4602-41bf-ac74-0ce1eebb63c1/',70,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:50:40.184',NULL,NULL),
('e88c1cf2-8cbd-11f1-83f5-21ba1cc19cf9','A01','cd35e7e0-8d1f-4f0b-bac9-2fd7e66777b8',1,'087af37e-77ff-4480-8f3e-0abf9c844804','環境管理部','環境','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/087af37e-77ff-4480-8f3e-0abf9c844804/cd35e7e0-8d1f-4f0b-bac9-2fd7e66777b8/',91,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:57:50.341',NULL,NULL),
('ec56abf1-8cb8-11f1-83f5-8b04cb28dbd3','A01','e0d58477-af58-4cb0-a2c7-ea9429410572',1,'8560d228-540d-4e8a-8901-e21a0e3769cd','總經理室','總經理室','DEPARTMENT',2,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/',0,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:22:09.217',NULL,NULL),
('f7ae7fd5-8cba-11f1-83f5-b1b42f7de8ae','A01','7f41e8b3-cace-419e-93bf-c2b78da807be',1,'f27a7a23-bea4-4722-8be7-59beb4939d1a','生產管理部','生產管理','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/f27a7a23-bea4-4722-8be7-59beb4939d1a/7f41e8b3-cace-419e-93bf-c2b78da807be/',41,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:36:47.242',NULL,NULL),
('fee35edf-8cbc-11f1-83f5-b7cb2c6d4d8e','A01','5bf5579b-bf06-4e05-a039-dab24dd48846',1,'bb772e13-7d41-41fc-88a0-a7dbc9b6f743','會計部','會計','DEPARTMENT',4,'/7f07391d-fa5a-4acc-9e85-627099a4ef6f/8560d228-540d-4e8a-8901-e21a0e3769cd/e0d58477-af58-4cb0-a2c7-ea9429410572/bb772e13-7d41-41fc-88a0-a7dbc9b6f743/5bf5579b-bf06-4e05-a039-dab24dd48846/',71,'N','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 16:51:18.326',NULL,NULL);
/*!40000 ALTER TABLE `fm_org_unit_version` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_process_def`
--

DROP TABLE IF EXISTS `fm_process_def`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_process_def` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) NOT NULL,
  `PROCESS_KEY` varchar(100) NOT NULL,
  `PROCESS_NAME` varchar(150) NOT NULL,
  `CATEGORY` varchar(50) DEFAULT NULL,
  `CURRENT_VERSION_NO` int(11) NOT NULL DEFAULT 0,
  `STATUS` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_PD_ID` (`TENANT_ID`,`PROCESS_DEF_ID`),
  UNIQUE KEY `UK_FM_PD_KEY` (`TENANT_ID`,`PROCESS_KEY`),
  CONSTRAINT `CK_FM_PD_STATUS` CHECK (`STATUS` in ('DRAFT','PUBLISHED','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_process_def`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_process_def` WRITE;
/*!40000 ALTER TABLE `fm_process_def` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_process_def` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_process_instance`
--

DROP TABLE IF EXISTS `fm_process_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_process_instance` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `PROCESS_INSTANCE_ID` varchar(64) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) NOT NULL,
  `PROCESS_VERSION_NO` int(11) NOT NULL,
  `FLOWABLE_PROCESS_DEF_ID` varchar(64) NOT NULL,
  `BUSINESS_KEY` varchar(100) NOT NULL,
  `FORM_DATA_ID` varchar(36) NOT NULL,
  `INITIATOR_ACCOUNT` varchar(24) NOT NULL,
  `INITIATOR_ORG_UNIT_ID` varchar(36) NOT NULL,
  `INSTANCE_STATUS` varchar(20) NOT NULL DEFAULT 'RUNNING',
  `START_DATE` datetime(3) NOT NULL,
  `END_DATE` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_PI_FLOWABLE` (`TENANT_ID`,`PROCESS_INSTANCE_ID`),
  UNIQUE KEY `UK_FM_PI_BUSINESS` (`TENANT_ID`,`BUSINESS_KEY`),
  KEY `IDX_FM_PI_INITIATOR` (`TENANT_ID`,`INITIATOR_ACCOUNT`,`INSTANCE_STATUS`,`START_DATE`),
  CONSTRAINT `CK_FM_PI_STATUS` CHECK (`INSTANCE_STATUS` in ('RUNNING','COMPLETED','REJECTED','CANCELLED','TERMINATED','SUSPENDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_process_instance`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_process_instance` WRITE;
/*!40000 ALTER TABLE `fm_process_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_process_instance` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_process_start_policy`
--

DROP TABLE IF EXISTS `fm_process_start_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_process_start_policy` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) NOT NULL,
  `PROCESS_VERSION_NO` int(11) NOT NULL,
  `POLICY_SEQ` int(11) NOT NULL,
  `SUBJECT_TYPE` varchar(30) NOT NULL,
  `SUBJECT_REF_ID` varchar(36) DEFAULT NULL,
  `ALLOW_START` char(1) NOT NULL DEFAULT 'Y',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_PSP_SEQ` (`TENANT_ID`,`PROCESS_DEF_ID`,`PROCESS_VERSION_NO`,`POLICY_SEQ`),
  CONSTRAINT `CK_FM_PSP_SUBJECT` CHECK (`SUBJECT_TYPE` in ('ALL','ACCOUNT','ORG_UNIT','APPROVAL_GROUP')),
  CONSTRAINT `CK_FM_PSP_ALLOW` CHECK (`ALLOW_START` in ('Y','N'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_process_start_policy`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_process_start_policy` WRITE;
/*!40000 ALTER TABLE `fm_process_start_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_process_start_policy` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_process_version`
--

DROP TABLE IF EXISTS `fm_process_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_process_version` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) NOT NULL,
  `VERSION_NO` int(11) NOT NULL,
  `VERSION_STATUS` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `BPMN_XML` longtext NOT NULL,
  `BPMN_SHA256` char(64) NOT NULL,
  `FLOWABLE_DEPLOYMENT_ID` varchar(64) DEFAULT NULL,
  `FLOWABLE_PROCESS_DEF_ID` varchar(64) DEFAULT NULL,
  `PUBLISHED_BY` varchar(24) DEFAULT NULL,
  `PUBLISHED_DATE` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_PV_VERSION` (`TENANT_ID`,`PROCESS_DEF_ID`,`VERSION_NO`),
  KEY `IDX_FM_PV_FLOWABLE` (`TENANT_ID`,`FLOWABLE_PROCESS_DEF_ID`),
  CONSTRAINT `CK_FM_PV_STATUS` CHECK (`VERSION_STATUS` in ('DRAFT','PUBLISHED','RETIRED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_process_version`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_process_version` WRITE;
/*!40000 ALTER TABLE `fm_process_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_process_version` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_task_action`
--

DROP TABLE IF EXISTS `fm_task_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_task_action` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `TASK_ACTION_ID` varchar(36) NOT NULL,
  `PROCESS_INSTANCE_ID` varchar(64) NOT NULL,
  `TASK_ID` varchar(64) DEFAULT NULL,
  `TASK_DEF_KEY` varchar(100) DEFAULT NULL,
  `ACTION_TYPE` varchar(30) NOT NULL,
  `OUTCOME` varchar(30) DEFAULT NULL,
  `ACTOR_ACCOUNT` varchar(24) NOT NULL,
  `PRINCIPAL_ACCOUNT` varchar(24) DEFAULT NULL,
  `FROM_ACCOUNT` varchar(24) DEFAULT NULL,
  `TO_ACCOUNT` varchar(24) DEFAULT NULL,
  `FORM_SNAPSHOT_ID` varchar(36) DEFAULT NULL,
  `ASSIGNMENT_SNAPSHOT_ID` varchar(36) DEFAULT NULL,
  `COMMENT_TEXT` varchar(2000) DEFAULT NULL,
  `REASON` varchar(2000) DEFAULT NULL,
  `CONTEXT_DATA` longtext DEFAULT NULL,
  `ACTION_DATE` datetime(3) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TASK_ACTION_ID` (`TENANT_ID`,`TASK_ACTION_ID`),
  KEY `IDX_FM_TASK_ACTION_PI` (`TENANT_ID`,`PROCESS_INSTANCE_ID`,`ACTION_DATE`),
  KEY `IDX_FM_TASK_ACTION_TASK` (`TENANT_ID`,`TASK_ID`,`ACTION_DATE`),
  CONSTRAINT `CK_FM_TASK_ACTION_TYPE` CHECK (`ACTION_TYPE` in ('SUBMIT','APPROVE','REJECT','RETURN','RESUBMIT','WITHDRAW','CANCEL','TRANSFER','DELEGATE','RESOLVE','ADD_SIGN','COMMENT','ADMIN_REASSIGN','TERMINATE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_action`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_action` WRITE;
/*!40000 ALTER TABLE `fm_task_action` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_task_action` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_task_assignment_rule`
--

DROP TABLE IF EXISTS `fm_task_assignment_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_task_assignment_rule` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) NOT NULL,
  `PROCESS_VERSION_NO` int(11) NOT NULL,
  `TASK_DEF_KEY` varchar(100) NOT NULL,
  `RULE_SEQ` int(11) NOT NULL,
  `RESOLVER_TYPE` varchar(50) NOT NULL,
  `RESOLVER_CONFIG` longtext DEFAULT NULL,
  `FALLBACK_CONFIG` longtext DEFAULT NULL,
  `MAX_RESULTS` int(11) NOT NULL DEFAULT 100,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TAR_SEQ` (`TENANT_ID`,`PROCESS_DEF_ID`,`PROCESS_VERSION_NO`,`TASK_DEF_KEY`,`RULE_SEQ`),
  CONSTRAINT `CK_FM_TAR_RESOLVER` CHECK (`RESOLVER_TYPE` in ('FIXED_ACCOUNT','APPROVAL_GROUP','INITIATOR_ORG_HEAD','PARENT_ORG_HEAD','NEXT_HIGHER_LEVEL_HEAD','TARGET_LEVEL_HEAD','LEVEL_HEAD_CHAIN','ROOT_ORG_HEAD','DIRECT_MANAGER','MANAGER_CHAIN','ORG_TITLE','ORG_DUTY','APPROVAL_AUTHORITY')),
  CONSTRAINT `CK_FM_TAR_MAX` CHECK (`MAX_RESULTS` between 1 and 1000),
  CONSTRAINT `CK_FM_TAR_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_assignment_rule`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_assignment_rule` WRITE;
/*!40000 ALTER TABLE `fm_task_assignment_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_task_assignment_rule` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_task_assignment_snapshot`
--

DROP TABLE IF EXISTS `fm_task_assignment_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_task_assignment_snapshot` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ASSIGNMENT_SNAPSHOT_ID` varchar(36) NOT NULL,
  `PROCESS_INSTANCE_ID` varchar(64) NOT NULL,
  `TASK_ID` varchar(64) DEFAULT NULL,
  `TASK_DEF_KEY` varchar(100) NOT NULL,
  `RESOLUTION_SEQ` int(11) NOT NULL,
  `RESOLVER_TYPE` varchar(50) NOT NULL,
  `SOURCE_ACCOUNT` varchar(24) DEFAULT NULL,
  `SOURCE_ORG_UNIT_ID` varchar(36) DEFAULT NULL,
  `RESOLUTION_STATUS` varchar(20) NOT NULL,
  `RESOLUTION_CONTEXT` longtext DEFAULT NULL,
  `RESOLVED_DATE` datetime(3) NOT NULL,
  `SUPERSEDED_DATE` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TAS_ID` (`TENANT_ID`,`ASSIGNMENT_SNAPSHOT_ID`),
  UNIQUE KEY `UK_FM_TAS_SEQ` (`TENANT_ID`,`PROCESS_INSTANCE_ID`,`TASK_DEF_KEY`,`RESOLUTION_SEQ`),
  KEY `IDX_FM_TAS_TASK` (`TENANT_ID`,`TASK_ID`,`RESOLUTION_STATUS`),
  CONSTRAINT `CK_FM_TAS_STATUS` CHECK (`RESOLUTION_STATUS` in ('RESOLVED','INCIDENT','SUPERSEDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_assignment_snapshot`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_assignment_snapshot` WRITE;
/*!40000 ALTER TABLE `fm_task_assignment_snapshot` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_task_assignment_snapshot` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_task_assignment_snapshot_dtl`
--

DROP TABLE IF EXISTS `fm_task_assignment_snapshot_dtl`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_task_assignment_snapshot_dtl` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ASSIGNMENT_SNAPSHOT_ID` varchar(36) NOT NULL,
  `RESULT_SEQ` int(11) NOT NULL,
  `RESULT_TYPE` varchar(20) NOT NULL,
  `RESULT_ACCOUNT` varchar(24) NOT NULL,
  `PRINCIPAL_ACCOUNT` varchar(24) DEFAULT NULL,
  `ORG_UNIT_ID` varchar(36) DEFAULT NULL,
  `ORG_UNIT_NAME` varchar(150) DEFAULT NULL,
  `APPROVAL_LEVEL_ID` varchar(36) DEFAULT NULL,
  `LEVEL_CODE` varchar(30) DEFAULT NULL,
  `LEVEL_NAME` varchar(100) DEFAULT NULL,
  `LEVEL_ORDER` int(11) DEFAULT NULL,
  `RESOLUTION_PATH` longtext DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TASD_SEQ` (`TENANT_ID`,`ASSIGNMENT_SNAPSHOT_ID`,`RESULT_SEQ`),
  KEY `IDX_FM_TASD_ACCOUNT` (`TENANT_ID`,`RESULT_ACCOUNT`),
  CONSTRAINT `CK_FM_TASD_TYPE` CHECK (`RESULT_TYPE` in ('ASSIGNEE','CANDIDATE','DELEGATE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_assignment_snapshot_dtl`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_assignment_snapshot_dtl` WRITE;
/*!40000 ALTER TABLE `fm_task_assignment_snapshot_dtl` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_task_assignment_snapshot_dtl` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_task_form_rule`
--

DROP TABLE IF EXISTS `fm_task_form_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_task_form_rule` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) NOT NULL,
  `PROCESS_VERSION_NO` int(11) NOT NULL,
  `TASK_DEF_KEY` varchar(100) NOT NULL,
  `FORM_ID` varchar(36) NOT NULL,
  `FORM_VERSION_NO` int(11) NOT NULL,
  `FIELD_POLICY` longtext NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TFR_TASK` (`TENANT_ID`,`PROCESS_DEF_ID`,`PROCESS_VERSION_NO`,`TASK_DEF_KEY`,`FORM_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_form_rule`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_form_rule` WRITE;
/*!40000 ALTER TABLE `fm_task_form_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_task_form_rule` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_task_policy`
--

DROP TABLE IF EXISTS `fm_task_policy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_task_policy` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `PROCESS_DEF_ID` varchar(36) NOT NULL,
  `PROCESS_VERSION_NO` int(11) NOT NULL,
  `TASK_DEF_KEY` varchar(100) NOT NULL,
  `TASK_NAME` varchar(150) NOT NULL,
  `ASSIGNMENT_MODE` varchar(20) NOT NULL DEFAULT 'ASSIGNEE',
  `SELF_APPROVAL_POLICY` varchar(30) NOT NULL DEFAULT 'SKIP_TO_NEXT',
  `DUPLICATE_POLICY` varchar(30) NOT NULL DEFAULT 'MERGE_CONSECUTIVE',
  `ALLOW_REJECT` char(1) NOT NULL DEFAULT 'Y',
  `ALLOW_RETURN` char(1) NOT NULL DEFAULT 'Y',
  `ALLOW_TRANSFER` char(1) NOT NULL DEFAULT 'N',
  `ALLOW_ADD_SIGN` char(1) NOT NULL DEFAULT 'N',
  `COMMENT_REQUIRED` varchar(30) NOT NULL DEFAULT 'ON_REJECT_RETURN',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TP_TASK` (`TENANT_ID`,`PROCESS_DEF_ID`,`PROCESS_VERSION_NO`,`TASK_DEF_KEY`),
  CONSTRAINT `CK_FM_TP_MODE` CHECK (`ASSIGNMENT_MODE` in ('ASSIGNEE','CANDIDATE','ALL','SEQUENTIAL')),
  CONSTRAINT `CK_FM_TP_SELF` CHECK (`SELF_APPROVAL_POLICY` in ('ALLOW','SKIP_TO_NEXT','REQUIRE_ALTERNATE','INCIDENT')),
  CONSTRAINT `CK_FM_TP_DUP` CHECK (`DUPLICATE_POLICY` in ('KEEP_EACH_LEVEL','MERGE_CONSECUTIVE','SKIP_ALREADY_APPROVED')),
  CONSTRAINT `CK_FM_TP_YN` CHECK (`ALLOW_REJECT` in ('Y','N') and `ALLOW_RETURN` in ('Y','N') and `ALLOW_TRANSFER` in ('Y','N') and `ALLOW_ADD_SIGN` in ('Y','N')),
  CONSTRAINT `CK_FM_TP_COMMENT` CHECK (`COMMENT_REQUIRED` in ('NEVER','ALWAYS','ON_REJECT_RETURN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_policy`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_policy` WRITE;
/*!40000 ALTER TABLE `fm_task_policy` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_task_policy` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_tenant`
--

DROP TABLE IF EXISTS `fm_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_tenant` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `TENANT_CODE` varchar(30) NOT NULL,
  `TENANT_NAME` varchar(100) NOT NULL,
  `DEFAULT_LOCALE` varchar(15) NOT NULL DEFAULT 'zh-TW',
  `DEFAULT_TIMEZONE` varchar(50) NOT NULL DEFAULT 'Asia/Taipei',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TENANT_ID` (`TENANT_ID`),
  UNIQUE KEY `UK_FM_TENANT_CODE` (`TENANT_CODE`),
  CONSTRAINT `CK_FM_TENANT_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_tenant`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_tenant` WRITE;
/*!40000 ALTER TABLE `fm_tenant` DISABLE KEYS */;
INSERT INTO `fm_tenant` VALUES
('01d982dc-8c20-11f1-8734-5f8dde7b5e28','A01','A01','台灣區','zh-TW','Asia/Taipei','ACTIVE','','admin','2026-07-30 22:07:32.308',NULL,NULL);
/*!40000 ALTER TABLE `fm_tenant` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_tenant_account`
--

DROP TABLE IF EXISTS `fm_tenant_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_tenant_account` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ACCOUNT` varchar(24) NOT NULL,
  `IS_DEFAULT` char(1) NOT NULL DEFAULT 'N',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_TENANT_ACCOUNT` (`TENANT_ID`,`ACCOUNT`),
  KEY `IDX_FM_TENANT_ACCOUNT_LOGIN` (`ACCOUNT`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  CONSTRAINT `CK_FM_TA_DEFAULT` CHECK (`IS_DEFAULT` in ('Y','N')),
  CONSTRAINT `CK_FM_TA_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_TA_DATE` CHECK (`EFFECTIVE_TO` is null or `EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_tenant_account`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_tenant_account` WRITE;
/*!40000 ALTER TABLE `fm_tenant_account` DISABLE KEYS */;
INSERT INTO `fm_tenant_account` VALUES
('b7f39e3d-8ca3-11f1-98fb-892691cae8be','A01','admin','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:50:21.896',NULL,NULL),
('c96fc8af-8ca3-11f1-98fb-57f5db0212b2','A01','tester','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:50:51.230','admin','2026-07-31 14:14:18.542'),
('e4596eb1-8ca3-11f1-98fb-c181205235bd','A01','steven','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:51:36.382',NULL,NULL),
('ec94abd3-8ca3-11f1-98fb-371f71dbf5cc','A01','tiffany','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:51:50.192',NULL,NULL);
/*!40000 ALTER TABLE `fm_tenant_account` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_workflow_delegation`
--

DROP TABLE IF EXISTS `fm_workflow_delegation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_workflow_delegation` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `DELEGATION_ID` varchar(36) NOT NULL,
  `PRINCIPAL_ACCOUNT` varchar(24) NOT NULL,
  `DELEGATE_ACCOUNT` varchar(24) NOT NULL,
  `SCOPE_TYPE` varchar(30) NOT NULL DEFAULT 'ALL',
  `SCOPE_REF_ID` varchar(36) DEFAULT NULL,
  `ALLOW_REDELEGATE` char(1) NOT NULL DEFAULT 'N',
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) NOT NULL,
  `REASON` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_WD_ID` (`TENANT_ID`,`DELEGATION_ID`),
  KEY `IDX_FM_WD_RESOLVE` (`TENANT_ID`,`PRINCIPAL_ACCOUNT`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  CONSTRAINT `CK_FM_WD_ACCOUNT` CHECK (`PRINCIPAL_ACCOUNT` <> `DELEGATE_ACCOUNT`),
  CONSTRAINT `CK_FM_WD_SCOPE` CHECK (`SCOPE_TYPE` in ('ALL','PROCESS','APPROVAL_GROUP')),
  CONSTRAINT `CK_FM_WD_REDELEGATE` CHECK (`ALLOW_REDELEGATE` in ('Y','N')),
  CONSTRAINT `CK_FM_WD_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_WD_DATE` CHECK (`EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_workflow_delegation`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_workflow_delegation` WRITE;
/*!40000 ALTER TABLE `fm_workflow_delegation` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_workflow_delegation` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_account`
--

DROP TABLE IF EXISTS `tb_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_account` (
  `OID` char(36) NOT NULL,
  `ACCOUNT` varchar(24) NOT NULL,
  `PASSWORD` varchar(255) NOT NULL,
  `ON_JOB` varchar(50) NOT NULL DEFAULT 'Y',
  `CUSERID` varchar(24) DEFAULT NULL,
  `CDATE` datetime DEFAULT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`ACCOUNT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_account`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_account` WRITE;
/*!40000 ALTER TABLE `tb_account` DISABLE KEYS */;
INSERT INTO `tb_account` VALUES
('0','admin','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2012-11-11 10:56:23','admin','2014-04-19 11:32:04'),
('15822da5-25dc-490c-bdfb-be75f5ff4843','tester','$2a$10$0NLveCNWsNQ7BWQgNQQiIuIFjQxq1fVH1LzfncNth3k7Jdc701Or2','Y','admin','2015-04-23 11:26:53','admin','2026-07-31 14:14:18'),
('52cb274e-388d-419f-a81e-67ca599bfb63','steven','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2015-09-11 10:33:53',NULL,NULL),
('9c239d19-3646-41db-b394-d34c5bf34671','tiffany','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2015-09-11 10:15:29',NULL,NULL);
/*!40000 ALTER TABLE `tb_account` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_role`
--

DROP TABLE IF EXISTS `tb_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_role` (
  `OID` char(36) NOT NULL,
  `ROLE` varchar(50) NOT NULL,
  `DESCRIPTION` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(50) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`ROLE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_role`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_role` WRITE;
/*!40000 ALTER TABLE `tb_role` DISABLE KEYS */;
INSERT INTO `tb_role` VALUES
('19f1523b-5afc-11f1-86b7-bd261cfeb8ff','testrole','test','admin','2026-05-29 09:17:03',NULL,NULL),
('4b1796ad-0bb7-4a65-b45e-439540ba5dbd','admin','administrator role!','admin','2014-10-09 15:02:24',NULL,NULL),
('58914623-46ea-4797-bbec-2dadc5d0800e','COMMON01','Common role!','admin','2017-05-09 13:31:42',NULL,NULL),
('c7c69396-e5e6-48ca-b09c-9445b69e2ad5','*','all role','admin','2014-10-09 15:02:54',NULL,NULL);
/*!40000 ALTER TABLE `tb_role` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_role_permission`
--

DROP TABLE IF EXISTS `tb_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_role_permission` (
  `OID` char(36) NOT NULL,
  `ROLE` varchar(50) NOT NULL,
  `PERMISSION` varchar(255) NOT NULL,
  `PERM_TYPE` varchar(15) NOT NULL,
  `DESCRIPTION` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(50) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`ROLE`,`PERMISSION`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_role_permission`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_role_permission` WRITE;
/*!40000 ALTER TABLE `tb_role_permission` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_role_permission` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys`
--

DROP TABLE IF EXISTS `tb_sys`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys` (
  `OID` char(36) NOT NULL,
  `SYS_ID` varchar(10) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `HOST` varchar(200) NOT NULL,
  `CONTEXT_PATH` varchar(100) NOT NULL,
  `IS_LOCAL` varchar(1) NOT NULL DEFAULT 'Y',
  `ICON` varchar(20) NOT NULL DEFAULT ' ',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`SYS_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys` WRITE;
/*!40000 ALTER TABLE `tb_sys` DISABLE KEYS */;
INSERT INTO `tb_sys` VALUES
('c6643182-85a5-4f91-9e73-10567ebd0dd5','CORE','Core-system','127.0.0.1:8080','core-web','Y','SYSTEM','admin','2017-04-10 20:42:00','admin','2026-06-01 15:35:15');
/*!40000 ALTER TABLE `tb_sys` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_bean_help`
--

DROP TABLE IF EXISTS `tb_sys_bean_help`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_bean_help` (
  `OID` char(36) NOT NULL,
  `BEAN_ID` varchar(255) NOT NULL,
  `METHOD` varchar(100) NOT NULL,
  `SYSTEM` varchar(10) NOT NULL,
  `ENABLE_FLAG` varchar(1) NOT NULL,
  `DESCRIPTION` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`BEAN_ID`,`METHOD`,`SYSTEM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_bean_help`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_bean_help` WRITE;
/*!40000 ALTER TABLE `tb_sys_bean_help` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_bean_help` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_bean_help_expr`
--

DROP TABLE IF EXISTS `tb_sys_bean_help_expr`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_bean_help_expr` (
  `OID` char(36) NOT NULL,
  `HELP_OID` char(36) NOT NULL,
  `EXPR_ID` varchar(20) NOT NULL,
  `EXPR_SEQ` varchar(10) NOT NULL,
  `RUN_TYPE` varchar(10) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`EXPR_ID`,`HELP_OID`,`RUN_TYPE`),
  KEY `IDX_1` (`HELP_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_bean_help_expr`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_bean_help_expr` WRITE;
/*!40000 ALTER TABLE `tb_sys_bean_help_expr` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_bean_help_expr` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_bean_help_expr_map`
--

DROP TABLE IF EXISTS `tb_sys_bean_help_expr_map`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_bean_help_expr_map` (
  `OID` char(36) NOT NULL,
  `HELP_EXPR_OID` char(36) NOT NULL,
  `METHOD_RESULT_FLAG` varchar(1) NOT NULL DEFAULT 'N',
  `METHOD_PARAM_CLASS` varchar(255) NOT NULL DEFAULT ' ',
  `METHOD_PARAM_INDEX` int(3) NOT NULL DEFAULT 0,
  `VAR_NAME` varchar(255) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`VAR_NAME`,`HELP_EXPR_OID`),
  KEY `IDX_1` (`HELP_EXPR_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_bean_help_expr_map`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_bean_help_expr_map` WRITE;
/*!40000 ALTER TABLE `tb_sys_bean_help_expr_map` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_bean_help_expr_map` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_code`
--

DROP TABLE IF EXISTS `tb_sys_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_code` (
  `OID` char(36) NOT NULL,
  `CODE` varchar(25) NOT NULL,
  `TYPE` varchar(10) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `PARAM1` varchar(100) DEFAULT NULL,
  `PARAM2` varchar(100) DEFAULT NULL,
  `PARAM3` varchar(100) DEFAULT NULL,
  `PARAM4` varchar(100) DEFAULT NULL,
  `PARAM5` varchar(100) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_code`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_code` WRITE;
/*!40000 ALTER TABLE `tb_sys_code` DISABLE KEYS */;
INSERT INTO `tb_sys_code` VALUES
('2d9c84e4-a956-42ac-96cb-1f6292d182a9','CNF_CONF002','CNF','enable mail sender!','Y',NULL,NULL,NULL,NULL,'admin','2014-12-25 09:09:57','admin','2020-09-14 04:36:34'),
('4df770a6-6a9c-4d25-bdcd-1dee819d2ba6','CNF_CONF001','CNF','default mail from account!','root@localhost',NULL,NULL,NULL,NULL,'admin','2014-12-24 21:51:16','admin','2020-09-14 04:36:34'),
('57877c4d-4f3e-4679-880a-a262eeba0c3d','TOKEN','AUTH','QiFu3 Client token','9TYM7TRuILqFk9XoR0v6Yx672','COMMON01',NULL,NULL,NULL,'admin','2021-10-30 17:12:04',NULL,NULL),
('a5f7ee37-f33f-48a6-b448-92ccb8cdf96a','CNF_CONF003','CNF','first load javascript','addTab(\'CORE_PROG999D9999Q\', null);',NULL,NULL,NULL,NULL,'admin','2014-12-25 09:09:57',NULL,NULL),
('caf00ba5-fe63-4dc4-a1a3-32527f6629b2','CMM_CONF001','CMM','Common role for default user!','COMMON01',NULL,NULL,NULL,NULL,'admin','2017-05-09 12:29:00',NULL,NULL);
/*!40000 ALTER TABLE `tb_sys_code` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_event_log`
--

DROP TABLE IF EXISTS `tb_sys_event_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_event_log` (
  `OID` char(36) NOT NULL,
  `USER` varchar(24) NOT NULL,
  `SYS_ID` varchar(10) NOT NULL,
  `EXECUTE_EVENT` varchar(255) NOT NULL,
  `IS_PERMIT` varchar(1) NOT NULL DEFAULT 'N',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  KEY `IDX_1` (`USER`),
  KEY `IDX_2` (`CDATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_event_log`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_event_log` WRITE;
/*!40000 ALTER TABLE `tb_sys_event_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_event_log` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_expr_job`
--

DROP TABLE IF EXISTS `tb_sys_expr_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_expr_job` (
  `OID` char(36) NOT NULL,
  `SYSTEM` varchar(10) NOT NULL,
  `ID` varchar(20) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `ACTIVE` varchar(1) NOT NULL DEFAULT 'Y',
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `RUN_STATUS` varchar(1) NOT NULL DEFAULT 'Y',
  `CHECK_FAULT` varchar(1) NOT NULL DEFAULT 'N',
  `EXPR_ID` varchar(20) NOT NULL,
  `RUN_DAY_OF_WEEK` varchar(1) NOT NULL,
  `RUN_HOUR` varchar(2) NOT NULL,
  `RUN_MINUTE` varchar(2) NOT NULL,
  `CONTACT_MODE` varchar(1) NOT NULL DEFAULT '0',
  `CONTACT` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`ID`),
  KEY `IDX_1` (`SYSTEM`,`ACTIVE`,`EXPR_ID`,`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_expr_job`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_expr_job` WRITE;
/*!40000 ALTER TABLE `tb_sys_expr_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_expr_job` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_expr_job_log`
--

DROP TABLE IF EXISTS `tb_sys_expr_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_expr_job_log` (
  `OID` char(36) NOT NULL,
  `ID` varchar(20) NOT NULL,
  `LOG_STATUS` varchar(1) NOT NULL DEFAULT 'N',
  `BEGIN_DATETIME` datetime NOT NULL,
  `END_DATETIME` datetime NOT NULL,
  `FAULT_MSG` varchar(2000) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  KEY `IDX_1` (`ID`,`LOG_STATUS`,`BEGIN_DATETIME`),
  KEY `IDX_2` (`CDATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_expr_job_log`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_expr_job_log` WRITE;
/*!40000 ALTER TABLE `tb_sys_expr_job_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_expr_job_log` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_expression`
--

DROP TABLE IF EXISTS `tb_sys_expression`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_expression` (
  `OID` char(36) NOT NULL,
  `EXPR_ID` varchar(20) NOT NULL,
  `TYPE` varchar(10) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `CONTENT` varchar(8000) NOT NULL,
  `DESCRIPTION` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`EXPR_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_expression`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_expression` WRITE;
/*!40000 ALTER TABLE `tb_sys_expression` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_expression` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_icon`
--

DROP TABLE IF EXISTS `tb_sys_icon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_icon` (
  `OID` char(36) NOT NULL,
  `ICON_ID` varchar(20) NOT NULL,
  `FILE_NAME` varchar(200) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`ICON_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_icon`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_icon` WRITE;
/*!40000 ALTER TABLE `tb_sys_icon` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_icon` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_jreport`
--

DROP TABLE IF EXISTS `tb_sys_jreport`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_jreport` (
  `OID` char(36) NOT NULL,
  `REPORT_ID` varchar(50) NOT NULL,
  `FILE` varchar(100) NOT NULL,
  `IS_COMPILE` varchar(50) NOT NULL DEFAULT 'N',
  `CONTENT` mediumblob NOT NULL,
  `DESCRIPTION` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`REPORT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_jreport`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_jreport` WRITE;
/*!40000 ALTER TABLE `tb_sys_jreport` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_jreport` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_jreport_param`
--

DROP TABLE IF EXISTS `tb_sys_jreport_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_jreport_param` (
  `OID` char(36) NOT NULL,
  `REPORT_ID` varchar(50) NOT NULL,
  `URL_PARAM` varchar(100) NOT NULL,
  `RPT_PARAM` varchar(100) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`REPORT_ID`,`RPT_PARAM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_jreport_param`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_jreport_param` WRITE;
/*!40000 ALTER TABLE `tb_sys_jreport_param` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_jreport_param` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_login_log`
--

DROP TABLE IF EXISTS `tb_sys_login_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_login_log` (
  `OID` char(36) NOT NULL,
  `USER` varchar(24) NOT NULL,
  `FAIL_FLAG` char(1) NOT NULL DEFAULT 'N',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  KEY `IDX_1` (`USER`),
  KEY `IDX_2` (`CDATE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_login_log`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_login_log` WRITE;
/*!40000 ALTER TABLE `tb_sys_login_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_login_log` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_mail_helper`
--

DROP TABLE IF EXISTS `tb_sys_mail_helper`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_mail_helper` (
  `OID` char(36) NOT NULL,
  `MAIL_ID` varchar(17) NOT NULL,
  `SUBJECT` varchar(200) NOT NULL,
  `TEXT` blob DEFAULT NULL,
  `MAIL_FROM` varchar(100) NOT NULL,
  `MAIL_TO` varchar(100) NOT NULL,
  `MAIL_CC` varchar(1000) DEFAULT NULL,
  `MAIL_BCC` varchar(1000) DEFAULT NULL,
  `SUCCESS_FLAG` varchar(1) NOT NULL DEFAULT 'N',
  `SUCCESS_TIME` datetime DEFAULT NULL,
  `RETAIN_FLAG` varchar(1) NOT NULL DEFAULT 'N',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`MAIL_ID`),
  KEY `IDX_1` (`MAIL_ID`),
  KEY `IDX_2` (`SUCCESS_FLAG`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_mail_helper`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_mail_helper` WRITE;
/*!40000 ALTER TABLE `tb_sys_mail_helper` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_mail_helper` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_menu`
--

DROP TABLE IF EXISTS `tb_sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_menu` (
  `OID` char(36) NOT NULL,
  `PROG_ID` varchar(50) NOT NULL,
  `PARENT_OID` char(36) NOT NULL,
  `ENABLE_FLAG` varchar(1) NOT NULL DEFAULT 'Y',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`PROG_ID`,`PARENT_OID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_menu`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_menu` WRITE;
/*!40000 ALTER TABLE `tb_sys_menu` DISABLE KEYS */;
INSERT INTO `tb_sys_menu` VALUES
('4bd4d202-5feb-495b-8c8c-ec6b7f5b8041','CORE_PROG002D0002Q','79e1cf24-2522-4cdf-abcc-6455b47d545b','Y','admin','2017-05-10 14:20:12',NULL,NULL),
('4d85e70f-8c1c-11f1-b7bb-df01fa5d82a7','FM_PROG001D','00000000-0000-0000-0000-000000000000','Y','admin','2026-07-30 21:41:01',NULL,NULL),
('571133d5-8caa-11f1-bf0e-b7fd3076ce3e','FM_PROG002D','00000000-0000-0000-0000-000000000000','Y','admin','2026-07-31 14:37:45',NULL,NULL),
('5e055f61-bfc5-402c-93b4-f241dc17b00b','CORE_PROG004D','00000000-0000-0000-0000-000000000000','Y','admin','2017-06-03 14:23:17',NULL,NULL),
('79e1cf24-2522-4cdf-abcc-6455b47d545b','CORE_PROG002D','00000000-0000-0000-0000-000000000000','Y','admin','2017-05-08 21:32:59',NULL,NULL),
('7aa1208a-5fc2-11f1-afe9-33fb6c1b9ce7','CORE_PROG005D','00000000-0000-0000-0000-000000000000','Y','admin','2026-06-04 11:07:11',NULL,NULL),
('7aa2590b-5fc2-11f1-afe9-73b1551d818b','CORE_PROG005D0001Q','7aa1208a-5fc2-11f1-afe9-33fb6c1b9ce7','Y','admin','2026-06-04 11:07:11',NULL,NULL),
('7ea68636-c93a-4669-ac42-dafc3770d20d','CORE_PROG001D','00000000-0000-0000-0000-000000000000','Y','admin','2017-04-20 11:24:53',NULL,NULL),
('83f3ec93-8c1c-11f1-b7bb-0b4bed3d6265','FM_PROG001D0001Q','4d85e70f-8c1c-11f1-b7bb-df01fa5d82a7','Y','admin','2026-07-30 21:42:32',NULL,NULL),
('9972c249-2985-49ac-9b8b-f6c25c65fd4e','CORE_PROG002D0003Q','79e1cf24-2522-4cdf-abcc-6455b47d545b','Y','admin','2017-05-10 14:20:12',NULL,NULL),
('a00d5586-8e42-11f1-a428-5d974d2ac9d9','FM_PROG002D0001Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a00e66f7-8e42-11f1-a428-2970c26328fb','FM_PROG002D0002Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a00f7868-8e42-11f1-a428-317034e796eb','FM_PROG002D0003Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a0103bb9-8e42-11f1-a428-95a98e92cad7','FM_PROG002D0004Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a0114d2a-8e42-11f1-a428-cd7012816922','FM_PROG002D0005Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a012378b-8e42-11f1-a428-a740bb435ab0','FM_PROG002D0006Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('c5349a26-6d6e-4d94-b817-82be6d14d5ed','CORE_PROG002D0001Q','79e1cf24-2522-4cdf-abcc-6455b47d545b','Y','admin','2017-05-10 14:20:12',NULL,NULL),
('f0242c17-4487-11ee-b50d-a593cf4a05bf','CORE_PROG001D0001Q','7ea68636-c93a-4669-ac42-dafc3770d20d','Y','admin','2023-08-27 11:15:13',NULL,NULL),
('f0253d88-4487-11ee-b50d-7f3d9b9812d0','CORE_PROG001D0002Q','7ea68636-c93a-4669-ac42-dafc3770d20d','Y','admin','2023-08-27 11:15:13',NULL,NULL),
('f0264ef9-4487-11ee-b50d-a55549dc8acf','CORE_PROG001D0003Q','7ea68636-c93a-4669-ac42-dafc3770d20d','Y','admin','2023-08-27 11:15:13',NULL,NULL),
('f027877a-4487-11ee-b50d-8fe1228e511a','CORE_PROG001D0004Q','7ea68636-c93a-4669-ac42-dafc3770d20d','Y','admin','2023-08-27 11:15:13',NULL,NULL),
('f02898eb-4487-11ee-b50d-45ee94442a45','CORE_PROG001D0005Q','7ea68636-c93a-4669-ac42-dafc3770d20d','Y','admin','2023-08-27 11:15:13',NULL,NULL),
('f07acfb8-4612-11ee-9a04-71984fef28fa','CORE_PROG004D0001Q','5e055f61-bfc5-402c-93b4-f241dc17b00b','Y','admin','2023-08-29 10:22:45',NULL,NULL),
('f07b9309-4612-11ee-9a04-9f3e4fe17b25','CORE_PROG004D0002Q','5e055f61-bfc5-402c-93b4-f241dc17b00b','Y','admin','2023-08-29 10:22:45',NULL,NULL);
/*!40000 ALTER TABLE `tb_sys_menu` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_menu_role`
--

DROP TABLE IF EXISTS `tb_sys_menu_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_menu_role` (
  `OID` char(36) NOT NULL,
  `PROG_ID` varchar(50) NOT NULL,
  `ROLE` varchar(50) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`PROG_ID`,`ROLE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_menu_role`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_menu_role` WRITE;
/*!40000 ALTER TABLE `tb_sys_menu_role` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_menu_role` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_prog`
--

DROP TABLE IF EXISTS `tb_sys_prog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_prog` (
  `OID` char(36) NOT NULL,
  `PROG_ID` varchar(50) NOT NULL,
  `NAME` varchar(100) NOT NULL,
  `URL` varchar(255) NOT NULL,
  `EDIT_MODE` varchar(1) NOT NULL DEFAULT 'N',
  `IS_DIALOG` varchar(1) NOT NULL DEFAULT 'N',
  `DIALOG_W` int(4) NOT NULL DEFAULT 0,
  `DIALOG_H` int(4) NOT NULL DEFAULT 0,
  `PROG_SYSTEM` varchar(10) NOT NULL,
  `ITEM_TYPE` varchar(10) NOT NULL,
  `ICON` varchar(20) NOT NULL,
  `FONT_ICON_CLASS_ID` varchar(100) NOT NULL DEFAULT 'circle-o',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`PROG_ID`),
  KEY `IDX_1` (`PROG_SYSTEM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_prog`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_prog` WRITE;
/*!40000 ALTER TABLE `tb_sys_prog` DISABLE KEYS */;
INSERT INTO `tb_sys_prog` VALUES
('186b1fb1-749f-4b6f-97d1-6b7fb8115345','CORE_PROG001D0004E','ZA04 - Freemarker樣板 (Edit)','#/prog001d0004/edit','Y','N',0,0,'CORE','ITEM','TEMPLATE','file-text','admin','2017-05-12 10:40:10','admin','2023-08-16 21:48:56'),
('1b11c7eb-6133-48fb-87f0-dfbd098ce914','CORE_PROG001D0001E','ZA01 - System site (Edit)','#/prog001d0001/edit','Y','N',0,0,'CORE','ITEM','COMPUTER','globe2','admin','2014-10-02 00:00:00','admin','2021-01-20 08:20:58'),
('1e393fe3-8bbc-482c-aa23-bbb22a1dbafb','CORE_PROG001D0005A','ZA05 - JasperReport (Create)','#/prog001d0005/create','N','N',0,0,'CORE','ITEM','APPLICATION_PDF','file-pdf','admin','2017-05-18 09:55:46','admin','2023-08-24 20:20:27'),
('22560527-90fb-4e5a-a89b-353d2aa1d433','CORE_PROG001D0005E','ZA05 - JasperReport (Edit)','#/prog001d0005/edit','Y','N',0,0,'CORE','ITEM','APPLICATION_PDF','file-pdf','admin','2017-05-18 09:56:27','admin','2023-08-24 20:20:40'),
('29e19038-8ca9-11f1-a791-005056c00001','FM_PROG002D','FB. FlowMint 組織管理','/','N','N',0,0,'CORE','FOLDER','ORGANIZATION','diagram-3','admin','2026-07-31 14:29:01',NULL,NULL),
('29e3bfc4-8ca9-11f1-a791-005056c00001','FM_PROG002D0001Q','FB01 - 員工資料管理','#/fm_prog002d0001','N','N',0,0,'CORE','ITEM','PEOPLE','people','admin','2026-07-31 14:29:01',NULL,NULL),
('29e57f23-8ca9-11f1-a791-005056c00001','FM_PROG002D0001A','FB01 - 員工資料管理（新增）','#/fm_prog002d0001/create','N','N',0,0,'CORE','ITEM','PEOPLE','people','admin','2026-07-31 14:29:01','admin','2026-07-31 14:36:07'),
('29e6d728-8ca9-11f1-a791-005056c00001','FM_PROG002D0001E','FB01 - 員工資料管理（編輯）','#/fm_prog002d0001/edit','Y','N',0,0,'CORE','ITEM','PEOPLE','people','admin','2026-07-31 14:29:01','admin','2026-07-31 14:35:54'),
('3630ee1b-6169-452f-821f-5c015dfb84d5','CORE_PROG001D','ZA. Config','/','N','N',0,0,'CORE','FOLDER','PROPERTIES','gear-fill','admin','2014-10-02 00:00:00','admin','2023-08-15 19:16:31'),
('3862b6d0-0551-45d8-8dd1-cd988a5e8e50','CORE_PROG004D0002Q','ZD02 - Token log','#/prog004d0002','N','N',0,0,'CORE','ITEM','PROPERTIES','clipboard-check','admin','2017-06-03 14:22:29','admin','2023-08-29 10:23:05'),
('4103c625-8c1c-11f1-a791-005056c00001','FM_PROG001D','FA. FlowMint 基本設定','/','N','N',0,0,'CORE','FOLDER','PROPERTIES','gear-fill','admin','2026-07-30 21:40:25',NULL,NULL),
('410572ff-8c1c-11f1-a791-005056c00001','FM_PROG001D0001Q','FA01 - Tenant 與帳號範圍','#/fm_prog001d0001','N','N',0,0,'CORE','ITEM','COMPANY','building','admin','2026-07-30 21:40:25',NULL,NULL),
('410698de-8c1c-11f1-a791-005056c00001','FM_PROG001D0001A','FA01 - Tenant 與帳號範圍（新增）','#/fm_prog001d0001/create','N','N',0,0,'CORE','ITEM','COMPANY','building-add','admin','2026-07-30 21:40:25',NULL,NULL),
('410ad0ae-8c1c-11f1-a791-005056c00001','FM_PROG001D0001E','FA01 - Tenant 與帳號範圍（編輯）','#/fm_prog001d0001/edit','Y','N',0,0,'CORE','ITEM','COMPANY','building-gear','admin','2026-07-30 21:40:25',NULL,NULL),
('41fa29d8-3a53-4fbd-b2b1-cdbfd0729767','CORE_PROG001D0004Q','ZA04 - Freemarker樣板','#/prog001d0004','N','N',0,0,'CORE','ITEM','TEMPLATE','file-text','admin','2017-05-12 10:36:41','admin','2023-08-16 21:48:29'),
('468d410c-8e3c-11f1-a861-005056c00001','FM_PROG002D0005Q','FB05 - 部門主管配置','#/fm_prog002d0005','N','N',0,0,'CORE','ITEM','ORGANIZATION','person-badge','admin','2026-08-02 14:34:48',NULL,NULL),
('468e67c5-8e3c-11f1-a861-005056c00001','FM_PROG002D0005A','FB05 - 部門主管配置（新增）','#/fm_prog002d0005/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','person-badge','admin','2026-08-02 14:34:48',NULL,NULL),
('468f2ffc-8e3c-11f1-a861-005056c00001','FM_PROG002D0005E','FB05 - 部門主管配置（編輯）','#/fm_prog002d0005/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','person-badge','admin','2026-08-02 14:34:48',NULL,NULL),
('5e082c7c-1730-4176-89c6-93e235707deb','CORE_PROG002D0001A','ZB01 - Role (Create)','#/prog002d0001/create','N','N',0,0,'CORE','ITEM','PEOPLE','person-square','admin','2017-05-09 11:15:50','admin','2023-08-27 16:46:40'),
('61aea7ff-7a42-4a92-9a0b-4a0dfe60858b','CORE_PROG004D0001Q','ZD01 - Event log','#/prog004d0001','N','N',0,0,'CORE','ITEM','PROPERTIES','clipboard-pulse','admin','2017-06-03 14:22:07','admin','2023-08-29 10:17:34'),
('69b048dc-8cb1-11f1-a791-005056c00001','FM_PROG002D0003Q','FB03 - 組織簽核層級','#/fm_prog002d0003','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-07-31 15:28:04',NULL,NULL),
('69b25a32-8cb1-11f1-a791-005056c00001','FM_PROG002D0003A','FB03 - 組織簽核層級（新增）','#/fm_prog002d0003/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-07-31 15:28:04',NULL,NULL),
('69b3ca8f-8cb1-11f1-a791-005056c00001','FM_PROG002D0003E','FB03 - 組織簽核層級（編輯）','#/fm_prog002d0003/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-07-31 15:28:04',NULL,NULL),
('6a442973-0e0c-4a7a-d546-464f4ff5f7a9','CORE_PROG001D0003Q','ZA03 - Menu settings','#/prog001d0003','N','N',0,0,'CORE','ITEM','FOLDER','menu-down','admin','2014-10-02 00:00:00','admin','2023-08-15 19:21:23'),
('6b210525-8975-4fb5-954c-fe349f66d3fe','CORE_PROG002D0001S01Q','ZB01 - Role (permission)','#/prog002d0001/setparam','Y','N',0,0,'CORE','ITEM','IMPORTANT','globe2','admin','2017-05-09 14:32:47','admin','2021-01-20 08:48:52'),
('72e6e0d1-1818-47d3-99f9-5134fb211b79','CORE_PROG002D','ZB. Role authority','/','N','N',0,0,'CORE','FOLDER','SHARED','person-square','admin','2017-05-08 21:27:52','admin','2023-08-27 16:47:03'),
('7746f746-961f-44c2-9b66-fa43c0f49838','CORE_PROG001D0004S01Q','ZA04 - Freemarker樣板 (Parameter)','#/prog001d0004/setparam','Y','N',0,0,'CORE','ITEM','TEMPLATE','file-text','admin','2017-05-12 10:42:04','admin','2023-08-16 21:49:12'),
('7d9ddc45-3eab-4f61-8c0a-d5505c0cc748','CORE_PROG001D0004A','ZA04 - Freemarker樣板 (Create)','#/prog001d0004/create','N','N',0,0,'CORE','ITEM','TEMPLATE','file-text','admin','2017-05-12 10:39:20','admin','2023-08-16 21:48:49'),
('8499957e-6da9-4160-c2ec-dfb7dbc202fe','CORE_PROG001D0002E','ZA02 - Program (Edit)','#/prog001d0002/edit','Y','N',0,0,'CORE','ITEM','G_APP_INSTALL','filetype-html','admin','2014-10-02 00:00:00','admin','2023-08-15 19:19:17'),
('87f9ae6a-1dd2-4585-b31d-9533bdda8fd5','CORE_PROG005D0001Q','ZE01 - MQTT Dashboard','#/prog005d0001','N','N',0,0,'CORE','ITEM','PROPERTIES','speedometer2','admin','2026-06-04 11:00:54',NULL,NULL),
('923add37-8cb7-11f1-a791-005056c00001','FM_PROG002D0002Q','FB02 - 部門資料與組織樹','#/fm_prog002d0002','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-2','admin','2026-07-31 16:12:09',NULL,NULL),
('923d71fa-8cb7-11f1-a791-005056c00001','FM_PROG002D0002A','FB02 - 部門資料與組織樹（新增）','#/fm_prog002d0002/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-2','admin','2026-07-31 16:12:09',NULL,NULL),
('923edcad-8cb7-11f1-a791-005056c00001','FM_PROG002D0002E','FB02 - 部門資料與組織樹（編輯）','#/fm_prog002d0002/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-2','admin','2026-07-31 16:12:09',NULL,NULL),
('92404571-8cb7-11f1-a791-005056c00001','FM_PROG002D0002T','FB02 - 部門組織樹','#/fm_prog002d0002/tree','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-2','admin','2026-07-31 16:12:09',NULL,NULL),
('ac5bcfd0-4abd-11e4-916c-0800200c9a66','CORE_PROG001D0001A','ZA01 - System site (Create)','#/prog001d0001/create','N','N',0,0,'CORE','ITEM','COMPUTER','globe2','admin','2014-10-02 00:00:00','admin','2021-01-20 08:20:45'),
('b39159ad-0707-4515-b78d-e3fc72c53974','CORE_PROG002D0001E','ZB01 - Role (Edit)','#/prog002d0001/edit','Y','N',0,0,'CORE','ITEM','PEOPLE','person-square','admin','2017-05-09 12:11:53','admin','2023-08-27 16:46:35'),
('b6b89559-6864-46ab-9ca9-0992dcf238f1','CORE_PROG001D0001Q','ZA01 - System site','#/prog001d0001','N','N',0,0,'CORE','ITEM','COMPUTER','globe2','admin','2014-10-02 00:00:00','admin','2021-01-20 08:20:29'),
('b978f706-4c5f-40f8-83b1-395492f141d4','CORE_PROG002D0001Q','ZB01 - Role','#/prog002d0001','N','N',0,0,'CORE','ITEM','PEOPLE','person-square','admin','2017-05-08 21:32:50','admin','2023-08-27 16:46:27'),
('bfeb7935-334f-4a94-9666-f77433793a8a','CORE_PROG005D','ZE. MQTT','/','N','N',0,0,'CORE','FOLDER','PROPERTIES','gear-fill','admin','2026-06-04 11:00:54',NULL,NULL),
('c96ebde8-7044-4b05-a155-68a0c2605619','CORE_PROG002D0003Q','ZB03 - Role for menu','#/prog002d0003','N','N',0,0,'CORE','ITEM','FOLDER','menu-app-fill','admin','2017-05-08 21:37:01','admin','2023-08-28 19:54:34'),
('cb565ce5-8e29-11f1-a861-005056c00001','FM_PROG002D0004Q','FB04 - 職稱與簽核Level','#/fm_prog002d0004','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-08-02 12:22:30',NULL,NULL),
('cb58395d-8e29-11f1-a861-005056c00001','FM_PROG002D0004A','FB04 - 職稱與簽核Level（新增）','#/fm_prog002d0004/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-08-02 12:22:30',NULL,NULL),
('cb59989b-8e29-11f1-a861-005056c00001','FM_PROG002D0004E','FB04 - 職稱與簽核Level（編輯）','#/fm_prog002d0004/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-08-02 12:22:30',NULL,NULL),
('da7d969a-5efb-4e84-9eab-4fdae236f28c','CORE_PROG002D0002Q','ZB02 - User role','#/prog002d0002','N','N',0,0,'CORE','ITEM','PERSON','person-check','admin','2017-05-08 21:34:39','admin','2023-08-28 19:54:25'),
('dda67b1d-e3a2-4534-835a-c62d9e8421f3','CORE_PROG001D0005S01Q','ZA05 - JasperReport (Parameter)','#/prog001d0005/setparam','Y','N',0,0,'CORE','ITEM','APPLICATION_PDF','file-pdf','admin','2017-05-18 09:57:26','admin','2023-08-24 20:21:02'),
('e32b9329-bb38-46d7-8552-2307bac77724','CORE_PROG001D0002A','ZA02 - Program (Create)','#/prog001d0002/create','N','N',0,0,'CORE','ITEM','G_APP_INSTALL','filetype-html','admin','2014-10-02 00:00:00','admin','2023-08-15 19:19:42'),
('e4ff684a-8e41-11f1-a861-005056c00001','FM_PROG002D0006Q','FB06 - 部門職務與擔任人','#/fm_prog002d0006','N','N',0,0,'CORE','ITEM','ORGANIZATION','person-workspace','admin','2026-08-02 15:15:01',NULL,NULL),
('e5010d72-8e41-11f1-a861-005056c00001','FM_PROG002D0006A','FB06 - 部門職務與擔任人（新增）','#/fm_prog002d0006/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','person-workspace','admin','2026-08-02 15:15:01',NULL,NULL),
('e5019cee-8e41-11f1-a861-005056c00001','FM_PROG002D0006E','FB06 - 部門職務與擔任人（編輯）','#/fm_prog002d0006/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','person-workspace','admin','2026-08-02 15:15:01',NULL,NULL),
('e86dbb1b-6870-4827-8039-72f5e15fa4f2','CORE_PROG004D','ZD. Log','/','N','N',0,0,'CORE','FOLDER','PROPERTIES','clipboard-check-fill','admin','2017-06-03 14:21:03','admin','2023-08-29 10:14:04'),
('eb6e199f-c853-4fbf-acf3-0c9c77ba9953','CORE_PROG001D0002Q','ZA02 - Program','#/prog001d0002','N','N',0,0,'CORE','ITEM','G_APP_INSTALL','filetype-html','admin','2014-10-02 00:00:00','admin','2023-08-15 19:19:05'),
('eb786ffd-c7d1-4631-aed2-4d9d7368eb13','CORE_PROG001D0005Q','ZA05 - JasperReport','#/prog001d0005','N','N',0,0,'CORE','ITEM','APPLICATION_PDF','file-pdf','admin','2017-05-18 09:54:35','admin','2023-08-24 20:20:16');
/*!40000 ALTER TABLE `tb_sys_prog` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_qfield_log`
--

DROP TABLE IF EXISTS `tb_sys_qfield_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_qfield_log` (
  `OID` char(36) NOT NULL,
  `SYSTEM` varchar(10) NOT NULL,
  `PROG_ID` varchar(50) NOT NULL,
  `METHOD_NAME` varchar(255) NOT NULL,
  `FIELD_NAME` varchar(255) NOT NULL,
  `FIELD_VALUE` varchar(500) DEFAULT NULL,
  `QUERY_USER_ID` varchar(24) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  KEY `IDX_1` (`SYSTEM`,`PROG_ID`),
  KEY `IDX_2` (`QUERY_USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_qfield_log`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_qfield_log` WRITE;
/*!40000 ALTER TABLE `tb_sys_qfield_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_qfield_log` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_template`
--

DROP TABLE IF EXISTS `tb_sys_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_template` (
  `OID` char(36) NOT NULL,
  `TEMPLATE_ID` varchar(10) NOT NULL,
  `TITLE` varchar(200) NOT NULL,
  `MESSAGE` varchar(4000) NOT NULL,
  `DESCRIPTION` varchar(200) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`TEMPLATE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_template`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_template` WRITE;
/*!40000 ALTER TABLE `tb_sys_template` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_template` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_template_param`
--

DROP TABLE IF EXISTS `tb_sys_template_param`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_template_param` (
  `OID` char(36) NOT NULL,
  `TEMPLATE_ID` varchar(10) NOT NULL,
  `IS_TITLE` varchar(1) NOT NULL DEFAULT 'N',
  `TEMPLATE_VAR` varchar(100) NOT NULL,
  `OBJECT_VAR` varchar(100) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`TEMPLATE_ID`,`TEMPLATE_VAR`,`IS_TITLE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_template_param`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_template_param` WRITE;
/*!40000 ALTER TABLE `tb_sys_template_param` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_template_param` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_token`
--

DROP TABLE IF EXISTS `tb_sys_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_token` (
  `OID` char(36) NOT NULL,
  `USER_ID` varchar(24) NOT NULL,
  `TOKEN` varchar(2048) NOT NULL,
  `EXPIRES_DATE` datetime NOT NULL,
  `RF_EXPIRES_DATE` datetime NOT NULL,
  `CDATE` datetime NOT NULL,
  PRIMARY KEY (`OID`),
  KEY `IDX_1` (`USER_ID`),
  KEY `IDX_2` (`TOKEN`(1024))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_token`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_token` WRITE;
/*!40000 ALTER TABLE `tb_sys_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_token` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_upload`
--

DROP TABLE IF EXISTS `tb_sys_upload`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_upload` (
  `OID` char(36) NOT NULL,
  `SYSTEM` varchar(10) NOT NULL,
  `SUB_DIR` varchar(4) NOT NULL,
  `TYPE` varchar(10) NOT NULL,
  `FILE_NAME` varchar(50) NOT NULL,
  `SHOW_NAME` varchar(255) NOT NULL,
  `IS_FILE` varchar(1) NOT NULL DEFAULT 'Y',
  `CONTENT` mediumblob DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  KEY `IDX_1` (`SYSTEM`,`TYPE`,`SUB_DIR`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_upload`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_upload` WRITE;
/*!40000 ALTER TABLE `tb_sys_upload` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_upload` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_sys_usess`
--

DROP TABLE IF EXISTS `tb_sys_usess`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_sys_usess` (
  `OID` char(36) NOT NULL,
  `SESSION_ID` varchar(64) NOT NULL,
  `ACCOUNT` varchar(24) NOT NULL,
  `CURRENT_ID` varchar(36) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`,`SESSION_ID`),
  UNIQUE KEY `UK_1` (`ACCOUNT`,`SESSION_ID`),
  KEY `IDX_1` (`CURRENT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_sys_usess`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_sys_usess` WRITE;
/*!40000 ALTER TABLE `tb_sys_usess` DISABLE KEYS */;
/*!40000 ALTER TABLE `tb_sys_usess` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `tb_user_role`
--

DROP TABLE IF EXISTS `tb_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `tb_user_role` (
  `OID` char(36) NOT NULL,
  `ROLE` varchar(50) NOT NULL,
  `ACCOUNT` varchar(24) NOT NULL,
  `DESCRIPTION` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime NOT NULL,
  `UUSERID` varchar(50) DEFAULT NULL,
  `UDATE` datetime DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_1` (`ROLE`,`ACCOUNT`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tb_user_role`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `tb_user_role` WRITE;
/*!40000 ALTER TABLE `tb_user_role` DISABLE KEYS */;
INSERT INTO `tb_user_role` VALUES
('1c62cf70-ca6b-4243-8aa9-49b555024c45','COMMON01','steven','','admin','2017-05-10 14:19:58',NULL,NULL),
('9243c7de-43b1-46ef-ac4b-2620697f319e','admin','admin','Administrator','admin','2014-09-23 00:00:00',NULL,NULL),
('a3d8caa3-45a8-11ee-b979-e9dd94b50b2d','COMMON01','tiffany','','admin','2023-08-28 21:41:50',NULL,NULL),
('bd7bf78c-d84b-4524-8273-273f883d30b5','COMMON01','tester','','admin','2017-05-10 11:01:50',NULL,NULL);
/*!40000 ALTER TABLE `tb_user_role` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2026-08-02 15:22:29
