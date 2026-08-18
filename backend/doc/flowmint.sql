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
INSERT INTO `fm_approval_authority` VALUES
('eec87608-97eb-11f1-a6a7-005056c00001','A01','eec85023-97eb-11f1-a6a7-005056c00001','PURCHASE_GM_APPROVAL','請購總經理核決',NULL,'2d939f65-b78e-454d-a48d-cdd538222d96','ACTIVE','2026-08-14 22:25:00.441',NULL,'請購專案總額超過新台幣 300,000 元時，由總經理核決。','admin','2026-08-14 22:25:00.441',NULL,NULL),
('eec88ab6-97eb-11f1-a6a7-005056c00001','A01','eec856db-97eb-11f1-a6a7-005056c00001','PURCHASE_CHAIRMAN_APPROVAL','請購董事長核決',NULL,'2d939f65-b78e-454d-a48d-cdd538222d96','ACTIVE','2026-08-14 22:25:00.442',NULL,'請購專案總額超過新台幣 500,000 元時，由董事長核決。','admin','2026-08-14 22:25:00.442',NULL,NULL);
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
INSERT INTO `fm_approval_authority_rule` VALUES
('eec8bceb-97eb-11f1-a6a7-005056c00001','A01','eec8bd0f-97eb-11f1-a6a7-005056c00001','eec85023-97eb-11f1-a6a7-005056c00001',1,'{\"match\": \"ALL\", \"conditions\": [{\"field\": \"form.projectTotalAmount\", \"operator\": \"GT\", \"value\": 300000}]}','FIXED_ACCOUNT','fm00100',NULL,'Y','ACTIVE','admin','2026-08-14 22:25:00.442','admin','2026-08-14 22:25:21.723'),
('eec8df7e-97eb-11f1-a6a7-005056c00001','A01','eec8dfa4-97eb-11f1-a6a7-005056c00001','eec856db-97eb-11f1-a6a7-005056c00001',1,'{\"match\": \"ALL\", \"conditions\": [{\"field\": \"form.projectTotalAmount\", \"operator\": \"GT\", \"value\": 500000}]}','FIXED_ACCOUNT','fm00002',NULL,'Y','ACTIVE','admin','2026-08-14 22:25:00.444','admin','2026-08-14 22:25:21.741');
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
INSERT INTO `fm_approval_group` VALUES
('4340fa09-8e47-11f1-8601-b976d82f5112','A01','00711d09-ee9e-43fc-8bd9-30ca85ed8d5e','FIN01','財務通知','SEQUENTIAL','ACTIVE','','admin','2026-08-02 15:53:34.735',NULL,NULL),
('4cede32f-97eb-11f1-a6a7-005056c00001','A01','4cede333-97eb-11f1-a6a7-005056c00001','PURCHASE_PROCESS_REVIEW','製程審查','CANDIDATE','ACTIVE','產線設備、原物料及製程相容性專業審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede3bf-97eb-11f1-a6a7-005056c00001','A01','4cede3c0-97eb-11f1-a6a7-005056c00001','PURCHASE_EQUIPMENT_REVIEW','設備工程審查','CANDIDATE','ACTIVE','設備規格、安裝、維護、保固及公用需求審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede3cf-97eb-11f1-a6a7-005056c00001','A01','4cede3d0-97eb-11f1-a6a7-005056c00001','PURCHASE_IT_REVIEW','資訊／資安審查','CANDIDATE','ACTIVE','資訊設備、軟體、設備連網、資料及資安審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede3de-97eb-11f1-a6a7-005056c00001','A01','4cede3df-97eb-11f1-a6a7-005056c00001','PURCHASE_QUALITY_REVIEW','品質審查','CANDIDATE','ACTIVE','品質、檢測設備、驗收標準及品質風險審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede3ed-97eb-11f1-a6a7-005056c00001','A01','4cede3ee-97eb-11f1-a6a7-005056c00001','PURCHASE_SAFETY_REVIEW','工安審查','CANDIDATE','ACTIVE','施工、動火、高處、用電及機械安全審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede3fd-97eb-11f1-a6a7-005056c00001','A01','4cede3fe-97eb-11f1-a6a7-005056c00001','PURCHASE_ENVIRONMENT_REVIEW','環保審查','CANDIDATE','ACTIVE','排放、化學品、環保及法規許可審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede40b-97eb-11f1-a6a7-005056c00001','A01','4cede40c-97eb-11f1-a6a7-005056c00001','PURCHASE_GENERAL_AFFAIRS','總務審查','CANDIDATE','ACTIVE','辦公設備、用品、總務及一般設施需求審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede41a-97eb-11f1-a6a7-005056c00001','A01','4cede41b-97eb-11f1-a6a7-005056c00001','PURCHASE_CONTRACT_REVIEW','合約／法務審查','CANDIDATE','INACTIVE','尚未指定合約或法務責任人；不得用於正式流程。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede429-97eb-11f1-a6a7-005056c00001','A01','4cede42a-97eb-11f1-a6a7-005056c00001','PURCHASE_COMMERCIAL_REVIEW','採購商務審查','CANDIDATE','ACTIVE','詢比議價、供應商、交期及採購條款審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede438-97eb-11f1-a6a7-005056c00001','A01','4cede439-97eb-11f1-a6a7-005056c00001','PURCHASE_FINANCE_REVIEW','財務審查','CANDIDATE','ACTIVE','預算、CAPEX/OPEX、資金及付款條件審查。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cede447-97eb-11f1-a6a7-005056c00001','A01','4cede448-97eb-11f1-a6a7-005056c00001','PURCHASE_INVESTMENT_REVIEW','重大投資審議','ALL','ACTIVE','重大 CAPEX 跨單位投資審議；正式門檻仍須公司確認。','SYSTEM','2026-08-14 22:20:28.895',NULL,NULL);
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
INSERT INTO `fm_approval_group_member` VALUES
('4cee153b-97eb-11f1-a6a7-005056c00001','A01','4cee1544-97eb-11f1-a6a7-005056c00001','4cede333-97eb-11f1-a6a7-005056c00001','2d200182-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee15f8-97eb-11f1-a6a7-005056c00001','A01','4cee15f9-97eb-11f1-a6a7-005056c00001','4cede333-97eb-11f1-a6a7-005056c00001','ac959867-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1657-97eb-11f1-a6a7-005056c00001','A01','4cee1658-97eb-11f1-a6a7-005056c00001','4cede3c0-97eb-11f1-a6a7-005056c00001','2d20046b-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee16ad-97eb-11f1-a6a7-005056c00001','A01','4cee16ae-97eb-11f1-a6a7-005056c00001','4cede3c0-97eb-11f1-a6a7-005056c00001','ac9598ba-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1707-97eb-11f1-a6a7-005056c00001','A01','4cee1708-97eb-11f1-a6a7-005056c00001','4cede3d0-97eb-11f1-a6a7-005056c00001','2d2004fd-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1765-97eb-11f1-a6a7-005056c00001','A01','4cee1766-97eb-11f1-a6a7-005056c00001','4cede3d0-97eb-11f1-a6a7-005056c00001','ac962163-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee17bc-97eb-11f1-a6a7-005056c00001','A01','4cee17bd-97eb-11f1-a6a7-005056c00001','4cede3df-97eb-11f1-a6a7-005056c00001','2d2009bc-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1813-97eb-11f1-a6a7-005056c00001','A01','4cee1814-97eb-11f1-a6a7-005056c00001','4cede3df-97eb-11f1-a6a7-005056c00001','ac95cf52-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1864-97eb-11f1-a6a7-005056c00001','A01','4cee1865-97eb-11f1-a6a7-005056c00001','4cede3df-97eb-11f1-a6a7-005056c00001','ac95d01a-9398-11f1-a8f5-005056c00001',30,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee18c5-97eb-11f1-a6a7-005056c00001','A01','4cee18c6-97eb-11f1-a6a7-005056c00001','4cede3df-97eb-11f1-a6a7-005056c00001','ac95d0dd-9398-11f1-a8f5-005056c00001',40,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee191b-97eb-11f1-a6a7-005056c00001','A01','4cee191c-97eb-11f1-a6a7-005056c00001','4cede3ee-97eb-11f1-a6a7-005056c00001','2d200a5a-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1973-97eb-11f1-a6a7-005056c00001','A01','4cee1974-97eb-11f1-a6a7-005056c00001','4cede3ee-97eb-11f1-a6a7-005056c00001','ac96221f-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee19c8-97eb-11f1-a6a7-005056c00001','A01','4cee19c9-97eb-11f1-a6a7-005056c00001','4cede3fe-97eb-11f1-a6a7-005056c00001','2d200d24-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1a32-97eb-11f1-a6a7-005056c00001','A01','4cee1a33-97eb-11f1-a6a7-005056c00001','4cede3fe-97eb-11f1-a6a7-005056c00001','ac962279-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1a83-97eb-11f1-a6a7-005056c00001','A01','4cee1a84-97eb-11f1-a6a7-005056c00001','4cede40c-97eb-11f1-a6a7-005056c00001','2d200324-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1ad3-97eb-11f1-a6a7-005056c00001','A01','4cee1ad4-97eb-11f1-a6a7-005056c00001','4cede40c-97eb-11f1-a6a7-005056c00001','ac962107-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1b23-97eb-11f1-a6a7-005056c00001','A01','4cee1b24-97eb-11f1-a6a7-005056c00001','4cede42a-97eb-11f1-a6a7-005056c00001','2d1fffab-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1b7b-97eb-11f1-a6a7-005056c00001','A01','4cee1b7c-97eb-11f1-a6a7-005056c00001','4cede42a-97eb-11f1-a6a7-005056c00001','ac9617f1-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1bcf-97eb-11f1-a6a7-005056c00001','A01','4cee1bd0-97eb-11f1-a6a7-005056c00001','4cede42a-97eb-11f1-a6a7-005056c00001','ac9618c8-9398-11f1-a8f5-005056c00001',30,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1c24-97eb-11f1-a6a7-005056c00001','A01','4cee1c25-97eb-11f1-a6a7-005056c00001','4cede439-97eb-11f1-a6a7-005056c00001','2d200c87-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1c7a-97eb-11f1-a6a7-005056c00001','A01','4cee1c7b-97eb-11f1-a6a7-005056c00001','4cede439-97eb-11f1-a6a7-005056c00001','ac961cfd-9398-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1cbf-97eb-11f1-a6a7-005056c00001','A01','4cee1cc0-97eb-11f1-a6a7-005056c00001','4cede448-97eb-11f1-a6a7-005056c00001','2d1fff2d-938f-11f1-a8f5-005056c00001',10,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1d06-97eb-11f1-a6a7-005056c00001','A01','4cee1d07-97eb-11f1-a6a7-005056c00001','4cede448-97eb-11f1-a6a7-005056c00001','2d200888-938f-11f1-a8f5-005056c00001',20,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1d55-97eb-11f1-a6a7-005056c00001','A01','4cee1d56-97eb-11f1-a6a7-005056c00001','4cede448-97eb-11f1-a6a7-005056c00001','2d1ffe20-938f-11f1-a8f5-005056c00001',30,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1d9c-97eb-11f1-a6a7-005056c00001','A01','4cee1d9d-97eb-11f1-a6a7-005056c00001','4cede448-97eb-11f1-a6a7-005056c00001','2d200ad4-938f-11f1-a8f5-005056c00001',40,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('4cee1df0-97eb-11f1-a6a7-005056c00001','A01','4cee1df1-97eb-11f1-a6a7-005056c00001','4cede448-97eb-11f1-a6a7-005056c00001','2d200db8-938f-11f1-a8f5-005056c00001',50,'ACTIVE','2026-08-14 22:20:28.895',NULL,'SYSTEM','2026-08-14 22:20:28.895',NULL,NULL),
('51f5ee30-8e47-11f1-8601-73527e41dcc6','A01','05591c9a-b103-4b81-84f6-b34b6e409a4d','00711d09-ee9e-43fc-8bd9-30ca85ed8d5e','a770ca51-0137-409f-af3a-af895f22ee50',100,'ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-08-02 15:53:59.410',NULL,NULL),
('5888ab73-8e47-11f1-8601-23d22da3d77d','A01','2ad62b93-4ed4-4efd-b1c2-bfcd5c7ab407','00711d09-ee9e-43fc-8bd9-30ca85ed8d5e','a3bb53cb-fbb1-4c9d-870b-9d2e39d17cba',101,'ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-08-02 15:54:10.437',NULL,NULL);
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
-- Table structure for table `fm_attachment_upload_file`
--

DROP TABLE IF EXISTS `fm_attachment_upload_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_attachment_upload_file` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `ATTACHMENT_ID` varchar(36) NOT NULL,
  `UPLOAD_SESSION_ID` varchar(36) NOT NULL,
  `FIELD_KEY` varchar(100) NOT NULL,
  `FILE_OID` char(36) NOT NULL,
  `FILE_NAME` varchar(255) NOT NULL,
  `CONTENT_TYPE` varchar(100) NOT NULL,
  `FILE_SIZE` bigint(20) NOT NULL,
  `CONTENT_SHA256` char(64) NOT NULL,
  `FILE_STATUS` varchar(20) NOT NULL DEFAULT 'TEMPORARY',
  `SCAN_STATUS` varchar(20) NOT NULL DEFAULT 'PENDING',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_ATTACHMENT_UPLOAD_FILE` (`TENANT_ID`,`ATTACHMENT_ID`),
  KEY `IDX_FM_ATTACHMENT_UPLOAD_SESSION_FILE` (`TENANT_ID`,`UPLOAD_SESSION_ID`,`FILE_STATUS`),
  CONSTRAINT `CK_FM_ATTACHMENT_UPLOAD_FILE_SIZE` CHECK (`FILE_SIZE` > 0),
  CONSTRAINT `CK_FM_ATTACHMENT_UPLOAD_FILE_STATUS` CHECK (`FILE_STATUS` in ('TEMPORARY','BOUND','DELETED')),
  CONSTRAINT `CK_FM_ATTACHMENT_UPLOAD_SCAN_STATUS` CHECK (`SCAN_STATUS` in ('PENDING','CLEAN','INFECTED','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_attachment_upload_file`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_attachment_upload_file` WRITE;
/*!40000 ALTER TABLE `fm_attachment_upload_file` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_attachment_upload_file` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_attachment_upload_session`
--

DROP TABLE IF EXISTS `fm_attachment_upload_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_attachment_upload_session` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `UPLOAD_SESSION_ID` varchar(36) NOT NULL,
  `OWNER_ACCOUNT` varchar(100) NOT NULL,
  `FORM_ID` varchar(36) NOT NULL,
  `FORM_VERSION_NO` int(11) NOT NULL,
  `SESSION_STATUS` varchar(20) NOT NULL DEFAULT 'OPEN',
  `EXPIRES_DATE` datetime(3) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_ATTACHMENT_UPLOAD_SESSION` (`TENANT_ID`,`UPLOAD_SESSION_ID`),
  KEY `IDX_FM_ATTACHMENT_UPLOAD_OWNER` (`TENANT_ID`,`OWNER_ACCOUNT`,`SESSION_STATUS`,`EXPIRES_DATE`),
  CONSTRAINT `CK_FM_ATTACHMENT_UPLOAD_STATUS` CHECK (`SESSION_STATUS` in ('OPEN','BOUND','EXPIRED','CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_attachment_upload_session`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_attachment_upload_session` WRITE;
/*!40000 ALTER TABLE `fm_attachment_upload_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_attachment_upload_session` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_data_action`
--

DROP TABLE IF EXISTS `fm_data_action`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_data_action` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(50) NOT NULL,
  `ACTION_ID` varchar(50) NOT NULL,
  `ACTION_CODE` varchar(50) NOT NULL,
  `ACTION_NAME` varchar(100) NOT NULL,
  `POOL_ID` varchar(50) NOT NULL,
  `ACTION_TYPE` varchar(20) NOT NULL,
  `REQUEST_SCHEMA` longtext NOT NULL,
  `RESPONSE_MODE` varchar(20) NOT NULL,
  `STATUS` varchar(20) NOT NULL,
  `CURRENT_VERSION_NO` int(11) NOT NULL DEFAULT 0,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `LOCK_VERSION` int(11) NOT NULL DEFAULT 0,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_DATA_ACTION_CODE` (`TENANT_ID`,`ACTION_CODE`),
  UNIQUE KEY `UK_FM_DATA_ACTION_ID` (`TENANT_ID`,`ACTION_ID`),
  KEY `IX_FM_DATA_ACTION_POOL` (`TENANT_ID`,`POOL_ID`,`STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_data_action`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_data_action` WRITE;
/*!40000 ALTER TABLE `fm_data_action` DISABLE KEYS */;
INSERT INTO `fm_data_action` VALUES
('06367c6a-4ed7-439f-bf4c-fc208e3de834','A01','fe970af8-94c5-403e-b08a-748fac30d9df','FM_PURCHASE_EMPLOYEE_OPTIONS','請購單有效員工選項','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"keyword\":\"$.keyword\"}','COMPOSITE','ACTIVE',1,'請購表單專用唯讀員工選項，回傳帳號、姓名與有效主要部門。',0,'SYSTEM','2026-08-12 21:24:01.454',NULL,NULL),
('0baa1654-3a39-4ad2-9fa9-9db220e2c8e8','A01','1cd2168e-62b3-47c6-92e1-53fbf38700ac','FM_LIST_ORG_DUTIES','FM_LIST_ORG_DUTIES','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"orgUnitId\":\"$.orgUnitId\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:10:45.941'),
('0eb87fb2-0e73-4ba8-8052-5b7933fe04fc','A01','75492614-9220-48d2-a79b-5ee4f34ff1d0','FM_GET_MY_PRIMARY_ASSIGNMENT','FM_GET_MY_PRIMARY_ASSIGNMENT','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:08:57.510'),
('16ae20ff-8666-4724-892b-10e6ea0604fe','A01','81a1db37-e29e-433c-a6df-49df2f9347cc','FM_LIST_MY_ASSIGNMENTS','FM_LIST_MY_ASSIGNMENTS','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:10:32.605'),
('29776cba-f15f-487a-91bb-38c4411ff10b','A01','eab2f1f2-f65c-429c-bfcf-ecada28d2697','FM_SEARCH_ACTIVE_EMPLOYEES','FM_SEARCH_ACTIVE_EMPLOYEES','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"keyword\":\"$.keyword\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:11:21.480'),
('4fe8e554-7d7b-4ef4-a237-362c73d3f971','A01','58a58b4c-0151-42f7-8842-84ad7dd7ef4a','FM_GET_ORG_PATH','FM_GET_ORG_PATH','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"orgUnitId\":\"$.orgUnitId\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:09:29.269'),
('74f1eafb-5c23-41ba-87b9-bdd1af19ebcc','A01','a5e883e1-3c1e-457d-ae02-71c33dd9cd02','FM_LIST_ORG_UNITS','FM_LIST_ORG_UNITS','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"keyword\":\"$.keyword\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:11:08.388'),
('76544c2d-f1c1-4e28-9830-9e4a20af75fc','A01','07923ae7-d0f2-4024-a587-6a50f44dfe36','FM_LIST_APPROVAL_GROUP_MEMBERS','FM_LIST_APPROVAL_GROUP_MEMBERS','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"approvalGroupId\":\"$.approvalGroupId\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:09:55.798'),
('9f542431-ba74-4afd-aeab-a3263107a0a9','A01','bfc4a332-3f94-4056-85bf-d4461893986a','FM_GET_CURRENT_EMPLOYEE','FM_GET_CURRENT_EMPLOYEE','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:07:44.455'),
('a34cc7e7-8d46-45f4-ae81-7cd3131dda9b','A01','afafc038-b9d1-4b6d-b275-c17eb11ebd8a','FM_LIST_APPROVAL_GROUPS','FM_LIST_APPROVAL_GROUPS','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"keyword\":\"$.keyword\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:10:13.398'),
('a8f06f50-5a58-4825-9fd0-b1ddf57ad213','A01','68e850e3-ab84-464a-9b58-1808d38219b9','FM_PURCHASE_EMPLOYEE_ASSIGNMENTS','請購申請人有效任職','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"applicantAccount\":\"$.applicantAccount\"}','COMPOSITE','ACTIVE',1,'依請購申請人帳號回傳有效主要及兼任部門。',0,'SYSTEM','2026-08-12 21:34:55.287',NULL,NULL),
('bcbc24a7-3259-46e2-a191-273888851428','A01','1926df27-9e01-4b7b-8730-431183fc15e2','FM_LIST_ORG_CHILDREN','FM_LIST_ORG_CHILDREN','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"parentOrgUnitId\":\"$.parentOrgUnitId\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:10:39.178'),
('dfb0ebfc-f218-4c91-a5ff-de00b90ea346','A01','db5d5935-0ca4-4931-9a31-cff606a8b5c7','FM_LIST_EMPLOYEES_BY_ORG','FM_LIST_EMPLOYEES_BY_ORG','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"orgUnitId\":\"$.orgUnitId\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:10:20.417'),
('e1fece91-401a-42ad-9a1a-d2c8f30f5c7d','A01','7db42add-deb0-4c11-8766-91d097405d40','FM_LIST_ORG_TITLES','FM_LIST_ORG_TITLES','7f177f66-864e-46cb-8b7e-48081be7d859','QUERY','{\"keyword\":\"$.keyword\"}','COMPOSITE','ACTIVE',1,'P0 common readonly action',2,'SYSTEM','2026-08-05 21:40:37.550','admin','2026-08-06 09:10:53.259');
/*!40000 ALTER TABLE `fm_data_action` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_data_action_step`
--

DROP TABLE IF EXISTS `fm_data_action_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_data_action_step` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(50) NOT NULL,
  `ACTION_ID` varchar(50) NOT NULL,
  `VERSION_NO` int(11) NOT NULL,
  `STEP_ID` varchar(50) NOT NULL,
  `STEP_CODE` varchar(50) NOT NULL,
  `STEP_NAME` varchar(100) NOT NULL,
  `EXECUTION_ORDER` int(11) NOT NULL,
  `STATEMENT_TYPE` varchar(20) NOT NULL,
  `EXECUTION_MODE` varchar(20) NOT NULL,
  `SQL_CONTENT` longtext NOT NULL,
  `ARRAY_PATH` varchar(200) DEFAULT NULL,
  `RESULT_KEY` varchar(100) NOT NULL,
  `RESULT_MODE` varchar(20) NOT NULL,
  `EXPECT_AFFECTED_ROWS` int(11) DEFAULT NULL,
  `CONTINUE_CONDITION` varchar(500) DEFAULT NULL,
  `QUERY_TIMEOUT_SECONDS` int(11) NOT NULL DEFAULT 30,
  `MAX_ROWS` int(11) NOT NULL DEFAULT 1000,
  `STATUS` varchar(20) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_DATA_ACTION_STEP_CODE` (`TENANT_ID`,`ACTION_ID`,`VERSION_NO`,`STEP_CODE`),
  UNIQUE KEY `UK_FM_DATA_ACTION_RESULT_KEY` (`TENANT_ID`,`ACTION_ID`,`VERSION_NO`,`RESULT_KEY`),
  KEY `IX_FM_DATA_ACTION_STEP_ORDER` (`TENANT_ID`,`ACTION_ID`,`VERSION_NO`,`EXECUTION_ORDER`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_data_action_step`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_data_action_step` WRITE;
/*!40000 ALTER TABLE `fm_data_action_step` DISABLE KEYS */;
INSERT INTO `fm_data_action_step` VALUES
('20fa1ccd-5abd-44fd-83bf-60b4ce00ba44','A01','1cd2168e-62b3-47c6-92e1-53fbf38700ac',1,'3568d733-4a57-4057-8539-3e8bd918952c','FM_LIST_ORG_DUTIES','FM_LIST_ORG_DUTIES',10,'SELECT_LIST','ONCE','SELECT d.DUTY_ID AS value, CONCAT(d.DUTY_CODE,\'／\',d.DUTY_NAME) AS label,\n       \'N\' AS disabled, d.ORG_UNIT_ID AS orgUnitId, d.DUTY_CODE AS dutyCode,\n       d.DUTY_NAME AS dutyName, d.DUTY_TYPE AS dutyType\n  FROM fm_org_duty d\n WHERE d.TENANT_ID=:tenantId AND d.ORG_UNIT_ID=:orgUnitId\n   AND d.STATUS=\'ACTIVE\'\n   AND (d.EFFECTIVE_FROM IS NULL OR d.EFFECTIVE_FROM<=:now)\n   AND (d.EFFECTIVE_TO IS NULL OR d.EFFECTIVE_TO>:now)\n ORDER BY d.DUTY_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('23eef43d-7c82-4a66-b7e1-cab8e3544106','A01','a5e883e1-3c1e-457d-ae02-71c33dd9cd02',1,'72dafd06-2a7f-4cb9-b5cd-240d4c6ff5fb','FM_LIST_ORG_UNITS','FM_LIST_ORG_UNITS',10,'SELECT_LIST','ONCE','SELECT u.ORG_UNIT_ID AS value, CONCAT(u.UNIT_CODE,\'／\',v.UNIT_NAME) AS label,\n       \'N\' AS disabled, u.UNIT_CODE AS unitCode, v.UNIT_NAME AS unitName,\n       v.PARENT_ORG_UNIT_ID AS parentOrgUnitId, v.UNIT_TYPE AS unitType,\n       v.TREE_DEPTH AS treeDepth, v.PATH AS path\n  FROM fm_org_unit u\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n WHERE u.TENANT_ID=:tenantId AND v.STATUS=\'ACTIVE\'\n   AND (v.EFFECTIVE_FROM IS NULL OR v.EFFECTIVE_FROM<=:now)\n   AND (v.EFFECTIVE_TO IS NULL OR v.EFFECTIVE_TO>:now)\n   AND (:keyword IS NULL OR :keyword=\'\' OR u.UNIT_CODE LIKE CONCAT(\'%\',:keyword,\'%\') OR v.UNIT_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY v.PATH, v.SORT_NO, u.UNIT_CODE',NULL,'options','LIST',NULL,NULL,30,1000,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('2b48f659-f553-41d6-8338-dd532f722eee','A01','db5d5935-0ca4-4931-9a31-cff606a8b5c7',1,'5ecb9ae8-325e-4a11-872d-fa3669a03acd','FM_LIST_EMPLOYEES_BY_ORG','FM_LIST_EMPLOYEES_BY_ORG',10,'SELECT_LIST','ONCE','SELECT e.EMPLOYEE_ID AS employeeId, e.EMPLOYEE_NO AS employeeNo,\n       e.DISPLAY_NAME AS displayName, a.EMPLOYEE_ORG_ASSIGNMENT_ID AS assignmentId,\n       a.IS_PRIMARY AS isPrimary, t.TITLE_NAME AS titleName,\n       CONCAT(e.EMPLOYEE_NO,\'／\',e.DISPLAY_NAME) AS label\n  FROM fm_employee_org_assignment a\n  JOIN fm_employee e ON e.TENANT_ID=a.TENANT_ID AND e.EMPLOYEE_ID=a.EMPLOYEE_ID\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE a.TENANT_ID=:tenantId AND a.ORG_UNIT_ID=:orgUnitId\n   AND a.STATUS=\'ACTIVE\' AND e.STATUS=\'ACTIVE\'\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM<=:now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO>:now)\n ORDER BY CASE WHEN a.IS_PRIMARY=\'Y\' THEN 0 ELSE 1 END, e.EMPLOYEE_NO',NULL,'employees','LIST',NULL,NULL,30,500,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('3b035600-9133-11f1-9d9b-e513afe164c0','A01','bfc4a332-3f94-4056-85bf-d4461893986a',2,'5c8334f8-352c-47f8-ab32-276152616fa3','FM_GET_CURRENT_EMPLOYEE','FM_GET_CURRENT_EMPLOYEE',10,'SELECT_ONE','ONCE','SELECT e.EMPLOYEE_ID AS employeeId, e.EMPLOYEE_NO AS employeeNo,\n       e.ACCOUNT AS account, e.DISPLAY_NAME AS displayName,\n       e.EMAIL AS email, e.LOCALE AS locale, e.TIMEZONE AS timezone\n  FROM fm_employee e\n WHERE e.TENANT_ID = :tenantId AND e.ACCOUNT = :loginAccount\n   AND e.STATUS = \'ACTIVE\'\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM <= :now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO > :now)',NULL,'employee','OBJECT',NULL,NULL,30,1,'ACTIVE','admin','2026-08-06 09:07:44.464',NULL,NULL),
('3dc2a511-2433-453f-9dbb-6e58276b3d5a','A01','eab2f1f2-f65c-429c-bfcf-ecada28d2697',1,'b9abba6d-c7d1-4343-b6ab-0b5484f813f4','FM_SEARCH_ACTIVE_EMPLOYEES','FM_SEARCH_ACTIVE_EMPLOYEES',10,'SELECT_LIST','ONCE','SELECT e.EMPLOYEE_ID AS employeeId, e.EMPLOYEE_NO AS employeeNo,\n       e.DISPLAY_NAME AS displayName, a.ORG_UNIT_ID AS orgUnitId,\n       u.UNIT_CODE AS orgUnitCode, v.UNIT_NAME AS orgUnitName,\n       t.TITLE_NAME AS titleName, CONCAT(e.EMPLOYEE_NO,\'／\',e.DISPLAY_NAME) AS label\n  FROM fm_employee e\n  LEFT JOIN fm_employee_org_assignment a ON a.TENANT_ID=e.TENANT_ID AND a.EMPLOYEE_ID=e.EMPLOYEE_ID AND a.IS_PRIMARY=\'Y\' AND a.STATUS=\'ACTIVE\'\n  LEFT JOIN fm_org_unit u ON u.TENANT_ID=a.TENANT_ID AND u.ORG_UNIT_ID=a.ORG_UNIT_ID\n  LEFT JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE e.TENANT_ID=:tenantId AND e.STATUS=\'ACTIVE\'\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM<=:now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO>:now)\n   AND (:keyword IS NULL OR :keyword=\'\' OR e.EMPLOYEE_NO LIKE CONCAT(\'%\',:keyword,\'%\') OR e.DISPLAY_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY e.EMPLOYEE_NO',NULL,'employees','LIST',NULL,NULL,30,100,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('45f10774-2828-4f6c-a298-e33056f0d82a','A01','7db42add-deb0-4c11-8766-91d097405d40',1,'9bcd6b2a-85d9-4eb7-8f64-252a5be008c3','FM_LIST_ORG_TITLES','FM_LIST_ORG_TITLES',10,'SELECT_LIST','ONCE','SELECT t.TITLE_ID AS value, CONCAT(t.TITLE_CODE,\'／\',t.TITLE_NAME) AS label,\n       \'N\' AS disabled, t.TITLE_CODE AS titleCode, t.TITLE_NAME AS titleName,\n       t.APPROVAL_LEVEL_ID AS approvalLevelId, t.IS_MANAGER_TITLE AS isManagerTitle\n  FROM fm_org_title t\n WHERE t.TENANT_ID=:tenantId AND t.STATUS=\'ACTIVE\'\n   AND (t.EFFECTIVE_FROM IS NULL OR t.EFFECTIVE_FROM<=:now)\n   AND (t.EFFECTIVE_TO IS NULL OR t.EFFECTIVE_TO>:now)\n   AND (:keyword IS NULL OR :keyword=\'\' OR t.TITLE_CODE LIKE CONCAT(\'%\',:keyword,\'%\') OR t.TITLE_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY t.SORT_NO, t.TITLE_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('4abf820b-cb71-4bdd-aa57-fe9880071acb','A01','07923ae7-d0f2-4024-a587-6a50f44dfe36',1,'3cbfdab3-cc46-4e51-9655-51792f119a77','FM_LIST_APPROVAL_GROUP_MEMBERS','FM_LIST_APPROVAL_GROUP_MEMBERS',10,'SELECT_LIST','ONCE','SELECT m.APPROVAL_GROUP_MEMBER_ID AS approvalGroupMemberId,\n       m.APPROVAL_GROUP_ID AS approvalGroupId, e.EMPLOYEE_ID AS employeeId,\n       e.EMPLOYEE_NO AS employeeNo, e.DISPLAY_NAME AS displayName,\n       m.PRIORITY AS priority, CONCAT(e.EMPLOYEE_NO,\'／\',e.DISPLAY_NAME) AS label\n  FROM fm_approval_group_member m\n  JOIN fm_approval_group g ON g.TENANT_ID=m.TENANT_ID AND g.APPROVAL_GROUP_ID=m.APPROVAL_GROUP_ID\n  JOIN fm_employee e ON e.TENANT_ID=m.TENANT_ID AND e.EMPLOYEE_ID=m.EMPLOYEE_ID\n WHERE m.TENANT_ID=:tenantId AND m.APPROVAL_GROUP_ID=:approvalGroupId\n   AND g.STATUS=\'ACTIVE\' AND m.STATUS=\'ACTIVE\' AND e.STATUS=\'ACTIVE\'\n   AND (m.EFFECTIVE_FROM IS NULL OR m.EFFECTIVE_FROM<=:now)\n   AND (m.EFFECTIVE_TO IS NULL OR m.EFFECTIVE_TO>:now)\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM<=:now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO>:now)\n ORDER BY m.PRIORITY, e.EMPLOYEE_NO',NULL,'members','LIST',NULL,NULL,30,500,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('5685a791-f2da-41d9-af45-b6c3d69cfafe','A01','58a58b4c-0151-42f7-8842-84ad7dd7ef4a',1,'905f039b-da92-45bd-9285-ced3892c3344','FM_GET_ORG_PATH','FM_GET_ORG_PATH',10,'SELECT_ONE','ONCE','SELECT u.ORG_UNIT_ID AS orgUnitId, u.UNIT_CODE AS unitCode,\n       v.UNIT_NAME AS unitName, v.PARENT_ORG_UNIT_ID AS parentOrgUnitId,\n       v.TREE_DEPTH AS treeDepth, v.PATH AS path,\n       CONCAT(u.UNIT_CODE,\'／\',v.UNIT_NAME) AS label\n  FROM fm_org_unit u\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n WHERE u.TENANT_ID=:tenantId AND u.ORG_UNIT_ID=:orgUnitId\n   AND v.STATUS=\'ACTIVE\'',NULL,'orgPath','OBJECT',NULL,NULL,30,1,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('60bc90cf-04a0-4613-bfc1-6cf866825e9d','A01','1926df27-9e01-4b7b-8730-431183fc15e2',1,'4e972eec-a82f-4b5b-91f7-fd9011d76478','FM_LIST_ORG_CHILDREN','FM_LIST_ORG_CHILDREN',10,'SELECT_LIST','ONCE','SELECT u.ORG_UNIT_ID AS value, CONCAT(u.UNIT_CODE,\'／\',v.UNIT_NAME) AS label,\n       \'N\' AS disabled, u.UNIT_CODE AS unitCode, v.UNIT_NAME AS unitName,\n       v.PARENT_ORG_UNIT_ID AS parentOrgUnitId, v.UNIT_TYPE AS unitType,\n       v.TREE_DEPTH AS treeDepth\n  FROM fm_org_unit u\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n WHERE u.TENANT_ID=:tenantId AND v.PARENT_ORG_UNIT_ID=:parentOrgUnitId\n   AND v.STATUS=\'ACTIVE\'\n   AND (v.EFFECTIVE_FROM IS NULL OR v.EFFECTIVE_FROM<=:now)\n   AND (v.EFFECTIVE_TO IS NULL OR v.EFFECTIVE_TO>:now)\n ORDER BY v.SORT_NO, u.UNIT_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('668de1ab-9133-11f1-9d9b-e32ff6f092f0','A01','75492614-9220-48d2-a79b-5ee4f34ff1d0',2,'94e50523-7143-4af1-a431-83b054388fbc','FM_GET_MY_PRIMARY_ASSIGNMENT','FM_GET_MY_PRIMARY_ASSIGNMENT',10,'SELECT_ONE','ONCE','SELECT a.EMPLOYEE_ORG_ASSIGNMENT_ID AS assignmentId, a.EMPLOYEE_ID AS employeeId,\n       a.ORG_UNIT_ID AS orgUnitId, u.UNIT_CODE AS orgUnitCode,\n       v.UNIT_NAME AS orgUnitName, a.TITLE_ID AS titleId,\n       t.TITLE_CODE AS titleCode, t.TITLE_NAME AS titleName,\n       a.MANAGER_SOURCE AS managerSource\n  FROM fm_employee e\n  JOIN fm_employee_org_assignment a ON a.TENANT_ID=e.TENANT_ID AND a.EMPLOYEE_ID=e.EMPLOYEE_ID\n  JOIN fm_org_unit u ON u.TENANT_ID=a.TENANT_ID AND u.ORG_UNIT_ID=a.ORG_UNIT_ID\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE e.TENANT_ID=:tenantId AND e.ACCOUNT=:loginAccount\n   AND e.STATUS=\'ACTIVE\' AND a.STATUS=\'ACTIVE\' AND a.IS_PRIMARY=\'Y\' AND v.STATUS=\'ACTIVE\'\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM<=:now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO>:now)',NULL,'assignment','OBJECT',NULL,NULL,30,1,'ACTIVE','admin','2026-08-06 09:08:57.514',NULL,NULL),
('68105931-ff1e-4a26-87ca-5cddc08167e2','A01','68e850e3-ab84-464a-9b58-1808d38219b9',1,'be0ed019-1607-45c4-8a60-3f1906f3af4e','FM_PURCHASE_EMPLOYEE_ASSIGNMENTS','請購申請人有效任職',10,'SELECT_LIST','ONCE','SELECT a.EMPLOYEE_ORG_ASSIGNMENT_ID AS assignment_id,\n       a.ORG_UNIT_ID AS org_unit_id,\n       u.UNIT_CODE AS org_unit_code,\n       v.UNIT_NAME AS org_unit_name,\n       a.TITLE_ID AS title_id,\n       t.TITLE_NAME AS title_name,\n       a.IS_PRIMARY AS is_primary,\n       CONCAT(\n           u.UNIT_CODE,\n           \'／\',\n           v.UNIT_NAME,\n           CASE WHEN t.TITLE_NAME IS NULL THEN \'\' ELSE CONCAT(\'／\', t.TITLE_NAME) END,\n           CASE WHEN a.IS_PRIMARY = \'Y\' THEN \'（主要）\' ELSE \'（兼任）\' END\n       ) AS label\n  FROM fm_employee e\n  JOIN fm_employee_org_assignment a\n    ON a.TENANT_ID = e.TENANT_ID\n   AND a.EMPLOYEE_ID = e.EMPLOYEE_ID\n   AND a.STATUS = \'ACTIVE\'\n  JOIN fm_org_unit u\n    ON u.TENANT_ID = a.TENANT_ID\n   AND u.ORG_UNIT_ID = a.ORG_UNIT_ID\n  JOIN fm_org_unit_version v\n    ON v.TENANT_ID = u.TENANT_ID\n   AND v.ORG_UNIT_ID = u.ORG_UNIT_ID\n   AND v.VERSION_NO = u.CURRENT_VERSION_NO\n   AND v.STATUS = \'ACTIVE\'\n  LEFT JOIN fm_org_title t\n    ON t.TENANT_ID = a.TENANT_ID\n   AND t.TITLE_ID = a.TITLE_ID\n   AND t.STATUS = \'ACTIVE\'\n WHERE e.TENANT_ID = :tenantId\n   AND e.ACCOUNT = :applicantAccount\n   AND e.STATUS = \'ACTIVE\'\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM <= :now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO > :now)\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM <= :now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO > :now)\n   AND (v.EFFECTIVE_FROM IS NULL OR v.EFFECTIVE_FROM <= :now)\n   AND (v.EFFECTIVE_TO IS NULL OR v.EFFECTIVE_TO > :now)\n ORDER BY CASE WHEN a.IS_PRIMARY = \'Y\' THEN 0 ELSE 1 END,\n          u.UNIT_CODE',NULL,'assignments','LIST',NULL,NULL,30,50,'ACTIVE','SYSTEM','2026-08-12 21:34:55.288','SYSTEM','2026-08-12 22:22:19.830'),
('69167aa0-f355-49f8-a514-f83249fa2b71','A01','fe970af8-94c5-403e-b08a-748fac30d9df',1,'d89140ee-b59a-4f73-84c2-77dfce87c1bb','FM_PURCHASE_EMPLOYEE_OPTIONS','請購單有效員工選項',10,'SELECT_LIST','ONCE','SELECT e.EMPLOYEE_ID AS employee_id,\n       e.EMPLOYEE_NO AS employee_no,\n       e.ACCOUNT AS account,\n       e.DISPLAY_NAME AS display_name,\n       a.ORG_UNIT_ID AS org_unit_id,\n       u.UNIT_CODE AS org_unit_code,\n       v.UNIT_NAME AS org_unit_name,\n       CONCAT(\n           e.EMPLOYEE_NO,\n           \'／\',\n           e.DISPLAY_NAME,\n           CASE\n               WHEN v.UNIT_NAME IS NULL THEN \'\'\n               ELSE CONCAT(\'／\', v.UNIT_NAME)\n           END\n       ) AS label\n  FROM fm_employee e\n  LEFT JOIN fm_employee_org_assignment a\n    ON a.TENANT_ID = e.TENANT_ID\n   AND a.EMPLOYEE_ID = e.EMPLOYEE_ID\n   AND a.IS_PRIMARY = \'Y\'\n   AND a.STATUS = \'ACTIVE\'\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM <= :now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO > :now)\n  LEFT JOIN fm_org_unit u\n    ON u.TENANT_ID = a.TENANT_ID\n   AND u.ORG_UNIT_ID = a.ORG_UNIT_ID\n  LEFT JOIN fm_org_unit_version v\n    ON v.TENANT_ID = u.TENANT_ID\n   AND v.ORG_UNIT_ID = u.ORG_UNIT_ID\n   AND v.VERSION_NO = u.CURRENT_VERSION_NO\n   AND v.STATUS = \'ACTIVE\'\n   AND (v.EFFECTIVE_FROM IS NULL OR v.EFFECTIVE_FROM <= :now)\n   AND (v.EFFECTIVE_TO IS NULL OR v.EFFECTIVE_TO > :now)\n WHERE e.TENANT_ID = :tenantId\n   AND e.STATUS = \'ACTIVE\'\n   AND e.ACCOUNT IS NOT NULL\n   AND e.ACCOUNT <> \'\'\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM <= :now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO > :now)\n   AND (\n       :keyword IS NULL\n       OR :keyword = \'\'\n       OR e.EMPLOYEE_NO LIKE CONCAT(\'%\', :keyword, \'%\')\n       OR e.ACCOUNT LIKE CONCAT(\'%\', :keyword, \'%\')\n       OR e.DISPLAY_NAME LIKE CONCAT(\'%\', :keyword, \'%\')\n   )\n ORDER BY e.EMPLOYEE_NO',NULL,'employees','LIST',NULL,NULL,30,200,'ACTIVE','SYSTEM','2026-08-12 21:24:01.455','SYSTEM','2026-08-12 22:22:19.809'),
('797beba6-9133-11f1-9d9b-0fa4f6a72abc','A01','58a58b4c-0151-42f7-8842-84ad7dd7ef4a',2,'204391a3-b5f1-4c19-b5de-86eff4fd637a','FM_GET_ORG_PATH','FM_GET_ORG_PATH',10,'SELECT_ONE','ONCE','SELECT u.ORG_UNIT_ID AS orgUnitId, u.UNIT_CODE AS unitCode,\n       v.UNIT_NAME AS unitName, v.PARENT_ORG_UNIT_ID AS parentOrgUnitId,\n       v.TREE_DEPTH AS treeDepth, v.PATH AS path,\n       CONCAT(u.UNIT_CODE,\'／\',v.UNIT_NAME) AS label\n  FROM fm_org_unit u\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n WHERE u.TENANT_ID=:tenantId AND u.ORG_UNIT_ID=:orgUnitId\n   AND v.STATUS=\'ACTIVE\'',NULL,'orgPath','OBJECT',NULL,NULL,30,1,'ACTIVE','admin','2026-08-06 09:09:29.273',NULL,NULL),
('803c24ce-ccd3-4b53-921a-ded3fbcf353b','A01','afafc038-b9d1-4b6d-b275-c17eb11ebd8a',1,'11ab0e6a-1f00-4460-a46c-d54b41a1851d','FM_LIST_APPROVAL_GROUPS','FM_LIST_APPROVAL_GROUPS',10,'SELECT_LIST','ONCE','SELECT g.APPROVAL_GROUP_ID AS value, CONCAT(g.GROUP_CODE,\'／\',g.GROUP_NAME) AS label,\n       \'N\' AS disabled, g.GROUP_CODE AS groupCode, g.GROUP_NAME AS groupName,\n       g.ASSIGNMENT_MODE AS assignmentMode\n  FROM fm_approval_group g\n WHERE g.TENANT_ID=:tenantId AND g.STATUS=\'ACTIVE\'\n   AND (:keyword IS NULL OR :keyword=\'\' OR g.GROUP_CODE LIKE CONCAT(\'%\',:keyword,\'%\') OR g.GROUP_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY g.GROUP_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('894becc6-9133-11f1-9d9b-9d74c7d64429','A01','07923ae7-d0f2-4024-a587-6a50f44dfe36',2,'3ab6e74a-28e2-4546-b0e0-b43eeb6fff7c','FM_LIST_APPROVAL_GROUP_MEMBERS','FM_LIST_APPROVAL_GROUP_MEMBERS',10,'SELECT_LIST','ONCE','SELECT m.APPROVAL_GROUP_MEMBER_ID AS approvalGroupMemberId,\n       m.APPROVAL_GROUP_ID AS approvalGroupId, e.EMPLOYEE_ID AS employeeId,\n       e.EMPLOYEE_NO AS employeeNo, e.DISPLAY_NAME AS displayName,\n       m.PRIORITY AS priority, CONCAT(e.EMPLOYEE_NO,\'／\',e.DISPLAY_NAME) AS label\n  FROM fm_approval_group_member m\n  JOIN fm_approval_group g ON g.TENANT_ID=m.TENANT_ID AND g.APPROVAL_GROUP_ID=m.APPROVAL_GROUP_ID\n  JOIN fm_employee e ON e.TENANT_ID=m.TENANT_ID AND e.EMPLOYEE_ID=m.EMPLOYEE_ID\n WHERE m.TENANT_ID=:tenantId AND m.APPROVAL_GROUP_ID=:approvalGroupId\n   AND g.STATUS=\'ACTIVE\' AND m.STATUS=\'ACTIVE\' AND e.STATUS=\'ACTIVE\'\n   AND (m.EFFECTIVE_FROM IS NULL OR m.EFFECTIVE_FROM<=:now)\n   AND (m.EFFECTIVE_TO IS NULL OR m.EFFECTIVE_TO>:now)\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM<=:now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO>:now)\n ORDER BY m.PRIORITY, e.EMPLOYEE_NO',NULL,'members','LIST',NULL,NULL,30,500,'ACTIVE','admin','2026-08-06 09:09:55.801',NULL,NULL),
('93c951c1-9133-11f1-9d9b-f1b1e1b20a26','A01','afafc038-b9d1-4b6d-b275-c17eb11ebd8a',2,'7a95ef46-697f-4ae2-856f-7adbb0e218f8','FM_LIST_APPROVAL_GROUPS','FM_LIST_APPROVAL_GROUPS',10,'SELECT_LIST','ONCE','SELECT g.APPROVAL_GROUP_ID AS value, CONCAT(g.GROUP_CODE,\'／\',g.GROUP_NAME) AS label,\n       \'N\' AS disabled, g.GROUP_CODE AS groupCode, g.GROUP_NAME AS groupName,\n       g.ASSIGNMENT_MODE AS assignmentMode\n  FROM fm_approval_group g\n WHERE g.TENANT_ID=:tenantId AND g.STATUS=\'ACTIVE\'\n   AND (:keyword IS NULL OR :keyword=\'\' OR g.GROUP_CODE LIKE CONCAT(\'%\',:keyword,\'%\') OR g.GROUP_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY g.GROUP_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','admin','2026-08-06 09:10:13.400',NULL,NULL),
('97f8557b-9133-11f1-9d9b-4181f7f3cc02','A01','db5d5935-0ca4-4931-9a31-cff606a8b5c7',2,'dff764fe-d99c-487e-a36f-31d1354da51a','FM_LIST_EMPLOYEES_BY_ORG','FM_LIST_EMPLOYEES_BY_ORG',10,'SELECT_LIST','ONCE','SELECT e.EMPLOYEE_ID AS employeeId, e.EMPLOYEE_NO AS employeeNo,\n       e.DISPLAY_NAME AS displayName, a.EMPLOYEE_ORG_ASSIGNMENT_ID AS assignmentId,\n       a.IS_PRIMARY AS isPrimary, t.TITLE_NAME AS titleName,\n       CONCAT(e.EMPLOYEE_NO,\'／\',e.DISPLAY_NAME) AS label\n  FROM fm_employee_org_assignment a\n  JOIN fm_employee e ON e.TENANT_ID=a.TENANT_ID AND e.EMPLOYEE_ID=a.EMPLOYEE_ID\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE a.TENANT_ID=:tenantId AND a.ORG_UNIT_ID=:orgUnitId\n   AND a.STATUS=\'ACTIVE\' AND e.STATUS=\'ACTIVE\'\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM<=:now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO>:now)\n ORDER BY CASE WHEN a.IS_PRIMARY=\'Y\' THEN 0 ELSE 1 END, e.EMPLOYEE_NO',NULL,'employees','LIST',NULL,NULL,30,500,'ACTIVE','admin','2026-08-06 09:10:20.419',NULL,NULL),
('981aabaa-1def-4f3f-9cc8-3c7c2d3cc37e','A01','bfc4a332-3f94-4056-85bf-d4461893986a',1,'3d9cad08-ba04-4faa-8507-18b0b40dfb66','FM_GET_CURRENT_EMPLOYEE','FM_GET_CURRENT_EMPLOYEE',10,'SELECT_ONE','ONCE','SELECT e.EMPLOYEE_ID AS employeeId, e.EMPLOYEE_NO AS employeeNo,\n       e.ACCOUNT AS account, e.DISPLAY_NAME AS displayName,\n       e.EMAIL AS email, e.LOCALE AS locale, e.TIMEZONE AS timezone\n  FROM fm_employee e\n WHERE e.TENANT_ID = :tenantId AND e.ACCOUNT = :loginAccount\n   AND e.STATUS = \'ACTIVE\'\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM <= :now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO > :now)',NULL,'employee','OBJECT',NULL,NULL,30,1,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('9f3c6165-9133-11f1-9d9b-8f398451f584','A01','81a1db37-e29e-433c-a6df-49df2f9347cc',2,'994a02dd-00bf-49ce-ac57-ed098fdff55f','FM_LIST_MY_ASSIGNMENTS','FM_LIST_MY_ASSIGNMENTS',10,'SELECT_LIST','ONCE','SELECT a.EMPLOYEE_ORG_ASSIGNMENT_ID AS assignmentId, a.ORG_UNIT_ID AS orgUnitId,\n       u.UNIT_CODE AS orgUnitCode, v.UNIT_NAME AS orgUnitName,\n       a.TITLE_ID AS titleId, t.TITLE_CODE AS titleCode,\n       t.TITLE_NAME AS titleName, a.IS_PRIMARY AS isPrimary,\n       a.MANAGER_SOURCE AS managerSource\n  FROM fm_employee e\n  JOIN fm_employee_org_assignment a ON a.TENANT_ID=e.TENANT_ID AND a.EMPLOYEE_ID=e.EMPLOYEE_ID\n  JOIN fm_org_unit u ON u.TENANT_ID=a.TENANT_ID AND u.ORG_UNIT_ID=a.ORG_UNIT_ID\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE e.TENANT_ID=:tenantId AND e.ACCOUNT=:loginAccount\n   AND e.STATUS=\'ACTIVE\' AND a.STATUS=\'ACTIVE\' AND v.STATUS=\'ACTIVE\'\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM<=:now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO>:now)\n ORDER BY CASE WHEN a.IS_PRIMARY=\'Y\' THEN 0 ELSE 1 END, u.UNIT_CODE',NULL,'assignments','LIST',NULL,NULL,30,100,'ACTIVE','admin','2026-08-06 09:10:32.609',NULL,NULL),
('a327302f-9133-11f1-9d9b-1395e232761f','A01','1926df27-9e01-4b7b-8730-431183fc15e2',2,'4f9c5d78-37d6-494c-921e-dd02854dda85','FM_LIST_ORG_CHILDREN','FM_LIST_ORG_CHILDREN',10,'SELECT_LIST','ONCE','SELECT u.ORG_UNIT_ID AS value, CONCAT(u.UNIT_CODE,\'／\',v.UNIT_NAME) AS label,\n       \'N\' AS disabled, u.UNIT_CODE AS unitCode, v.UNIT_NAME AS unitName,\n       v.PARENT_ORG_UNIT_ID AS parentOrgUnitId, v.UNIT_TYPE AS unitType,\n       v.TREE_DEPTH AS treeDepth\n  FROM fm_org_unit u\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n WHERE u.TENANT_ID=:tenantId AND v.PARENT_ORG_UNIT_ID=:parentOrgUnitId\n   AND v.STATUS=\'ACTIVE\'\n   AND (v.EFFECTIVE_FROM IS NULL OR v.EFFECTIVE_FROM<=:now)\n   AND (v.EFFECTIVE_TO IS NULL OR v.EFFECTIVE_TO>:now)\n ORDER BY v.SORT_NO, u.UNIT_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','admin','2026-08-06 09:10:39.182',NULL,NULL),
('a72efcd9-9133-11f1-9d9b-b767820515f9','A01','1cd2168e-62b3-47c6-92e1-53fbf38700ac',2,'545c002f-6327-4234-9b82-674a4912419a','FM_LIST_ORG_DUTIES','FM_LIST_ORG_DUTIES',10,'SELECT_LIST','ONCE','SELECT d.DUTY_ID AS value, CONCAT(d.DUTY_CODE,\'／\',d.DUTY_NAME) AS label,\n       \'N\' AS disabled, d.ORG_UNIT_ID AS orgUnitId, d.DUTY_CODE AS dutyCode,\n       d.DUTY_NAME AS dutyName, d.DUTY_TYPE AS dutyType\n  FROM fm_org_duty d\n WHERE d.TENANT_ID=:tenantId AND d.ORG_UNIT_ID=:orgUnitId\n   AND d.STATUS=\'ACTIVE\'\n   AND (d.EFFECTIVE_FROM IS NULL OR d.EFFECTIVE_FROM<=:now)\n   AND (d.EFFECTIVE_TO IS NULL OR d.EFFECTIVE_TO>:now)\n ORDER BY d.DUTY_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','admin','2026-08-06 09:10:45.944',NULL,NULL),
('ab8bc753-9133-11f1-9d9b-413dbc867020','A01','7db42add-deb0-4c11-8766-91d097405d40',2,'4f4d5b33-37ec-4ba3-a8cd-a85cdb5349a3','FM_LIST_ORG_TITLES','FM_LIST_ORG_TITLES',10,'SELECT_LIST','ONCE','SELECT t.TITLE_ID AS value, CONCAT(t.TITLE_CODE,\'／\',t.TITLE_NAME) AS label,\n       \'N\' AS disabled, t.TITLE_CODE AS titleCode, t.TITLE_NAME AS titleName,\n       t.APPROVAL_LEVEL_ID AS approvalLevelId, t.IS_MANAGER_TITLE AS isManagerTitle\n  FROM fm_org_title t\n WHERE t.TENANT_ID=:tenantId AND t.STATUS=\'ACTIVE\'\n   AND (t.EFFECTIVE_FROM IS NULL OR t.EFFECTIVE_FROM<=:now)\n   AND (t.EFFECTIVE_TO IS NULL OR t.EFFECTIVE_TO>:now)\n   AND (:keyword IS NULL OR :keyword=\'\' OR t.TITLE_CODE LIKE CONCAT(\'%\',:keyword,\'%\') OR t.TITLE_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY t.SORT_NO, t.TITLE_CODE',NULL,'options','LIST',NULL,NULL,30,500,'ACTIVE','admin','2026-08-06 09:10:53.262',NULL,NULL),
('b49020de-9133-11f1-9d9b-4f6835bf7cb7','A01','a5e883e1-3c1e-457d-ae02-71c33dd9cd02',2,'2dcc8dd4-5e9e-4673-be88-d38e7533489e','FM_LIST_ORG_UNITS','FM_LIST_ORG_UNITS',10,'SELECT_LIST','ONCE','SELECT u.ORG_UNIT_ID AS value, CONCAT(u.UNIT_CODE,\'／\',v.UNIT_NAME) AS label,\n       \'N\' AS disabled, u.UNIT_CODE AS unitCode, v.UNIT_NAME AS unitName,\n       v.PARENT_ORG_UNIT_ID AS parentOrgUnitId, v.UNIT_TYPE AS unitType,\n       v.TREE_DEPTH AS treeDepth, v.PATH AS path\n  FROM fm_org_unit u\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n WHERE u.TENANT_ID=:tenantId AND v.STATUS=\'ACTIVE\'\n   AND (v.EFFECTIVE_FROM IS NULL OR v.EFFECTIVE_FROM<=:now)\n   AND (v.EFFECTIVE_TO IS NULL OR v.EFFECTIVE_TO>:now)\n   AND (:keyword IS NULL OR :keyword=\'\' OR u.UNIT_CODE LIKE CONCAT(\'%\',:keyword,\'%\') OR v.UNIT_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY v.PATH, v.SORT_NO, u.UNIT_CODE',NULL,'options','LIST',NULL,NULL,30,1000,'ACTIVE','admin','2026-08-06 09:11:08.391',NULL,NULL),
('bc5df639-9133-11f1-9d9b-31bbf486e05e','A01','eab2f1f2-f65c-429c-bfcf-ecada28d2697',2,'b718551c-414a-4288-bb44-9d12efe81091','FM_SEARCH_ACTIVE_EMPLOYEES','FM_SEARCH_ACTIVE_EMPLOYEES',10,'SELECT_LIST','ONCE','SELECT e.EMPLOYEE_ID AS employeeId, e.EMPLOYEE_NO AS employeeNo,\n       e.DISPLAY_NAME AS displayName, a.ORG_UNIT_ID AS orgUnitId,\n       u.UNIT_CODE AS orgUnitCode, v.UNIT_NAME AS orgUnitName,\n       t.TITLE_NAME AS titleName, CONCAT(e.EMPLOYEE_NO,\'／\',e.DISPLAY_NAME) AS label\n  FROM fm_employee e\n  LEFT JOIN fm_employee_org_assignment a ON a.TENANT_ID=e.TENANT_ID AND a.EMPLOYEE_ID=e.EMPLOYEE_ID AND a.IS_PRIMARY=\'Y\' AND a.STATUS=\'ACTIVE\'\n  LEFT JOIN fm_org_unit u ON u.TENANT_ID=a.TENANT_ID AND u.ORG_UNIT_ID=a.ORG_UNIT_ID\n  LEFT JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE e.TENANT_ID=:tenantId AND e.STATUS=\'ACTIVE\'\n   AND (e.EFFECTIVE_FROM IS NULL OR e.EFFECTIVE_FROM<=:now)\n   AND (e.EFFECTIVE_TO IS NULL OR e.EFFECTIVE_TO>:now)\n   AND (:keyword IS NULL OR :keyword=\'\' OR e.EMPLOYEE_NO LIKE CONCAT(\'%\',:keyword,\'%\') OR e.DISPLAY_NAME LIKE CONCAT(\'%\',:keyword,\'%\'))\n ORDER BY e.EMPLOYEE_NO',NULL,'employees','LIST',NULL,NULL,30,100,'ACTIVE','admin','2026-08-06 09:11:21.484',NULL,NULL),
('da438b1f-5cd0-47a4-b59e-8fc176b5c93f','A01','75492614-9220-48d2-a79b-5ee4f34ff1d0',1,'b9f2f435-75df-49cf-b458-9fe70b5337ee','FM_GET_MY_PRIMARY_ASSIGNMENT','FM_GET_MY_PRIMARY_ASSIGNMENT',10,'SELECT_ONE','ONCE','SELECT a.EMPLOYEE_ORG_ASSIGNMENT_ID AS assignmentId, a.EMPLOYEE_ID AS employeeId,\n       a.ORG_UNIT_ID AS orgUnitId, u.UNIT_CODE AS orgUnitCode,\n       v.UNIT_NAME AS orgUnitName, a.TITLE_ID AS titleId,\n       t.TITLE_CODE AS titleCode, t.TITLE_NAME AS titleName,\n       a.MANAGER_SOURCE AS managerSource\n  FROM fm_employee e\n  JOIN fm_employee_org_assignment a ON a.TENANT_ID=e.TENANT_ID AND a.EMPLOYEE_ID=e.EMPLOYEE_ID\n  JOIN fm_org_unit u ON u.TENANT_ID=a.TENANT_ID AND u.ORG_UNIT_ID=a.ORG_UNIT_ID\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE e.TENANT_ID=:tenantId AND e.ACCOUNT=:loginAccount\n   AND e.STATUS=\'ACTIVE\' AND a.STATUS=\'ACTIVE\' AND a.IS_PRIMARY=\'Y\' AND v.STATUS=\'ACTIVE\'\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM<=:now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO>:now)',NULL,'assignment','OBJECT',NULL,NULL,30,1,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL),
('feebcec5-ce8b-45cb-a62f-6f4fa6678f63','A01','81a1db37-e29e-433c-a6df-49df2f9347cc',1,'2a0db4f3-b5cd-409f-b669-729098c56f88','FM_LIST_MY_ASSIGNMENTS','FM_LIST_MY_ASSIGNMENTS',10,'SELECT_LIST','ONCE','SELECT a.EMPLOYEE_ORG_ASSIGNMENT_ID AS assignmentId, a.ORG_UNIT_ID AS orgUnitId,\n       u.UNIT_CODE AS orgUnitCode, v.UNIT_NAME AS orgUnitName,\n       a.TITLE_ID AS titleId, t.TITLE_CODE AS titleCode,\n       t.TITLE_NAME AS titleName, a.IS_PRIMARY AS isPrimary,\n       a.MANAGER_SOURCE AS managerSource\n  FROM fm_employee e\n  JOIN fm_employee_org_assignment a ON a.TENANT_ID=e.TENANT_ID AND a.EMPLOYEE_ID=e.EMPLOYEE_ID\n  JOIN fm_org_unit u ON u.TENANT_ID=a.TENANT_ID AND u.ORG_UNIT_ID=a.ORG_UNIT_ID\n  JOIN fm_org_unit_version v ON v.TENANT_ID=u.TENANT_ID AND v.ORG_UNIT_ID=u.ORG_UNIT_ID AND v.VERSION_NO=u.CURRENT_VERSION_NO\n  LEFT JOIN fm_org_title t ON t.TENANT_ID=a.TENANT_ID AND t.TITLE_ID=a.TITLE_ID\n WHERE e.TENANT_ID=:tenantId AND e.ACCOUNT=:loginAccount\n   AND e.STATUS=\'ACTIVE\' AND a.STATUS=\'ACTIVE\' AND v.STATUS=\'ACTIVE\'\n   AND (a.EFFECTIVE_FROM IS NULL OR a.EFFECTIVE_FROM<=:now)\n   AND (a.EFFECTIVE_TO IS NULL OR a.EFFECTIVE_TO>:now)\n ORDER BY CASE WHEN a.IS_PRIMARY=\'Y\' THEN 0 ELSE 1 END, u.UNIT_CODE',NULL,'assignments','LIST',NULL,NULL,30,100,'ACTIVE','SYSTEM','2026-08-05 21:40:37.556',NULL,NULL);
/*!40000 ALTER TABLE `fm_data_action_step` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_data_action_version`
--

DROP TABLE IF EXISTS `fm_data_action_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_data_action_version` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(50) NOT NULL,
  `ACTION_ID` varchar(50) NOT NULL,
  `VERSION_NO` int(11) NOT NULL,
  `VERSION_STATUS` varchar(20) NOT NULL,
  `CONTENT_SHA256` char(64) DEFAULT NULL,
  `PUBLISHED_BY` varchar(24) DEFAULT NULL,
  `PUBLISHED_DATE` datetime(3) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_DATA_ACTION_VERSION` (`TENANT_ID`,`ACTION_ID`,`VERSION_NO`),
  KEY `IX_FM_DATA_ACTION_VERSION_STATUS` (`TENANT_ID`,`ACTION_ID`,`VERSION_STATUS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_data_action_version`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_data_action_version` WRITE;
/*!40000 ALTER TABLE `fm_data_action_version` DISABLE KEYS */;
INSERT INTO `fm_data_action_version` VALUES
('0e758be6-83a6-4f9e-83f6-615db31ed0ea','A01','75492614-9220-48d2-a79b-5ee4f34ff1d0',1,'PUBLISHED','24a2b5481434fb3eb476037c753172ab75c338a73118947ddde58e101585672e','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('141aa31e-4711-4fb7-80f1-af2c9bab2917','A01','68e850e3-ab84-464a-9b58-1808d38219b9',1,'PUBLISHED','e12cf76f1c8b542e281634507494b7687d39fda4ecd07eae9e8cbf4d217ef70c','SYSTEM','2026-08-12 21:34:55.288','SYSTEM','2026-08-12 21:34:55.288','SYSTEM','2026-08-12 22:22:19.834'),
('2454713e-6997-49bb-9d0e-679c16d0ab0f','A01','07923ae7-d0f2-4024-a587-6a50f44dfe36',1,'PUBLISHED','c83b46a8f09278f4ec9f69e1882ba8ab6c3f27f463f6e63de74b3a5f6e8adeb0','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('2c64cb7c-c69e-479a-af06-621a7defc854','A01','afafc038-b9d1-4b6d-b275-c17eb11ebd8a',1,'PUBLISHED','34d6840400ddbc264124912926fae064c49761e3d1286e2f20dfc0b78085a2ac','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('32982087-928a-45b9-af85-05dc577308eb','A01','a5e883e1-3c1e-457d-ae02-71c33dd9cd02',1,'PUBLISHED','077091e89e696585c70474a535c64f3d79a0b4d5839513fb4873f368f33dfd9d','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('3b01cf5f-9133-11f1-9d9b-9167e765a5e9','A01','bfc4a332-3f94-4056-85bf-d4461893986a',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:07:44.453',NULL,NULL),
('5ae6ad0c-abac-4694-91da-a7fb1affecab','A01','fe970af8-94c5-403e-b08a-748fac30d9df',1,'PUBLISHED','5fa9e26a19e8ef02c2cdc62cf493e7c026c8305a6a9b3da6776e1222dd5afcab','SYSTEM','2026-08-12 21:24:01.455','SYSTEM','2026-08-12 21:24:01.455','SYSTEM','2026-08-12 22:22:19.826'),
('668cf74a-9133-11f1-9d9b-0107ca007dbe','A01','75492614-9220-48d2-a79b-5ee4f34ff1d0',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:08:57.508',NULL,NULL),
('6b29ddb7-198d-42af-85d4-17d73d7da473','A01','58a58b4c-0151-42f7-8842-84ad7dd7ef4a',1,'PUBLISHED','02b32412df177407bfd86f89775d60ec54fbfa351bb0ab257b8d8b0e60d2403c','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('6bc9e0d8-5fda-44ab-a119-59661efe776a','A01','7db42add-deb0-4c11-8766-91d097405d40',1,'PUBLISHED','853c747a7baac25b8ebfc86b1a7068551577b192675a3f073cf132f8bd64b487','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('7229cf27-db2e-4725-942e-3bf06c96227a','A01','1926df27-9e01-4b7b-8730-431183fc15e2',1,'PUBLISHED','0d316d48ee7e1b6242183c4598e5c2bb657107d845354e2671b61059214fcb1b','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('797b4f65-9133-11f1-9d9b-ffb603570a15','A01','58a58b4c-0151-42f7-8842-84ad7dd7ef4a',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:09:29.268',NULL,NULL),
('894b0265-9133-11f1-9d9b-8f96eca27692','A01','07923ae7-d0f2-4024-a587-6a50f44dfe36',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:09:55.796',NULL,NULL),
('93c88e70-9133-11f1-9d9b-1d10896c3276','A01','afafc038-b9d1-4b6d-b275-c17eb11ebd8a',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:10:13.395',NULL,NULL),
('97f7b93a-9133-11f1-9d9b-9f202b537d14','A01','db5d5935-0ca4-4931-9a31-cff606a8b5c7',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:10:20.415',NULL,NULL),
('9f3b7704-9133-11f1-9d9b-6dc0fa095bd4','A01','81a1db37-e29e-433c-a6df-49df2f9347cc',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:10:32.603',NULL,NULL),
('a3266cde-9133-11f1-9d9b-8b2240183060','A01','1926df27-9e01-4b7b-8730-431183fc15e2',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:10:39.177',NULL,NULL),
('a72e3988-9133-11f1-9d9b-f1b527e7dc93','A01','1cd2168e-62b3-47c6-92e1-53fbf38700ac',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:10:45.940',NULL,NULL),
('ab8b0402-9133-11f1-9d9b-a5e9ca612caa','A01','7db42add-deb0-4c11-8766-91d097405d40',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:10:53.257',NULL,NULL),
('b48f849d-9133-11f1-9d9b-6142ae25634b','A01','a5e883e1-3c1e-457d-ae02-71c33dd9cd02',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:11:08.386',NULL,NULL),
('b7e5fd4e-7a7e-42a5-8d0d-203e44e808bb','A01','81a1db37-e29e-433c-a6df-49df2f9347cc',1,'PUBLISHED','62a48216325c3eca43f528cd81aa44b459b2c08ad437a0b4ca83794fa9c64429','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('bc5d59f8-9133-11f1-9d9b-2727be153590','A01','eab2f1f2-f65c-429c-bfcf-ecada28d2697',2,'DRAFT',NULL,NULL,NULL,'admin','2026-08-06 09:11:21.479',NULL,NULL),
('bc987d6d-0227-4c87-bf99-c152d8b2c2d9','A01','bfc4a332-3f94-4056-85bf-d4461893986a',1,'PUBLISHED','cad2cd2d71d65fec214d661ba5702248bf6d9a2071417d878a2e57934cd5dcbb','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('c826bc9b-aa75-4549-a703-08db42df7e6e','A01','eab2f1f2-f65c-429c-bfcf-ecada28d2697',1,'PUBLISHED','be676cd6a0adfaa29638a46fae64dfe28776c19deb3e0e8ffc774d41d708f574','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('cb622802-5339-4368-a652-108900f46456','A01','db5d5935-0ca4-4931-9a31-cff606a8b5c7',1,'PUBLISHED','e168668356b229d86721a4ad0e9bf3b97eaa1bbd06d457c52d1362075aad5f4a','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL),
('e935196d-1b36-4096-9306-6ec2a2451e2f','A01','1cd2168e-62b3-47c6-92e1-53fbf38700ac',1,'PUBLISHED','0b7a038f3b74663087436aca06242aebc25a7e583feff463974c06c990580726','SYSTEM','2026-08-05 21:40:37.553','SYSTEM','2026-08-05 21:40:37.553',NULL,NULL);
/*!40000 ALTER TABLE `fm_data_action_version` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_data_source_pool`
--

DROP TABLE IF EXISTS `fm_data_source_pool`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_data_source_pool` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `POOL_ID` varchar(36) NOT NULL,
  `POOL_CODE` varchar(50) NOT NULL,
  `POOL_NAME` varchar(100) NOT NULL,
  `DB_TYPE` varchar(20) NOT NULL,
  `DRIVER_CLASS` varchar(200) NOT NULL,
  `JDBC_URL` varchar(1000) NOT NULL,
  `USERNAME` varchar(200) NOT NULL,
  `PASSWORD_CONTENT` longtext NOT NULL,
  `MAXIMUM_POOL_SIZE` int(11) NOT NULL DEFAULT 10,
  `MINIMUM_IDLE` int(11) NOT NULL DEFAULT 1,
  `CONNECTION_TIMEOUT_MS` bigint(20) NOT NULL DEFAULT 10000,
  `IDLE_TIMEOUT_MS` bigint(20) NOT NULL DEFAULT 600000,
  `MAX_LIFETIME_MS` bigint(20) NOT NULL DEFAULT 1800000,
  `VALIDATION_QUERY` varchar(500) DEFAULT NULL,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `LOCK_VERSION` int(11) NOT NULL DEFAULT 0,
  `DESCRIPTION` varchar(500) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_DS_POOL_ID` (`TENANT_ID`,`POOL_ID`),
  UNIQUE KEY `UK_FM_DS_POOL_CODE` (`TENANT_ID`,`POOL_CODE`),
  KEY `IDX_FM_DS_POOL_STATUS` (`TENANT_ID`,`STATUS`,`DB_TYPE`),
  CONSTRAINT `CK_FM_DS_POOL_DB_TYPE` CHECK (`DB_TYPE` in ('MARIADB','ORACLE','MSSQL')),
  CONSTRAINT `CK_FM_DS_POOL_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_DS_POOL_LIMIT` CHECK (`MAXIMUM_POOL_SIZE` between 1 and 100 and `MINIMUM_IDLE` between 0 and `MAXIMUM_POOL_SIZE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_data_source_pool`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_data_source_pool` WRITE;
/*!40000 ALTER TABLE `fm_data_source_pool` DISABLE KEYS */;
INSERT INTO `fm_data_source_pool` VALUES
('f57f025b-8fff-11f1-a75b-2d3d040a70b6','A01','7f177f66-864e-46cb-8b7e-48081be7d859','FLOWMINT','本系統','MARIADB','org.mariadb.jdbc.Driver','jdbc:mariadb://127.0.0.1:3306/flowmint','root','v1:0Hmwe0cWJ06UWFDa:6gnZqZQ4uK2fuKrfmvMc8PV4DH8AFI7B',5,1,10000,600000,1800000,'SELECT 1','ACTIVE',0,'flowmint系統資料來源','admin','2026-08-04 20:28:12.338',NULL,NULL);
/*!40000 ALTER TABLE `fm_data_source_pool` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_document_number_rule`
--

DROP TABLE IF EXISTS `fm_document_number_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_document_number_rule` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `DOCUMENT_TYPE` varchar(50) NOT NULL,
  `PREFIX` varchar(20) NOT NULL,
  `PERIOD_TYPE` varchar(10) NOT NULL,
  `SEQUENCE_LENGTH` smallint(6) NOT NULL,
  `FORMAT_PATTERN` varchar(200) NOT NULL,
  `STATUS` varchar(16) NOT NULL DEFAULT 'ACTIVE',
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_DOC_NO_RULE` (`TENANT_ID`,`DOCUMENT_TYPE`),
  CONSTRAINT `CK_FM_DOC_NO_RULE_PERIOD` CHECK (`PERIOD_TYPE` in ('NONE','YEAR','MONTH')),
  CONSTRAINT `CK_FM_DOC_NO_RULE_LENGTH` CHECK (`SEQUENCE_LENGTH` between 4 and 12),
  CONSTRAINT `CK_FM_DOC_NO_RULE_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_document_number_rule`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_document_number_rule` WRITE;
/*!40000 ALTER TABLE `fm_document_number_rule` DISABLE KEYS */;
INSERT INTO `fm_document_number_rule` VALUES
('ca888676-98a7-11f1-a730-005056c00001','A01','PURCHASE_REQUEST','PR','MONTH',6,'{PREFIX}-{TENANT}-{YYYY}{MM}-{SEQ}','ACTIVE','SYSTEM','2026-08-15 20:49:45.009',NULL,NULL);
/*!40000 ALTER TABLE `fm_document_number_rule` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_document_sequence`
--

DROP TABLE IF EXISTS `fm_document_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_document_sequence` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `DOCUMENT_TYPE` varchar(50) NOT NULL,
  `PERIOD_KEY` varchar(12) NOT NULL,
  `CURRENT_NO` bigint(20) NOT NULL DEFAULT 0,
  `LOCK_VERSION` bigint(20) NOT NULL DEFAULT 0,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_DOC_SEQUENCE` (`TENANT_ID`,`DOCUMENT_TYPE`,`PERIOD_KEY`),
  CONSTRAINT `CK_FM_DOC_SEQUENCE_CURRENT` CHECK (`CURRENT_NO` >= 0),
  CONSTRAINT `CK_FM_DOC_SEQUENCE_LOCK` CHECK (`LOCK_VERSION` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_document_sequence`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_document_sequence` WRITE;
/*!40000 ALTER TABLE `fm_document_sequence` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_document_sequence` ENABLE KEYS */;
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
('2250e92c-8cab-11f1-bf0e-4b61e379b27b','A01','a3bb53cb-fbb1-4c9d-870b-9d2e39d17cba','A0004','tester','林冠宇','aaa@aaa.org','0800888222','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 14:43:26.823','admin','2026-08-09 09:15:28.000'),
('2d1ffa86-938f-11f1-a8f5-005056c00001','A01','2d1ffa8d-938f-11f1-a8f5-005056c00001','FM00530','fm00530','彭郁婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d1ffc75-938f-11f1-a8f5-005056c00001','A01','2d1ffc77-938f-11f1-a8f5-005056c00001','FM00110','fm00110','黃俊傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d1ffd08-938f-11f1-a8f5-005056c00001','A01','2d1ffd09-938f-11f1-a8f5-005056c00001','FM00430','fm00430','蘇柏宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d1ffd89-938f-11f1-a8f5-005056c00001','A01','2d1ffd8a-938f-11f1-a8f5-005056c00001','FM00120','fm00120','張雅雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d1ffe1f-938f-11f1-a8f5-005056c00001','A01','2d1ffe20-938f-11f1-a8f5-005056c00001','FM00600','fm00600','許家豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d1ffea2-938f-11f1-a8f5-005056c00001','A01','2d1ffea3-938f-11f1-a8f5-005056c00001','FM00800','fm00800','郭志強',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d1fff2c-938f-11f1-a8f5-005056c00001','A01','2d1fff2d-938f-11f1-a8f5-005056c00001','FM00300','fm00300','李文凱',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d1fffaa-938f-11f1-a8f5-005056c00001','A01','2d1fffab-938f-11f1-a8f5-005056c00001','FM00610','fm00610','謝佩珊',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200037-938f-11f1-a8f5-005056c00001','A01','2d200038-938f-11f1-a8f5-005056c00001','FM00310','fm00310','徐子晴',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d2000ba-938f-11f1-a8f5-005056c00001','A01','2d2000bb-938f-11f1-a8f5-005056c00001','FM00810','fm00810','沈佳蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200181-938f-11f1-a8f5-005056c00001','A01','2d200182-938f-11f1-a8f5-005056c00001','FM00320','fm00320','何承翰',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200203-938f-11f1-a8f5-005056c00001','A01','2d200205-938f-11f1-a8f5-005056c00001','FM00200','fm00200','王世昌',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d2002a0-938f-11f1-a8f5-005056c00001','A01','2d2002a1-938f-11f1-a8f5-005056c00001','FM00620','fm00620','曾冠霖',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200323-938f-11f1-a8f5-005056c00001','A01','2d200324-938f-11f1-a8f5-005056c00001','FM00820','fm00820','羅偉倫',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d2003e5-938f-11f1-a8f5-005056c00001','A01','2d2003e6-938f-11f1-a8f5-005056c00001','FM00210','fm00210','李佳穎',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d20046a-938f-11f1-a8f5-005056c00001','A01','2d20046b-938f-11f1-a8f5-005056c00001','FM00330','fm00330','高宇辰',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d2004fc-938f-11f1-a8f5-005056c00001','A01','2d2004fd-938f-11f1-a8f5-005056c00001','FM00830','fm00830','江柏毅',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200581-938f-11f1-a8f5-005056c00001','A01','2d200582-938f-11f1-a8f5-005056c00001','FM00630','fm00630','邱昱翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200609-938f-11f1-a8f5-005056c00001','A01','2d20060a-938f-11f1-a8f5-005056c00001','FM00500','fm00500','蔡明哲',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200691-938f-11f1-a8f5-005056c00001','A01','2d200692-938f-11f1-a8f5-005056c00001','FM00220','fm00220','周柏翰',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200770-938f-11f1-a8f5-005056c00001','A01','2d200771-938f-11f1-a8f5-005056c00001','FM00900','fm00900','謝承恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d2007fe-938f-11f1-a8f5-005056c00001','A01','2d2007ff-938f-11f1-a8f5-005056c00001','FM00230','fm00230','吳怡君',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200887-938f-11f1-a8f5-005056c00001','A01','2d200888-938f-11f1-a8f5-005056c00001','FM00400','fm00400','吳宗翰',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d2009bb-938f-11f1-a8f5-005056c00001','A01','2d2009bc-938f-11f1-a8f5-005056c00001','FM00510','fm00510','鄭雅琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200a58-938f-11f1-a8f5-005056c00001','A01','2d200a5a-938f-11f1-a8f5-005056c00001','FM00910','fm00910','葉家銘',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200ad3-938f-11f1-a8f5-005056c00001','A01','2d200ad4-938f-11f1-a8f5-005056c00001','FM00700','fm00700','鄭淑芬',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200b50-938f-11f1-a8f5-005056c00001','A01','2d200b51-938f-11f1-a8f5-005056c00001','FM00410','fm00410','劉俊廷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200bf1-938f-11f1-a8f5-005056c00001','A01','2d200bf2-938f-11f1-a8f5-005056c00001','FM00520','fm00520','許庭瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200c86-938f-11f1-a8f5-005056c00001','A01','2d200c87-938f-11f1-a8f5-005056c00001','FM00710','fm00710','洪嘉宏',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200d23-938f-11f1-a8f5-005056c00001','A01','2d200d24-938f-11f1-a8f5-005056c00001','FM00920','fm00920','鍾欣芸',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200db7-938f-11f1-a8f5-005056c00001','A01','2d200db8-938f-11f1-a8f5-005056c00001','FM00100','fm00100','林志遠',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200e46-938f-11f1-a8f5-005056c00001','A01','2d200e47-938f-11f1-a8f5-005056c00001','FM00420','fm00420','楊欣怡',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('2d200ecc-938f-11f1-a8f5-005056c00001','A01','2d200ecd-938f-11f1-a8f5-005056c00001','FM00720','fm00720','賴怡伶',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000','admin','2026-08-09 09:15:28.000'),
('925cf380-8caa-11f1-bf0e-3b1efcd0c2a7','A01','b5a80859-2382-44ee-967c-5969ed3457d4','A0001','admin','陳信宏','chen.xin.nien@gmail.com','0800956956','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2199-12-31 23:59:00.000','','admin','2026-07-31 14:39:25.310','admin','2026-08-09 09:15:28.000'),
('ac9590b9-9398-11f1-a8f5-005056c00001','A01','ac9590d9-9398-11f1-a8f5-005056c00001','FM00002E01','fm00002e01','陳志豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95942c-9398-11f1-a8f5-005056c00001','A01','ac959432-9398-11f1-a8f5-005056c00001','FM00100E01','fm00100e01','林志婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9594b2-9398-11f1-a8f5-005056c00001','A01','ac9594b6-9398-11f1-a8f5-005056c00001','FM00110E01','fm00110e01','黃志宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959510-9398-11f1-a8f5-005056c00001','A01','ac959514-9398-11f1-a8f5-005056c00001','FM00120E01','fm00120e01','張志雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95964a-9398-11f1-a8f5-005056c00001','A01','ac95964f-9398-11f1-a8f5-005056c00001','FM00200E01','fm00200e01','李志傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9596a5-9398-11f1-a8f5-005056c00001','A01','ac9596a9-9398-11f1-a8f5-005056c00001','FM00210E01','fm00210e01','王志蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9596fd-9398-11f1-a8f5-005056c00001','A01','ac959701-9398-11f1-a8f5-005056c00001','FM00220E01','fm00220e01','吳志翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959756-9398-11f1-a8f5-005056c00001','A01','ac95975a-9398-11f1-a8f5-005056c00001','FM00230E01','fm00230e01','劉志琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9597ac-9398-11f1-a8f5-005056c00001','A01','ac9597b0-9398-11f1-a8f5-005056c00001','FM00300E01','fm00300e01','蔡志恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959800-9398-11f1-a8f5-005056c00001','A01','ac959804-9398-11f1-a8f5-005056c00001','FM00310E01','fm00310e01','楊志瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959863-9398-11f1-a8f5-005056c00001','A01','ac959867-9398-11f1-a8f5-005056c00001','FM00320E01','fm00320e01','許志豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9598b6-9398-11f1-a8f5-005056c00001','A01','ac9598ba-9398-11f1-a8f5-005056c00001','FM00330E01','fm00330e01','鄭志婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95990a-9398-11f1-a8f5-005056c00001','A01','ac95990e-9398-11f1-a8f5-005056c00001','FM00400E01','fm00400e01','謝志宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95995c-9398-11f1-a8f5-005056c00001','A01','ac959960-9398-11f1-a8f5-005056c00001','FM00400E02','fm00400e02','郭志雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9599b1-9398-11f1-a8f5-005056c00001','A01','ac9599b5-9398-11f1-a8f5-005056c00001','FM00400E03','fm00400e03','洪志傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959a04-9398-11f1-a8f5-005056c00001','A01','ac959a08-9398-11f1-a8f5-005056c00001','FM00400E04','fm00400e04','邱志蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959a57-9398-11f1-a8f5-005056c00001','A01','ac959a5b-9398-11f1-a8f5-005056c00001','FM00400E05','fm00400e05','曾志翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959aa5-9398-11f1-a8f5-005056c00001','A01','ac959aa9-9398-11f1-a8f5-005056c00001','FM00400E06','fm00400e06','廖志琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959af5-9398-11f1-a8f5-005056c00001','A01','ac959af9-9398-11f1-a8f5-005056c00001','FM00400E07','fm00400e07','賴志恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959b44-9398-11f1-a8f5-005056c00001','A01','ac959b48-9398-11f1-a8f5-005056c00001','FM00400E08','fm00400e08','徐志瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959b94-9398-11f1-a8f5-005056c00001','A01','ac959b97-9398-11f1-a8f5-005056c00001','FM00400E09','fm00400e09','陳雅豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959bef-9398-11f1-a8f5-005056c00001','A01','ac959bf3-9398-11f1-a8f5-005056c00001','FM00400E10','fm00400e10','林雅婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959c42-9398-11f1-a8f5-005056c00001','A01','ac959c46-9398-11f1-a8f5-005056c00001','FM00400E11','fm00400e11','黃雅宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959c92-9398-11f1-a8f5-005056c00001','A01','ac959c96-9398-11f1-a8f5-005056c00001','FM00410E01','fm00410e01','張雅玲',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073','admin','2026-08-09 10:22:58.571'),
('ac959ce9-9398-11f1-a8f5-005056c00001','A01','ac959cec-9398-11f1-a8f5-005056c00001','FM00410E02','fm00410e02','李雅傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959d3d-9398-11f1-a8f5-005056c00001','A01','ac959d40-9398-11f1-a8f5-005056c00001','FM00410E03','fm00410e03','王雅蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959d91-9398-11f1-a8f5-005056c00001','A01','ac959d95-9398-11f1-a8f5-005056c00001','FM00410E04','fm00410e04','吳雅翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959de5-9398-11f1-a8f5-005056c00001','A01','ac959de9-9398-11f1-a8f5-005056c00001','FM00410E05','fm00410e05','劉雅琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959e34-9398-11f1-a8f5-005056c00001','A01','ac959e38-9398-11f1-a8f5-005056c00001','FM00410E06','fm00410e06','蔡雅恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959e82-9398-11f1-a8f5-005056c00001','A01','ac959e86-9398-11f1-a8f5-005056c00001','FM00410E07','fm00410e07','楊雅瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959ed0-9398-11f1-a8f5-005056c00001','A01','ac959ed4-9398-11f1-a8f5-005056c00001','FM00410E08','fm00410e08','許雅豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959f1f-9398-11f1-a8f5-005056c00001','A01','ac959f23-9398-11f1-a8f5-005056c00001','FM00410E09','fm00410e09','鄭雅婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959f6d-9398-11f1-a8f5-005056c00001','A01','ac959f71-9398-11f1-a8f5-005056c00001','FM00410E10','fm00410e10','謝雅宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac959fbb-9398-11f1-a8f5-005056c00001','A01','ac959fbf-9398-11f1-a8f5-005056c00001','FM00410E11','fm00410e11','郭雅雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95a021-9398-11f1-a8f5-005056c00001','A01','ac95a025-9398-11f1-a8f5-005056c00001','FM00420E01','fm00420e01','洪雅傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c373-9398-11f1-a8f5-005056c00001','A01','ac95c3a1-9398-11f1-a8f5-005056c00001','FM00420E02','fm00420e02','邱雅蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c5da-9398-11f1-a8f5-005056c00001','A01','ac95c5df-9398-11f1-a8f5-005056c00001','FM00420E03','fm00420e03','曾雅翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c644-9398-11f1-a8f5-005056c00001','A01','ac95c648-9398-11f1-a8f5-005056c00001','FM00420E04','fm00420e04','廖雅琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c6ae-9398-11f1-a8f5-005056c00001','A01','ac95c6b2-9398-11f1-a8f5-005056c00001','FM00420E05','fm00420e05','賴雅恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c70d-9398-11f1-a8f5-005056c00001','A01','ac95c710-9398-11f1-a8f5-005056c00001','FM00420E06','fm00420e06','徐雅瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c765-9398-11f1-a8f5-005056c00001','A01','ac95c769-9398-11f1-a8f5-005056c00001','FM00420E07','fm00420e07','陳承豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c7c4-9398-11f1-a8f5-005056c00001','A01','ac95c7c7-9398-11f1-a8f5-005056c00001','FM00420E08','fm00420e08','林承婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c820-9398-11f1-a8f5-005056c00001','A01','ac95c823-9398-11f1-a8f5-005056c00001','FM00420E09','fm00420e09','黃承宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c876-9398-11f1-a8f5-005056c00001','A01','ac95c87a-9398-11f1-a8f5-005056c00001','FM00420E10','fm00420e10','張承雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c8d0-9398-11f1-a8f5-005056c00001','A01','ac95c8d4-9398-11f1-a8f5-005056c00001','FM00420E11','fm00420e11','李承傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c929-9398-11f1-a8f5-005056c00001','A01','ac95c92d-9398-11f1-a8f5-005056c00001','FM00430E01','fm00430e01','王承蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c990-9398-11f1-a8f5-005056c00001','A01','ac95c994-9398-11f1-a8f5-005056c00001','FM00430E02','fm00430e02','吳承翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95c9ef-9398-11f1-a8f5-005056c00001','A01','ac95c9f3-9398-11f1-a8f5-005056c00001','FM00430E03','fm00430e03','劉承琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95ca4c-9398-11f1-a8f5-005056c00001','A01','ac95ca50-9398-11f1-a8f5-005056c00001','FM00430E04','fm00430e04','蔡承恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95caac-9398-11f1-a8f5-005056c00001','A01','ac95caaf-9398-11f1-a8f5-005056c00001','FM00430E05','fm00430e05','楊承瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cb05-9398-11f1-a8f5-005056c00001','A01','ac95cb09-9398-11f1-a8f5-005056c00001','FM00430E06','fm00430e06','許承豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cb5c-9398-11f1-a8f5-005056c00001','A01','ac95cb60-9398-11f1-a8f5-005056c00001','FM00430E07','fm00430e07','鄭承婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cbb3-9398-11f1-a8f5-005056c00001','A01','ac95cbb7-9398-11f1-a8f5-005056c00001','FM00430E08','fm00430e08','謝承宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cc12-9398-11f1-a8f5-005056c00001','A01','ac95cc16-9398-11f1-a8f5-005056c00001','FM00430E09','fm00430e09','郭承雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cc6b-9398-11f1-a8f5-005056c00001','A01','ac95cc6f-9398-11f1-a8f5-005056c00001','FM00430E10','fm00430e10','洪承傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95ccd0-9398-11f1-a8f5-005056c00001','A01','ac95ccd4-9398-11f1-a8f5-005056c00001','FM00430E11','fm00430e11','邱承蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cd25-9398-11f1-a8f5-005056c00001','A01','ac95cd29-9398-11f1-a8f5-005056c00001','FM00500E01','fm00500e01','曾承翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cd87-9398-11f1-a8f5-005056c00001','A01','ac95cd8a-9398-11f1-a8f5-005056c00001','FM00500E02','fm00500e02','廖承琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cde5-9398-11f1-a8f5-005056c00001','A01','ac95cde8-9398-11f1-a8f5-005056c00001','FM00500E03','fm00500e03','賴承恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95ce3f-9398-11f1-a8f5-005056c00001','A01','ac95ce43-9398-11f1-a8f5-005056c00001','FM00500E04','fm00500e04','徐承瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cef4-9398-11f1-a8f5-005056c00001','A01','ac95cef8-9398-11f1-a8f5-005056c00001','FM00500E05','fm00500e05','陳家豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cf4e-9398-11f1-a8f5-005056c00001','A01','ac95cf52-9398-11f1-a8f5-005056c00001','FM00510E01','fm00510e01','林家婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95cfb7-9398-11f1-a8f5-005056c00001','A01','ac95cfbb-9398-11f1-a8f5-005056c00001','FM00510E02','fm00510e02','黃家宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d016-9398-11f1-a8f5-005056c00001','A01','ac95d01a-9398-11f1-a8f5-005056c00001','FM00510E03','fm00510e03','張家雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d075-9398-11f1-a8f5-005056c00001','A01','ac95d079-9398-11f1-a8f5-005056c00001','FM00510E04','fm00510e04','李家傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d0d9-9398-11f1-a8f5-005056c00001','A01','ac95d0dd-9398-11f1-a8f5-005056c00001','FM00510E05','fm00510e05','王家蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d130-9398-11f1-a8f5-005056c00001','A01','ac95d134-9398-11f1-a8f5-005056c00001','FM00520E01','fm00520e01','吳家翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d198-9398-11f1-a8f5-005056c00001','A01','ac95d19c-9398-11f1-a8f5-005056c00001','FM00520E02','fm00520e02','劉家琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d1ff-9398-11f1-a8f5-005056c00001','A01','ac95d203-9398-11f1-a8f5-005056c00001','FM00520E03','fm00520e03','蔡家恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d25f-9398-11f1-a8f5-005056c00001','A01','ac95d263-9398-11f1-a8f5-005056c00001','FM00520E04','fm00520e04','楊家瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95d2c1-9398-11f1-a8f5-005056c00001','A01','ac95d2c5-9398-11f1-a8f5-005056c00001','FM00520E05','fm00520e05','許家銘',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073','admin','2026-08-09 10:22:58.571'),
('ac95d316-9398-11f1-a8f5-005056c00001','A01','ac95d319-9398-11f1-a8f5-005056c00001','FM00530E01','fm00530e01','鄭家婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95efc3-9398-11f1-a8f5-005056c00001','A01','ac95f898-9398-11f1-a8f5-005056c00001','FM00530E02','fm00530e02','謝家宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac95fa81-9398-11f1-a8f5-005056c00001','A01','ac95fa86-9398-11f1-a8f5-005056c00001','FM00530E03','fm00530e03','郭家雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9614f3-9398-11f1-a8f5-005056c00001','A01','ac96150b-9398-11f1-a8f5-005056c00001','FM00530E04','fm00530e04','洪家傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961615-9398-11f1-a8f5-005056c00001','A01','ac96161a-9398-11f1-a8f5-005056c00001','FM00530E05','fm00530e05','邱家蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961676-9398-11f1-a8f5-005056c00001','A01','ac96167a-9398-11f1-a8f5-005056c00001','FM00600E01','fm00600e01','曾家翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9616d6-9398-11f1-a8f5-005056c00001','A01','ac9616da-9398-11f1-a8f5-005056c00001','FM00600E02','fm00600e02','廖家琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961735-9398-11f1-a8f5-005056c00001','A01','ac961738-9398-11f1-a8f5-005056c00001','FM00600E03','fm00600e03','賴家恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac96178f-9398-11f1-a8f5-005056c00001','A01','ac961793-9398-11f1-a8f5-005056c00001','FM00600E04','fm00600e04','徐家瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9617ee-9398-11f1-a8f5-005056c00001','A01','ac9617f1-9398-11f1-a8f5-005056c00001','FM00610E01','fm00610e01','陳怡豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961866-9398-11f1-a8f5-005056c00001','A01','ac96186a-9398-11f1-a8f5-005056c00001','FM00610E02','fm00610e02','林怡婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9618c4-9398-11f1-a8f5-005056c00001','A01','ac9618c8-9398-11f1-a8f5-005056c00001','FM00610E03','fm00610e03','黃怡宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961923-9398-11f1-a8f5-005056c00001','A01','ac961927-9398-11f1-a8f5-005056c00001','FM00610E04','fm00610e04','張怡雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961983-9398-11f1-a8f5-005056c00001','A01','ac961987-9398-11f1-a8f5-005056c00001','FM00620E01','fm00620e01','李怡傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9619e8-9398-11f1-a8f5-005056c00001','A01','ac9619ec-9398-11f1-a8f5-005056c00001','FM00620E02','fm00620e02','王怡蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961a48-9398-11f1-a8f5-005056c00001','A01','ac961a4d-9398-11f1-a8f5-005056c00001','FM00620E03','fm00620e03','吳怡翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961ab2-9398-11f1-a8f5-005056c00001','A01','ac961ab6-9398-11f1-a8f5-005056c00001','FM00620E04','fm00620e04','劉怡琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961b24-9398-11f1-a8f5-005056c00001','A01','ac961b28-9398-11f1-a8f5-005056c00001','FM00630E01','fm00630e01','蔡怡恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961b80-9398-11f1-a8f5-005056c00001','A01','ac961b84-9398-11f1-a8f5-005056c00001','FM00630E02','fm00630e02','楊怡瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961bdd-9398-11f1-a8f5-005056c00001','A01','ac961be0-9398-11f1-a8f5-005056c00001','FM00630E03','fm00630e03','許怡豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961c3b-9398-11f1-a8f5-005056c00001','A01','ac961c3f-9398-11f1-a8f5-005056c00001','FM00630E04','fm00630e04','鄭怡婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961c9d-9398-11f1-a8f5-005056c00001','A01','ac961ca1-9398-11f1-a8f5-005056c00001','FM00700E01','fm00700e01','謝怡宇',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961cf9-9398-11f1-a8f5-005056c00001','A01','ac961cfd-9398-11f1-a8f5-005056c00001','FM00710E01','fm00710e01','郭怡雯',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961d55-9398-11f1-a8f5-005056c00001','A01','ac961d59-9398-11f1-a8f5-005056c00001','FM00720E01','fm00720e01','洪怡傑',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac961db2-9398-11f1-a8f5-005056c00001','A01','ac961db5-9398-11f1-a8f5-005056c00001','FM00800E01','fm00800e01','邱怡蓉',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac96208f-9398-11f1-a8f5-005056c00001','A01','ac962096-9398-11f1-a8f5-005056c00001','FM00810E01','fm00810e01','曾怡翔',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac962104-9398-11f1-a8f5-005056c00001','A01','ac962107-9398-11f1-a8f5-005056c00001','FM00820E01','fm00820e01','廖怡琪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac96215f-9398-11f1-a8f5-005056c00001','A01','ac962163-9398-11f1-a8f5-005056c00001','FM00830E01','fm00830e01','賴怡恩',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac9621bd-9398-11f1-a8f5-005056c00001','A01','ac9621c1-9398-11f1-a8f5-005056c00001','FM00900E01','fm00900e01','徐怡瑋',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac96221b-9398-11f1-a8f5-005056c00001','A01','ac96221f-9398-11f1-a8f5-005056c00001','FM00910E01','fm00910e01','陳俊豪',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('ac962275-9398-11f1-a8f5-005056c00001','A01','ac962279-9398-11f1-a8f5-005056c00001','FM00920E01','fm00920e01','林俊婷',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','一般員工測試資料','admin','2026-08-09 10:18:41.073',NULL,NULL),
('c657aefb-8caa-11f1-bf0e-e7465f4ccf97','A01','8bf5c5c3-809c-45c4-9a43-9e97696a3845','A0002','tiffany','王雅婷','aaa@aaa.org','0800999444','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 14:40:52.516','admin','2026-08-09 09:15:28.000'),
('f3594a46-8caa-11f1-bf0e-91b42c41b1a5','A01','a770ca51-0137-409f-af3a-af895f22ee50','A0003','steven','張志豪','aaa@aaa.org','0800999111','zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','','admin','2026-07-31 14:42:08.024','admin','2026-08-09 09:15:28.000'),
('f750825e-938e-11f1-a8f5-005056c00001','A01','f750827a-938e-11f1-a8f5-005056c00001','FM00002','fm00002','陳建宏',NULL,NULL,'zh-TW','Asia/Taipei','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:09:11.000','admin','2026-08-09 09:15:28.000');
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
('2d205dcc-938f-11f1-a8f5-005056c00001','A01','2d205dd3-938f-11f1-a8f5-005056c00001','2d1ffa8d-938f-11f1-a8f5-005056c00001','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','3fe52a9d-86a9-42ab-9736-3c6a47e77436','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d205f1a-938f-11f1-a8f5-005056c00001','A01','2d205f1b-938f-11f1-a8f5-005056c00001','2d1ffc77-938f-11f1-a8f5-005056c00001','b7ff38ab-98a8-44d0-8318-85145e9e5b57','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d205ffd-938f-11f1-a8f5-005056c00001','A01','2d205ffe-938f-11f1-a8f5-005056c00001','2d1ffd09-938f-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d2060da-938f-11f1-a8f5-005056c00001','A01','2d2060db-938f-11f1-a8f5-005056c00001','2d1ffd8a-938f-11f1-a8f5-005056c00001','11e2a4e7-30a4-4ced-8454-9753ce4a1b60','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d2061b7-938f-11f1-a8f5-005056c00001','A01','2d2061b8-938f-11f1-a8f5-005056c00001','2d1ffe20-938f-11f1-a8f5-005056c00001','92f87544-d491-4a96-a8d3-425795376136','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20629d-938f-11f1-a8f5-005056c00001','A01','2d20629e-938f-11f1-a8f5-005056c00001','2d1ffea3-938f-11f1-a8f5-005056c00001','2e50d437-f16d-4a63-8a6b-69e6bf564e7e','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206366-938f-11f1-a8f5-005056c00001','A01','2d206367-938f-11f1-a8f5-005056c00001','2d1fff2d-938f-11f1-a8f5-005056c00001','f092bb40-d87f-407b-9b12-2221470489a3','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20644a-938f-11f1-a8f5-005056c00001','A01','2d20644b-938f-11f1-a8f5-005056c00001','2d1fffab-938f-11f1-a8f5-005056c00001','e2043fd6-978c-4341-a1fe-7751fecb9b2b','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206533-938f-11f1-a8f5-005056c00001','A01','2d206534-938f-11f1-a8f5-005056c00001','2d200038-938f-11f1-a8f5-005056c00001','4f4a3de9-3b13-44f5-a783-19506696d7d7','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206654-938f-11f1-a8f5-005056c00001','A01','2d206655-938f-11f1-a8f5-005056c00001','2d2000bb-938f-11f1-a8f5-005056c00001','121bbf1f-9fde-499d-95a6-b4d59645ccb2','86751be1-e9e9-4908-b6f2-e264d105f4e0','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206730-938f-11f1-a8f5-005056c00001','A01','2d206731-938f-11f1-a8f5-005056c00001','2d200182-938f-11f1-a8f5-005056c00001','9c2c3ed6-2c5a-4678-a585-d8d6aef6d2c7','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206815-938f-11f1-a8f5-005056c00001','A01','2d206816-938f-11f1-a8f5-005056c00001','2d200205-938f-11f1-a8f5-005056c00001','c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d2068e1-938f-11f1-a8f5-005056c00001','A01','2d2068e2-938f-11f1-a8f5-005056c00001','2d2002a1-938f-11f1-a8f5-005056c00001','84ada4ab-426a-4b15-a2c0-e31676c5b183','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d2069f0-938f-11f1-a8f5-005056c00001','A01','2d2069f1-938f-11f1-a8f5-005056c00001','2d200324-938f-11f1-a8f5-005056c00001','210f59af-a6a8-4133-adda-c927af8d69c2','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206ac8-938f-11f1-a8f5-005056c00001','A01','2d206ac9-938f-11f1-a8f5-005056c00001','2d2003e6-938f-11f1-a8f5-005056c00001','ada00d6d-df68-4c9b-84d0-28b8ee8c0f58','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206ba3-938f-11f1-a8f5-005056c00001','A01','2d206ba4-938f-11f1-a8f5-005056c00001','2d20046b-938f-11f1-a8f5-005056c00001','c7db44c2-4d03-4eb0-b3be-2d569c3965bb','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206c87-938f-11f1-a8f5-005056c00001','A01','2d206c88-938f-11f1-a8f5-005056c00001','2d2004fd-938f-11f1-a8f5-005056c00001','e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206d6f-938f-11f1-a8f5-005056c00001','A01','2d206d70-938f-11f1-a8f5-005056c00001','2d200582-938f-11f1-a8f5-005056c00001','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206e6b-938f-11f1-a8f5-005056c00001','A01','2d206e6c-938f-11f1-a8f5-005056c00001','2d20060a-938f-11f1-a8f5-005056c00001','60f6bfc3-2436-4002-a152-db891073629d','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d206fb9-938f-11f1-a8f5-005056c00001','A01','2d206fba-938f-11f1-a8f5-005056c00001','2d200692-938f-11f1-a8f5-005056c00001','75b2cf0e-c377-4261-b0e6-74295af900e1','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207151-938f-11f1-a8f5-005056c00001','A01','2d207153-938f-11f1-a8f5-005056c00001','2d200771-938f-11f1-a8f5-005056c00001','087af37e-77ff-4480-8f3e-0abf9c844804','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207281-938f-11f1-a8f5-005056c00001','A01','2d207282-938f-11f1-a8f5-005056c00001','2d2007ff-938f-11f1-a8f5-005056c00001','650841c6-d104-46da-804d-b7eb45cfbc67','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d2073a7-938f-11f1-a8f5-005056c00001','A01','2d2073a8-938f-11f1-a8f5-005056c00001','2d200888-938f-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','8e95bf09-ebac-42e4-b933-a9fd25f5d96c','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000','admin','2026-08-09 11:05:48.723'),
('2d20760a-938f-11f1-a8f5-005056c00001','A01','2d20760f-938f-11f1-a8f5-005056c00001','2d2009bc-938f-11f1-a8f5-005056c00001','d4e31f21-45fd-4a05-8272-d60e7a0688cb','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d2076f3-938f-11f1-a8f5-005056c00001','A01','2d2076f4-938f-11f1-a8f5-005056c00001','2d200a5a-938f-11f1-a8f5-005056c00001','e42fa17a-18a6-41e2-b15e-a7f77ad24e03','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d2077d3-938f-11f1-a8f5-005056c00001','A01','2d2077d5-938f-11f1-a8f5-005056c00001','2d200ad4-938f-11f1-a8f5-005056c00001','bb772e13-7d41-41fc-88a0-a7dbc9b6f743','869206f0-661f-4f9b-8a06-d3a35385e2de','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20791b-938f-11f1-a8f5-005056c00001','A01','2d20791c-938f-11f1-a8f5-005056c00001','2d200b51-938f-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207a13-938f-11f1-a8f5-005056c00001','A01','2d207a14-938f-11f1-a8f5-005056c00001','2d200bf2-938f-11f1-a8f5-005056c00001','f5c52e35-64a9-451b-978f-fe8410daecc8','3fe52a9d-86a9-42ab-9736-3c6a47e77436','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207af8-938f-11f1-a8f5-005056c00001','A01','2d207af9-938f-11f1-a8f5-005056c00001','2d200c87-938f-11f1-a8f5-005056c00001','6dfafa1c-4602-41bf-ac74-0ce1eebb63c1','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207bd7-938f-11f1-a8f5-005056c00001','A01','2d207bd8-938f-11f1-a8f5-005056c00001','2d200d24-938f-11f1-a8f5-005056c00001','cd35e7e0-8d1f-4f0b-bac9-2fd7e66777b8','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207ca8-938f-11f1-a8f5-005056c00001','A01','2d207ca9-938f-11f1-a8f5-005056c00001','2d200db8-938f-11f1-a8f5-005056c00001','e0d58477-af58-4cb0-a2c7-ea9429410572','55fcf5e9-5fb0-4de7-b17d-a28b9e326eae','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207d8c-938f-11f1-a8f5-005056c00001','A01','2d207d8d-938f-11f1-a8f5-005056c00001','2d200e47-938f-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d207e7f-938f-11f1-a8f5-005056c00001','A01','2d207e80-938f-11f1-a8f5-005056c00001','2d200ecd-938f-11f1-a8f5-005056c00001','5bf5579b-bf06-4e05-a039-dab24dd48846','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('32b18c97-8e3a-11f1-b073-cd1c8aecc952','A01','d15a5922-9db1-47b1-bb5c-071351e6eef5','8bf5c5c3-809c-45c4-9a43-9e97696a3845','121bbf1f-9fde-499d-95a6-b4d59645ccb2','8e6ac20e-1015-442b-a731-9d1408cbfbc2','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-08-02 14:20:03.494',NULL,NULL),
('524c167c-9391-11f1-a8f5-005056c00001','A01','524c16c2-9391-11f1-a8f5-005056c00001','a3bb53cb-fbb1-4c9d-870b-9d2e39d17cba','e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:26:03.123',NULL,NULL),
('524c28ae-9391-11f1-a8f5-005056c00001','A01','524c28b9-9391-11f1-a8f5-005056c00001','a770ca51-0137-409f-af3a-af895f22ee50','8560d228-540d-4e8a-8901-e21a0e3769cd','869206f0-661f-4f9b-8a06-d3a35385e2de','EXPLICIT','f7511c38-938e-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:26:03.124',NULL,NULL),
('ac964fbd-9398-11f1-a8f5-005056c00001','A01','ac964fca-9398-11f1-a8f5-005056c00001','ac95d319-9398-11f1-a8f5-005056c00001','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96522f-9398-11f1-a8f5-005056c00001','A01','ac965234-9398-11f1-a8f5-005056c00001','ac95f898-9398-11f1-a8f5-005056c00001','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96531c-9398-11f1-a8f5-005056c00001','A01','ac965320-9398-11f1-a8f5-005056c00001','ac95fa86-9398-11f1-a8f5-005056c00001','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9653c6-9398-11f1-a8f5-005056c00001','A01','ac9653ca-9398-11f1-a8f5-005056c00001','ac96150b-9398-11f1-a8f5-005056c00001','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96551e-9398-11f1-a8f5-005056c00001','A01','ac965523-9398-11f1-a8f5-005056c00001','ac96161a-9398-11f1-a8f5-005056c00001','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96563b-9398-11f1-a8f5-005056c00001','A01','ac965640-9398-11f1-a8f5-005056c00001','ac9594b6-9398-11f1-a8f5-005056c00001','b7ff38ab-98a8-44d0-8318-85145e9e5b57','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac965707-9398-11f1-a8f5-005056c00001','A01','ac96570b-9398-11f1-a8f5-005056c00001','ac95c92d-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','2176a3a7-e085-4b41-8d26-c4ec08a44573','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.198'),
('ac9657a6-9398-11f1-a8f5-005056c00001','A01','ac9657aa-9398-11f1-a8f5-005056c00001','ac95c994-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96585d-9398-11f1-a8f5-005056c00001','A01','ac965861-9398-11f1-a8f5-005056c00001','ac95c9f3-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac9658f7-9398-11f1-a8f5-005056c00001','A01','ac9658fb-9398-11f1-a8f5-005056c00001','ac95ca50-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac9659a3-9398-11f1-a8f5-005056c00001','A01','ac9659a7-9398-11f1-a8f5-005056c00001','ac95caaf-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac965a3b-9398-11f1-a8f5-005056c00001','A01','ac965a3e-9398-11f1-a8f5-005056c00001','ac95cb09-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac965acf-9398-11f1-a8f5-005056c00001','A01','ac965ad3-9398-11f1-a8f5-005056c00001','ac95cb60-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac965b62-9398-11f1-a8f5-005056c00001','A01','ac965b65-9398-11f1-a8f5-005056c00001','ac95cbb7-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac965c09-9398-11f1-a8f5-005056c00001','A01','ac965c0d-9398-11f1-a8f5-005056c00001','ac95cc16-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac965ca0-9398-11f1-a8f5-005056c00001','A01','ac965ca4-9398-11f1-a8f5-005056c00001','ac95cc6f-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac965d37-9398-11f1-a8f5-005056c00001','A01','ac965d3b-9398-11f1-a8f5-005056c00001','ac95ccd4-9398-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96570b-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac965e03-9398-11f1-a8f5-005056c00001','A01','ac965e0b-9398-11f1-a8f5-005056c00001','ac959514-9398-11f1-a8f5-005056c00001','11e2a4e7-30a4-4ced-8454-9753ce4a1b60','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac965f15-9398-11f1-a8f5-005056c00001','A01','ac965f19-9398-11f1-a8f5-005056c00001','ac96167a-9398-11f1-a8f5-005056c00001','92f87544-d491-4a96-a8d3-425795376136','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96612a-9398-11f1-a8f5-005056c00001','A01','ac96612e-9398-11f1-a8f5-005056c00001','ac9616da-9398-11f1-a8f5-005056c00001','92f87544-d491-4a96-a8d3-425795376136','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9661e9-9398-11f1-a8f5-005056c00001','A01','ac9661ed-9398-11f1-a8f5-005056c00001','ac961738-9398-11f1-a8f5-005056c00001','92f87544-d491-4a96-a8d3-425795376136','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96629f-9398-11f1-a8f5-005056c00001','A01','ac9662a3-9398-11f1-a8f5-005056c00001','ac961793-9398-11f1-a8f5-005056c00001','92f87544-d491-4a96-a8d3-425795376136','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968562-9398-11f1-a8f5-005056c00001','A01','ac96856f-9398-11f1-a8f5-005056c00001','ac961db5-9398-11f1-a8f5-005056c00001','2e50d437-f16d-4a63-8a6b-69e6bf564e7e','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9686e9-9398-11f1-a8f5-005056c00001','A01','ac9686ef-9398-11f1-a8f5-005056c00001','ac9597b0-9398-11f1-a8f5-005056c00001','f092bb40-d87f-407b-9b12-2221470489a3','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9687da-9398-11f1-a8f5-005056c00001','A01','ac9687de-9398-11f1-a8f5-005056c00001','ac9617f1-9398-11f1-a8f5-005056c00001','e2043fd6-978c-4341-a1fe-7751fecb9b2b','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968886-9398-11f1-a8f5-005056c00001','A01','ac96888a-9398-11f1-a8f5-005056c00001','ac96186a-9398-11f1-a8f5-005056c00001','e2043fd6-978c-4341-a1fe-7751fecb9b2b','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968944-9398-11f1-a8f5-005056c00001','A01','ac968948-9398-11f1-a8f5-005056c00001','ac9618c8-9398-11f1-a8f5-005056c00001','e2043fd6-978c-4341-a1fe-7751fecb9b2b','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9689ef-9398-11f1-a8f5-005056c00001','A01','ac9689f3-9398-11f1-a8f5-005056c00001','ac961927-9398-11f1-a8f5-005056c00001','e2043fd6-978c-4341-a1fe-7751fecb9b2b','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968aad-9398-11f1-a8f5-005056c00001','A01','ac968ab1-9398-11f1-a8f5-005056c00001','ac959804-9398-11f1-a8f5-005056c00001','4f4a3de9-3b13-44f5-a783-19506696d7d7','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968b94-9398-11f1-a8f5-005056c00001','A01','ac968b99-9398-11f1-a8f5-005056c00001','ac962096-9398-11f1-a8f5-005056c00001','121bbf1f-9fde-499d-95a6-b4d59645ccb2','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968c7b-9398-11f1-a8f5-005056c00001','A01','ac968c7f-9398-11f1-a8f5-005056c00001','ac959867-9398-11f1-a8f5-005056c00001','9c2c3ed6-2c5a-4678-a585-d8d6aef6d2c7','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968d3f-9398-11f1-a8f5-005056c00001','A01','ac968d43-9398-11f1-a8f5-005056c00001','ac95964f-9398-11f1-a8f5-005056c00001','c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968e17-9398-11f1-a8f5-005056c00001','A01','ac968e1d-9398-11f1-a8f5-005056c00001','ac961987-9398-11f1-a8f5-005056c00001','84ada4ab-426a-4b15-a2c0-e31676c5b183','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968eca-9398-11f1-a8f5-005056c00001','A01','ac968ece-9398-11f1-a8f5-005056c00001','ac9619ec-9398-11f1-a8f5-005056c00001','84ada4ab-426a-4b15-a2c0-e31676c5b183','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac968f71-9398-11f1-a8f5-005056c00001','A01','ac968f75-9398-11f1-a8f5-005056c00001','ac961a4d-9398-11f1-a8f5-005056c00001','84ada4ab-426a-4b15-a2c0-e31676c5b183','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969018-9398-11f1-a8f5-005056c00001','A01','ac96901c-9398-11f1-a8f5-005056c00001','ac961ab6-9398-11f1-a8f5-005056c00001','84ada4ab-426a-4b15-a2c0-e31676c5b183','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9690f3-9398-11f1-a8f5-005056c00001','A01','ac9690f7-9398-11f1-a8f5-005056c00001','ac962107-9398-11f1-a8f5-005056c00001','210f59af-a6a8-4133-adda-c927af8d69c2','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9691b7-9398-11f1-a8f5-005056c00001','A01','ac9691bb-9398-11f1-a8f5-005056c00001','ac9596a9-9398-11f1-a8f5-005056c00001','ada00d6d-df68-4c9b-84d0-28b8ee8c0f58','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96928b-9398-11f1-a8f5-005056c00001','A01','ac96928f-9398-11f1-a8f5-005056c00001','ac9598ba-9398-11f1-a8f5-005056c00001','c7db44c2-4d03-4eb0-b3be-2d569c3965bb','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969350-9398-11f1-a8f5-005056c00001','A01','ac969354-9398-11f1-a8f5-005056c00001','ac962163-9398-11f1-a8f5-005056c00001','e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969411-9398-11f1-a8f5-005056c00001','A01','ac969415-9398-11f1-a8f5-005056c00001','ac961b28-9398-11f1-a8f5-005056c00001','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9694b4-9398-11f1-a8f5-005056c00001','A01','ac9694b8-9398-11f1-a8f5-005056c00001','ac961b84-9398-11f1-a8f5-005056c00001','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969739-9398-11f1-a8f5-005056c00001','A01','ac96973e-9398-11f1-a8f5-005056c00001','ac961be0-9398-11f1-a8f5-005056c00001','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9697e7-9398-11f1-a8f5-005056c00001','A01','ac9697eb-9398-11f1-a8f5-005056c00001','ac961c3f-9398-11f1-a8f5-005056c00001','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9698a4-9398-11f1-a8f5-005056c00001','A01','ac9698a8-9398-11f1-a8f5-005056c00001','ac95cd29-9398-11f1-a8f5-005056c00001','60f6bfc3-2436-4002-a152-db891073629d','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96994a-9398-11f1-a8f5-005056c00001','A01','ac96994e-9398-11f1-a8f5-005056c00001','ac95cd8a-9398-11f1-a8f5-005056c00001','60f6bfc3-2436-4002-a152-db891073629d','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9699f1-9398-11f1-a8f5-005056c00001','A01','ac9699f5-9398-11f1-a8f5-005056c00001','ac95cde8-9398-11f1-a8f5-005056c00001','60f6bfc3-2436-4002-a152-db891073629d','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969ccb-9398-11f1-a8f5-005056c00001','A01','ac969cd1-9398-11f1-a8f5-005056c00001','ac95ce43-9398-11f1-a8f5-005056c00001','60f6bfc3-2436-4002-a152-db891073629d','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969d91-9398-11f1-a8f5-005056c00001','A01','ac969d95-9398-11f1-a8f5-005056c00001','ac95cef8-9398-11f1-a8f5-005056c00001','60f6bfc3-2436-4002-a152-db891073629d','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969e4e-9398-11f1-a8f5-005056c00001','A01','ac969e52-9398-11f1-a8f5-005056c00001','ac959701-9398-11f1-a8f5-005056c00001','75b2cf0e-c377-4261-b0e6-74295af900e1','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac969f4d-9398-11f1-a8f5-005056c00001','A01','ac969f51-9398-11f1-a8f5-005056c00001','ac9621c1-9398-11f1-a8f5-005056c00001','087af37e-77ff-4480-8f3e-0abf9c844804','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96bb6a-9398-11f1-a8f5-005056c00001','A01','ac96bb7a-9398-11f1-a8f5-005056c00001','ac95975a-9398-11f1-a8f5-005056c00001','650841c6-d104-46da-804d-b7eb45cfbc67','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96d4ba-9398-11f1-a8f5-005056c00001','A01','ac96d4c8-9398-11f1-a8f5-005056c00001','ac95990e-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96d62e-9398-11f1-a8f5-005056c00001','A01','ac96d633-9398-11f1-a8f5-005056c00001','ac959960-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96d6e6-9398-11f1-a8f5-005056c00001','A01','ac96d6ea-9398-11f1-a8f5-005056c00001','ac9599b5-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96d797-9398-11f1-a8f5-005056c00001','A01','ac96d79b-9398-11f1-a8f5-005056c00001','ac959a08-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96d83a-9398-11f1-a8f5-005056c00001','A01','ac96d83e-9398-11f1-a8f5-005056c00001','ac959a5b-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96d8d9-9398-11f1-a8f5-005056c00001','A01','ac96d8dc-9398-11f1-a8f5-005056c00001','ac959aa9-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96daed-9398-11f1-a8f5-005056c00001','A01','ac96daf1-9398-11f1-a8f5-005056c00001','ac959af9-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96ddb6-9398-11f1-a8f5-005056c00001','A01','ac96ddbb-9398-11f1-a8f5-005056c00001','ac959b48-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96de7e-9398-11f1-a8f5-005056c00001','A01','ac96de83-9398-11f1-a8f5-005056c00001','ac959b97-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96df2e-9398-11f1-a8f5-005056c00001','A01','ac96df32-9398-11f1-a8f5-005056c00001','ac959bf3-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96dfce-9398-11f1-a8f5-005056c00001','A01','ac96dfd2-9398-11f1-a8f5-005056c00001','ac959c46-9398-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e092-9398-11f1-a8f5-005056c00001','A01','ac96e096-9398-11f1-a8f5-005056c00001','ac9590d9-9398-11f1-a8f5-005056c00001','8560d228-540d-4e8a-8901-e21a0e3769cd','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e15d-9398-11f1-a8f5-005056c00001','A01','ac96e161-9398-11f1-a8f5-005056c00001','ac95cf52-9398-11f1-a8f5-005056c00001','d4e31f21-45fd-4a05-8272-d60e7a0688cb','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e210-9398-11f1-a8f5-005056c00001','A01','ac96e214-9398-11f1-a8f5-005056c00001','ac95cfbb-9398-11f1-a8f5-005056c00001','d4e31f21-45fd-4a05-8272-d60e7a0688cb','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e2cf-9398-11f1-a8f5-005056c00001','A01','ac96e2d3-9398-11f1-a8f5-005056c00001','ac95d01a-9398-11f1-a8f5-005056c00001','d4e31f21-45fd-4a05-8272-d60e7a0688cb','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e376-9398-11f1-a8f5-005056c00001','A01','ac96e37a-9398-11f1-a8f5-005056c00001','ac95d079-9398-11f1-a8f5-005056c00001','d4e31f21-45fd-4a05-8272-d60e7a0688cb','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e41f-9398-11f1-a8f5-005056c00001','A01','ac96e423-9398-11f1-a8f5-005056c00001','ac95d0dd-9398-11f1-a8f5-005056c00001','d4e31f21-45fd-4a05-8272-d60e7a0688cb','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e4e0-9398-11f1-a8f5-005056c00001','A01','ac96e4e4-9398-11f1-a8f5-005056c00001','ac96221f-9398-11f1-a8f5-005056c00001','e42fa17a-18a6-41e2-b15e-a7f77ad24e03','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e5b5-9398-11f1-a8f5-005056c00001','A01','ac96e5b9-9398-11f1-a8f5-005056c00001','ac961ca1-9398-11f1-a8f5-005056c00001','bb772e13-7d41-41fc-88a0-a7dbc9b6f743','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96e683-9398-11f1-a8f5-005056c00001','A01','ac96e687-9398-11f1-a8f5-005056c00001','ac959c96-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','2176a3a7-e085-4b41-8d26-c4ec08a44573','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.198'),
('ac96e740-9398-11f1-a8f5-005056c00001','A01','ac96e744-9398-11f1-a8f5-005056c00001','ac959cec-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96e7e8-9398-11f1-a8f5-005056c00001','A01','ac96e7ec-9398-11f1-a8f5-005056c00001','ac959d40-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96e88e-9398-11f1-a8f5-005056c00001','A01','ac96e892-9398-11f1-a8f5-005056c00001','ac959d95-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96e92f-9398-11f1-a8f5-005056c00001','A01','ac96e932-9398-11f1-a8f5-005056c00001','ac959de9-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96e9c7-9398-11f1-a8f5-005056c00001','A01','ac96e9cb-9398-11f1-a8f5-005056c00001','ac959e38-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96ea64-9398-11f1-a8f5-005056c00001','A01','ac96ea68-9398-11f1-a8f5-005056c00001','ac959e86-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96eb13-9398-11f1-a8f5-005056c00001','A01','ac96eb17-9398-11f1-a8f5-005056c00001','ac959ed4-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96ebaf-9398-11f1-a8f5-005056c00001','A01','ac96ebb3-9398-11f1-a8f5-005056c00001','ac959f23-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96ec4a-9398-11f1-a8f5-005056c00001','A01','ac96ec4e-9398-11f1-a8f5-005056c00001','ac959f71-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96ecfc-9398-11f1-a8f5-005056c00001','A01','ac96ed00-9398-11f1-a8f5-005056c00001','ac959fbf-9398-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac96e687-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac96edae-9398-11f1-a8f5-005056c00001','A01','ac96edb2-9398-11f1-a8f5-005056c00001','ac95d134-9398-11f1-a8f5-005056c00001','f5c52e35-64a9-451b-978f-fe8410daecc8','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96ee56-9398-11f1-a8f5-005056c00001','A01','ac96ee5a-9398-11f1-a8f5-005056c00001','ac95d19c-9398-11f1-a8f5-005056c00001','f5c52e35-64a9-451b-978f-fe8410daecc8','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96ef08-9398-11f1-a8f5-005056c00001','A01','ac96ef0c-9398-11f1-a8f5-005056c00001','ac95d203-9398-11f1-a8f5-005056c00001','f5c52e35-64a9-451b-978f-fe8410daecc8','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96efac-9398-11f1-a8f5-005056c00001','A01','ac96efb0-9398-11f1-a8f5-005056c00001','ac95d263-9398-11f1-a8f5-005056c00001','f5c52e35-64a9-451b-978f-fe8410daecc8','440aeddb-f2e4-485d-a3ed-7e92cb52380b','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96f054-9398-11f1-a8f5-005056c00001','A01','ac96f058-9398-11f1-a8f5-005056c00001','ac95d2c5-9398-11f1-a8f5-005056c00001','f5c52e35-64a9-451b-978f-fe8410daecc8','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96f106-9398-11f1-a8f5-005056c00001','A01','ac96f10a-9398-11f1-a8f5-005056c00001','ac961cfd-9398-11f1-a8f5-005056c00001','6dfafa1c-4602-41bf-ac74-0ce1eebb63c1','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96f1da-9398-11f1-a8f5-005056c00001','A01','ac96f1de-9398-11f1-a8f5-005056c00001','ac962279-9398-11f1-a8f5-005056c00001','cd35e7e0-8d1f-4f0b-bac9-2fd7e66777b8','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac96f2aa-9398-11f1-a8f5-005056c00001','A01','ac96f2ae-9398-11f1-a8f5-005056c00001','ac959432-9398-11f1-a8f5-005056c00001','e0d58477-af58-4cb0-a2c7-ea9429410572','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('ac9721c0-9398-11f1-a8f5-005056c00001','A01','ac9721de-9398-11f1-a8f5-005056c00001','ac95a025-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','2176a3a7-e085-4b41-8d26-c4ec08a44573','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.198'),
('ac972477-9398-11f1-a8f5-005056c00001','A01','ac972481-9398-11f1-a8f5-005056c00001','ac95c3a1-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac97260a-9398-11f1-a8f5-005056c00001','A01','ac972613-9398-11f1-a8f5-005056c00001','ac95c5df-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac972844-9398-11f1-a8f5-005056c00001','A01','ac97284d-9398-11f1-a8f5-005056c00001','ac95c648-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac9729c8-9398-11f1-a8f5-005056c00001','A01','ac9729d2-9398-11f1-a8f5-005056c00001','ac95c6b2-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac972b48-9398-11f1-a8f5-005056c00001','A01','ac972b51-9398-11f1-a8f5-005056c00001','ac95c710-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac973a7a-9398-11f1-a8f5-005056c00001','A01','ac973a99-9398-11f1-a8f5-005056c00001','ac95c769-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac973cf5-9398-11f1-a8f5-005056c00001','A01','ac973cf9-9398-11f1-a8f5-005056c00001','ac95c7c7-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac973daa-9398-11f1-a8f5-005056c00001','A01','ac973dae-9398-11f1-a8f5-005056c00001','ac95c823-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac973e5e-9398-11f1-a8f5-005056c00001','A01','ac973e62-9398-11f1-a8f5-005056c00001','ac95c87a-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','440aeddb-f2e4-485d-a3ed-7e92cb52380b','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac973f0e-9398-11f1-a8f5-005056c00001','A01','ac973f12-9398-11f1-a8f5-005056c00001','ac95c8d4-9398-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','86751be1-e9e9-4908-b6f2-e264d105f4e0','EXPLICIT','ac9721de-9398-11f1-a8f5-005056c00001','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081','admin','2026-08-09 11:01:56.206'),
('ac97402a-9398-11f1-a8f5-005056c00001','A01','ac97402e-9398-11f1-a8f5-005056c00001','ac961d59-9398-11f1-a8f5-005056c00001','5bf5579b-bf06-4e05-a039-dab24dd48846','86751be1-e9e9-4908-b6f2-e264d105f4e0','ORG_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.081',NULL,NULL),
('f7511c1f-938e-11f1-a8f5-005056c00001','A01','f7511c38-938e-11f1-a8f5-005056c00001','f750827a-938e-11f1-a8f5-005056c00001','8560d228-540d-4e8a-8901-e21a0e3769cd','6e3b108b-6cc3-4648-8412-ccf7bbd96691','PARENT_HEAD',NULL,'Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:09:11.000',NULL,NULL),
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
  `DOCUMENT_NUMBER` varchar(100) DEFAULT NULL,
  `IDEMPOTENCY_KEY` varchar(100) DEFAULT NULL,
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
  UNIQUE KEY `UK_FM_FORM_DATA_IDEMPOTENCY` (`TENANT_ID`,`IDEMPOTENCY_KEY`),
  UNIQUE KEY `UK_FM_FORM_DATA_DOC_NO` (`TENANT_ID`,`DOCUMENT_NUMBER`),
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
INSERT INTO `fm_form_def` VALUES
('8d4a47c4-6e44-4fe3-94b4-79fcf70b3991','A01','2d939f65-b78e-454d-a48d-cdd538222d96','FM_PURCHASE_REQUEST','請購申請單',1,'PUBLISHED','正式請購申請表單，包含申請資訊、DataGrid 品項明細及自動金額彙總。','SYSTEM','2026-08-12 21:06:17.341','admin','2026-08-17 20:09:56.836');
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
  `CUSTOM_SCRIPT_CONTENT` longtext DEFAULT NULL,
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
INSERT INTO `fm_form_version` VALUES
('254fa6f0-16d5-49e7-a5a3-aa41565f09bb','A01','2d939f65-b78e-454d-a48d-cdd538222d96',1,'PUBLISHED','{\"display\":\"form\",\"components\":[{\"type\":\"content\",\"key\":\"purchaseIntroduction\",\"html\":\"\\u003cdiv class=\\u0027alert alert-light border mb-0\\u0027\\u003e\\u003cstrong\\u003e請購申請單\\u003c/strong\\u003e\\u003cbr\\u003e\\u003cspan class=\\u0027text-muted\\u0027\\u003e請填寫申請資料與至少一筆品項；列金額及合計由系統自動計算。\\u003c/span\\u003e\\u003c/div\\u003e\",\"input\":false},{\"type\":\"panel\",\"key\":\"requestInformation\",\"title\":\"申請資訊\",\"theme\":\"primary\",\"components\":[{\"type\":\"textfield\",\"key\":\"documentNumber\",\"label\":\"請購單編號\",\"description\":\"正式送出後由系統自動產生，不可自行修改。\",\"placeholder\":\"送出後由系統產生\",\"input\":true,\"disabled\":true,\"persistent\":false,\"validate\":{\"maxLength\":100}},{\"type\":\"columns\",\"key\":\"requestInformationColumns1\",\"columns\":[{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"select\",\"key\":\"purchaseCategory\",\"label\":\"請購分類\",\"input\":true,\"dataSrc\":\"values\",\"data\":{\"values\":[{\"label\":\"產線設備\",\"value\":\"PRODUCTION_EQUIPMENT\"},{\"label\":\"資訊設備\",\"value\":\"IT_EQUIPMENT\"},{\"label\":\"軟體／雲端服務\",\"value\":\"SOFTWARE_SERVICE\"},{\"label\":\"品質／檢測設備\",\"value\":\"QUALITY_EQUIPMENT\"},{\"label\":\"工安／環保設備\",\"value\":\"EHS_EQUIPMENT\"},{\"label\":\"原物料\",\"value\":\"RAW_MATERIAL\"},{\"label\":\"工程／修繕\",\"value\":\"CONSTRUCTION\"},{\"label\":\"辦公設備／用品\",\"value\":\"OFFICE_SUPPLIES\"},{\"label\":\"顧問／委外專業服務\",\"value\":\"PROFESSIONAL_SERVICE\"},{\"label\":\"一般採購\",\"value\":\"GENERAL\"},{\"label\":\"其他\",\"value\":\"OTHER\"}]},\"validate\":{\"required\":true}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"input\",\"key\":\"requestDate\",\"label\":\"申請日期\",\"input\":true,\"validate\":{\"required\":true},\"inputType\":\"date\",\"disabled\":false}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"select\",\"key\":\"currency\",\"label\":\"幣別\",\"input\":true,\"dataSrc\":\"values\",\"defaultValue\":\"TWD\",\"data\":{\"values\":[{\"label\":\"新臺幣 TWD\",\"value\":\"TWD\"},{\"label\":\"美元 USD\",\"value\":\"USD\"},{\"label\":\"歐元 EUR\",\"value\":\"EUR\"},{\"label\":\"日圓 JPY\",\"value\":\"JPY\"},{\"label\":\"人民幣 CNY\",\"value\":\"CNY\"}]},\"validate\":{\"required\":true}}]}],\"input\":false},{\"type\":\"columns\",\"key\":\"requestInformationColumns2\",\"columns\":[{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"starterAccount\",\"label\":\"實際發起人帳號\",\"input\":true,\"disabled\":true,\"persistent\":true}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"select\",\"key\":\"applicantAccount\",\"label\":\"申請人\",\"description\":\"須與起單區選擇的申請人一致。\",\"input\":true,\"validate\":{\"required\":true,\"maxLength\":100},\"dataSrc\":\"values\",\"data\":{\"values\":[]},\"searchEnabled\":true}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"applicantName\",\"label\":\"申請人姓名\",\"input\":true,\"validate\":{\"required\":true,\"maxLength\":100},\"disabled\":false,\"attributes\":{\"readonly\":\"readonly\"},\"persistent\":true}]}],\"input\":false},{\"type\":\"columns\",\"key\":\"requestInformationColumns3\",\"columns\":[{\"width\":6,\"size\":\"md\",\"components\":[{\"type\":\"select\",\"key\":\"applicantAssignmentId\",\"label\":\"申請部門\",\"input\":true,\"validate\":{\"required\":true,\"maxLength\":150},\"dataSrc\":\"values\",\"data\":{\"values\":[]},\"searchEnabled\":true}]},{\"width\":6,\"size\":\"md\",\"components\":[{\"type\":\"input\",\"key\":\"expectedDeliveryDate\",\"label\":\"期望交貨日\",\"input\":true,\"validate\":{\"required\":true},\"inputType\":\"date\",\"disabled\":false}]}],\"input\":false},{\"type\":\"textfield\",\"key\":\"subject\",\"label\":\"請購主旨\",\"input\":true,\"validate\":{\"required\":true,\"maxLength\":200}},{\"type\":\"textarea\",\"key\":\"purpose\",\"label\":\"請購用途／原因\",\"input\":true,\"rows\":3,\"validate\":{\"required\":true,\"maxLength\":1000}},{\"type\":\"hidden\",\"key\":\"applicantEmployeeId\",\"input\":true,\"persistent\":true},{\"type\":\"hidden\",\"key\":\"applicantOrgName\",\"input\":true,\"persistent\":true},{\"type\":\"hidden\",\"key\":\"applicantOrgId\",\"input\":true,\"persistent\":true},{\"type\":\"hidden\",\"key\":\"applicantTitleId\",\"input\":true,\"persistent\":true},{\"type\":\"hidden\",\"key\":\"applicantTitleName\",\"input\":true,\"persistent\":true}]},{\"type\":\"panel\",\"key\":\"purchaseGovernance\",\"title\":\"支出與預算\",\"theme\":\"primary\",\"components\":[{\"type\":\"columns\",\"key\":\"purchaseGovernanceColumns1\",\"input\":false,\"columns\":[{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"select\",\"key\":\"requestType\",\"label\":\"申請類型\",\"input\":true,\"dataSrc\":\"values\",\"data\":{\"values\":[{\"label\":\"新購\",\"value\":\"NEW\"},{\"label\":\"汰換\",\"value\":\"REPLACEMENT\"},{\"label\":\"增購\",\"value\":\"ADDITION\"},{\"label\":\"續約\",\"value\":\"RENEWAL\"},{\"label\":\"維修\",\"value\":\"REPAIR\"},{\"label\":\"租賃\",\"value\":\"LEASE\"}]},\"validate\":{\"required\":true}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"select\",\"key\":\"expenseType\",\"label\":\"支出性質\",\"input\":true,\"dataSrc\":\"values\",\"data\":{\"values\":[{\"label\":\"資本支出 CAPEX\",\"value\":\"CAPEX\"},{\"label\":\"營業費用 OPEX\",\"value\":\"OPEX\"}]},\"validate\":{\"required\":true}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"select\",\"key\":\"budgetStatus\",\"label\":\"預算狀態\",\"input\":true,\"dataSrc\":\"values\",\"data\":{\"values\":[{\"label\":\"預算內\",\"value\":\"IN_BUDGET\"},{\"label\":\"預算外\",\"value\":\"OUT_OF_BUDGET\"}]},\"validate\":{\"required\":true}}]}]},{\"type\":\"columns\",\"key\":\"purchaseGovernanceColumns2\",\"input\":false,\"columns\":[{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"budgetNo\",\"label\":\"預算編號\",\"input\":true,\"validate\":{\"maxLength\":50}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"costCenter\",\"label\":\"成本中心\",\"input\":true,\"validate\":{\"required\":true,\"maxLength\":50}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"projectCode\",\"label\":\"專案代碼\",\"input\":true,\"validate\":{\"maxLength\":50}}]}]},{\"type\":\"number\",\"key\":\"projectTotalAmount\",\"label\":\"專案總額（TWD，含本次及相關請購）\",\"description\":\"用於重大投資與防拆單判斷；不得低於本次請購含稅總額。\",\"input\":true,\"validate\":{\"required\":true,\"min\":0,\"max\":999999999999,\"decimalLimit\":2}}]},{\"type\":\"panel\",\"key\":\"sourcingInformation\",\"title\":\"採購方式與例外\",\"theme\":\"primary\",\"components\":[{\"type\":\"select\",\"key\":\"sourcingMethod\",\"label\":\"採購方式\",\"input\":true,\"dataSrc\":\"values\",\"data\":{\"values\":[{\"label\":\"一般詢比價\",\"value\":\"COMPETITIVE_QUOTE\"},{\"label\":\"公開／邀請招標\",\"value\":\"TENDER\"},{\"label\":\"單一來源\",\"value\":\"SINGLE_SOURCE\"},{\"label\":\"框架合約\",\"value\":\"FRAMEWORK_AGREEMENT\"}]},\"validate\":{\"required\":true}},{\"type\":\"textfield\",\"key\":\"preferredSupplier\",\"label\":\"建議供應商\",\"description\":\"僅供採購評估，不代表免除詢比價程序。\",\"input\":true,\"validate\":{\"maxLength\":150}},{\"type\":\"textarea\",\"key\":\"singleSourceReason\",\"label\":\"單一來源／指定供應商理由\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":1000}},{\"type\":\"checkbox\",\"key\":\"emergencyPurchase\",\"label\":\"緊急採購\",\"input\":true,\"defaultValue\":false},{\"type\":\"textarea\",\"key\":\"emergencyReason\",\"label\":\"緊急原因與未即時採購的影響\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":1000}},{\"type\":\"checkbox\",\"key\":\"newSupplier\",\"label\":\"新供應商\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"relatedParty\",\"label\":\"可能涉及關係人交易\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"importPurchase\",\"label\":\"進口採購\",\"input\":true,\"defaultValue\":false}]},{\"type\":\"panel\",\"key\":\"riskQuestionnaire\",\"title\":\"風險與專業審查問卷\",\"theme\":\"primary\",\"description\":\"請依實際情況勾選；系統將據此增加必要專業審查，不代表可自行略過審查。\",\"components\":[{\"type\":\"checkbox\",\"key\":\"involvesInformationSystem\",\"label\":\"涉及公司網路、帳號、系統或設備連網\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"involvesCompanyData\",\"label\":\"涉及公司資料\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"involvesPersonalData\",\"label\":\"涉及員工、客戶或其他個人資料\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"involvesConstruction\",\"label\":\"涉及施工、動火、高處、用電、管線或廠務工程\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"involvesEhsRisk\",\"label\":\"涉及機械安全、職安或其他環安衛風險\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"involvesEnvironmentalPermit\",\"label\":\"涉及排放、化學品、環保或法規許可\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"requiresContract\",\"label\":\"需要合約或特殊付款條件\",\"input\":true,\"defaultValue\":false},{\"type\":\"checkbox\",\"key\":\"requiresConfidentiality\",\"label\":\"涉及保密、智慧財產或 NDA\",\"input\":true,\"defaultValue\":false}]},{\"type\":\"panel\",\"key\":\"equipmentEngineeringDetails\",\"title\":\"設備／工程與驗收資料\",\"theme\":\"primary\",\"description\":\"產線、品質、工安設備或涉及施工時必須完整填寫。\",\"customConditional\":\"show = [\\\"PRODUCTION_EQUIPMENT\\\", \\\"QUALITY_EQUIPMENT\\\", \\\"EHS_EQUIPMENT\\\", \\\"CONSTRUCTION\\\"].includes(data.purchaseCategory) || data.involvesConstruction;\",\"components\":[{\"type\":\"textfield\",\"key\":\"installationLocation\",\"label\":\"安裝／使用地點\",\"input\":true,\"validate\":{\"maxLength\":200}},{\"type\":\"textarea\",\"key\":\"technicalSpecification\",\"label\":\"技術規格\",\"input\":true,\"rows\":4,\"validate\":{\"maxLength\":4000}},{\"type\":\"textarea\",\"key\":\"acceptanceCriteria\",\"label\":\"驗收標準\",\"input\":true,\"rows\":4,\"validate\":{\"maxLength\":4000}},{\"type\":\"columns\",\"key\":\"capacityColumns\",\"input\":false,\"columns\":[{\"width\":6,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"currentCapacity\",\"label\":\"目前產能／能力\",\"input\":true,\"validate\":{\"maxLength\":200}}]},{\"width\":6,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"expectedCapacity\",\"label\":\"預計產能／能力\",\"input\":true,\"validate\":{\"maxLength\":200}}]}]},{\"type\":\"textarea\",\"key\":\"utilityRequirements\",\"label\":\"水電、氣體、管線、網路及其他公用需求\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":2000}},{\"type\":\"textarea\",\"key\":\"maintenancePlan\",\"label\":\"維護與備品計畫\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":2000}},{\"type\":\"columns\",\"key\":\"warrantyTrainingColumns\",\"input\":false,\"columns\":[{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"warrantyRequirement\",\"label\":\"保固需求\",\"input\":true,\"validate\":{\"maxLength\":200}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"textfield\",\"key\":\"trainingRequirement\",\"label\":\"教育訓練需求\",\"input\":true,\"validate\":{\"maxLength\":200}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"expectedUsefulLife\",\"label\":\"預計使用年限\",\"suffix\":\"年\",\"input\":true,\"validate\":{\"min\":0,\"max\":100,\"decimalLimit\":1}}]}]}]},{\"type\":\"panel\",\"key\":\"paymentInformation\",\"title\":\"外幣與付款條件\",\"theme\":\"primary\",\"components\":[{\"type\":\"columns\",\"key\":\"currencyColumns\",\"input\":false,\"columns\":[{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"exchangeRate\",\"label\":\"換算匯率（1 原幣兌 TWD）\",\"input\":true,\"defaultValue\":1,\"validate\":{\"required\":true,\"min\":0.000001,\"max\":1000000,\"decimalLimit\":6}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"totalAmountTwd\",\"label\":\"TWD 換算含稅總額\",\"input\":true,\"disabled\":true,\"persistent\":true,\"validate\":{\"min\":0,\"decimalLimit\":2}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"checkbox\",\"key\":\"crossFiscalYear\",\"label\":\"跨年度案件\",\"input\":true,\"defaultValue\":false}]}]},{\"type\":\"textarea\",\"key\":\"paymentTerms\",\"label\":\"付款條件\",\"input\":true,\"rows\":3,\"validate\":{\"required\":true,\"maxLength\":1000}},{\"type\":\"checkbox\",\"key\":\"prepaymentRequired\",\"label\":\"需要預付款\",\"input\":true,\"defaultValue\":false},{\"type\":\"number\",\"key\":\"prepaymentPercentage\",\"label\":\"預付款比例\",\"suffix\":\"%\",\"input\":true,\"conditional\":{\"show\":true,\"when\":\"prepaymentRequired\",\"eq\":\"true\"},\"validate\":{\"min\":0,\"max\":100,\"decimalLimit\":2}}]},{\"type\":\"panel\",\"key\":\"investmentBenefit\",\"title\":\"CAPEX 投資效益\",\"theme\":\"primary\",\"customConditional\":\"show = data.expenseType === \\\"CAPEX\\\";\",\"components\":[{\"type\":\"textarea\",\"key\":\"investmentReason\",\"label\":\"投資原因\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":2000}},{\"type\":\"textarea\",\"key\":\"expectedBenefit\",\"label\":\"預期效益\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":2000}},{\"type\":\"columns\",\"key\":\"investmentMetricColumns\",\"input\":false,\"columns\":[{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"paybackPeriod\",\"label\":\"預估回收年限\",\"suffix\":\"年\",\"input\":true,\"validate\":{\"min\":0,\"max\":100,\"decimalLimit\":2}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"npv\",\"label\":\"NPV（TWD）\",\"input\":true,\"validate\":{\"min\":-999999999999,\"max\":999999999999,\"decimalLimit\":2}}]},{\"width\":4,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"irr\",\"label\":\"IRR\",\"suffix\":\"%\",\"input\":true,\"validate\":{\"min\":-100,\"max\":10000,\"decimalLimit\":2}}]}]},{\"type\":\"textarea\",\"key\":\"capacityIncrease\",\"label\":\"產能提升說明\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":2000}},{\"type\":\"textarea\",\"key\":\"qualityImprovement\",\"label\":\"品質改善說明\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":2000}},{\"type\":\"textarea\",\"key\":\"riskWithoutInvestment\",\"label\":\"不投資的影響與風險\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":2000}}]},{\"type\":\"panel\",\"key\":\"purchaseItemsPanel\",\"title\":\"品項明細\",\"theme\":\"primary\",\"components\":[{\"type\":\"datagrid\",\"key\":\"items\",\"label\":\"請購品項\",\"input\":true,\"initEmpty\":true,\"reorder\":true,\"addAnother\":\"新增品項\",\"validate\":{\"required\":true,\"minLength\":1},\"components\":[{\"type\":\"number\",\"key\":\"lineNo\",\"label\":\"項次\",\"input\":true,\"disabled\":true,\"persistent\":true},{\"type\":\"textfield\",\"key\":\"itemName\",\"label\":\"品名\",\"input\":true,\"validate\":{\"required\":true,\"maxLength\":150}},{\"type\":\"textfield\",\"key\":\"specification\",\"label\":\"規格\",\"input\":true,\"validate\":{\"maxLength\":300}},{\"type\":\"number\",\"key\":\"quantity\",\"label\":\"數量\",\"input\":true,\"defaultValue\":1,\"validate\":{\"required\":true,\"min\":0.0001,\"max\":999999999}},{\"type\":\"textfield\",\"key\":\"unit\",\"label\":\"單位\",\"input\":true,\"validate\":{\"required\":true,\"maxLength\":30}},{\"type\":\"number\",\"key\":\"unitPrice\",\"label\":\"未稅單價\",\"input\":true,\"validate\":{\"required\":true,\"min\":0,\"max\":999999999999,\"decimalLimit\":2}},{\"type\":\"number\",\"key\":\"amount\",\"label\":\"未稅金額\",\"input\":true,\"disabled\":true,\"persistent\":true,\"validate\":{\"min\":0,\"max\":999999999999,\"decimalLimit\":2}},{\"type\":\"textfield\",\"key\":\"itemRemark\",\"label\":\"備註\",\"input\":true,\"validate\":{\"maxLength\":300}}]}]},{\"type\":\"panel\",\"key\":\"amountSummary\",\"title\":\"金額彙總\",\"theme\":\"primary\",\"components\":[{\"type\":\"columns\",\"key\":\"amountSummaryColumns\",\"columns\":[{\"width\":3,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"subtotal\",\"label\":\"未稅總額\",\"input\":true,\"disabled\":true,\"persistent\":true,\"validate\":{\"min\":0,\"decimalLimit\":2}}]},{\"width\":3,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"taxRate\",\"label\":\"稅率（%）\",\"input\":true,\"defaultValue\":5,\"validate\":{\"required\":true,\"min\":0,\"max\":100,\"decimalLimit\":2}}]},{\"width\":3,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"taxAmount\",\"label\":\"稅額\",\"input\":true,\"disabled\":true,\"persistent\":true,\"validate\":{\"min\":0,\"decimalLimit\":2}}]},{\"width\":3,\"size\":\"md\",\"components\":[{\"type\":\"number\",\"key\":\"totalAmount\",\"label\":\"含稅總金額（原幣）\",\"input\":true,\"disabled\":true,\"persistent\":true,\"validate\":{\"min\":0,\"decimalLimit\":2}}]}],\"input\":false}]},{\"type\":\"panel\",\"key\":\"attachmentsPanel\",\"title\":\"附件\",\"theme\":\"primary\",\"input\":false,\"components\":[{\"type\":\"file\",\"key\":\"attachments\",\"label\":\"請購附件\",\"description\":\"可上傳報價單、規格文件、圖片或常用壓縮檔。\",\"input\":true,\"persistent\":true,\"storage\":\"url\",\"url\":\"\",\"multiple\":true,\"fileMaxSize\":\"8MB\",\"maxNumberOfFiles\":10,\"flowmintMaxTotalSize\":\"20MB\",\"fileTypes\":[{\"label\":\"PDF\",\"value\":\".pdf\"},{\"label\":\"JPEG\",\"value\":\".jpg,.jpeg\"},{\"label\":\"圖片\",\"value\":\".png,.bmp\"},{\"label\":\"Word\",\"value\":\".doc,.docx\"},{\"label\":\"Excel\",\"value\":\".xls,.xlsx\"},{\"label\":\"PowerPoint\",\"value\":\".ppt,.pptx\"},{\"label\":\"壓縮檔\",\"value\":\".zip,.7z,.rar\"}],\"validate\":{\"required\":false}}]},{\"type\":\"panel\",\"key\":\"additionalInformation\",\"title\":\"補充資料\",\"components\":[{\"type\":\"textarea\",\"key\":\"remark\",\"label\":\"整單備註\",\"input\":true,\"rows\":3,\"validate\":{\"maxLength\":1000}}]}]}','{\"engine\":\"FORMIO\",\"version\":1,\"dataActions\":[]}','const money = (value) => Math.round((Number(value || 0) + Number.EPSILON) * 100) / 100;\nconst localDate = (value = new Date()) => {\n  const year = value.getFullYear();\n  const month = String(value.getMonth() + 1).padStart(2, \"0\");\n  const day = String(value.getDate()).padStart(2, \"0\");\n  return `${year}-${month}-${day}`;\n};\nconst dateKey = (value) => {\n  if (!value) return \"\";\n  if (value instanceof Date && !Number.isNaN(value.getTime())) return localDate(value);\n  const match = String(value).match(/^([0-9]{4}-[0-9]{2}-[0-9]{2})/);\n  return match ? match[1] : \"\";\n};\nlet employeeOptions = [];\nlet assignmentOptions = [];\nlet selectedApplicantAccount = \"\";\nlet selectedAssignmentId = \"\";\nconst selectedValue = (value) => {\n  if (value && typeof value === \"object\" && \"value\" in value) return String(value.value || \"\");\n  return String(value || \"\");\n};\nconst setSelectItems = (ctx, key, items) => {\n  return ctx.setSelectOptions(key, items);\n};\nconst applyAssignment = (ctx, assignment) => {\n  ctx.data.applicantAssignmentId = assignment ? assignment.assignmentId || \"\" : \"\";\n  ctx.data.applicantOrgId = assignment ? assignment.orgUnitId || \"\" : \"\";\n  ctx.data.applicantOrgName = assignment ? assignment.orgUnitName || \"\" : \"\";\n  ctx.data.applicantTitleId = assignment ? assignment.titleId || \"\" : \"\";\n  ctx.data.applicantTitleName = assignment ? assignment.titleName || \"\" : \"\";\n};\nconst loadAssignments = async (ctx, account) => {\n  assignmentOptions = [];\n  await setSelectItems(ctx, \"applicantAssignmentId\", []);\n  applyAssignment(ctx, null);\n  if (!account) return;\n  const result = await ctx.executeDataAction(\n    \"FM_PURCHASE_EMPLOYEE_ASSIGNMENTS\",\n    { applicantAccount: account }\n  );\n  assignmentOptions = result.assignments || [];\n  await setSelectItems(ctx, \"applicantAssignmentId\", assignmentOptions.map((assignment) => ({\n    label: assignment.label,\n    value: assignment.assignmentId\n  })));\n  const preferred = assignmentOptions.find((assignment) => assignment.isPrimary === \"Y\") || assignmentOptions[0];\n  applyAssignment(ctx, preferred || null);\n  await ctx.setValue(\"applicantAssignmentId\", ctx.data.applicantAssignmentId);\n  await ctx.setValue(\"applicantName\", ctx.data.applicantName);\n};\nconst recalculate = (data) => {\n  const items = Array.isArray(data.items) ? data.items : [];\n  items.forEach((item, index) => {\n    item.lineNo = index + 1;\n    item.amount = money(Number(item.quantity || 0) * Number(item.unitPrice || 0));\n  });\n  data.subtotal = money(items.reduce((sum, item) => sum + Number(item.amount || 0), 0));\n  data.taxAmount = money(data.subtotal * Number(data.taxRate || 0) / 100);\n  data.totalAmount = money(data.subtotal + data.taxAmount);\n  if (data.currency === \"TWD\") data.exchangeRate = 1;\n  const exchangeRate = Number(data.exchangeRate || 0);\n  data.totalAmountTwd = money(data.totalAmount * exchangeRate);\n  if (!(Number(data.projectTotalAmount) > 0)) {\n    data.projectTotalAmount = data.totalAmountTwd;\n  }\n};\n\nreturn {\n  async onFormLoad(ctx) {\n    if (!ctx.data.requestDate) {\n      ctx.data.requestDate = localDate();\n    }\n    if (!ctx.data.currency) ctx.data.currency = \"TWD\";\n    if (ctx.data.taxRate === undefined || ctx.data.taxRate === null) ctx.data.taxRate = 5;\n    if (!Array.isArray(ctx.data.items) || ctx.data.items.length === 0) {\n      ctx.data.items = [{ lineNo: 1, quantity: 1, unitPrice: 0, amount: 0 }];\n    }\n    try {\n      const [currentResult, employeeResult] = await Promise.all([\n        ctx.executeDataAction(\"FM_GET_CURRENT_EMPLOYEE\", {}),\n        ctx.executeDataAction(\"FM_PURCHASE_EMPLOYEE_OPTIONS\", { keyword: \"\" })\n      ]);\n      const current = currentResult.employee || currentResult;\n      employeeOptions = employeeResult.employees || [];\n      await setSelectItems(ctx, \"applicantAccount\", employeeOptions.map((employee) => ({ label: employee.label, value: employee.account })));\n      if (!ctx.data.starterAccount) ctx.data.starterAccount = current.account || \"\";\n      if (!ctx.data.applicantAccount) ctx.data.applicantAccount = current.account || \"\";\n      selectedApplicantAccount = selectedValue(ctx.data.applicantAccount);\n      ctx.data.applicantAccount = selectedApplicantAccount;\n      const selected = employeeOptions.find((employee) => employee.account === selectedApplicantAccount);\n      if (selected) {\n        ctx.data.applicantEmployeeId = selected.employeeId || \"\";\n        ctx.data.applicantName = selected.displayName || \"\";\n      }\n      await loadAssignments(ctx, ctx.data.applicantAccount);\n      selectedAssignmentId = selectedValue(ctx.data.applicantAssignmentId);\n      await ctx.setValue(\"applicantName\", ctx.data.applicantName);\n    } catch (error) {\n      ctx.warn(\"無法載入申請人或部門選項\", error);\n      ctx.notify.warning(\"無法載入申請人或部門選項，請稍後再試\");\n    }\n    recalculate(ctx.data);\n    await ctx.redraw();\n  },\n\n  async onFieldChange(ctx) {\n    const changedKey = ctx.changed && ctx.changed.component && ctx.changed.component.key;\n    const currentApplicantAccount = selectedValue(ctx.data.applicantAccount);\n    if (currentApplicantAccount !== selectedApplicantAccount) {\n      ctx.log(\"申請人變更\", currentApplicantAccount);\n      selectedApplicantAccount = currentApplicantAccount;\n      const selected = employeeOptions.find((employee) => employee.account === currentApplicantAccount);\n      ctx.log(\"申請人解析\", selected || null);\n      ctx.data.applicantEmployeeId = selected ? selected.employeeId || \"\" : \"\";\n      ctx.data.applicantName = selected ? selected.displayName || \"\" : \"\";\n      await ctx.setValue(\"applicantName\", ctx.data.applicantName);\n      await loadAssignments(ctx, currentApplicantAccount);\n      selectedAssignmentId = selectedValue(ctx.data.applicantAssignmentId);\n    }\n    const currentAssignmentId = selectedValue(ctx.data.applicantAssignmentId);\n    if (currentAssignmentId !== selectedAssignmentId) {\n      selectedAssignmentId = currentAssignmentId;\n      const selectedAssignment = assignmentOptions.find(\n        (assignment) => assignment.assignmentId === currentAssignmentId\n      );\n      applyAssignment(ctx, selectedAssignment || null);\n      await ctx.setValue(\"applicantName\", ctx.data.applicantName);\n    }\n    recalculate(ctx.data);\n    if ([\"items\", \"quantity\", \"unitPrice\", \"taxRate\", \"currency\", \"exchangeRate\"].includes(changedKey)) {\n      await ctx.setValue(\"items\", ctx.data.items);\n      await ctx.setValue(\"subtotal\", ctx.data.subtotal);\n      await ctx.setValue(\"taxAmount\", ctx.data.taxAmount);\n      await ctx.setValue(\"totalAmount\", ctx.data.totalAmount);\n      await ctx.setValue(\"totalAmountTwd\", ctx.data.totalAmountTwd);\n      await ctx.setValue(\"projectTotalAmount\", ctx.data.projectTotalAmount);\n    }\n  },\n\n  async beforeSubmit(ctx) {\n    recalculate(ctx.data);\n    const items = Array.isArray(ctx.data.items) ? ctx.data.items : [];\n    if (items.length === 0) return { valid: false, message: \"至少需要一筆請購品項\" };\n    for (let index = 0; index < items.length; index += 1) {\n      const item = items[index] || {};\n      if (!String(item.itemName || \"\").trim()) return { valid: false, message: `第 ${index + 1} 筆品項未填寫品名` };\n      if (!(Number(item.quantity) > 0)) return { valid: false, message: `第 ${index + 1} 筆品項數量必須大於 0` };\n      if (Number(item.unitPrice) < 0) return { valid: false, message: `第 ${index + 1} 筆品項單價不得小於 0` };\n      if (!String(item.unit || \"\").trim()) return { valid: false, message: `第 ${index + 1} 筆品項未填寫單位` };\n    }\n    const requestDate = dateKey(ctx.data.requestDate);\n    const expectedDeliveryDate = dateKey(ctx.data.expectedDeliveryDate);\n    if (requestDate && expectedDeliveryDate\n        && expectedDeliveryDate < requestDate) {\n      return { valid: false, message: \"期望交貨日不得早於申請日期\" };\n    }\n    const exchangeRate = Number(ctx.data.exchangeRate || 0);\n    if (!(exchangeRate > 0)) {\n      return { valid: false, message: \"換算匯率必須大於零\" };\n    }\n    const currentTotal = Number(ctx.data.totalAmountTwd || 0);\n    const projectTotal = Number(ctx.data.projectTotalAmount || 0);\n    if (projectTotal < currentTotal) {\n      return { valid: false, message: \"專案總額不得低於本次請購含稅總額\" };\n    }\n    if (ctx.data.budgetStatus === \"IN_BUDGET\"\n        && !String(ctx.data.budgetNo || \"\").trim()) {\n      return { valid: false, message: \"預算內請購必須填寫預算編號\" };\n    }\n    if (ctx.data.sourcingMethod === \"SINGLE_SOURCE\"\n        && !String(ctx.data.singleSourceReason || \"\").trim()) {\n      return { valid: false, message: \"單一來源採購必須填寫原因\" };\n    }\n    if (ctx.data.emergencyPurchase\n        && !String(ctx.data.emergencyReason || \"\").trim()) {\n      return { valid: false, message: \"緊急採購必須填寫原因與影響\" };\n    }\n    if (ctx.data.prepaymentRequired) {\n      const percentage = Number(ctx.data.prepaymentPercentage || 0);\n      if (!(percentage > 0 && percentage <= 100)) {\n        return { valid: false, message: \"預付款比例必須大於 0 且不超過 100%\" };\n      }\n    }\n    const equipmentCategories = [\n      \"PRODUCTION_EQUIPMENT\", \"QUALITY_EQUIPMENT\",\n      \"EHS_EQUIPMENT\", \"CONSTRUCTION\"\n    ];\n    if (equipmentCategories.includes(ctx.data.purchaseCategory)\n        || ctx.data.involvesConstruction) {\n      if (!String(ctx.data.installationLocation || \"\").trim()) {\n        return { valid: false, message: \"設備或工程請購必須填寫安裝／使用地點\" };\n      }\n      if (!String(ctx.data.technicalSpecification || \"\").trim()) {\n        return { valid: false, message: \"設備或工程請購必須填寫技術規格\" };\n      }\n      if (!String(ctx.data.acceptanceCriteria || \"\").trim()) {\n        return { valid: false, message: \"設備或工程請購必須填寫驗收標準\" };\n      }\n    }\n    if (ctx.data.expenseType === \"CAPEX\") {\n      if (!String(ctx.data.investmentReason || \"\").trim()) {\n        return { valid: false, message: \"CAPEX 必須填寫投資原因\" };\n      }\n      if (!String(ctx.data.expectedBenefit || \"\").trim()) {\n        return { valid: false, message: \"CAPEX 必須填寫預期效益\" };\n      }\n    }\n    await ctx.redraw();\n    return true;\n  }\n};','9eb767e747d3a206f06cfeff6615b274c67ef5eb172b58394fb75457a10258db','admin','2026-08-17 20:09:56.743','SYSTEM','2026-08-12 21:06:17.343','admin','2026-08-17 20:32:52.105');
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
  `PROVIDER_MESSAGE_ID` varchar(64) DEFAULT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_NOTIFICATION_ID` (`TENANT_ID`,`NOTIFICATION_ID`),
  KEY `IDX_FM_NOTIFICATION_DELIVERY` (`DELIVERY_STATUS`,`NEXT_RETRY_DATE`),
  KEY `IDX_FM_NOTIFICATION_USER` (`TENANT_ID`,`RECIPIENT_ACCOUNT`,`READ_DATE`,`CDATE`),
  KEY `IDX_FM_NOTIFICATION_PROVIDER` (`CHANNEL_TYPE`,`DELIVERY_STATUS`,`PROVIDER_MESSAGE_ID`),
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
('2d20b28f-938f-11f1-a8f5-005056c00001','A01','2d20b294-938f-11f1-a8f5-005056c00001','3e5f4331-6ed9-4fe4-80ae-6aebc9b27749','2d1ffa8d-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b372-938f-11f1-a8f5-005056c00001','A01','2d20b374-938f-11f1-a8f5-005056c00001','b7ff38ab-98a8-44d0-8318-85145e9e5b57','2d1ffc77-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b3fb-938f-11f1-a8f5-005056c00001','A01','2d20b3fc-938f-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','2d1ffd09-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b47a-938f-11f1-a8f5-005056c00001','A01','2d20b47b-938f-11f1-a8f5-005056c00001','11e2a4e7-30a4-4ced-8454-9753ce4a1b60','2d1ffd8a-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b4fc-938f-11f1-a8f5-005056c00001','A01','2d20b4fd-938f-11f1-a8f5-005056c00001','92f87544-d491-4a96-a8d3-425795376136','2d1ffe20-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b582-938f-11f1-a8f5-005056c00001','A01','2d20b583-938f-11f1-a8f5-005056c00001','2e50d437-f16d-4a63-8a6b-69e6bf564e7e','2d1ffea3-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b5fb-938f-11f1-a8f5-005056c00001','A01','2d20b5fc-938f-11f1-a8f5-005056c00001','f092bb40-d87f-407b-9b12-2221470489a3','2d1fff2d-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b676-938f-11f1-a8f5-005056c00001','A01','2d20b677-938f-11f1-a8f5-005056c00001','e2043fd6-978c-4341-a1fe-7751fecb9b2b','2d1fffab-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b6f5-938f-11f1-a8f5-005056c00001','A01','2d20b6f6-938f-11f1-a8f5-005056c00001','4f4a3de9-3b13-44f5-a783-19506696d7d7','2d200038-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b7ef-938f-11f1-a8f5-005056c00001','A01','2d20b7f0-938f-11f1-a8f5-005056c00001','9c2c3ed6-2c5a-4678-a585-d8d6aef6d2c7','2d200182-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b874-938f-11f1-a8f5-005056c00001','A01','2d20b875-938f-11f1-a8f5-005056c00001','c44c3e2c-a3e0-4e7f-ba72-994f3d11d30e','2d200205-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b8ec-938f-11f1-a8f5-005056c00001','A01','2d20b8ed-938f-11f1-a8f5-005056c00001','84ada4ab-426a-4b15-a2c0-e31676c5b183','2d2002a1-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20b989-938f-11f1-a8f5-005056c00001','A01','2d20b98a-938f-11f1-a8f5-005056c00001','210f59af-a6a8-4133-adda-c927af8d69c2','2d200324-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20ba07-938f-11f1-a8f5-005056c00001','A01','2d20ba08-938f-11f1-a8f5-005056c00001','ada00d6d-df68-4c9b-84d0-28b8ee8c0f58','2d2003e6-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20ba87-938f-11f1-a8f5-005056c00001','A01','2d20ba88-938f-11f1-a8f5-005056c00001','c7db44c2-4d03-4eb0-b3be-2d569c3965bb','2d20046b-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20bb0e-938f-11f1-a8f5-005056c00001','A01','2d20bb0f-938f-11f1-a8f5-005056c00001','e6b5fc7c-25ef-4f4a-abe6-bb19e7f9e14d','2d2004fd-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20bb95-938f-11f1-a8f5-005056c00001','A01','2d20bb96-938f-11f1-a8f5-005056c00001','5b95eafe-ce3d-4b83-82d5-4ac49808bf0f','2d200582-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20bc2a-938f-11f1-a8f5-005056c00001','A01','2d20bc2b-938f-11f1-a8f5-005056c00001','60f6bfc3-2436-4002-a152-db891073629d','2d20060a-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20bca7-938f-11f1-a8f5-005056c00001','A01','2d20bca8-938f-11f1-a8f5-005056c00001','75b2cf0e-c377-4261-b0e6-74295af900e1','2d200692-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20bd93-938f-11f1-a8f5-005056c00001','A01','2d20bd94-938f-11f1-a8f5-005056c00001','087af37e-77ff-4480-8f3e-0abf9c844804','2d200771-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20be17-938f-11f1-a8f5-005056c00001','A01','2d20be18-938f-11f1-a8f5-005056c00001','650841c6-d104-46da-804d-b7eb45cfbc67','2d2007ff-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20be91-938f-11f1-a8f5-005056c00001','A01','2d20be92-938f-11f1-a8f5-005056c00001','f27a7a23-bea4-4722-8be7-59beb4939d1a','2d200888-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20bf5a-938f-11f1-a8f5-005056c00001','A01','2d20bf5b-938f-11f1-a8f5-005056c00001','d4e31f21-45fd-4a05-8272-d60e7a0688cb','2d2009bc-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20bfd0-938f-11f1-a8f5-005056c00001','A01','2d20bfd1-938f-11f1-a8f5-005056c00001','e42fa17a-18a6-41e2-b15e-a7f77ad24e03','2d200a5a-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c059-938f-11f1-a8f5-005056c00001','A01','2d20c05a-938f-11f1-a8f5-005056c00001','bb772e13-7d41-41fc-88a0-a7dbc9b6f743','2d200ad4-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c0e9-938f-11f1-a8f5-005056c00001','A01','2d20c0ea-938f-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','2d200b51-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c16f-938f-11f1-a8f5-005056c00001','A01','2d20c170-938f-11f1-a8f5-005056c00001','f5c52e35-64a9-451b-978f-fe8410daecc8','2d200bf2-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c1f8-938f-11f1-a8f5-005056c00001','A01','2d20c1f9-938f-11f1-a8f5-005056c00001','6dfafa1c-4602-41bf-ac74-0ce1eebb63c1','2d200c87-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c27c-938f-11f1-a8f5-005056c00001','A01','2d20c27d-938f-11f1-a8f5-005056c00001','cd35e7e0-8d1f-4f0b-bac9-2fd7e66777b8','2d200d24-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c2f9-938f-11f1-a8f5-005056c00001','A01','2d20c2fa-938f-11f1-a8f5-005056c00001','e0d58477-af58-4cb0-a2c7-ea9429410572','2d200db8-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c382-938f-11f1-a8f5-005056c00001','A01','2d20c383-938f-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','2d200e47-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d20c41a-938f-11f1-a8f5-005056c00001','A01','2d20c41b-938f-11f1-a8f5-005056c00001','5bf5579b-bf06-4e05-a039-dab24dd48846','2d200ecd-938f-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:10:41.000',NULL,NULL),
('b766bc47-939e-11f1-a8f5-005056c00001','A01','b766bc4f-939e-11f1-a8f5-005056c00001','92a905e6-4212-4aa4-ba07-1a2db0f4e39d','ac959c96-9398-11f1-a8f5-005056c00001','DEPUTY_HEAD',200,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','部門副理／副主管','admin','2026-08-09 11:01:56.205',NULL,NULL),
('b766bf52-939e-11f1-a8f5-005056c00001','A01','b766bf54-939e-11f1-a8f5-005056c00001','7f41e8b3-cace-419e-93bf-c2b78da807be','ac95a025-9398-11f1-a8f5-005056c00001','DEPUTY_HEAD',200,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','部門副理／副主管','admin','2026-08-09 11:01:56.205',NULL,NULL),
('b766c1b8-939e-11f1-a8f5-005056c00001','A01','b766c1b9-939e-11f1-a8f5-005056c00001','db43606f-64d2-4304-8850-fbd9d8956d1f','ac95c92d-9398-11f1-a8f5-005056c00001','DEPUTY_HEAD',200,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','部門副理／副主管','admin','2026-08-09 11:01:56.205',NULL,NULL),
('ce2a816d-8e3c-11f1-a6ca-8b1822d4d94c','A01','d85ece41-c37e-44a6-8d3f-b16ebbecce01','121bbf1f-9fde-499d-95a6-b4d59645ccb2','8bf5c5c3-809c-45c4-9a43-9e97696a3845','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000',NULL,'','admin','2026-08-02 14:38:43.328',NULL,NULL),
('f751893a-938e-11f1-a8f5-005056c00001','A01','f7518965-938e-11f1-a8f5-005056c00001','8560d228-540d-4e8a-8901-e21a0e3769cd','f750827a-938e-11f1-a8f5-005056c00001','HEAD',100,'ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','組織主管測試資料','admin','2026-08-09 09:09:11.000',NULL,NULL);
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
  `DOCUMENT_TYPE` varchar(50) DEFAULT NULL,
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
  KEY `IDX_FM_PD_DOCUMENT_TYPE` (`TENANT_ID`,`DOCUMENT_TYPE`),
  CONSTRAINT `CK_FM_PD_STATUS` CHECK (`STATUS` in ('DRAFT','PUBLISHED','INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_process_def`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_process_def` WRITE;
/*!40000 ALTER TABLE `fm_process_def` DISABLE KEYS */;
INSERT INTO `fm_process_def` VALUES
('b68785e3-97ec-11f1-a6a7-005056c00001','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986','FM_PURCHASE_APPROVAL','請購簽核流程','PURCHASE','PURCHASE_REQUEST',1,'DRAFT','請購單類別與風險專業會簽、採購、財務及金額核決流程草稿。','admin','2026-08-14 22:30:35.560','admin','2026-08-17 21:36:19.256');
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
  `DOCUMENT_NUMBER` varchar(100) DEFAULT NULL,
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
  UNIQUE KEY `UK_FM_PI_DOC_NO` (`TENANT_ID`,`DOCUMENT_NUMBER`),
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
INSERT INTO `fm_process_start_policy` VALUES
('b68901b7-97ec-11f1-a6a7-005056c00001','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,1,'ALL',NULL,'Y','admin','2026-08-14 22:30:35.570',NULL,NULL);
/*!40000 ALTER TABLE `fm_process_start_policy` ENABLE KEYS */;
UNLOCK TABLES;
COMMIT;
SET AUTOCOMMIT=@OLD_AUTOCOMMIT;

--
-- Table structure for table `fm_process_start_proxy`
--

DROP TABLE IF EXISTS `fm_process_start_proxy`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `fm_process_start_proxy` (
  `OID` char(36) NOT NULL,
  `TENANT_ID` varchar(36) NOT NULL,
  `START_PROXY_ID` varchar(36) NOT NULL,
  `PRINCIPAL_ACCOUNT` varchar(24) NOT NULL,
  `PROXY_ACCOUNT` varchar(24) NOT NULL,
  `SCOPE_TYPE` varchar(20) NOT NULL DEFAULT 'ALL',
  `SCOPE_REF_ID` varchar(36) DEFAULT NULL,
  `STATUS` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `EFFECTIVE_FROM` datetime(3) NOT NULL,
  `EFFECTIVE_TO` datetime(3) NOT NULL,
  `REASON` varchar(500) NOT NULL,
  `CUSERID` varchar(24) NOT NULL,
  `CDATE` datetime(3) NOT NULL,
  `UUSERID` varchar(24) DEFAULT NULL,
  `UDATE` datetime(3) DEFAULT NULL,
  PRIMARY KEY (`OID`),
  UNIQUE KEY `UK_FM_START_PROXY_ID` (`TENANT_ID`,`START_PROXY_ID`),
  KEY `IDX_FM_START_PROXY_RESOLVE` (`TENANT_ID`,`PRINCIPAL_ACCOUNT`,`PROXY_ACCOUNT`,`STATUS`,`EFFECTIVE_FROM`,`EFFECTIVE_TO`),
  CONSTRAINT `CK_FM_START_PROXY_ACCOUNT` CHECK (`PRINCIPAL_ACCOUNT` <> `PROXY_ACCOUNT`),
  CONSTRAINT `CK_FM_START_PROXY_SCOPE` CHECK (`SCOPE_TYPE` in ('ALL','PROCESS')),
  CONSTRAINT `CK_FM_START_PROXY_STATUS` CHECK (`STATUS` in ('ACTIVE','INACTIVE')),
  CONSTRAINT `CK_FM_START_PROXY_DATE` CHECK (`EFFECTIVE_TO` > `EFFECTIVE_FROM`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_process_start_proxy`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_process_start_proxy` WRITE;
/*!40000 ALTER TABLE `fm_process_start_proxy` DISABLE KEYS */;
/*!40000 ALTER TABLE `fm_process_start_proxy` ENABLE KEYS */;
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
INSERT INTO `fm_process_version` VALUES
('b687a2de-97ec-11f1-a6a7-005056c00001','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'DRAFT','<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<definitions xmlns=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:omgdc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:omgdi=\"http://www.omg.org/spec/DD/20100524/DI\" targetNamespace=\"FlowMint\">\n  <process id=\"FM_PURCHASE_APPROVAL\" name=\"請購簽核流程\" isExecutable=\"true\">\n    <startEvent id=\"start\" name=\"開始\" />\n    <userTask id=\"departmentApproval\" name=\"申請部門主管簽核\" />\n    <inclusiveGateway id=\"professionalReviewSplit\" name=\"類別與風險分流\" default=\"flow_professional_default\" />\n    <userTask id=\"processReview\" name=\"製程審查\" />\n    <userTask id=\"productionEquipmentReview\" name=\"設備工程審查\" />\n    <userTask id=\"itReview\" name=\"資訊與資安審查\" />\n    <userTask id=\"qualityReview\" name=\"品質與檢測審查\" />\n    <userTask id=\"safetyReview\" name=\"工安審查\" />\n    <userTask id=\"environmentReview\" name=\"環保審查\" />\n    <userTask id=\"generalAffairsReview\" name=\"總務審查\" />\n    <inclusiveGateway id=\"professionalReviewJoin\" name=\"專業審查匯流\" />\n    <userTask id=\"purchaseReview\" name=\"採購部商務審查\" />\n    <userTask id=\"financeApproval\" name=\"財務與預算審查\" />\n    <exclusiveGateway id=\"gmGateway\" name=\"是否需總經理核決\" default=\"flow_gm_skip\" />\n    <userTask id=\"generalManagerApproval\" name=\"總經理核准\" />\n    <exclusiveGateway id=\"chairmanGateway\" name=\"是否需董事長核決\" default=\"flow_chairman_skip\" />\n    <userTask id=\"chairmanApproval\" name=\"董事長核准\" />\n    <endEvent id=\"end\" name=\"請購核准\" />\n    <sequenceFlow id=\"flow_start_department\" sourceRef=\"start\" targetRef=\"departmentApproval\" />\n    <sequenceFlow id=\"flow_department_split\" sourceRef=\"departmentApproval\" targetRef=\"professionalReviewSplit\" />\n    <sequenceFlow id=\"flow_to_process\" name=\"生產設備：製程審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"processReview\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.purchaseCategory == \"PRODUCTION_EQUIPMENT\"}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_to_equipment\" name=\"設備／工程審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"productionEquipmentReview\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.purchaseCategory == \"PRODUCTION_EQUIPMENT\" || flowmintFormData.purchaseCategory == \"QUALITY_EQUIPMENT\" || flowmintFormData.purchaseCategory == \"EHS_EQUIPMENT\" || flowmintFormData.purchaseCategory == \"CONSTRUCTION\" || flowmintFormData.involvesConstruction == true}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_to_it\" name=\"資訊系統／資安審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"itReview\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.purchaseCategory == \"IT_EQUIPMENT\" || flowmintFormData.purchaseCategory == \"SOFTWARE_SERVICE\" || flowmintFormData.involvesInformationSystem == true || flowmintFormData.involvesCompanyData == true || flowmintFormData.involvesPersonalData == true}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_to_quality\" name=\"品質設備／原物料審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"qualityReview\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.purchaseCategory == \"QUALITY_EQUIPMENT\" || flowmintFormData.purchaseCategory == \"RAW_MATERIAL\"}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_to_safety\" name=\"工程／工安風險審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"safetyReview\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.purchaseCategory == \"EHS_EQUIPMENT\" || flowmintFormData.purchaseCategory == \"CONSTRUCTION\" || flowmintFormData.involvesConstruction == true || flowmintFormData.involvesEhsRisk == true}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_to_environment\" name=\"環保許可審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"environmentReview\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.purchaseCategory == \"EHS_EQUIPMENT\" || flowmintFormData.involvesEnvironmentalPermit == true}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_to_general\" name=\"一般／總務採購審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"generalAffairsReview\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.purchaseCategory == \"OFFICE_SUPPLIES\" || flowmintFormData.purchaseCategory == \"PROFESSIONAL_SERVICE\" || flowmintFormData.purchaseCategory == \"GENERAL\" || flowmintFormData.purchaseCategory == \"OTHER\"}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_professional_default\" name=\"無需專業審查\" sourceRef=\"professionalReviewSplit\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_process_join\" sourceRef=\"processReview\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_equipment_join\" sourceRef=\"productionEquipmentReview\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_it_join\" sourceRef=\"itReview\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_quality_join\" sourceRef=\"qualityReview\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_safety_join\" sourceRef=\"safetyReview\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_environment_join\" sourceRef=\"environmentReview\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_general_join\" sourceRef=\"generalAffairsReview\" targetRef=\"professionalReviewJoin\" />\n    <sequenceFlow id=\"flow_join_purchase\" sourceRef=\"professionalReviewJoin\" targetRef=\"purchaseReview\" />\n    <sequenceFlow id=\"flow_purchase_finance\" sourceRef=\"purchaseReview\" targetRef=\"financeApproval\" />\n    <sequenceFlow id=\"flow_finance_gm_gateway\" sourceRef=\"financeApproval\" targetRef=\"gmGateway\" />\n    <sequenceFlow id=\"flow_to_gm\" name=\"超過 30 萬：總經理核決\" sourceRef=\"gmGateway\" targetRef=\"generalManagerApproval\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.projectTotalAmount &gt; 300000}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_gm_skip\" name=\"30 萬以下：直接核准\" sourceRef=\"gmGateway\" targetRef=\"end\" />\n    <sequenceFlow id=\"flow_gm_chairman_gateway\" sourceRef=\"generalManagerApproval\" targetRef=\"chairmanGateway\" />\n    <sequenceFlow id=\"flow_to_chairman\" name=\"超過 50 萬：董事長核決\" sourceRef=\"chairmanGateway\" targetRef=\"chairmanApproval\">\n      <conditionExpression xsi:type=\"tFormalExpression\">${flowmintFormData.projectTotalAmount &gt; 500000}</conditionExpression>\n    </sequenceFlow>\n    <sequenceFlow id=\"flow_chairman_skip\" name=\"50 萬以下：總經理核准後結束\" sourceRef=\"chairmanGateway\" targetRef=\"end\" />\n    <sequenceFlow id=\"flow_chairman_end\" sourceRef=\"chairmanApproval\" targetRef=\"end\" />\n  </process>\n  <bpmndi:BPMNDiagram id=\"FM_PURCHASE_APPROVAL_diagram\">\n    <bpmndi:BPMNPlane id=\"FM_PURCHASE_APPROVAL_plane\" bpmnElement=\"FM_PURCHASE_APPROVAL\">\n      <bpmndi:BPMNShape id=\"start_di\" bpmnElement=\"start\">\n        <omgdc:Bounds x=\"100\" y=\"322\" width=\"36\" height=\"36\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"departmentApproval_di\" bpmnElement=\"departmentApproval\">\n        <omgdc:Bounds x=\"190\" y=\"300\" width=\"120\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"professionalReviewSplit_di\" bpmnElement=\"professionalReviewSplit\">\n        <omgdc:Bounds x=\"370\" y=\"315\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"356\" y=\"375\" width=\"77\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"professionalReviewJoin_di\" bpmnElement=\"professionalReviewJoin\">\n        <omgdc:Bounds x=\"670\" y=\"315\" width=\"50\" height=\"50\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"662\" y=\"375\" width=\"66\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"purchaseReview_di\" bpmnElement=\"purchaseReview\">\n        <omgdc:Bounds x=\"780\" y=\"300\" width=\"120\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"financeApproval_di\" bpmnElement=\"financeApproval\">\n        <omgdc:Bounds x=\"960\" y=\"300\" width=\"120\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"gmGateway_di\" bpmnElement=\"gmGateway\" isMarkerVisible=\"true\">\n        <omgdc:Bounds x=\"1140\" y=\"315\" width=\"50\" height=\"50\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"generalManagerApproval_di\" bpmnElement=\"generalManagerApproval\">\n        <omgdc:Bounds x=\"1250\" y=\"300\" width=\"120\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"chairmanGateway_di\" bpmnElement=\"chairmanGateway\" isMarkerVisible=\"true\">\n        <omgdc:Bounds x=\"1430\" y=\"315\" width=\"50\" height=\"50\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"chairmanApproval_di\" bpmnElement=\"chairmanApproval\">\n        <omgdc:Bounds x=\"1540\" y=\"300\" width=\"120\" height=\"80\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"end_di\" bpmnElement=\"end\">\n        <omgdc:Bounds x=\"1730\" y=\"322\" width=\"36\" height=\"36\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"processReview_di\" bpmnElement=\"processReview\">\n        <omgdc:Bounds x=\"480\" y=\"-135\" width=\"120\" height=\"70\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"productionEquipmentReview_di\" bpmnElement=\"productionEquipmentReview\">\n        <omgdc:Bounds x=\"480\" y=\"-35\" width=\"120\" height=\"70\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"generalAffairsReview_di\" bpmnElement=\"generalAffairsReview\">\n        <omgdc:Bounds x=\"470\" y=\"795\" width=\"120\" height=\"70\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"environmentReview_di\" bpmnElement=\"environmentReview\">\n        <omgdc:Bounds x=\"470\" y=\"665\" width=\"120\" height=\"70\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"safetyReview_di\" bpmnElement=\"safetyReview\">\n        <omgdc:Bounds x=\"480\" y=\"535\" width=\"120\" height=\"70\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"itReview_di\" bpmnElement=\"itReview\">\n        <omgdc:Bounds x=\"480\" y=\"105\" width=\"120\" height=\"70\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNShape id=\"qualityReview_di\" bpmnElement=\"qualityReview\">\n        <omgdc:Bounds x=\"480\" y=\"-295\" width=\"120\" height=\"70\" />\n      </bpmndi:BPMNShape>\n      <bpmndi:BPMNEdge id=\"Edge_flow_start_department\" bpmnElement=\"flow_start_department\">\n        <omgdi:waypoint x=\"136\" y=\"340\" />\n        <omgdi:waypoint x=\"190\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_department_split\" bpmnElement=\"flow_department_split\">\n        <omgdi:waypoint x=\"310\" y=\"340\" />\n        <omgdi:waypoint x=\"370\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_process\" bpmnElement=\"flow_to_process\">\n        <omgdi:waypoint x=\"420\" y=\"340\" />\n        <omgdi:waypoint x=\"420\" y=\"-100\" />\n        <omgdi:waypoint x=\"480\" y=\"-100\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"403\" y=\"-124\" width=\"89\" height=\"53\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_equipment\" bpmnElement=\"flow_to_equipment\">\n        <omgdi:waypoint x=\"420\" y=\"340\" />\n        <omgdi:waypoint x=\"420\" y=\"0\" />\n        <omgdi:waypoint x=\"480\" y=\"0\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"395\" y=\"-42\" width=\"89\" height=\"106\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_it\" bpmnElement=\"flow_to_it\">\n        <omgdi:waypoint x=\"420\" y=\"340\" />\n        <omgdi:waypoint x=\"420\" y=\"140\" />\n        <omgdi:waypoint x=\"480\" y=\"140\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"391\" y=\"105\" width=\"88\" height=\"106\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_quality\" bpmnElement=\"flow_to_quality\">\n        <omgdi:waypoint x=\"395\" y=\"315\" />\n        <omgdi:waypoint x=\"395\" y=\"-275\" />\n        <omgdi:waypoint x=\"480\" y=\"-275\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"376\" y=\"-36\" width=\"89\" height=\"93\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_safety\" bpmnElement=\"flow_to_safety\">\n        <omgdi:waypoint x=\"420\" y=\"340\" />\n        <omgdi:waypoint x=\"420\" y=\"570\" />\n        <omgdi:waypoint x=\"480\" y=\"570\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"424\" y=\"538\" width=\"87\" height=\"106\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_environment\" bpmnElement=\"flow_to_environment\">\n        <omgdi:waypoint x=\"420\" y=\"340\" />\n        <omgdi:waypoint x=\"420\" y=\"700\" />\n        <omgdi:waypoint x=\"470\" y=\"700\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"408\" y=\"668\" width=\"86\" height=\"80\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_general\" bpmnElement=\"flow_to_general\">\n        <omgdi:waypoint x=\"420\" y=\"340\" />\n        <omgdi:waypoint x=\"420\" y=\"830\" />\n        <omgdi:waypoint x=\"470\" y=\"830\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"410\" y=\"785\" width=\"84\" height=\"106\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_professional_default\" bpmnElement=\"flow_professional_default\">\n        <omgdi:waypoint x=\"420\" y=\"340\" />\n        <omgdi:waypoint x=\"670\" y=\"340\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"518\" y=\"322\" width=\"55\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_process_join\" bpmnElement=\"flow_process_join\">\n        <omgdi:waypoint x=\"600\" y=\"-100\" />\n        <omgdi:waypoint x=\"670\" y=\"-100\" />\n        <omgdi:waypoint x=\"670\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_equipment_join\" bpmnElement=\"flow_equipment_join\">\n        <omgdi:waypoint x=\"600\" y=\"0\" />\n        <omgdi:waypoint x=\"670\" y=\"0\" />\n        <omgdi:waypoint x=\"670\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_it_join\" bpmnElement=\"flow_it_join\">\n        <omgdi:waypoint x=\"600\" y=\"140\" />\n        <omgdi:waypoint x=\"670\" y=\"140\" />\n        <omgdi:waypoint x=\"670\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_quality_join\" bpmnElement=\"flow_quality_join\">\n        <omgdi:waypoint x=\"600\" y=\"-273\" />\n        <omgdi:waypoint x=\"695\" y=\"-273\" />\n        <omgdi:waypoint x=\"695\" y=\"315\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_safety_join\" bpmnElement=\"flow_safety_join\">\n        <omgdi:waypoint x=\"600\" y=\"570\" />\n        <omgdi:waypoint x=\"670\" y=\"570\" />\n        <omgdi:waypoint x=\"670\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_environment_join\" bpmnElement=\"flow_environment_join\">\n        <omgdi:waypoint x=\"590\" y=\"700\" />\n        <omgdi:waypoint x=\"670\" y=\"700\" />\n        <omgdi:waypoint x=\"670\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_general_join\" bpmnElement=\"flow_general_join\">\n        <omgdi:waypoint x=\"590\" y=\"830\" />\n        <omgdi:waypoint x=\"670\" y=\"830\" />\n        <omgdi:waypoint x=\"670\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_join_purchase\" bpmnElement=\"flow_join_purchase\">\n        <omgdi:waypoint x=\"720\" y=\"340\" />\n        <omgdi:waypoint x=\"780\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_purchase_finance\" bpmnElement=\"flow_purchase_finance\">\n        <omgdi:waypoint x=\"900\" y=\"340\" />\n        <omgdi:waypoint x=\"960\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_finance_gm_gateway\" bpmnElement=\"flow_finance_gm_gateway\">\n        <omgdi:waypoint x=\"1080\" y=\"340\" />\n        <omgdi:waypoint x=\"1140\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_gm\" bpmnElement=\"flow_to_gm\">\n        <omgdi:waypoint x=\"1190\" y=\"340\" />\n        <omgdi:waypoint x=\"1250\" y=\"340\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"1179\" y=\"322\" width=\"83\" height=\"27\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_gm_skip\" bpmnElement=\"flow_gm_skip\">\n        <omgdi:waypoint x=\"1190\" y=\"340\" />\n        <omgdi:waypoint x=\"1190\" y=\"250\" />\n        <omgdi:waypoint x=\"1730\" y=\"250\" />\n        <omgdi:waypoint x=\"1730\" y=\"340\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"1433\" y=\"232\" width=\"55\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_gm_chairman_gateway\" bpmnElement=\"flow_gm_chairman_gateway\">\n        <omgdi:waypoint x=\"1370\" y=\"340\" />\n        <omgdi:waypoint x=\"1430\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_to_chairman\" bpmnElement=\"flow_to_chairman\">\n        <omgdi:waypoint x=\"1480\" y=\"340\" />\n        <omgdi:waypoint x=\"1540\" y=\"340\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"1469\" y=\"322\" width=\"83\" height=\"27\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_chairman_skip\" bpmnElement=\"flow_chairman_skip\">\n        <omgdi:waypoint x=\"1480\" y=\"340\" />\n        <omgdi:waypoint x=\"1480\" y=\"250\" />\n        <omgdi:waypoint x=\"1730\" y=\"250\" />\n        <omgdi:waypoint x=\"1730\" y=\"340\" />\n        <bpmndi:BPMNLabel>\n          <omgdc:Bounds x=\"1578\" y=\"232\" width=\"55\" height=\"14\" />\n        </bpmndi:BPMNLabel>\n      </bpmndi:BPMNEdge>\n      <bpmndi:BPMNEdge id=\"Edge_flow_chairman_end\" bpmnElement=\"flow_chairman_end\">\n        <omgdi:waypoint x=\"1660\" y=\"340\" />\n        <omgdi:waypoint x=\"1730\" y=\"340\" />\n      </bpmndi:BPMNEdge>\n    </bpmndi:BPMNPlane>\n  </bpmndi:BPMNDiagram>\n</definitions>\n','31591ab21badad438a4d864ad78b3ddaf07ae33905297e118c0196507c3ce282',NULL,NULL,NULL,NULL,'admin','2026-08-14 22:30:35.561','codex','2026-08-18 12:35:03.368');
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
INSERT INTO `fm_task_assignment_rule` VALUES
('a120c5eb-9a40-11f1-b372-299ecbd4f950','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'chairmanApproval',1,'APPROVAL_AUTHORITY','{\"approvalAuthorityId\": \"eec856db-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.706',NULL,NULL),
('a121622c-9a40-11f1-b372-1330610907ef','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'departmentApproval',1,'INITIATOR_ORG_HEAD','{}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.711',NULL,NULL),
('a121fe6d-9a40-11f1-b372-4d5c9f2bd60e','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'environmentReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede3fe-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.714',NULL,NULL),
('a1224c8e-9a40-11f1-b372-b11eafb3430b','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'financeApproval',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede439-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.716',NULL,NULL),
('a1229aaf-9a40-11f1-b372-2ba3dff56c17','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'generalAffairsReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede40c-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.719',NULL,NULL),
('a1230fe0-9a40-11f1-b372-3f6240b685fe','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'generalManagerApproval',1,'APPROVAL_AUTHORITY','{\"approvalAuthorityId\": \"eec85023-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.721',NULL,NULL),
('a1235e01-9a40-11f1-b372-554cdd74248d','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'itReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede3d0-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.723',NULL,NULL),
('a123ac22-9a40-11f1-b372-83b52ac5df61','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'processReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede333-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.725',NULL,NULL),
('a125a7f3-9a40-11f1-b372-dba11cc8b146','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'productionEquipmentReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede3c0-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.738',NULL,NULL),
('a125f614-9a40-11f1-b372-d14900c4d59d','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'purchaseReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede42a-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.740',NULL,NULL),
('a1264435-9a40-11f1-b372-cf39cbe33277','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'qualityReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede3df-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.743',NULL,NULL),
('a126b966-9a40-11f1-b372-27d434f5211f','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'safetyReview',1,'APPROVAL_GROUP','{\"approvalGroupId\": \"4cede3ee-97eb-11f1-a6a7-005056c00001\"}',NULL,100,'ACTIVE','admin','2026-08-17 21:36:19.746',NULL,NULL);
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
  UNIQUE KEY `UK_FM_TFR_TASK` (`TENANT_ID`,`PROCESS_DEF_ID`,`PROCESS_VERSION_NO`,`TASK_DEF_KEY`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_form_rule`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_form_rule` WRITE;
/*!40000 ALTER TABLE `fm_task_form_rule` DISABLE KEYS */;
INSERT INTO `fm_task_form_rule` VALUES
('0178c248-200c-415a-a425-1417acc23c8e','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'generalManagerApproval','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.638',NULL,NULL),
('0371e991-fd86-438a-8c74-33ca44dff160','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'environmentReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.637',NULL,NULL),
('39c00c60-f2c9-4f19-97c6-730f9fd14e2c','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'productionEquipmentReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.638',NULL,NULL),
('4ce43021-31a5-4ef7-904d-9b5a9f621ed5','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'purchaseReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.638',NULL,NULL),
('57f0c6ec-30e9-4573-a7c0-71c09b9d3675','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'generalAffairsReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.638',NULL,NULL),
('58d2a93c-80fa-4512-9635-b92d999d707c','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'safetyReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.639',NULL,NULL),
('757dcd59-37f2-4c6f-a446-30f0b4a57091','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'departmentApproval','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.637',NULL,NULL),
('7f0ad5ad-9f1a-4bde-9451-1425a1ac3dfa','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'chairmanApproval','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.637',NULL,NULL),
('a56736bf-d5cb-4b98-bc35-24f599d11bda','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'qualityReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.639',NULL,NULL),
('b1eaf7e4-2982-43bf-86bd-9141a05afcd4','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'processReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.638',NULL,NULL),
('bc4b5639-3a69-458c-a9ba-78c4608ab2aa','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'financeApproval','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.637',NULL,NULL),
('c6be1608-85ed-4329-b618-083639fc36ed','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'itReview','2d939f65-b78e-454d-a48d-cdd538222d96',1,'{\"default\": \"READ\", \"fields\": {}}','admin','2026-08-17 21:36:19.638',NULL,NULL);
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
  `DUE_HOURS` int(11) DEFAULT NULL,
  `REMINDER_BEFORE_HOURS` int(11) DEFAULT NULL,
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
  CONSTRAINT `CK_FM_TP_COMMENT` CHECK (`COMMENT_REQUIRED` in ('NEVER','ALWAYS','ON_REJECT_RETURN')),
  CONSTRAINT `CK_FM_TP_DUE_HOURS` CHECK (`DUE_HOURS` is null or `DUE_HOURS` between 1 and 8760),
  CONSTRAINT `CK_FM_TP_REMINDER_HOURS` CHECK (`REMINDER_BEFORE_HOURS` is null or `DUE_HOURS` is not null and `REMINDER_BEFORE_HOURS` >= 0 and `REMINDER_BEFORE_HOURS` < `DUE_HOURS`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fm_task_policy`
--

SET @OLD_AUTOCOMMIT=@@AUTOCOMMIT, @@AUTOCOMMIT=0;
LOCK TABLES `fm_task_policy` WRITE;
/*!40000 ALTER TABLE `fm_task_policy` DISABLE KEYS */;
INSERT INTO `fm_task_policy` VALUES
('a11ad26f-9a40-11f1-b372-8fa4035d7087','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'chairmanApproval','董事長核准','ASSIGNEE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',72,8,'admin','2026-08-17 21:36:19.667',NULL,NULL),
('a11bbcd0-9a40-11f1-b372-5debbfbe70e1','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'departmentApproval','申請部門主管簽核','ASSIGNEE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.673',NULL,NULL),
('a11c0af1-9a40-11f1-b372-492b92ca2aa5','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'environmentReview','環保審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.676',NULL,NULL),
('a11c8022-9a40-11f1-b372-4db4bf3caecc','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'financeApproval','財務與預算審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.679',NULL,NULL),
('a11cf553-9a40-11f1-b372-338bb37c2c71','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'generalAffairsReview','總務審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.681',NULL,NULL),
('a11d4374-9a40-11f1-b372-1b284b42564d','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'generalManagerApproval','總經理核准','ASSIGNEE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',72,8,'admin','2026-08-17 21:36:19.683',NULL,NULL),
('a11db8a5-9a40-11f1-b372-fb8bca27251d','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'itReview','資訊與資安審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.687',NULL,NULL),
('a11e2dd6-9a40-11f1-b372-931b50011e0a','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'processReview','製程審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.689',NULL,NULL),
('a11e7bf7-9a40-11f1-b372-1b5651df7ab3','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'productionEquipmentReview','設備工程審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',72,8,'admin','2026-08-17 21:36:19.692',NULL,NULL),
('a11ef128-9a40-11f1-b372-e92362969f6c','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'purchaseReview','採購部商務審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.695',NULL,NULL),
('a11f8d69-9a40-11f1-b372-9fa3f0f4732e','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'qualityReview','品質與檢測審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.698',NULL,NULL),
('a11fdb8a-9a40-11f1-b372-13ee409fca16','A01','cdbaf0cb-197e-4c42-b03d-402e7dd71986',1,'safetyReview','工安審查','CANDIDATE','SKIP_TO_NEXT','MERGE_CONSECUTIVE','Y','Y','N','N','ON_REJECT_RETURN',48,8,'admin','2026-08-17 21:36:19.701',NULL,NULL);
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
('2d1fb32a-938f-11f1-a8f5-005056c00001','A01','fm00530','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb43b-938f-11f1-a8f5-005056c00001','A01','fm00110','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb4a8-938f-11f1-a8f5-005056c00001','A01','fm00430','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb51c-938f-11f1-a8f5-005056c00001','A01','fm00120','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb57c-938f-11f1-a8f5-005056c00001','A01','fm00600','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb5de-938f-11f1-a8f5-005056c00001','A01','fm00800','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb630-938f-11f1-a8f5-005056c00001','A01','fm00300','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb684-938f-11f1-a8f5-005056c00001','A01','fm00610','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb6d6-938f-11f1-a8f5-005056c00001','A01','fm00310','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb72a-938f-11f1-a8f5-005056c00001','A01','fm00810','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb781-938f-11f1-a8f5-005056c00001','A01','fm00320','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb7db-938f-11f1-a8f5-005056c00001','A01','fm00200','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb839-938f-11f1-a8f5-005056c00001','A01','fm00620','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb8a4-938f-11f1-a8f5-005056c00001','A01','fm00820','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb90a-938f-11f1-a8f5-005056c00001','A01','fm00210','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb965-938f-11f1-a8f5-005056c00001','A01','fm00330','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fb9ba-938f-11f1-a8f5-005056c00001','A01','fm00830','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fba12-938f-11f1-a8f5-005056c00001','A01','fm00630','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fba66-938f-11f1-a8f5-005056c00001','A01','fm00500','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbabc-938f-11f1-a8f5-005056c00001','A01','fm00220','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbb64-938f-11f1-a8f5-005056c00001','A01','fm00900','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbbb9-938f-11f1-a8f5-005056c00001','A01','fm00230','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbc12-938f-11f1-a8f5-005056c00001','A01','fm00400','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbcad-938f-11f1-a8f5-005056c00001','A01','fm00510','Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbd02-938f-11f1-a8f5-005056c00001','A01','fm00910','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbd5a-938f-11f1-a8f5-005056c00001','A01','fm00700','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbdc4-938f-11f1-a8f5-005056c00001','A01','fm00410','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbe2d-938f-11f1-a8f5-005056c00001','A01','fm00520','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbe84-938f-11f1-a8f5-005056c00001','A01','fm00710','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbede-938f-11f1-a8f5-005056c00001','A01','fm00920','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbf32-938f-11f1-a8f5-005056c00001','A01','fm00100','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbf84-938f-11f1-a8f5-005056c00001','A01','fm00420','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('2d1fbfd7-938f-11f1-a8f5-005056c00001','A01','fm00720','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:10:41.000',NULL,NULL),
('ac94863d-9398-11f1-a8f5-005056c00001','A01','fm00530e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94880d-9398-11f1-a8f5-005056c00001','A01','fm00530e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948875-9398-11f1-a8f5-005056c00001','A01','fm00530e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac9488ed-9398-11f1-a8f5-005056c00001','A01','fm00530e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948953-9398-11f1-a8f5-005056c00001','A01','fm00530e05','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948b18-9398-11f1-a8f5-005056c00001','A01','fm00110e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948bfe-9398-11f1-a8f5-005056c00001','A01','fm00430e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948c59-9398-11f1-a8f5-005056c00001','A01','fm00430e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948cad-9398-11f1-a8f5-005056c00001','A01','fm00430e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948d00-9398-11f1-a8f5-005056c00001','A01','fm00430e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948d52-9398-11f1-a8f5-005056c00001','A01','fm00430e05','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948da3-9398-11f1-a8f5-005056c00001','A01','fm00430e06','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948df2-9398-11f1-a8f5-005056c00001','A01','fm00430e07','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948e43-9398-11f1-a8f5-005056c00001','A01','fm00430e08','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948e92-9398-11f1-a8f5-005056c00001','A01','fm00430e09','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948ee3-9398-11f1-a8f5-005056c00001','A01','fm00430e10','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948f34-9398-11f1-a8f5-005056c00001','A01','fm00430e11','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac948fc3-9398-11f1-a8f5-005056c00001','A01','fm00120e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94908f-9398-11f1-a8f5-005056c00001','A01','fm00600e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949103-9398-11f1-a8f5-005056c00001','A01','fm00600e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949179-9398-11f1-a8f5-005056c00001','A01','fm00600e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac9491f1-9398-11f1-a8f5-005056c00001','A01','fm00600e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949287-9398-11f1-a8f5-005056c00001','A01','fm00800e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94932c-9398-11f1-a8f5-005056c00001','A01','fm00300e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac9493d2-9398-11f1-a8f5-005056c00001','A01','fm00610e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949441-9398-11f1-a8f5-005056c00001','A01','fm00610e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac9494b0-9398-11f1-a8f5-005056c00001','A01','fm00610e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949520-9398-11f1-a8f5-005056c00001','A01','fm00610e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac9498df-9398-11f1-a8f5-005056c00001','A01','fm00310e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949989-9398-11f1-a8f5-005056c00001','A01','fm00810e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949a42-9398-11f1-a8f5-005056c00001','A01','fm00320e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949ab8-9398-11f1-a8f5-005056c00001','A01','fm00200e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949b40-9398-11f1-a8f5-005056c00001','A01','fm00620e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949b96-9398-11f1-a8f5-005056c00001','A01','fm00620e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949bfb-9398-11f1-a8f5-005056c00001','A01','fm00620e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949c51-9398-11f1-a8f5-005056c00001','A01','fm00620e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949cb9-9398-11f1-a8f5-005056c00001','A01','fm00820e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949d2b-9398-11f1-a8f5-005056c00001','A01','fm00210e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949d9c-9398-11f1-a8f5-005056c00001','A01','fm00330e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949e79-9398-11f1-a8f5-005056c00001','A01','fm00830e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949ee9-9398-11f1-a8f5-005056c00001','A01','fm00630e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949f3f-9398-11f1-a8f5-005056c00001','A01','fm00630e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949f95-9398-11f1-a8f5-005056c00001','A01','fm00630e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac949feb-9398-11f1-a8f5-005056c00001','A01','fm00630e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a055-9398-11f1-a8f5-005056c00001','A01','fm00500e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a0ab-9398-11f1-a8f5-005056c00001','A01','fm00500e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a111-9398-11f1-a8f5-005056c00001','A01','fm00500e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a167-9398-11f1-a8f5-005056c00001','A01','fm00500e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a1bb-9398-11f1-a8f5-005056c00001','A01','fm00500e05','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a21b-9398-11f1-a8f5-005056c00001','A01','fm00220e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a2c4-9398-11f1-a8f5-005056c00001','A01','fm00900e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a334-9398-11f1-a8f5-005056c00001','A01','fm00230e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a3a3-9398-11f1-a8f5-005056c00001','A01','fm00400e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a3f9-9398-11f1-a8f5-005056c00001','A01','fm00400e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a44e-9398-11f1-a8f5-005056c00001','A01','fm00400e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a4a4-9398-11f1-a8f5-005056c00001','A01','fm00400e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a4f7-9398-11f1-a8f5-005056c00001','A01','fm00400e05','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a547-9398-11f1-a8f5-005056c00001','A01','fm00400e06','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a597-9398-11f1-a8f5-005056c00001','A01','fm00400e07','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a5e7-9398-11f1-a8f5-005056c00001','A01','fm00400e08','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a645-9398-11f1-a8f5-005056c00001','A01','fm00400e09','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a695-9398-11f1-a8f5-005056c00001','A01','fm00400e10','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a6e6-9398-11f1-a8f5-005056c00001','A01','fm00400e11','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a743-9398-11f1-a8f5-005056c00001','A01','fm00002e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a7b5-9398-11f1-a8f5-005056c00001','A01','fm00510e01','Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a80e-9398-11f1-a8f5-005056c00001','A01','fm00510e02','Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a862-9398-11f1-a8f5-005056c00001','A01','fm00510e03','Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a8b7-9398-11f1-a8f5-005056c00001','A01','fm00510e04','Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a90b-9398-11f1-a8f5-005056c00001','A01','fm00510e05','Y','ACTIVE','2026-01-01 00:00:00.000','2026-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a96c-9398-11f1-a8f5-005056c00001','A01','fm00910e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94a9e0-9398-11f1-a8f5-005056c00001','A01','fm00700e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94aa4e-9398-11f1-a8f5-005056c00001','A01','fm00410e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94aab6-9398-11f1-a8f5-005056c00001','A01','fm00410e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ab0b-9398-11f1-a8f5-005056c00001','A01','fm00410e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ab6e-9398-11f1-a8f5-005056c00001','A01','fm00410e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94abc3-9398-11f1-a8f5-005056c00001','A01','fm00410e05','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ac11-9398-11f1-a8f5-005056c00001','A01','fm00410e06','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ac5e-9398-11f1-a8f5-005056c00001','A01','fm00410e07','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94acae-9398-11f1-a8f5-005056c00001','A01','fm00410e08','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94acfd-9398-11f1-a8f5-005056c00001','A01','fm00410e09','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ad4a-9398-11f1-a8f5-005056c00001','A01','fm00410e10','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ad9a-9398-11f1-a8f5-005056c00001','A01','fm00410e11','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ae2e-9398-11f1-a8f5-005056c00001','A01','fm00520e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94aed6-9398-11f1-a8f5-005056c00001','A01','fm00520e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94af40-9398-11f1-a8f5-005056c00001','A01','fm00520e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94af98-9398-11f1-a8f5-005056c00001','A01','fm00520e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b139-9398-11f1-a8f5-005056c00001','A01','fm00520e05','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b1a1-9398-11f1-a8f5-005056c00001','A01','fm00710e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b254-9398-11f1-a8f5-005056c00001','A01','fm00920e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b3c1-9398-11f1-a8f5-005056c00001','A01','fm00100e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b48d-9398-11f1-a8f5-005056c00001','A01','fm00420e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b502-9398-11f1-a8f5-005056c00001','A01','fm00420e02','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b578-9398-11f1-a8f5-005056c00001','A01','fm00420e03','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b5f2-9398-11f1-a8f5-005056c00001','A01','fm00420e04','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b6f2-9398-11f1-a8f5-005056c00001','A01','fm00420e05','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b775-9398-11f1-a8f5-005056c00001','A01','fm00420e06','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b7f5-9398-11f1-a8f5-005056c00001','A01','fm00420e07','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b86e-9398-11f1-a8f5-005056c00001','A01','fm00420e08','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b8e9-9398-11f1-a8f5-005056c00001','A01','fm00420e09','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94b99b-9398-11f1-a8f5-005056c00001','A01','fm00420e10','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ba19-9398-11f1-a8f5-005056c00001','A01','fm00420e11','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('ac94ba9c-9398-11f1-a8f5-005056c00001','A01','fm00720e01','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 10:18:41.070',NULL,NULL),
('b7f39e3d-8ca3-11f1-98fb-892691cae8be','A01','admin','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:50:21.896',NULL,NULL),
('c96fc8af-8ca3-11f1-98fb-57f5db0212b2','A01','tester','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:50:51.230','admin','2026-07-31 14:14:18.542'),
('e4596eb1-8ca3-11f1-98fb-c181205235bd','A01','steven','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:51:36.382',NULL,NULL),
('ec94abd3-8ca3-11f1-98fb-371f71dbf5cc','A01','tiffany','Y','ACTIVE','2026-01-01 00:00:00.000',NULL,'admin','2026-07-31 13:51:50.192',NULL,NULL),
('f7504e6b-938e-11f1-a8f5-005056c00001','A01','fm00002','Y','ACTIVE','2026-01-01 00:00:00.000','2099-12-30 00:00:00.000','admin','2026-08-09 09:09:11.000',NULL,NULL);
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
('2d1f533a-938f-11f1-a8f5-005056c00001','fm00530','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5465-938f-11f1-a8f5-005056c00001','fm00110','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f54e3-938f-11f1-a8f5-005056c00001','fm00430','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f554c-938f-11f1-a8f5-005056c00001','fm00120','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f55b5-938f-11f1-a8f5-005056c00001','fm00600','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5622-938f-11f1-a8f5-005056c00001','fm00800','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5694-938f-11f1-a8f5-005056c00001','fm00300','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f56fa-938f-11f1-a8f5-005056c00001','fm00610','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5762-938f-11f1-a8f5-005056c00001','fm00310','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f57c8-938f-11f1-a8f5-005056c00001','fm00810','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f582e-938f-11f1-a8f5-005056c00001','fm00320','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5896-938f-11f1-a8f5-005056c00001','fm00200','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5909-938f-11f1-a8f5-005056c00001','fm00620','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f596d-938f-11f1-a8f5-005056c00001','fm00820','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f59d4-938f-11f1-a8f5-005056c00001','fm00210','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5a3f-938f-11f1-a8f5-005056c00001','fm00330','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5aa7-938f-11f1-a8f5-005056c00001','fm00830','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5b14-938f-11f1-a8f5-005056c00001','fm00630','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5b81-938f-11f1-a8f5-005056c00001','fm00500','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5bef-938f-11f1-a8f5-005056c00001','fm00220','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5cb6-938f-11f1-a8f5-005056c00001','fm00900','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5d2f-938f-11f1-a8f5-005056c00001','fm00230','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5da2-938f-11f1-a8f5-005056c00001','fm00400','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5e4b-938f-11f1-a8f5-005056c00001','fm00510','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5eba-938f-11f1-a8f5-005056c00001','fm00910','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5f2d-938f-11f1-a8f5-005056c00001','fm00700','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f5f9d-938f-11f1-a8f5-005056c00001','fm00410','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f602a-938f-11f1-a8f5-005056c00001','fm00520','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f60ad-938f-11f1-a8f5-005056c00001','fm00710','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f613a-938f-11f1-a8f5-005056c00001','fm00920','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f61b2-938f-11f1-a8f5-005056c00001','fm00100','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f622a-938f-11f1-a8f5-005056c00001','fm00420','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('2d1f629a-938f-11f1-a8f5-005056c00001','fm00720','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:10:41',NULL,NULL),
('52cb274e-388d-419f-a81e-67ca599bfb63','steven','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2015-09-11 10:33:53',NULL,NULL),
('9c239d19-3646-41db-b394-d34c5bf34671','tiffany','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2015-09-11 10:15:29',NULL,NULL),
('ac9402f2-9398-11f1-a8f5-005056c00001','fm00530e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9405d2-9398-11f1-a8f5-005056c00001','fm00530e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940634-9398-11f1-a8f5-005056c00001','fm00530e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94068c-9398-11f1-a8f5-005056c00001','fm00530e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9406fe-9398-11f1-a8f5-005056c00001','fm00530e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94076c-9398-11f1-a8f5-005056c00001','fm00110e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94084b-9398-11f1-a8f5-005056c00001','fm00430e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940898-9398-11f1-a8f5-005056c00001','fm00430e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9408df-9398-11f1-a8f5-005056c00001','fm00430e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940927-9398-11f1-a8f5-005056c00001','fm00430e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94096f-9398-11f1-a8f5-005056c00001','fm00430e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9409b5-9398-11f1-a8f5-005056c00001','fm00430e06','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9409fa-9398-11f1-a8f5-005056c00001','fm00430e07','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940a3f-9398-11f1-a8f5-005056c00001','fm00430e08','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940a85-9398-11f1-a8f5-005056c00001','fm00430e09','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940ac9-9398-11f1-a8f5-005056c00001','fm00430e10','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940b0f-9398-11f1-a8f5-005056c00001','fm00430e11','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940b5e-9398-11f1-a8f5-005056c00001','fm00120e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940bc4-9398-11f1-a8f5-005056c00001','fm00600e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940c0c-9398-11f1-a8f5-005056c00001','fm00600e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940c76-9398-11f1-a8f5-005056c00001','fm00600e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940cc7-9398-11f1-a8f5-005056c00001','fm00600e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940d28-9398-11f1-a8f5-005056c00001','fm00800e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940d8f-9398-11f1-a8f5-005056c00001','fm00300e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940df5-9398-11f1-a8f5-005056c00001','fm00610e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940e3f-9398-11f1-a8f5-005056c00001','fm00610e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940e88-9398-11f1-a8f5-005056c00001','fm00610e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940ed1-9398-11f1-a8f5-005056c00001','fm00610e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940f2f-9398-11f1-a8f5-005056c00001','fm00310e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac940fa7-9398-11f1-a8f5-005056c00001','fm00810e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94100c-9398-11f1-a8f5-005056c00001','fm00320e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94106f-9398-11f1-a8f5-005056c00001','fm00200e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9410d2-9398-11f1-a8f5-005056c00001','fm00620e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941272-9398-11f1-a8f5-005056c00001','fm00620e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9412c3-9398-11f1-a8f5-005056c00001','fm00620e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94130b-9398-11f1-a8f5-005056c00001','fm00620e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941369-9398-11f1-a8f5-005056c00001','fm00820e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9413ce-9398-11f1-a8f5-005056c00001','fm00210e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941432-9398-11f1-a8f5-005056c00001','fm00330e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941495-9398-11f1-a8f5-005056c00001','fm00830e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94153d-9398-11f1-a8f5-005056c00001','fm00630e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941587-9398-11f1-a8f5-005056c00001','fm00630e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9415cf-9398-11f1-a8f5-005056c00001','fm00630e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941617-9398-11f1-a8f5-005056c00001','fm00630e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941675-9398-11f1-a8f5-005056c00001','fm00500e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9416cf-9398-11f1-a8f5-005056c00001','fm00500e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94171a-9398-11f1-a8f5-005056c00001','fm00500e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941763-9398-11f1-a8f5-005056c00001','fm00500e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9417ac-9398-11f1-a8f5-005056c00001','fm00500e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941803-9398-11f1-a8f5-005056c00001','fm00220e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9418a0-9398-11f1-a8f5-005056c00001','fm00900e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941902-9398-11f1-a8f5-005056c00001','fm00230e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941964-9398-11f1-a8f5-005056c00001','fm00400e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9419ac-9398-11f1-a8f5-005056c00001','fm00400e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9419f6-9398-11f1-a8f5-005056c00001','fm00400e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941a3d-9398-11f1-a8f5-005056c00001','fm00400e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941a84-9398-11f1-a8f5-005056c00001','fm00400e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941ac9-9398-11f1-a8f5-005056c00001','fm00400e06','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941b0f-9398-11f1-a8f5-005056c00001','fm00400e07','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941b54-9398-11f1-a8f5-005056c00001','fm00400e08','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941b9a-9398-11f1-a8f5-005056c00001','fm00400e09','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac941bde-9398-11f1-a8f5-005056c00001','fm00400e10','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac942055-9398-11f1-a8f5-005056c00001','fm00400e11','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac942140-9398-11f1-a8f5-005056c00001','fm00002e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac942249-9398-11f1-a8f5-005056c00001','fm00510e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9422f0-9398-11f1-a8f5-005056c00001','fm00510e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94238f-9398-11f1-a8f5-005056c00001','fm00510e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94242c-9398-11f1-a8f5-005056c00001','fm00510e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9424c9-9398-11f1-a8f5-005056c00001','fm00510e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac942589-9398-11f1-a8f5-005056c00001','fm00910e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94266a-9398-11f1-a8f5-005056c00001','fm00700e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945270-9398-11f1-a8f5-005056c00001','fm00410e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945393-9398-11f1-a8f5-005056c00001','fm00410e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9453fc-9398-11f1-a8f5-005056c00001','fm00410e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94544e-9398-11f1-a8f5-005056c00001','fm00410e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94549f-9398-11f1-a8f5-005056c00001','fm00410e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9454f1-9398-11f1-a8f5-005056c00001','fm00410e06','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94553c-9398-11f1-a8f5-005056c00001','fm00410e07','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945588-9398-11f1-a8f5-005056c00001','fm00410e08','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9455d3-9398-11f1-a8f5-005056c00001','fm00410e09','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945621-9398-11f1-a8f5-005056c00001','fm00410e10','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac94566d-9398-11f1-a8f5-005056c00001','fm00410e11','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9456cd-9398-11f1-a8f5-005056c00001','fm00520e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945724-9398-11f1-a8f5-005056c00001','fm00520e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945914-9398-11f1-a8f5-005056c00001','fm00520e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945966-9398-11f1-a8f5-005056c00001','fm00520e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac9459b9-9398-11f1-a8f5-005056c00001','fm00520e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945a1e-9398-11f1-a8f5-005056c00001','fm00710e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945a91-9398-11f1-a8f5-005056c00001','fm00920e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945b18-9398-11f1-a8f5-005056c00001','fm00100e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945b8a-9398-11f1-a8f5-005056c00001','fm00420e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945bdb-9398-11f1-a8f5-005056c00001','fm00420e02','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945c2d-9398-11f1-a8f5-005056c00001','fm00420e03','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945c7c-9398-11f1-a8f5-005056c00001','fm00420e04','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945ccd-9398-11f1-a8f5-005056c00001','fm00420e05','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945d1c-9398-11f1-a8f5-005056c00001','fm00420e06','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945d6b-9398-11f1-a8f5-005056c00001','fm00420e07','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945db8-9398-11f1-a8f5-005056c00001','fm00420e08','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945e06-9398-11f1-a8f5-005056c00001','fm00420e09','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945e54-9398-11f1-a8f5-005056c00001','fm00420e10','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945ea3-9398-11f1-a8f5-005056c00001','fm00420e11','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('ac945efa-9398-11f1-a8f5-005056c00001','fm00720e01','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 10:18:41',NULL,NULL),
('f75024a4-938e-11f1-a8f5-005056c00001','fm00002','$2y$12$Q4x02Q0WKHWXAQ.NoGCs8ObX4sac890xeRnaNUxNnz/VEiHWazIp.','Y','admin','2026-08-09 09:09:11',NULL,NULL);
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
('19aa0707-8e5a-11f1-952d-8b02dbdce924','FM_PROG004D','00000000-0000-0000-0000-000000000000','Y','admin','2026-08-02 18:08:25',NULL,NULL),
('19ab3f88-8e5a-11f1-952d-67a6bce6b52a','FM_PROG004D0001Q','19aa0707-8e5a-11f1-952d-8b02dbdce924','Y','admin','2026-08-02 18:08:25',NULL,NULL),
('1f1a7071-8ff2-11f1-9e94-2bf67523a857','FM_PROG006D','00000000-0000-0000-0000-000000000000','Y','admin','2026-08-04 18:49:09',NULL,NULL),
('288800ef-8e47-11f1-8601-75f6d2252822','FM_PROG003D','00000000-0000-0000-0000-000000000000','Y','admin','2026-08-02 15:52:49',NULL,NULL),
('2bd1d3f0-8ef1-11f1-84eb-25df749b5ea1','FM_PROG005D','00000000-0000-0000-0000-000000000000','Y','admin','2026-08-03 12:09:49',NULL,NULL),
('2bd33381-8ef1-11f1-84eb-710e91c27197','FM_PROG005D0001Q','2bd1d3f0-8ef1-11f1-84eb-25df749b5ea1','Y','admin','2026-08-03 12:09:49',NULL,NULL),
('329d4cf9-8e55-11f1-aaf8-2fb682b5b8ed','FM_PROG003D0001Q','288800ef-8e47-11f1-8601-75f6d2252822','Y','admin','2026-08-02 17:33:19',NULL,NULL),
('329e375a-8e55-11f1-aaf8-7b7075c81664','FM_PROG003D0002Q','288800ef-8e47-11f1-8601-75f6d2252822','Y','admin','2026-08-02 17:33:19',NULL,NULL),
('4bd4d202-5feb-495b-8c8c-ec6b7f5b8041','CORE_PROG002D0002Q','79e1cf24-2522-4cdf-abcc-6455b47d545b','Y','admin','2017-05-10 14:20:12',NULL,NULL),
('4d85e70f-8c1c-11f1-b7bb-df01fa5d82a7','FM_PROG001D','00000000-0000-0000-0000-000000000000','Y','admin','2026-07-30 21:41:01',NULL,NULL),
('571133d5-8caa-11f1-bf0e-b7fd3076ce3e','FM_PROG002D','00000000-0000-0000-0000-000000000000','Y','admin','2026-07-31 14:37:45',NULL,NULL),
('5e055f61-bfc5-402c-93b4-f241dc17b00b','CORE_PROG004D','00000000-0000-0000-0000-000000000000','Y','admin','2017-06-03 14:23:17',NULL,NULL),
('79e1cf24-2522-4cdf-abcc-6455b47d545b','CORE_PROG002D','00000000-0000-0000-0000-000000000000','Y','admin','2017-05-08 21:32:59',NULL,NULL),
('7aa1208a-5fc2-11f1-afe9-33fb6c1b9ce7','CORE_PROG005D','00000000-0000-0000-0000-000000000000','Y','admin','2026-06-04 11:07:11',NULL,NULL),
('7aa2590b-5fc2-11f1-afe9-73b1551d818b','CORE_PROG005D0001Q','7aa1208a-5fc2-11f1-afe9-33fb6c1b9ce7','Y','admin','2026-06-04 11:07:11',NULL,NULL),
('7ea68636-c93a-4669-ac42-dafc3770d20d','CORE_PROG001D','00000000-0000-0000-0000-000000000000','Y','admin','2017-04-20 11:24:53',NULL,NULL),
('83f3ec93-8c1c-11f1-b7bb-0b4bed3d6265','FM_PROG001D0001Q','4d85e70f-8c1c-11f1-b7bb-df01fa5d82a7','Y','admin','2026-07-30 21:42:32',NULL,NULL),
('84a0c173-9005-11f1-9e5f-d93ed7a02673','FM_PROG006D0001Q','1f1a7071-8ff2-11f1-9e94-2bf67523a857','Y','admin','2026-08-04 21:07:59',NULL,NULL),
('84a1abd4-9005-11f1-9e5f-25ec38f9ba54','FM_PROG006D0002Q','1f1a7071-8ff2-11f1-9e94-2bf67523a857','Y','admin','2026-08-04 21:07:59',NULL,NULL),
('9972c249-2985-49ac-9b8b-f6c25c65fd4e','CORE_PROG002D0003Q','79e1cf24-2522-4cdf-abcc-6455b47d545b','Y','admin','2017-05-10 14:20:12',NULL,NULL),
('a00d5586-8e42-11f1-a428-5d974d2ac9d9','FM_PROG002D0001Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a00e66f7-8e42-11f1-a428-2970c26328fb','FM_PROG002D0002Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a00f7868-8e42-11f1-a428-317034e796eb','FM_PROG002D0003Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a0103bb9-8e42-11f1-a428-95a98e92cad7','FM_PROG002D0004Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a0114d2a-8e42-11f1-a428-cd7012816922','FM_PROG002D0005Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('a012378b-8e42-11f1-a428-a740bb435ab0','FM_PROG002D0006Q','571133d5-8caa-11f1-bf0e-b7fd3076ce3e','Y','admin','2026-08-02 15:20:22',NULL,NULL),
('b487c0cf-9594-11f1-b921-3df0459a44c5','FM_PROG008D','00000000-0000-0000-0000-000000000000','Y','admin','2026-08-11 22:55:34',NULL,NULL),
('b7a331a7-9594-11f1-b921-551247a3ee6e','FM_PROG008D0001Q','b487c0cf-9594-11f1-b921-3df0459a44c5','Y','admin','2026-08-11 22:55:39',NULL,NULL),
('b7a3f4f8-9594-11f1-b921-b3bac6417b9c','FM_PROG008D0002Q','b487c0cf-9594-11f1-b921-3df0459a44c5','Y','admin','2026-08-11 22:55:39',NULL,NULL),
('b7a4b849-9594-11f1-b921-390883fe4a65','FM_PROG008D0004Q','b487c0cf-9594-11f1-b921-3df0459a44c5','Y','admin','2026-08-11 22:55:39',NULL,NULL),
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
('0226073c-8e5a-11f1-a861-005056c00001','FM_PROG004D','FD. FlowMint 流程設計','/','N','N',0,0,'CORE','FOLDER','ORGANIZATION','diagram-3','admin','2026-08-02 18:07:38',NULL,NULL),
('0226e64b-8e5a-11f1-a861-005056c00001','FM_PROG004D0001Q','FD01 - 流程設計與版本','#/fm_prog004d0001','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-08-02 18:07:38',NULL,NULL),
('02279464-8e5a-11f1-a861-005056c00001','FM_PROG004D0001A','FD01 - 流程設計與版本（新增）','#/fm_prog004d0001/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-08-02 18:07:38',NULL,NULL),
('0228606f-8e5a-11f1-a861-005056c00001','FM_PROG004D0001E','FD01 - 流程設計與版本（編輯）','#/fm_prog004d0001/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-08-02 18:07:38',NULL,NULL),
('14d1db4f-8e47-11f1-a861-005056c00001','FM_PROG003D','FC. FlowMint 簽核資源','/','N','N',0,0,'CORE','FOLDER','ORGANIZATION','people','admin','2026-08-02 15:52:09',NULL,NULL),
('14d3285d-8e47-11f1-a861-005056c00001','FM_PROG003D0001Q','FC01 - 簽核群組','#/fm_prog003d0001','N','N',0,0,'CORE','ITEM','ORGANIZATION','people','admin','2026-08-02 15:52:09',NULL,NULL),
('14d41871-8e47-11f1-a861-005056c00001','FM_PROG003D0001A','FC01 - 簽核群組（新增）','#/fm_prog003d0001/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','people','admin','2026-08-02 15:52:09',NULL,NULL),
('14d50885-8e47-11f1-a861-005056c00001','FM_PROG003D0001E','FC01 - 簽核群組（編輯）','#/fm_prog003d0001/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','people','admin','2026-08-02 15:52:09',NULL,NULL),
('186b1fb1-749f-4b6f-97d1-6b7fb8115345','CORE_PROG001D0004E','ZA04 - Freemarker樣板 (Edit)','#/prog001d0004/edit','Y','N',0,0,'CORE','ITEM','TEMPLATE','file-text','admin','2017-05-12 10:40:10','admin','2023-08-16 21:48:56'),
('19834f86-8ef1-11f1-a861-005056c00001','FM_PROG005D','FE. FlowMint 表單設計','/','N','N',0,0,'CORE','FOLDER','ORGANIZATION','file-earmark-text','admin','2026-08-03 12:09:07',NULL,NULL),
('1985a871-8ef1-11f1-a861-005056c00001','FM_PROG005D0001Q','FE01 - 表單設計與版本','#/fm_prog005d0001','N','N',0,0,'CORE','ITEM','ORGANIZATION','file-earmark-text','admin','2026-08-03 12:09:07',NULL,NULL),
('19870260-8ef1-11f1-a861-005056c00001','FM_PROG005D0001A','FE01 - 表單設計與版本（新增）','#/fm_prog005d0001/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','file-earmark-text','admin','2026-08-03 12:09:07',NULL,NULL),
('1988535e-8ef1-11f1-a861-005056c00001','FM_PROG005D0001E','FE01 - 表單設計與版本（編輯）','#/fm_prog005d0001/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','file-earmark-text','admin','2026-08-03 12:09:07',NULL,NULL),
('1b11c7eb-6133-48fb-87f0-dfbd098ce914','CORE_PROG001D0001E','ZA01 - System site (Edit)','#/prog001d0001/edit','Y','N',0,0,'CORE','ITEM','COMPUTER','globe2','admin','2014-10-02 00:00:00','admin','2021-01-20 08:20:58'),
('1e393fe3-8bbc-482c-aa23-bbb22a1dbafb','CORE_PROG001D0005A','ZA05 - JasperReport (Create)','#/prog001d0005/create','N','N',0,0,'CORE','ITEM','APPLICATION_PDF','file-pdf','admin','2017-05-18 09:55:46','admin','2023-08-24 20:20:27'),
('22560527-90fb-4e5a-a89b-353d2aa1d433','CORE_PROG001D0005E','ZA05 - JasperReport (Edit)','#/prog001d0005/edit','Y','N',0,0,'CORE','ITEM','APPLICATION_PDF','file-pdf','admin','2017-05-18 09:56:27','admin','2023-08-24 20:20:40'),
('29e19038-8ca9-11f1-a791-005056c00001','FM_PROG002D','FB. FlowMint 組織管理','/','N','N',0,0,'CORE','FOLDER','ORGANIZATION','diagram-3','admin','2026-07-31 14:29:01',NULL,NULL),
('29e3bfc4-8ca9-11f1-a791-005056c00001','FM_PROG002D0001Q','FB01 - 員工資料管理','#/fm_prog002d0001','N','N',0,0,'CORE','ITEM','PEOPLE','people','admin','2026-07-31 14:29:01',NULL,NULL),
('29e57f23-8ca9-11f1-a791-005056c00001','FM_PROG002D0001A','FB01 - 員工資料管理（新增）','#/fm_prog002d0001/create','N','N',0,0,'CORE','ITEM','PEOPLE','people','admin','2026-07-31 14:29:01','admin','2026-07-31 14:36:07'),
('29e6d728-8ca9-11f1-a791-005056c00001','FM_PROG002D0001E','FB01 - 員工資料管理（編輯）','#/fm_prog002d0001/edit','Y','N',0,0,'CORE','ITEM','PEOPLE','people','admin','2026-07-31 14:29:01','admin','2026-07-31 14:35:54'),
('35031b7e-9593-11f1-a8f5-005056c00001','FM_PROG008D','FH. FlowMint 流程營運','/','N','N',0,0,'CORE','FOLDER','ORGANIZATION','activity','admin','2026-08-11 22:44:24',NULL,NULL),
('350502a5-9593-11f1-a8f5-005056c00001','FM_PROG008D0001Q','FH01 - 流程實例監控','#/operations/processes','N','N',0,0,'CORE','ITEM','ORGANIZATION','diagram-3','admin','2026-08-11 22:44:24',NULL,NULL),
('35062993-9593-11f1-a8f5-005056c00001','FM_PROG008D0002Q','FH02 - 指派異常處理','#/operations/incidents','N','N',0,0,'CORE','ITEM','ORGANIZATION','exclamation-triangle','admin','2026-08-11 22:44:24',NULL,NULL),
('35076d22-9593-11f1-a8f5-005056c00001','FM_PROG008D0004Q','FH04 - 流程營運報表','#/operations/reports','N','N',0,0,'CORE','ITEM','ORGANIZATION','bar-chart-line','admin','2026-08-11 22:44:24',NULL,NULL),
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
('a5ffb65c-9004-11f1-a98b-005056c00001','FM_PROG006D0002Q','FF02 - Data Action 設計','#/fm_prog006d0002','N','N',0,0,'CORE','ITEM','DATABASE','database','admin','2026-08-04 21:01:46',NULL,NULL),
('a5ffb92b-9004-11f1-a98b-005056c00001','FM_PROG006D0002A','FF02 - Data Action 設計（新增）','#/fm_prog006d0002/create','N','N',0,0,'CORE','ITEM','DATABASE','database','admin','2026-08-04 21:01:46',NULL,NULL),
('a5ffb97b-9004-11f1-a98b-005056c00001','FM_PROG006D0002E','FF02 - Data Action 設計（編輯）','#/fm_prog006d0002/edit','Y','N',0,0,'CORE','ITEM','DATABASE','database','admin','2026-08-04 21:01:46',NULL,NULL),
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
('eb54711c-8e54-11f1-a861-005056c00001','FM_PROG003D0002Q','FC02 - 工作代理','#/fm_prog003d0002','N','N',0,0,'CORE','ITEM','ORGANIZATION','people','admin','2026-08-02 17:31:12',NULL,NULL),
('eb565b12-8e54-11f1-a861-005056c00001','FM_PROG003D0002A','FC02 - 工作代理（新增）','#/fm_prog003d0002/create','N','N',0,0,'CORE','ITEM','ORGANIZATION','people','admin','2026-08-02 17:31:12',NULL,NULL),
('eb572938-8e54-11f1-a861-005056c00001','FM_PROG003D0002E','FC02 - 工作代理（編輯）','#/fm_prog003d0002/edit','Y','N',0,0,'CORE','ITEM','ORGANIZATION','people','admin','2026-08-02 17:31:12',NULL,NULL),
('eb6e199f-c853-4fbf-acf3-0c9c77ba9953','CORE_PROG001D0002Q','ZA02 - Program','#/prog001d0002','N','N',0,0,'CORE','ITEM','G_APP_INSTALL','filetype-html','admin','2014-10-02 00:00:00','admin','2023-08-15 19:19:05'),
('eb786ffd-c7d1-4631-aed2-4d9d7368eb13','CORE_PROG001D0005Q','ZA05 - JasperReport','#/prog001d0005','N','N',0,0,'CORE','ITEM','APPLICATION_PDF','file-pdf','admin','2017-05-18 09:54:35','admin','2023-08-24 20:20:16'),
('ef5c35e7-8fef-11f1-a98b-005056c00001','FM_PROG006D','FF. FlowMint 動態資料服務','/','N','N',0,0,'CORE','FOLDER','DATABASE','database','admin','2026-08-04 18:33:30',NULL,NULL),
('ef5c3962-8fef-11f1-a98b-005056c00001','FM_PROG006D0001Q','FF01 - DataSource 管理','#/fm_prog006d0001','N','N',0,0,'CORE','ITEM','DATABASE','database','admin','2026-08-04 18:33:30','admin','2026-08-04 20:25:53'),
('ef5c39bb-8fef-11f1-a98b-005056c00001','FM_PROG006D0001A','FF01 - DataSource 管理（新增）','#/fm_prog006d0001/create','N','N',0,0,'CORE','ITEM','DATABASE','database','admin','2026-08-04 18:33:30','admin','2026-08-04 20:26:06'),
('ef5c3a05-8fef-11f1-a98b-005056c00001','FM_PROG006D0001E','FF01 - DataSource 管理（編輯）','#/fm_prog006d0001/edit','Y','N',0,0,'CORE','ITEM','DATABASE','database','admin','2026-08-04 18:33:30','admin','2026-08-04 20:26:17');
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
INSERT INTO `tb_sys_template` VALUES
('39882be1-958e-11f1-a8f5-005056c00001','FMTASKASG','你有新的流程待辦','${taskName}','FlowMint 待辦指派通知','system','2026-08-11 22:08:44',NULL,NULL),
('39882eee-958e-11f1-a8f5-005056c00001','FMPROCMP','你的流程已完成','流程編號：${referenceId}','FlowMint 流程完成通知','system','2026-08-11 22:08:44',NULL,NULL),
('39882f5a-958e-11f1-a8f5-005056c00001','FMPROREJ','你的流程已駁回','流程編號：${referenceId}','FlowMint 流程駁回通知','system','2026-08-11 22:08:44',NULL,NULL),
('39882f8d-958e-11f1-a8f5-005056c00001','FMPROCAN','流程已取消','流程編號：${referenceId}','FlowMint 流程撤回或取消通知','system','2026-08-11 22:08:44',NULL,NULL),
('39882fbb-958e-11f1-a8f5-005056c00001','FMTASKDUE','流程待辦即將到期','${taskName}','FlowMint 待辦期限提前提醒','system','2026-08-11 22:08:44',NULL,NULL),
('39882fe0-958e-11f1-a8f5-005056c00001','FMTASKOVD','流程待辦已逾時','${taskName}','FlowMint 待辦逾時通知','system','2026-08-11 22:08:44',NULL,NULL);
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
INSERT INTO `tb_sys_template_param` VALUES
('398a32e0-958e-11f1-a8f5-005056c00001','FMTASKASG','N','taskName','taskName','system','2026-08-11 22:08:44',NULL,NULL),
('398a36e6-958e-11f1-a8f5-005056c00001','FMPROCMP','N','referenceId','referenceId','system','2026-08-11 22:08:44',NULL,NULL),
('398a3816-958e-11f1-a8f5-005056c00001','FMPROREJ','N','referenceId','referenceId','system','2026-08-11 22:08:44',NULL,NULL),
('398a3871-958e-11f1-a8f5-005056c00001','FMPROCAN','N','referenceId','referenceId','system','2026-08-11 22:08:44',NULL,NULL),
('398a38c1-958e-11f1-a8f5-005056c00001','FMTASKDUE','N','taskName','taskName','system','2026-08-11 22:08:44',NULL,NULL),
('398a390d-958e-11f1-a8f5-005056c00001','FMTASKOVD','N','taskName','taskName','system','2026-08-11 22:08:44',NULL,NULL);
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

-- Dump completed on 2026-08-18 20:43:43
