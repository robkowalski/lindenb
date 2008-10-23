package org.lindenb.wikipedia.api;

/** http://svn.wikimedia.org/viewvc/mediawiki/trunk/phase3/includes/Defines.php */
public enum MWNamespace {
	 Main(0),
     Talk(1),
     User(2),
     User_talk(3),
     Project(4),
     Project_talk(5),
     Image(6),
     Image_talk(7),
     Mediawiki(8),
     Mediawiki_talk(9),
     Template(10),
     Template_talk(11),
     Help(12),
     Help_talk(13),
     Category(14),
     Category_talk(15)
     ;

     private final int id;
     MWNamespace(int id)
             {
             this.id=id;
             }
     public int getId() { return id;}

	}
