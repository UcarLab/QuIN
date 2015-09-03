CREATE SCHEMA `chiapet` ;
CREATE SCHEMA `chiapetdata` ;
CREATE SCHEMA `clinvar` ;
CREATE SCHEMA `dbsnp` ;
CREATE SCHEMA `diseaselists` ;
CREATE SCHEMA `genelists` ;
CREATE SCHEMA `gwas` ;
CREATE SCHEMA `ncbi` ;
CREATE SCHEMA `regionlists` ;
CREATE SCHEMA `snplists` ;
CREATE SCHEMA `ucsc` ;
CREATE SCHEMA `usersessions` ;


CREATE TABLE `ChiapetData` (
  `fid` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=latin1;
CREATE TABLE `DiseaseLists` (
  `fid` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
CREATE TABLE `GeneLists` (
  `fid` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `Networks` (
  `fid` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=latin1;
CREATE TABLE `RegionLists` (
  `fid` int(11) NOT NULL AUTO_INCREMENT,
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=latin1;
CREATE TABLE `Sessions` (
  `UID` int(11) NOT NULL AUTO_INCREMENT,
  `PHRASE` varchar(45) NOT NULL,
  `LASTUSED` datetime NOT NULL,
  PRIMARY KEY (`UID`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=latin1;
CREATE TABLE `SNPLists` (
  `fid` bigint(20) NOT NULL AUTO_INCREMENT,
  `uid` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`fid`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;
