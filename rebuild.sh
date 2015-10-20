echo "Compiling"
mvn clean compile assembly:single
echo "Creating Directories (errors here are ok)"
mkdir runme
mkdir runme/strategies
echo "Moving & Copying Fresh Data (errors here are not ok)"
mv target/engine-1.0-SNAPSHOT-jar-with-dependencies.jar runme/hashbuddy.jar
cp strategies/* -Rv runme/strategies/
echo "Rebuild is complete, please enjoy your hashbuddy!"

