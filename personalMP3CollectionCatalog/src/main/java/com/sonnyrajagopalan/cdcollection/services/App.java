package com.sonnyrajagopalan.cdcollection.services;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class App extends Application {
   private Set<Object> singletons = new HashSet<Object>();
   private Set<Class<?>> empty = new HashSet<Class<?>>();

   public App() {
       singletons.add(new MP3SongResource ()); // Add each resource's singleton.
       singletons.add(new MP3AlbumResource ());
       singletons.add(new MP3TrackResource ());
       singletons.add(new MP3ComposerResource ());
   }

   @Override
   public Set<Class<?>> getClasses() {
      return empty;
   }

   @Override
   public Set<Object> getSingletons() {
      return singletons;
   }
}
