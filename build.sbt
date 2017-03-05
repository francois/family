organization := "teksol"
name := "family"
version := "1.0"
scalaVersion := "2.12.1"

// https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.1"

// https://mvnrepository.com/artifact/org.springframework/spring-core
libraryDependencies += "org.springframework" % "spring-jdbc" % "4.3.6.RELEASE"

// https://mvnrepository.com/artifact/com.jolbox/bonecp
libraryDependencies += "com.jolbox" % "bonecp" % "0.8.0.RELEASE"

// https://mvnrepository.com/artifact/org.postgresql/postgresql
libraryDependencies += "org.postgresql" % "postgresql" % "42.0.0.jre7"

// https://mvnrepository.com/artifact/org.eclipse.jetty/jetty-server
libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.4.2.v20170220"
