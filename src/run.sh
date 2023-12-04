#clear;
javac -cp 'opencsv 5.8.jar:commons-lang3-3.14.0.jar' $1.java Pair.java; 
java -cp .:'opencsv 5.8.jar:commons-lang3-3.14.0.jar' $1;
