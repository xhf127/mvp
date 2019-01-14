-- MySQL dump 10.13  Distrib 8.0.13, for Linux (x86_64)
--
-- Host: localhost    Database: qed_bbq
-- ------------------------------------------------------
-- Server version	8.0.13

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8mb4 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `analyze_result`
--

DROP TABLE IF EXISTS `analyze_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `analyze_result` (
  `analyze_result_key` int(11) NOT NULL AUTO_INCREMENT COMMENT '脑定量分析参数和结果表主键key',
  `user_key` int(11) NOT NULL,
  `brain_atlas_key` int(11) NOT NULL COMMENT '脑图谱主键key',
  `normal_control_key` int(11) NOT NULL COMMENT '正常人群组主键key',
  `series_key` int(11) NOT NULL COMMENT 'series主键key',
  `statistical_method` char(11) DEFAULT NULL COMMENT '统计方式：SUV, BQ',
  `statistical_mode` char(5) DEFAULT NULL COMMENT '统计模式：both,+,-',
  `abnormal_volume` int(11) DEFAULT NULL COMMENT '病灶体积',
  `patient_weight` float NOT NULL COMMENT '体重            0010,1030            ',
  `acquisition_date` date NOT NULL COMMENT '采集日期\r\n            0008,0022\r\n            ',
  `acquisition_time` time NOT NULL COMMENT '采集时间\r\n            0008,0032\r\n            ',
  `radiopharmaceutical_start_date` date NOT NULL COMMENT '打药日期\r\n            0018,1078\r\n            ',
  `radiopharmaceutical_start_time` time NOT NULL COMMENT '打药时间\r\n            0018,1072\r\n            ',
  `radionuclide_total_dose` float NOT NULL COMMENT '打药剂量\r\n            0018,1074\r\n            ',
  `radionuclide_half_life` float NOT NULL COMMENT '半衰期\r\n            0018,1075\r\n            ',
  `p_value` double DEFAULT NULL,
  `t_value` double DEFAULT NULL,
  `imaging_interval` int(11) NOT NULL DEFAULT '0',
  `analyze_status` tinyint(4) NOT NULL COMMENT '分析状态',
  `err_code` text,
  `source_path` varchar(255) DEFAULT NULL,
  `normalized_image_path` varchar(255) DEFAULT NULL COMMENT '归一化数据文件路径',
  `suv_t_path` varchar(255) DEFAULT NULL COMMENT 'SUV T值矩阵文件路径',
  `bq_t_path` varchar(255) DEFAULT NULL COMMENT 'BQ T值矩阵文件路径',
  `create_datetime` datetime DEFAULT NULL,
  `update_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`analyze_result_key`),
  KEY `FK_Reference_11` (`series_key`),
  KEY `FK_Reference_12` (`user_key`),
  KEY `FK_Reference_15` (`brain_atlas_key`),
  KEY `FK_Reference_16` (`normal_control_key`),
  CONSTRAINT `FK_Reference_11` FOREIGN KEY (`series_key`) REFERENCES `series` (`series_key`),
  CONSTRAINT `FK_Reference_12` FOREIGN KEY (`user_key`) REFERENCES `user` (`user_key`),
  CONSTRAINT `FK_Reference_15` FOREIGN KEY (`brain_atlas_key`) REFERENCES `brain_atlas` (`brain_atlas_key`),
  CONSTRAINT `FK_Reference_16` FOREIGN KEY (`normal_control_key`) REFERENCES `normal_control` (`normal_control_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='脑定量分析参数和结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `analyze_result`
--

LOCK TABLES `analyze_result` WRITE;
/*!40000 ALTER TABLE `analyze_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `analyze_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `brain_atlas`
--

DROP TABLE IF EXISTS `brain_atlas`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `brain_atlas` (
  `brain_atlas_key` int(11) NOT NULL AUTO_INCREMENT,
  `brain_atlas_name` varchar(32) DEFAULT NULL COMMENT '脑功能区模板 名称',
  `description` varchar(255) DEFAULT NULL COMMENT '脑功能区模板 描述',
  `brain_atlas_path` varchar(255) NOT NULL COMMENT '脑功能区模板 路径',
  `create_datetime` datetime DEFAULT NULL COMMENT '创建时间',
  `update_datetime` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`brain_atlas_key`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8 COMMENT='脑功能区（脑图谱）模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brain_atlas`
--

LOCK TABLES `brain_atlas` WRITE;
/*!40000 ALTER TABLE `brain_atlas` DISABLE KEYS */;
INSERT INTO `brain_atlas` VALUES (1,'全脑图谱',NULL,'/bbq/brain_atlas/labels_Neuromorphometrics.nii',NULL,NULL),(2,'小脑图谱',NULL,'/bbq/brain_atlas/labels_Neuromorphometrics_cerebellum.nii',NULL,NULL),(3,'全脑轮廓',NULL,'/bbq/brain_atlas/labels_Neuromorphometrics_contour.nii',NULL,NULL),(4,'全脑图谱无白质',NULL,'/bbq/brain_atlas/labels_Neuromorphometrics_without_WM.nii',NULL,NULL);
/*!40000 ALTER TABLE `brain_atlas` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `brain_atlas_info`
--

DROP TABLE IF EXISTS `brain_atlas_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `brain_atlas_info` (
  `id` int(11) NOT NULL,
  `name` varchar(64) NOT NULL,
  `value` double NOT NULL,
  `symmetry_value` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brain_atlas_info`
--

LOCK TABLES `brain_atlas_info` WRITE;
/*!40000 ALTER TABLE `brain_atlas_info` DISABLE KEYS */;
INSERT INTO `brain_atlas_info` VALUES (3,'Right_Accumbens_Area',23,1),(4,'Left_Accumbens_Area',30,1),(5,'Right_Amygdala',31,2),(6,'Left_Amygdala',32,2),(8,'Right_Caudate',36,3),(9,'Left_Caudate',37,3),(10,'Right_Cerebellum_Exterior',38,4),(11,'Left_Cerebellum_Exterior',39,4),(17,'Right_Hippocampus',47,5),(18,'Left_Hippocampus',48,5),(23,'Right_Pallidum',55,6),(24,'Left_Pallidum',56,6),(25,'Right_Putamen',57,7),(26,'Left_Putamen',58,7),(27,'Right_Thalamus_Proper',59,8),(28,'Left_Thalamus_Proper',60,8),(34,'Cerebellar_Vermal_Lobules_I-V',71,NULL),(35,'Cerebellar_Vermal_Lobules_VI-VII',72,NULL),(36,'Cerebellar_Vermal_Lobules_VIII-X',73,NULL),(37,'Left_Basal_Forebrain',75,9),(38,'Right_Basal_Forebrain',76,9),(39,'Right_ACgG_anterior_cingulate_gyrus',100,10),(40,'Left_ACgG_anterior_cingulate_gyrus',101,10),(41,'Right_AIns_anterior_insula',102,11),(42,'Left_AIns_anterior_insula',103,11),(43,'Right_AOrG_anterior_orbital_gyrus',104,12),(44,'Left_AOrG_anterior_orbital_gyrus',105,12),(45,'Right_AnG_angular_gyrus',106,13),(46,'Left_AnG_angular_gyrus',107,13),(47,'Right_Calc_calcarine_cortex',108,14),(48,'Left_Calc_calcarine_cortex',109,14),(49,'Right_CO_central_operculum',112,15),(50,'Left_CO_central_operculum',113,15),(51,'Right_Cun_cuneus',114,16),(52,'Left_Cun_cuneus',115,16),(53,'Right_Ent_entorhinal_area',116,17),(54,'Left_Ent_entorhinal_area',117,17),(55,'Right_FO_frontal_operculum',118,18),(56,'Left_FO_frontal_operculum',119,18),(57,'Right_FRP_frontal_pole',120,19),(58,'Left_FRP_frontal_pole',121,19),(59,'Right_FuG_fusiform_gyrus',122,20),(60,'Left_FuG_fusiform_gyrus',123,20),(61,'Right_GRe_gyrus_rectus',124,21),(62,'Left_GRe_gyrus_rectus',125,21),(63,'Right_IOG_inferior_occipital_gyrus',128,22),(64,'Left_IOG_inferior_occipital_gyrus',129,22),(65,'Right_ITG_inferior_temporal_gyrus',132,23),(66,'Left_ITG_inferior_temporal_gyrus',133,23),(67,'Right_LiG_lingual_gyrus',134,24),(68,'Left_LiG_lingual_gyrus',135,24),(69,'Right_LOrG_lateral_orbital_gyrus',136,25),(70,'Left_LOrG_lateral_orbital_gyrus',137,25),(71,'Right_MCgG_middle_cingulate_gyrus',138,26),(72,'Left_MCgG_middle_cingulate_gyrus',139,26),(73,'Right_MFC_medial_frontal_cortex',140,27),(74,'Left_MFC_medial_frontal_cortex',141,27),(75,'Right_MFG_middle_frontal_gyrus',142,28),(76,'Left_MFG_middle_frontal_gyrus',143,28),(77,'Right_MOG_middle_occipital_gyrus',144,29),(78,'Left_MOG_middle_occipital_gyrus',145,29),(79,'Right_MOrG_medial_orbital_gyrus',146,30),(80,'Left_MOrG_medial_orbital_gyrus',147,30),(81,'Right_MPoG_postcentral_gyrus_medial_segment',148,31),(82,'Left_MPoG_postcentral_gyrus_medial_segment',149,31),(83,'Right_MPrG_precentral_gyrus_medial_segment',150,32),(84,'Left_MPrG_precentral_gyrus_medial_segment',151,32),(85,'Right_MSFG_superior_frontal_gyrus_medial_segment',152,33),(86,'Left_MSFG_superior_frontal_gyrus_medial_segment',153,33),(87,'Right_MTG_middle_temporal_gyrus',154,34),(88,'Left_MTG_middle_temporal_gyrus',155,34),(89,'Right_OCP_occipital_pole',156,35),(90,'Left_OCP_occipital_pole',157,35),(91,'Right_OFuG_occipital_fusiform_gyrus',160,36),(92,'Left_OFuG_occipital_fusiform_gyrus',161,36),(93,'Right_OpIFG_opercular_part_of_the_inferior_frontal_gyrus',162,37),(94,'Left_OpIFG_opercular_part_of_the_inferior_frontal_gyrus',163,37),(95,'Right_OrIFG_orbital_part_of_the_inferior_frontal_gyrus',164,38),(96,'Left_OrIFG_orbital_part_of_the_inferior_frontal_gyrus',165,38),(97,'Right_PCgG_posterior_cingulate_gyrus',166,39),(98,'Left_PCgG_posterior_cingulate_gyrus',167,39),(99,'Right_PCu_precuneus',168,40),(100,'Left_PCu_precuneus',169,40),(101,'Right_PHG_parahippocampal_gyrus',170,41),(102,'Left_PHG_parahippocampal_gyrus',171,41),(103,'Right_PIns_posterior_insula',172,42),(104,'Left_PIns_posterior_insula',173,42),(105,'Right_PO_parietal_operculum',174,43),(106,'Left_PO_parietal_operculum',175,43),(107,'Right_PoG_postcentral_gyrus',176,44),(108,'Left_PoG_postcentral_gyrus',177,44),(109,'Right_POrG_posterior_orbital_gyrus',178,45),(110,'Left_POrG_posterior_orbital_gyrus',179,45),(111,'Right_PP_planum_polare',180,46),(112,'Left_PP_planum_polare',181,46),(113,'Right_PrG_precentral_gyrus',182,47),(114,'Left_PrG_precentral_gyrus',183,47),(115,'Right_PT_planum_temporale',184,48),(116,'Left_PT_planum_temporale',185,48),(117,'Right_SCA_subcallosal_area',186,49),(118,'Left_SCA_subcallosal_area',187,49),(119,'Right_SFG_superior_frontal_gyrus',190,50),(120,'Left_SFG_superior_frontal_gyrus',191,50),(121,'Right_SMC_supplementary_motor_cortex',192,51),(122,'Left_SMC_supplementary_motor_cortex',193,51),(123,'Right_SMG_supramarginal_gyrus',194,52),(124,'Left_SMG_supramarginal_gyrus',195,52),(125,'Right_SOG_superior_occipital_gyrus',196,53),(126,'Left_SOG_superior_occipital_gyrus',197,53),(127,'Right_SPL_superior_parietal_lobule',198,54),(128,'Left_SPL_superior_parietal_lobule',199,54),(129,'Right_STG_superior_temporal_gyrus',200,55),(130,'Left_STG_superior_temporal_gyrus',201,55),(131,'Right_TMP_temporal_pole',202,56),(132,'Left_TMP_temporal_pole',203,56),(133,'Right_TrIFG_triangular_part_of_the_inferior_frontal_gyrus',204,57),(134,'Left_TrIFG_triangular_part_of_the_inferior_frontal_gyrus',205,57),(135,'Right_TTG_transverse_temporal_gyrus',206,58),(136,'Left_TTG_transverse_temporal_gyrus',207,58);
/*!40000 ALTER TABLE `brain_atlas_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `normal_control`
--

DROP TABLE IF EXISTS `normal_control`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `normal_control` (
  `normal_control_key` int(11) NOT NULL AUTO_INCREMENT COMMENT '正常人群组主键key',
  `normal_control_name` varchar(64) DEFAULT NULL COMMENT '正常人群组名称',
  `description` varchar(128) DEFAULT NULL COMMENT '正常人群组描述',
  `modality` char(8) DEFAULT NULL,
  `bq_suv` varchar(11) NOT NULL,
  `person_count` int(11) NOT NULL,
  `normal_control_path` varchar(255) DEFAULT NULL COMMENT '正常人群组文件、目录路径',
  `create_datetime` datetime DEFAULT NULL,
  `update_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`normal_control_key`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8 COMMENT='正常人群组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `normal_control`
--

LOCK TABLES `normal_control` WRITE;
/*!40000 ALTER TABLE `normal_control` DISABLE KEYS */;
INSERT INTO `normal_control` VALUES (1,'TEST',NULL,'PET','bq',23,'/bbq/matlab/TPM/TEST',NULL,NULL),(2,'TEST_14',NULL,'PET','bq',14,'/bbq/matlab/TPM/TEST_14',NULL,NULL),(3,'TEST_23',NULL,'PET','bq',40,'/bbq/matlab/TPM/TEST_40',NULL,NULL),(4,'TESTSP',NULL,'SPECT','bq',15,'/bbq/matlab/TPM/TESTSP',NULL,NULL),(5,'TESTSP_181',NULL,'SPECT','bq',15,'/bbq/matlab/TPM/TESTSP_181',NULL,NULL),(6,'TESTV',NULL,'PET','suv',23,'/bbq/matlab/TPM/TESTV',NULL,NULL),(7,'TESTV_14',NULL,'PET','suv',14,'/bbq/matlab/TPM/TESTV_14',NULL,NULL),(8,'TESTV_23',NULL,'PET','suv',40,'/bbq/matlab/TPM/TESTV_40',NULL,NULL);
/*!40000 ALTER TABLE `normal_control` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `patient`
--

DROP TABLE IF EXISTS `patient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `patient` (
  `patient_key` int(11) NOT NULL AUTO_INCREMENT COMMENT '患者主键key',
  `patient_id` varchar(64) DEFAULT NULL COMMENT '患者ID\r\n            0010,0020',
  `patient_name` varchar(64) DEFAULT NULL COMMENT '姓名 0010,0010',
  `patient_gender` char(4) DEFAULT NULL COMMENT '性别\r\n            0010,0040\r\n            M：男， F：女，O：其它',
  `patient_birth_date` date COMMENT '出生日期\r\n            0010,0030',
  PRIMARY KEY (`patient_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='dcm影像patient数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `patient`
--

LOCK TABLES `patient` WRITE;
/*!40000 ALTER TABLE `patient` DISABLE KEYS */;
/*!40000 ALTER TABLE `patient` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `series`
--

DROP TABLE IF EXISTS `series`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `series` (
  `series_key` int(11) NOT NULL AUTO_INCREMENT COMMENT 'series主键key',
  `study_key` int(11) NOT NULL COMMENT '检查主键ID',
  `series_instance_uid` varchar(64) NOT NULL COMMENT '序列UID\r\n            0020,000e\r\n            ',
  `series_number` varchar(64) DEFAULT NULL COMMENT '序列号\r\n            0020,0011\r\n            ',
  `series_date` date DEFAULT NULL COMMENT '检查日期\r\n            0008,0021\r\n            ',
  `series_time` time DEFAULT NULL COMMENT '检查时间\r\n            0008,0031\r\n            ',
  `series_description` varchar(64) DEFAULT NULL COMMENT '检查描述\r\n            0008,103e\r\n            ',
  `modality` char(8) NOT NULL COMMENT '设备类型\r\n            0008,0060\r\n            ',
  `manufacturer` varchar(64) DEFAULT NULL COMMENT '设备厂商\r\n            0008,0070\r\n            ',
  `station_name` varchar(64) DEFAULT '' COMMENT '工作站名称\r\n            0008,1010\r\n            ',
  `institution_name` varchar(64) DEFAULT '' COMMENT '图像数量\r\n            0054,0081\r\n            ',
  `acquisition_time` time NOT NULL COMMENT '采集时间\r\n            0008,0032\r\n            ',
  `radiopharmaceutical_start_date` date NOT NULL COMMENT '打药日期\r\n            0018,1078\r\n            ',
  `radiopharmaceutical_start_time` time NOT NULL COMMENT '打药时间\r\n            0018,1072\r\n            ',
  `radionuclide_total_dose` float NOT NULL COMMENT '打药剂量\r\n            0018,1074\r\n            ',
  `radionuclide_half_life` float NOT NULL COMMENT '半衰期\r\n            0018,1075\r\n            ',
  `acquisition_date` date NOT NULL COMMENT '采集日期\r\n            0008,0022\r\n            ',
  `radiopharmaceutical` varchar(64) DEFAULT NULL COMMENT '核素',
  `patient_weight` float NOT NULL COMMENT '体重\r\n            0010,1030\r\n            ',
  `series_path` varchar(256) DEFAULT NULL COMMENT 'series Dir',
  `imaging_interval` int(11) DEFAULT NULL COMMENT '显像时间差',
  `slice_thickness` float DEFAULT '1' COMMENT '层厚',
  `pixel_spacing` double DEFAULT '1' COMMENT '像素间距',
  `window_center` varchar(64) DEFAULT '0' COMMENT '窗位',
  `window_width` varchar(64) DEFAULT '1' COMMENT '窗宽',
  `rescale_intercept` double DEFAULT '0' COMMENT '截距',
  `rescale_slope` double DEFAULT '1' COMMENT '斜率',
  `image_count` int(11) NOT NULL DEFAULT '0',
  `create_datetime` datetime DEFAULT NULL,
  `study_age` char(8) DEFAULT '' COMMENT '检查时年龄',
  `update_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`series_key`),
  KEY `FK_Reference_14` (`study_key`),
  CONSTRAINT `FK_Reference_14` FOREIGN KEY (`study_key`) REFERENCES `study` (`study_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='dcm影像series数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `series`
--

LOCK TABLES `series` WRITE;
/*!40000 ALTER TABLE `series` DISABLE KEYS */;
/*!40000 ALTER TABLE `series` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `study`
--

DROP TABLE IF EXISTS `study`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `study` (
  `study_key` int(11) NOT NULL AUTO_INCREMENT COMMENT '检查主键key',
  `patient_key` int(11) NOT NULL COMMENT '患者主键key',
  `study_instance_uid` varchar(64) NOT NULL COMMENT '检查UID\r\n            0020,000d\r\n            ',
  `study_id` varchar(64) DEFAULT NULL COMMENT '检查ID\r\n            0020,0010\r\n            ',
  `accession_number` varchar(64) DEFAULT NULL COMMENT '检查号\r\n            0008,0050\r\n            ',
  `study_date` date DEFAULT NULL COMMENT '检查日期\r\n            0008,0020\r\n            ',
  `study_time` time DEFAULT NULL COMMENT '检查时间\r\n            0008,0030\r\n            ',
  `study_description` varchar(64) DEFAULT NULL COMMENT '检查描述\r\n            0008,1030\r\n            ',
  PRIMARY KEY (`study_key`),
  KEY `FK_Reference_13` (`patient_key`),
  CONSTRAINT `FK_Reference_13` FOREIGN KEY (`patient_key`) REFERENCES `patient` (`patient_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='dcm影像检查级数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `study`
--

LOCK TABLES `study` WRITE;
/*!40000 ALTER TABLE `study` DISABLE KEYS */;
/*!40000 ALTER TABLE `study` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user` (
  `user_key` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL COMMENT '用户账号',
  `mobile_number` char(16) NOT NULL,
  `password` char(64) NOT NULL,
  `username` varchar(64) DEFAULT NULL,
  `email` varchar(32) NOT NULL,
  `hospital` varchar(64) NOT NULL,
  `department` varchar(64) DEFAULT NULL,
  `title` varchar(64) DEFAULT NULL COMMENT '职务',
  `user_status` tinyint(4) NOT NULL COMMENT '用户状态',
  `create_datetime` datetime NOT NULL,
  `update_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`user_key`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COMMENT='用户表，用户是指DCM影像的拥有者（不是患者）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'13810014375','13810014375','$2a$10$3NmrisVMuP6QMywr6f8FB.xgyw8Xms6K6h1.yTN9G2K5AvjvJM1OO','ceshi','fanweibin@qed-tec.com','qed','产品部','产品经理',0,'2018-12-20 04:33:38','2018-12-20 04:37:04');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_bind_image`
--

DROP TABLE IF EXISTS `user_bind_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user_bind_image` (
  `user_bind_image_key` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID\r\n            ',
  `series_key` int(11) NOT NULL COMMENT 'series主键ID',
  `study_key` int(11) NOT NULL COMMENT '检查主键ID',
  `patient_key` int(11) NOT NULL COMMENT '患者主键key',
  `user_key` int(11) NOT NULL,
  `is_deleted` tinyint(4) NOT NULL COMMENT '用户主动逻辑删除patient,study,series,instance（0：默认，1：删除）',
  `create_datetime` datetime DEFAULT NULL,
  `update_datetime` datetime DEFAULT NULL,
  PRIMARY KEY (`user_bind_image_key`),
  KEY `FK_Reference_10` (`user_key`),
  KEY `FK_Reference_7` (`series_key`),
  KEY `FK_Reference_8` (`study_key`),
  KEY `FK_Reference_9` (`patient_key`),
  CONSTRAINT `FK_Reference_10` FOREIGN KEY (`user_key`) REFERENCES `user` (`user_key`),
  CONSTRAINT `FK_Reference_7` FOREIGN KEY (`series_key`) REFERENCES `series` (`series_key`),
  CONSTRAINT `FK_Reference_8` FOREIGN KEY (`study_key`) REFERENCES `study` (`study_key`),
  CONSTRAINT `FK_Reference_9` FOREIGN KEY (`patient_key`) REFERENCES `patient` (`patient_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='user与patient,study,series,instance关系绑定表 ';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_bind_image`
--

LOCK TABLES `user_bind_image` WRITE;
/*!40000 ALTER TABLE `user_bind_image` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_bind_image` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2018-12-23 22:12:33
