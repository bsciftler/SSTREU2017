# Security of Smart Things REU 2017  
1- REUServer directory holds all the Java code running on the Server  
2- The other file (REU2017.zip) contains the Android Client Side Code  
3- Generation.java is just a file I used to automate encrypting RSS values squared.

IMPORTANT NOTICE:  
THE FOLLOWING CLASSES IN REU SERVER  
- SecureTriple  
- UnsecutreTriple  
- TrainingArray  
must be stored within a package called edu.fiu.reu2017.  
This solves the Class Not Found exception when the Android app sends those classes through the socket.  

Also this program requires the MySQL Driver found here: https://dev.mysql.com/downloads/connector/j/ so the localhost can communicate with the SQL Server.
