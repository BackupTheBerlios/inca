/*
 * $Id: CollisionableHashtable.java,v 1.1 2005/02/17 18:34:51 achim Exp $
 * 
 * This file is part of the Foafscape project (http://foafscape.berlios.de)
 * 
 * Copyright (C) 2004 Universit�t Stuttgart 
 * 
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the 
 * Free Software Foundation; either version 2 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software Foundation, 
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Author: Mark Giereth
 */
package org.inca.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * A hashtable extension in order to handle collisions. A collision occurs if a
 * new item is added under the same key an existing item is stored. In case of
 * the first collision a List object is created and the old and the new value is
 * added to the list, which is stored under the same key. Each new item is then
 * added to the list. If there are 2 items in such a list and one item is
 * removed then the List is also removed and the remaining item is stored as in
 * a normal hashtable value. <br>
 * <b>Note that this handling can cause type differences. If two String objects
 * are put to the hashtable under a key x, then a List object is returned when
 * getting the value for x. </b> <br>
 * This implementation also provides indexing and sorting abilities for keys and
 * values.
 * 
 * @author Mark Giereth
 */
public class CollisionableHashtable {

   public static Comparator DEFAULT_DESC_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
         if (o1 instanceof Comparable && o2 instanceof Comparable) {
            return ((Comparable) o1).compareTo(o2);
         } else {
            return 0;
         }
      }
   };

   public static Comparator DEFAULT_ASC_COMPARATOR = new Comparator() {
      public int compare(Object o1, Object o2) {
         if (o1 instanceof Comparable && o2 instanceof Comparable) {
            return ((Comparable) o2).compareTo(o1);
         } else {
            return 0;
         }
      }
   };

   /** The underlaying hashtable nearly all methods delegate to. */
   private Hashtable m_hashtable;

   /** List of key for the hashtable. Used to make the keys indexable. */
   private ArrayList m_keyList;

   /** List of values for the hashtable. Used to make the values indexable. */
   private ArrayList m_valueList;

   private boolean m_distinctValuesOnly;

   /**
    * Creates a new instance where the values of a key must be distinct (i.e. a
    * values set).
    */
   public CollisionableHashtable() {
      m_hashtable = new Hashtable();
      m_keyList = new ArrayList();
      m_valueList = new ArrayList();
      m_distinctValuesOnly = false;
   }

   /**
    * Creates a new instance.
    * @param distinctValuesOnly if true then the values of a key must be
    * distinct (i.e. a values set). If false then multiple items with the same
    * value can be stored under one key (i.e. a values list).
    */
   public CollisionableHashtable(boolean distinctValuesOnly) {
      m_hashtable = new Hashtable();
      m_keyList = new ArrayList();
      m_valueList = new ArrayList();
      m_distinctValuesOnly = distinctValuesOnly;
   }

   /**
    * Creates a new instance with the given initial capacity.
    * @param initialCapacity
    */
   public CollisionableHashtable(int initialCapacity) {
      m_hashtable = new Hashtable(initialCapacity);
      m_keyList = new ArrayList(initialCapacity + 1);
      m_valueList = new ArrayList(initialCapacity + 1);
      m_distinctValuesOnly = false;
   }

   /**
    * Creates a new instance with the given initial capacity and load factor.
    * @param initialCapacity
    * @param loadFactor
    */
   public CollisionableHashtable(int initialCapacity, float loadFactor) {
      m_hashtable = new Hashtable(initialCapacity, loadFactor);
      m_keyList = new ArrayList(initialCapacity + 1);
      m_valueList = new ArrayList(initialCapacity + 1);
      m_distinctValuesOnly = false;
   }

   /**
    * Empties the hashtable.
    */
   public void clear() {
      m_hashtable.clear();
      m_keyList.clear();
      m_valueList.clear();
   }

   /**
    * Returns true if a key is contained in the hashtable, false otherwise.
    * @return true if a key is contained in the hashtable, false otherwise
    */
   public boolean containsKey(Object key) {
      return m_keyList.contains(key);
   }

   /**
    * Returns true if a value is contained in the hashtable, false otherwise.
    * @return true if a value is contained in the hashtable, false otherwise
    */
   public boolean containsValue(Object value) {
      return m_valueList.contains(value);
   }

   /**
    * Gets the object stored under the given key. <b>Note: </b> this mothod
    * <b>might return a List object </b> although the original objects stored
    * under the key had another type. This is the case if more then one object
    * has been stored under one key. The list contains all the values stored
    * under the same key in an <b>arbitrary order </b>.
    * @param key the key
    * @return the original object or a list of objects.
    */
   public Object get(Object key) {
      return m_hashtable.get(key);
   }

   /**
    * Gets all keys of this hashtable as list.
    * @return List object containing all keys.
    */
   public List getKeys() {
      return (List) m_keyList.clone();
   }

   /**
    * Sorts the keys using the given Comparator object.
    * @param comparator spezifies the order of the keys.
    * @return the old ordered key list.
    */
   public List sortKeys(Comparator comparator) {
      List old = (List) m_keyList.clone(); // remember the old List
      if (comparator != null) {
         Collections.sort(m_keyList, comparator);
      }
      return old;
   }

   /**
    * Sorts the keys using the given Comparator object.
    * @param comparator spezifies the order of the keys.
    * @return the old ordered key list.
    */
   public List sortValues(Comparator comparator) {
      List old = (List) m_valueList.clone(); // remember the old List
      if (comparator != null) {
         Collections.sort(m_valueList, comparator);
      }
      return old;
   }

   /**
    * Gets the key at position i.
    * @param i 0 &lt;= i &th; getKeyCount()
    * @return the key at position i.
    */
   public Object getKey(int i) {
      return m_keyList.get(i);
   }

   /**
    * Gets the value at position i.
    * @param i 0 &lt;= i &th; getValueCount()
    * @return the value at position i.
    */
   public Object getValue(int i) {
      return m_valueList.get(i);
   }

   /**
    * Gets all values of this hashtable as list.
    * @return List object containing all values.
    */
   public List getValues() {
      return (List) m_valueList.clone();
   }

   /**
    * Adds the given key-value pair to the hashtable.
    * @param key the key
    * @param value the value
    * @return the old value stored under the key
    */
   public Object put(Object key, Object value) {
      if (key == null || value == null) return null;

      Object old = get(key);
      if (old == null) {
         // new key-value pair -> add it to the hashtable
         m_hashtable.put(key, value);
         // add the key to the key list
         m_keyList.add(key);
         // add value to the value list
         // -> NOTE: m_valueList might contain a value n-times
         m_valueList.add(value);

      } else {
         // key-value pair already exists
         if (old instanceof MyList) {
            // more then one values for the key
            // -> add value to list
            MyList list = (MyList) old;
            if (m_distinctValuesOnly) {
               // only add distinct values to list
               // -> check value already exists
               if (!list.contains(value)) {
                  old = list.clone();
                  list.add(value);
                  m_valueList.add(value);
               }
            } else {
               old = list.clone();
               list.add(value);
               m_valueList.add(value);
            }
            // the key must not be added a second time

         } else {
            // existing single value
            // -> make list and add both values
            if (m_distinctValuesOnly) {
               // only add distinct values to list
               // -> check if old == new value
               if (!old.equals(value)) {
                  MyList list = new MyList();
                  list.add(old);
                  list.add(value);
                  // store list under the same key
                  m_hashtable.put(key, list);
                  // the key must not be added a second time
                  m_valueList.add(value);
               }
            } else {
               MyList list = new MyList();
               list.add(old);
               list.add(value);
               // store list under the same key
               m_hashtable.put(key, list);
               // the key must not be added a second time
               m_valueList.add(value);
            }
         }
      }
      return old;
   }

   /**
    * Remove all values stored under the given key.
    * @param key the key
    * @return the old value stored under the key
    */
   public Object remove(Object key) {
      if (key == null) return null;
      m_keyList.remove(key);
      // remove all values of the key
      Object value = m_hashtable.get(key);
      if (value != null) {
         if (value instanceof MyList) {
            MyList list = (MyList) value;
            for (int i = 0; i < list.size(); i++) {
               // Note: this might change the order of items in valueList!
               m_valueList.remove(list.get(i));
            }
         } else {
            m_valueList.remove(value); // just remove the value
         }
      } // else we don't have to remove anything
      return m_hashtable.remove(key); // might remove all other values
   }

   /**
    * Gets the size of this hashtable which is the total number of keys in this
    * hashtable.
    * @return the total number of keys
    */
   public int size() {
      return m_hashtable.size();
   }

   /**
    * Gets the total number of keys in this hashtable.
    * @return the total number of keys
    */
   public int getKeyCount() {
      return m_hashtable.size();
   }

   /**
    * Removes only the given value under the given key from the hashtable. If
    * there are other values stored under the key they are not removed.
    * @param key the key.
    * @param value the value.
    */
   public Object remove(Object key, Object value) {
      if (key == null || value == null) {
         return null;

      } else {
         Object old = get(key);
         if (old == null) {
            // nothing to remove
            return null;

         } else {
            if (old instanceof MyList) {
               // more then on items -> remove from list
               MyList list = (MyList) old;
               old = list.clone();
               if (list.contains(value)) {
                  if (list.size() == 1) {
                     // this should not be the case!
                     list.remove(value);
                     m_valueList.remove(value);
                     list.clear();
                     m_hashtable.remove(key);
                     m_keyList.remove(key);

                  } else if (list.size() == 2) {
                     list.remove(value);
                     m_valueList.remove(value);
                     // now the list contains only one item
                     // -> change list to single object
                     Object item = list.get(0);
                     list.clear();
                     m_hashtable.put(key, item);

                  } else {
                     // remove value from list and from m_valueList
                     list.remove(value);
                     m_valueList.remove(value);
                  }
               } else {
                  // nothing to remove: value is not in list
               }

            } else if (old.equals(value)) {
               // only one item -> also remove key
               m_keyList.remove(key);
               m_valueList.remove(value);
               m_hashtable.remove(key);

            } else {
               // do nothing
            }

            return old;
         }
      }
   }

   /**
    * True if getValueCount(key) &gt; 1, false otherwise.
    * @param key the key.
    * @return true if the value stored under the key is a List object.
    */
   public boolean isList(Object key) {
      return getValueCount(key) > 1;
   }

   /**
    * Gets the number of values stored under the given key. If this method
    * returns 0 then the key does not exist. If it returns 1 then a normal value
    * object has been stored (no collision). If it returns an int &gt; 1 then
    * the value is a List object and n-1 collisions have occured.
    * @param key the key.
    * @return number of values stored under the key.
    */
   public int getValueCount(Object key) {
      Object value = get(key);
      if (value == null) {
         return 0;
      } else if (value instanceof MyList) {
         return ((MyList) value).size();
      } else {
         return 1;
      }
   }

   /**
    * Gets the total number of values stored in the hashtable (including the
    * values stored in lists).
    * @return the total number of values stored in the hashtable
    */
   public int getValueCount() {
      return m_valueList.size();
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer();
      Iterator keys = m_keyList.iterator();
      buffer.append("{\n");
      while (keys.hasNext()) {
         Object key = keys.next();
         Object value = this.get(key);
         buffer.append("  " + key + ", " + value + "\n");
      }
      buffer.append("}");
      return buffer.toString();
   }

   /**
    * The class used to store multiple values. This marker class is used in
    * order to allow ArrayLists Objects also to be stored.
    * @author Mark Giereth
    */
   private class MyList extends ArrayList {

      public String toString() {
         StringBuffer buffer = new StringBuffer();
         buffer.append("[");
         for (int i = 0; i < this.size(); i++) {
            buffer.append(this.get(i));
            if (i < (this.size() - 1)) {
               buffer.append(", ");
            }
         }
         buffer.append("]");
         return buffer.toString();
      }

   }

   // some test cases
   public static void main(String[] args) {
      CollisionableHashtable ht = new CollisionableHashtable();
      System.out.println("isList(key) " + ht.isList("key"));
      System.out.println("getValueCount(key) " + ht.getValueCount("key"));

      System.out.println("put(key,1) " + ht.put("key", new Integer(1))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key,1) " + ht.put("key", new Integer(1))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key,1) " + ht.put("key", new Integer(1))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key,2) " + ht.put("key", new Integer(2))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key,2) " + ht.put("key", new Integer(2))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key,3) " + ht.put("key", new Integer(3))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key,3) " + ht.put("key", new Integer(3))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key,3) " + ht.put("key", new Integer(3))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("isList(key) " + ht.isList("key"));

      System.out.println("put(key1,3) " + ht.put("key1", new Integer(3))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key1"));
      System.out.println("isList(key1) " + ht.isList("key1"));

      System.out.println("put(key1,2) " + ht.put("key1", new Integer(2))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key1"));
      System.out.println("isList(key1) " + ht.isList("key1"));

      System.out.println("\nvalues:" + ht.getValues());
      System.out.println("\nkeys:" + ht.getKeys());
      System.out.println(ht);

      System.out.println();
      System.out.println("remove(key,2) " + ht.remove("key", new Integer(2))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,2) " + ht.remove("key", new Integer(2))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,3) " + ht.remove("key", new Integer(3))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,3) " + ht.remove("key", new Integer(3))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,3) " + ht.remove("key", new Integer(3))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,1) " + ht.remove("key", new Integer(1))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,1) " + ht.remove("key", new Integer(1))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,1) " + ht.remove("key", new Integer(1))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));
      System.out.println("remove(key,1) " + ht.remove("key", new Integer(1))
            + "\t\t#value=" + ht.getValueCount() + "\t#key=" + ht.size()
            + "\t#valuesOfKey=" + ht.getValueCount("key"));
      System.out.println("get(key) " + ht.get("key"));

      System.out.println("\nvalues:" + ht.getValues());

      //ht.clear();
      ht.remove(null);
      ht.remove(null, null);
      ht.remove("key1", null);
      ht.remove(null, new Integer(2));
      ht.put("key2", null);
      ht.put(null, null);
      ht.put(null, new Integer(1000));

      System.out.println("\n" + ht);

   }

   /**
    * @return
    */
   public boolean isDistinctValuesOnly() {
      return m_distinctValuesOnly;
   }

   /**
    * @return
    */
   public Hashtable getHashtable() {
      return (Hashtable) m_hashtable.clone();
   }

}