#!/bin/bash
mvn install:install-file "-DgroupId=it.sauronsoftware" "-DartifactId=jave" "-Dversion=1.0.2" "-Dpackaging=jar" "-Dfile=jave-1.0.2.jar"
mvn install:install-file "-DgroupId=com.aliyun.alicom" "-DartifactId=alicom-mns-receive-sdk" "-Dversion=1.1.3" "-Dpackaging=jar" "-Dfile=alicom-mns-receive-sdk-1.1.3.jar"
mvn install:install-file "-DgroupId=com.aliyun" "-DartifactId=aliyun-java-sdk-dybaseapi" "-Dversion=1.0.1" "-Dpackaging=jar" "-Dfile=aliyun-java-sdk-dybaseapi-1.0.1.jar"