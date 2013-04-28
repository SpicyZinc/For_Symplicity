/**
Author: Liang Xin

Task:
	For every file "foo.java", jxref creates a file foo.html that looks just like the Java source, 
	except that every keyword is in bold and every method invocation is a live link to docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html. 
    All .html files are grouped together in a subdirectory named XREF, with an extra file index.html that contains a list of all the source files (with links), 
	a special link to main, and information about when and where the xref tool was run.  

*/


Files submitted:
	1. jxref.pl
	2. abstract_gray.jpg
	3. CrossReference.jpg
	4. jxref.css
	(
	5. Hello.java
	6. Primes.java
	) for test

=======================================
To run:

	perl jxref.pl


=======================================

Files generated and Explanations:

After run by perl jxref.pl, it will automatically search for .java files in current directory, and generate a folder XREF, 
in which the same number as the number of java source files of .html files would be generatd. In the current folder, 
.class filess and .txt files corresponding to the same names of .java fils will be generated as well for lexing and parsing.

=======================================
Because of time, I cannot fully finished points as below

index.html cannot generate correctly
may not deal with the condition that a java file which can have many .class files after compilation


