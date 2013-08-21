package com.sonnyrajagopalan.cdcollection.services;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import java.util.regex.Pattern;
//import com.mongodb.WriteConcern;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBCursor;
import com.mongodb.ServerAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

@Path("/tracksIn")
public class MP3TrackResource 
{
    Mongo mongoClient = null;
    DB db = null;
    DBCollection collection = null;

    public MP3TrackResource() 
    {
	// Connect to the db.
	try
	    {
		mongoClient = new Mongo( "localhost" , 27017 );
	    }
	catch (UnknownHostException e)
	    {
		e.printStackTrace ();
	    }
	
	
	if (mongoClient != null)
	    {
		db = mongoClient.getDB( "REST" );
		collection = db.getCollection("sonnysMusicCollection");
	    }
    }
    @GET
	@Path("/")
	@Produces ("text/plan")
	public StreamingOutput getDefaultMessage ()
    {
	return new StreamingOutput ()
	    {
		public void write (OutputStream outputStream) throws IOException, WebApplicationException
		{
		    outputDefaultMessage (outputStream);
		}
	};
    }

    protected void outputDefaultMessage (OutputStream os) throws IOException
    {
	PrintStream writer = new PrintStream (os);
	writer.println ("/tracksIn resource.");
	writer.println ("Given an album name string (or substring, which is case sensitive), return the tracks in the album(s).");
	writer.println ("E.g.:");
	writer.println ("http://localhost:9095/tracksIn/Colossus");
	writer.println ("returns");
	writer.println("Strode Rode");
	writer.println("Blue 7");
	writer.println("St. Thomas");
	writer.println("You Don't Know What Love Is");
	writer.println("Moritat");
    }

   @GET
       @Path("{name}") // Spaces (" ") replaced by ("%20")
       @Produces({"text/plain"})
       public StreamingOutput getSong(@PathParam("name") String name) 
    {
	BasicDBObject searchQuery = new BasicDBObject ();
	BasicDBObject searchField = new BasicDBObject ();

	// Convert the album name to an album name that is recognizable 
	// (re-introduce literal spaces, and potentially remove punctuation)
	name.replace ("%20", " ");

	//searchQuery.put ("ARTIST", java.util.regex.Pattern.compile(name));
	searchQuery.put ("ALBUM", java.util.regex.Pattern.compile(name));
	searchField.put ("TITLE", 1);
	searchField.put ("_id", 0);
	final DBCursor cursor = collection.find (searchQuery, searchField);

	// Get the song from the mongo db.
	if (cursor == null) 
	    {
		throw new WebApplicationException(Response.Status.NOT_FOUND);
	    }
	return new StreamingOutput() 
	    {
		public void write(OutputStream outputStream) throws IOException, WebApplicationException 
		{
		    outputSong(outputStream, cursor);
		}
	};
    }
    
    //@PUT
    //Cannot PUT to /songs
    
    // Helper function to translate DB output to text/plain
    protected void outputSong(OutputStream os, DBCursor cursor) throws IOException 
    {
	PrintStream writer = new PrintStream(os);
	while (cursor.hasNext ())
	    {
		BasicDBObject obj = (BasicDBObject) cursor.next ();
		writer.println (obj.getString ("TITLE"));
	    }
    }        
}
