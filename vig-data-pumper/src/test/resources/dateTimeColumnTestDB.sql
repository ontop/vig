CREATE TABLE `dateTimeColTest` (
  `col` date DEFAULT ,
  `apaAreaGeometry_KML_WGS84` text NOT NULL,
  `apaAreaGross_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`apaAreaGross_id`),
  UNIQUE KEY `apaAreaGross_id` (`apaAreaGross_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
