# ether

java -cp ".:lib/*:ether.jar"  -DTaConfigFile="config/ta.properties" org.tdod.ether.Genesis

mvn install:install-file -Dfile=lib/pwts.jar -DgroupId=com.meyling.telnet -DartifactId=TelnetD -Dversion=0.0.1 -Dpackaging=jar

