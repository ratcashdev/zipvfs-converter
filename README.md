# zipvfs-converter
Small utility to convert ZIPVFS packaged SQLITE databases to a regular SQLITE file. Such files will start with the `ZV-zlib` magic heade and generally follow the specifications here: http://www.sqlite.org/zipvfs/doc/trunk/www/fileformat.wiki

Also handy to convert and open some NDS files (used in automotive navigation databases, e.g. from navigation.com), in your favorite SQLite GUI, unless those are encrypted.

This utility was born as a result of discussions here: http://www.seatcupra.net/forums/showthread.php?t=388586&page=26

Pull requests are welcome.

##usage

1. build using java and maven, i.e. `mvn package`
2. Execute conversion using `java -jar [path_to/repo]/target/zipvfs-converter-1.0-SNAPSHOT.jar [yourNDSFileHere]`

# TODO

1. Process FreeLists
2. Integrate with SQLJet https://sqljet.com/
3. Ability to WRITE

Note: a good source of information for further work could be found here: http://www.sqlite.org/zipvfs/doc/trunk/www/howitworks.wiki
