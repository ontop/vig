CREATE DATABASE  IF NOT EXISTS "lubm1" /*!40100 DEFAULT CHARACTER SET latin1 */;
USE `lubm1`;
-- MySQL dump 10.13  Distrib 5.5.47, for debian-linux-gnu (x86_64)
--
-- Host: 10.7.20.39    Database: lubm1
-- ------------------------------------------------------
-- Server version	5.6.10

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `coauthors`
--

DROP TABLE IF EXISTS `coauthors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coauthors` (
  `depid` smallint(6) DEFAULT NULL,
  `uniid` smallint(6) DEFAULT NULL,
  `publicationid` smallint(6) DEFAULT NULL,
  `authortype` tinyint(4) DEFAULT NULL,
  `authorid` smallint(6) DEFAULT NULL,
  KEY `idx_co_1` (`depid`),
  KEY `idx_co_2` (`uniid`),
  KEY `idx_co_3` (`publicationid`),
  KEY `idx_co_4` (`authortype`),
  KEY `idx_co_5` (`authorid`),
  KEY `fk_co_1` (`depid`,`uniid`,`authortype`,`authorid`),
  CONSTRAINT `fk_co_1` FOREIGN KEY (`depid`, `uniid`, `authortype`, `authorid`) REFERENCES `students` (`depid`, `uniid`, `stype`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `courses` (
  `depid` smallint(6) NOT NULL,
  `uniid` smallint(6) NOT NULL,
  `ctype` tinyint(4) NOT NULL,
  `id` smallint(6) NOT NULL,
  `teacherid` smallint(6) NOT NULL,
  `teachertype` tinyint(4) NOT NULL,
  PRIMARY KEY (`depid`,`uniid`,`ctype`,`id`),
  KEY `idx_c_1` (`teacherid`),
  KEY `idx_c_2` (`teachertype`),
  KEY `fk_courses_1` (`depid`,`uniid`,`teachertype`,`teacherid`),
  CONSTRAINT `fk_courses_1` FOREIGN KEY (`depid`, `uniid`, `teachertype`, `teacherid`) REFERENCES `teachers` (`depid`, `uniid`, `ttype`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `departments` (
  `departmentid` smallint(6) NOT NULL,
  `universityid` smallint(6) NOT NULL,
  PRIMARY KEY (`departmentid`,`universityid`),
  KEY `idx_dep_1` (`departmentid`),
  KEY `idx_dep_2` (`universityid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `heads`
--

DROP TABLE IF EXISTS `heads`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `heads` (
  `depid` smallint(6) NOT NULL,
  `uniid` smallint(6) NOT NULL,
  `proftype` tinyint(4) NOT NULL,
  `profid` smallint(6) NOT NULL,
  PRIMARY KEY (`depid`,`uniid`,`proftype`,`profid`),
  CONSTRAINT `fk_heads_1` FOREIGN KEY (`depid`, `uniid`) REFERENCES `departments` (`departmentid`, `universityid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `publications`
--

DROP TABLE IF EXISTS `publications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `publications` (
  `depid` smallint(6) DEFAULT NULL,
  `uniid` smallint(6) DEFAULT NULL,
  `publicationid` smallint(6) DEFAULT NULL,
  `authortype` tinyint(4) DEFAULT NULL,
  `authorid` smallint(6) DEFAULT NULL,
  KEY `idx_p_1` (`depid`),
  KEY `idx_p_2` (`uniid`),
  KEY `idx_p_3` (`publicationid`),
  KEY `idx_p_4` (`authortype`),
  KEY `idx_p_5` (`authorid`),
  KEY `fk_publications_1` (`depid`,`uniid`,`authortype`,`authorid`),
  CONSTRAINT `fk_publications_1` FOREIGN KEY (`depid`, `uniid`, `authortype`, `authorid`) REFERENCES `teachers` (`depid`, `uniid`, `ttype`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ra`
--

DROP TABLE IF EXISTS `ra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ra` (
  `depid` smallint(6) DEFAULT NULL,
  `uniid` smallint(6) DEFAULT NULL,
  `studid` smallint(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `researchgroups`
--

DROP TABLE IF EXISTS `researchgroups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `researchgroups` (
  `depid` smallint(6) NOT NULL,
  `uniid` smallint(6) NOT NULL,
  `id` smallint(6) NOT NULL,
  PRIMARY KEY (`id`,`depid`,`uniid`),
  KEY `fk_researchgroups_1` (`depid`,`uniid`),
  CONSTRAINT `fk_researchgroups_1` FOREIGN KEY (`depid`, `uniid`) REFERENCES `departments` (`departmentid`, `universityid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `students` (
  `depid` smallint(6) NOT NULL,
  `uniid` smallint(6) NOT NULL,
  `stype` tinyint(4) NOT NULL,
  `id` smallint(6) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `degreeuniid` smallint(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `advisortype` tinyint(4) DEFAULT NULL,
  `advisorid` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`depid`,`uniid`,`stype`,`id`),
  KEY `idx_stud_1` (`degreeuniid`),
  KEY `idx_stud_2` (`advisortype`),
  KEY `idx_stud_3` (`advisorid`),
  CONSTRAINT `fk_students_1` FOREIGN KEY (`depid`, `uniid`) REFERENCES `departments` (`departmentid`, `universityid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ta`
--

DROP TABLE IF EXISTS `ta`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ta` (
  `depid` smallint(6) DEFAULT NULL,
  `uniid` smallint(6) DEFAULT NULL,
  `studid` smallint(6) DEFAULT NULL,
  `coursetype` tinyint(4) DEFAULT NULL,
  `courseid` smallint(6) DEFAULT NULL,
  KEY `idx_ta_1` (`depid`),
  KEY `idx_ta_2` (`uniid`),
  KEY `idx_ta_3` (`studid`),
  KEY `idx_ta_4` (`coursetype`),
  KEY `idx_ta_5` (`courseid`),
  KEY `fk_ta_1` (`depid`,`uniid`,`coursetype`,`courseid`),
  CONSTRAINT `fk_ta_1` FOREIGN KEY (`depid`, `uniid`, `coursetype`, `courseid`) REFERENCES `courses` (`depid`, `uniid`, `ctype`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `takescourses`
--

DROP TABLE IF EXISTS `takescourses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `takescourses` (
  `depid` smallint(6) DEFAULT NULL,
  `uniid` smallint(6) DEFAULT NULL,
  `studtype` tinyint(4) DEFAULT NULL,
  `studid` smallint(6) DEFAULT NULL,
  `coursetype` tinyint(4) DEFAULT NULL,
  `courseid` smallint(6) DEFAULT NULL,
  KEY `idx_takescourses_1` (`depid`),
  KEY `idx_takescourses_2` (`uniid`),
  KEY `idx_takescourses_3` (`studtype`),
  KEY `idx_takescourses_4` (`studid`),
  KEY `idx_takescourses_5` (`coursetype`),
  KEY `idx_takescourses_6` (`courseid`),
  KEY `fk_takescourses_1` (`depid`,`uniid`,`studtype`,`studid`),
  KEY `fk_takescourses_2` (`depid`,`uniid`,`coursetype`,`courseid`),
  CONSTRAINT `fk_takescourses_1` FOREIGN KEY (`depid`, `uniid`, `studtype`, `studid`) REFERENCES `students` (`depid`, `uniid`, `stype`, `id`),
  CONSTRAINT `fk_takescourses_2` FOREIGN KEY (`depid`, `uniid`, `coursetype`, `courseid`) REFERENCES `courses` (`depid`, `uniid`, `ctype`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `teachers`
--

DROP TABLE IF EXISTS `teachers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `teachers` (
  `depid` smallint(6) NOT NULL,
  `uniid` smallint(6) NOT NULL,
  `ttype` tinyint(4) NOT NULL,
  `id` smallint(6) NOT NULL,
  `name` varchar(45) DEFAULT NULL,
  `underD` smallint(6) DEFAULT NULL,
  `masterD` smallint(6) DEFAULT NULL,
  `docD` smallint(6) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `research` smallint(6) DEFAULT NULL,
  PRIMARY KEY (`depid`,`uniid`,`ttype`,`id`),
  KEY `idx_teach_1` (`name`),
  KEY `idx_teach_2` (`underD`),
  KEY `idx_teach_3` (`masterD`),
  KEY `idx_teach_4` (`docD`),
  CONSTRAINT `fk_teachers_1` FOREIGN KEY (`depid`, `uniid`) REFERENCES `departments` (`departmentid`, `universityid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-02-26 13:53:55
