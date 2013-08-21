import org.apache.commons.io.FileUtils;

import org.jaudiotagger.audio.*;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Collection;

import java.net.UnknownHostException;
 
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.Mongo;
import com.mongodb.MongoException;

class ParseAndGetTags
{
    public static int songID = 0;
    public static DBCollection initializeMongoCollection (String server, int port, String theDb, String theCollection)
    {
	// connect to mongoDB, ip and port number
	Mongo mongo = null;
	try
	    {
		mongo = new Mongo(server, port);
	    }
	catch (UnknownHostException e)
	    {
		e.printStackTrace ();
	    }
	
	// get database from MongoDB,
	// if database doesn't exists, mongoDB will create it automatically
	if (mongo != null)
	    {
		DB db = mongo.getDB(theDb);
	
		// Get collection from MongoDB, database named "yourDB"
		// if collection doesn't exists, mongoDB will create it automatically
		DBCollection collection = db.getCollection(theCollection);
		
		return collection;
	    }
	return null;
    }

    public static void placeTrackInDb (String mp3AudioFile, DBCollection collection)
    {
	File mp3File = new File (mp3AudioFile);
	BasicDBObject document = new BasicDBObject ();

	if (FileUtils.sizeOf (mp3File) == 0)
	    {
		System.out.println ("CORRUPT (EMPTY) FILE: " + mp3File);
		return;
	    }
	
	try
	    {
		AudioFile f = AudioFileIO.read (mp3File);
		Tag tag = f.getTag (); 
		if (tag != null)
		    readTagsFromMP3AudioFile (document, tag, collection);
		else
		    System.out.println ("NO TAGS FOUND FOR !" + mp3AudioFile);
	    }
	catch (IOException e)
	    {
		e.printStackTrace ();
	    }
	catch (CannotReadException e)
	    {
		e.printStackTrace ();
	    }
	catch (TagException e)
	    {
		e.printStackTrace ();
	    }
	catch (ReadOnlyFileException e)
	    {
		e.printStackTrace ();
	    }
	catch (InvalidAudioFrameException e)
	    {
		e.printStackTrace ();
	    }	
    }

    public static void getFilesInDirectoryIntoCollection (File directoryPath, String [] extensions, boolean recursive, DBCollection collection)
    {
	Collection files = FileUtils.listFiles (directoryPath, extensions, recursive);
	for (Iterator iterator = files.iterator (); iterator.hasNext ();)
	    {
		File file = (File) iterator.next ();
		if (file.isFile ())
		    {
			////System.out.println ("Getting MP3 tags for file = " + file.getAbsolutePath () + " of size = " + FileUtils.sizeOf (directoryPath));
			placeTrackInDb (file.getAbsolutePath (), collection);
		    }
		else
		    {
			getFilesInDirectoryIntoCollection (file, extensions, recursive, collection);
		    }
	    }
    }

    public static void readTagsFromMP3AudioFile (BasicDBObject document, Tag tag, DBCollection collection)
    {
	songID ++;
	//System.out.println ("Song ID = " + songID);
	document.put ("id", songID);
	//System.out.println ("ARTIST      = " + tag.getFirst (FieldKey.ARTIST));
	document.put ("ARTIST", tag.getFirst (FieldKey.ARTIST));
	//System.out.println ("ALBUM       = " + tag.getFirst (FieldKey.ALBUM));
	document.put ("ALBUM", tag.getFirst (FieldKey.ALBUM));
	//System.out.println ("TITLE       = " + tag.getFirst (FieldKey.TITLE));
	document.put ("TITLE", tag.getFirst (FieldKey.TITLE));
	//System.out.println ("COMMENT     = " + tag.getFirst (FieldKey.COMMENT));
	document.put ("COMMENT", tag.getFirst (FieldKey.COMMENT));
	//System.out.println ("YEAR        = " + tag.getFirst (FieldKey.YEAR));
	document.put ("YEAR", tag.getFirst (FieldKey.YEAR));
	//System.out.println ("TRACK       = " + tag.getFirst (FieldKey.TRACK));
	document.put ("TRACK", tag.getFirst (FieldKey.TRACK));
	//System.out.println ("DISC_NO     = " + tag.getFirst (FieldKey.DISC_NO));
	document.put ("DISC_NO", tag.getFirst (FieldKey.DISC_NO));
	//System.out.println ("COMPOSER    = " + tag.getFirst (FieldKey.COMPOSER));
	document.put ("COMPOSER", tag.getFirst (FieldKey.COMPOSER));
	//System.out.println ("ARTIST_SORT = " + tag.getFirst (FieldKey.ARTIST_SORT));
	document.put ("ARTIST_SORT", tag.getFirst (FieldKey.ARTIST_SORT));

	collection.insert (document);
    }

    public static void main (String [] args)
    {
	DBCollection collection = null;

	try
	    {
		////System.out.println ("Trying root " + args[0]);
		String [] extensions = {"mp3", "MP3", "mP3", "Mp3"};
		File root = new File (args[0]);
		boolean recursive = true;
		
		collection = initializeMongoCollection ("localhost", 27017, "REST", "sonnysMusicCollection");
		if (root.isDirectory ())
		    {
			getFilesInDirectoryIntoCollection (root, extensions, recursive, collection);
		    }
		else // Assume it is an MP3 file.
		    {
			////System.out.println ("Getting MP3 tags for file = " + args [0] + " of size = " + FileUtils.sizeOf (root));
			if (FileUtils.sizeOf (root) != 0)
			    {
				placeTrackInDb (args [0], collection);
			    }
			else
			    {
				System.out.println ("CORRUPT (EMPTY) FILE: " + args [0]);
			    }
		    }
	    }
	catch (Exception e)
	    {
		e.printStackTrace ();
	    }

	//System.out.println ("-----------");
	//System.out.println ("-----------");
	//System.out.println ("-----------");
	//System.out.println ("-----------");
	//System.out.println ("-----------");
	//System.out.println ("-----------");
	//System.out.println ("-----------");
	//System.out.println ("-----------");

	BasicDBObject searchQuery = new BasicDBObject ();
	searchQuery.put ("ARTIST", "Pink Floyd");

	DBCursor cursor = collection.find (searchQuery);

	// loop over the cursor and display the retrieved result
	while (cursor.hasNext()) 
	    {
		//System.out.println(cursor.next());
	    }
	
	//System.out.println("Done");
    }
}