-- MySQL dump 10.13  Distrib 5.5.37, for debian-linux-gnu (x86_64)
--
-- Host: 10.7.20.39    Database: pumperTest
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
-- Table structure for table `dateTest`
--

DROP TABLE IF EXISTS `dateTest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `dateTest` (
  `id` int(10) DEFAULT NULL,
  `date` date DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dateTest`
--

LOCK TABLES `dateTest` WRITE;
/*!40000 ALTER TABLE `dateTest` DISABLE KEYS */;
/*!40000 ALTER TABLE `dateTest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `datetimeTest`
--

DROP TABLE IF EXISTS `datetimeTest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `datetimeTest` (
  `id` int(10) DEFAULT NULL,
  `date` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `datetimeTest`
--

LOCK TABLES `datetimeTest` WRITE;
/*!40000 ALTER TABLE `datetimeTest` DISABLE KEYS */;
/*!40000 ALTER TABLE `datetimeTest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fKeyA`
--

DROP TABLE IF EXISTS `fKeyA`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fKeyA` (
  `id` int(10) NOT NULL DEFAULT '0',
  `value` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `chiaveA` (`value`),
  CONSTRAINT `chiaveA` FOREIGN KEY (`value`) REFERENCES `fKeyB` (`value`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fKeyA`
--

LOCK TABLES `fKeyA` WRITE;
/*!40000 ALTER TABLE `fKeyA` DISABLE KEYS */;
/*!40000 ALTER TABLE `fKeyA` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fKeyB`
--

DROP TABLE IF EXISTS `fKeyB`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `fKeyB` (
  `id` int(10) DEFAULT NULL,
  `value` varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (`value`),
  KEY `chiaveB` (`id`),
  CONSTRAINT `chiaveB` FOREIGN KEY (`id`) REFERENCES `fKeyA` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fKeyB`
--

LOCK TABLES `fKeyB` WRITE;
/*!40000 ALTER TABLE `fKeyB` DISABLE KEYS */;
/*!40000 ALTER TABLE `fKeyB` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pkeyTest`
--

DROP TABLE IF EXISTS `pkeyTest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pkeyTest` (
  `id` int(10) NOT NULL DEFAULT '0',
  `value` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pkeyTest`
--

LOCK TABLES `pkeyTest` WRITE;
/*!40000 ALTER TABLE `pkeyTest` DISABLE KEYS */;
/*!40000 ALTER TABLE `pkeyTest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pointTest`
--

DROP TABLE IF EXISTS `pointTest`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `pointTest` (
  `id` int(10) DEFAULT NULL,
  `point` point DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pointTest`
--

LOCK TABLES `pointTest` WRITE;
/*!40000 ALTER TABLE `pointTest` DISABLE KEYS */;
/*!40000 ALTER TABLE `pointTest` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `selfDependency`
--

DROP TABLE IF EXISTS `selfDependency`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `selfDependency` (
  `id` int(10) NOT NULL DEFAULT '0',
  `value` varchar(40) DEFAULT NULL,
  `id1` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id1` (`id1`),
  CONSTRAINT `fk1` FOREIGN KEY (`id`) REFERENCES `selfDependency` (`id1`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `selfDependency`
--

LOCK TABLES `selfDependency` WRITE;
/*!40000 ALTER TABLE `selfDependency` DISABLE KEYS */;
/*!40000 ALTER TABLE `selfDependency` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `testAutoincrement`
--

DROP TABLE IF EXISTS `testAutoincrement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `testAutoincrement` (
  `id` int(10) DEFAULT NULL,
  `id1` bigint(10) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id1`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testAutoincrement`
--

LOCK TABLES `testAutoincrement` WRITE;
/*!40000 ALTER TABLE `testAutoincrement` DISABLE KEYS */;
/*!40000 ALTER TABLE `testAutoincrement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `testBinaryKey`
--

DROP TABLE IF EXISTS `testBinaryKey`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `testBinaryKey` (
  `id` int(10) NOT NULL DEFAULT '0',
  `id1` int(10) NOT NULL DEFAULT '0',
  `value` varchar(40) DEFAULT NULL,
  PRIMARY KEY (`id`,`id1`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testBinaryKey`
--

LOCK TABLES `testBinaryKey` WRITE;
/*!40000 ALTER TABLE `testBinaryKey` DISABLE KEYS */;
/*!40000 ALTER TABLE `testBinaryKey` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `testLinestring`
--

DROP TABLE IF EXISTS `testLinestring`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `testLinestring` (
  `id` int(10) DEFAULT NULL,
  `linestring` linestring DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testLinestring`
--

LOCK TABLES `testLinestring` WRITE;
/*!40000 ALTER TABLE `testLinestring` DISABLE KEYS */;
/*!40000 ALTER TABLE `testLinestring` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `testMultilinestring`
--

DROP TABLE IF EXISTS `testMultilinestring`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `testMultilinestring` (
  `id` int(10) DEFAULT NULL,
  `multilinestring` multilinestring DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testMultilinestring`
--

LOCK TABLES `testMultilinestring` WRITE;
/*!40000 ALTER TABLE `testMultilinestring` DISABLE KEYS */;
/*!40000 ALTER TABLE `testMultilinestring` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `testMultipolygon`
--

DROP TABLE IF EXISTS `testMultipolygon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `testMultipolygon` (
  `id` int(10) DEFAULT NULL,
  `multipolygon` multipolygon DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testMultipolygon`
--

LOCK TABLES `testMultipolygon` WRITE;
/*!40000 ALTER TABLE `testMultipolygon` DISABLE KEYS */;
/*!40000 ALTER TABLE `testMultipolygon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `testPolygon`
--

DROP TABLE IF EXISTS `testPolygon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `testPolygon` (
  `id` int(10) DEFAULT NULL,
  `polygon` polygon DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `testPolygon`
--

LOCK TABLES `testPolygon` WRITE;
/*!40000 ALTER TABLE `testPolygon` DISABLE KEYS */;
/*!40000 ALTER TABLE `testPolygon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `trivial`
--

DROP TABLE IF EXISTS `trivial`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trivial` (
  `id` int(15) DEFAULT NULL,
  `name` varchar(40) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trivial`
--

LOCK TABLES `trivial` WRITE;
/*!40000 ALTER TABLE `trivial` DISABLE KEYS */;
/*!40000 ALTER TABLE `trivial` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2014-06-18 14:52:21
