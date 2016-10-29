@echo off
mvn install:install-file -Dfile=hyperapplet.jar -DgroupId=hypergraph -DartifactId=hyperapplet -Dversion=1.0.2 -Dpackaging=jar 

pause