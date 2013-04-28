#!/usr/local/bin/perl
use 5.010;
use File::Copy;

# keywords
@keywords = ("abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean",
				"do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public",
				"throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final",
				"interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super",
				"while");


	@lines_dir = `dir`;
	foreach $line_dir (@lines_dir) {
		if($line_dir =~ m/\s+(\S+)(\.java)$/){
			$file = $1;
			push @javaFiles, $file;				 
			print "Found $file.java in current $dir\n";
		}
	}	
	print @javaFiles . " of Java Files\n";
	
	# Create a XREF folder
		$folder = "XREF";
		mkdir($folder);	

	# move css to XREF
		
		$filetobecopied = "./jxref.css";
		$newfile = "XREF/jxref.css";
		copy($filetobecopied, $newfile) or die "File cannot be copied.";
		
	# generate index.html file
	$index = "index";
	open(HTML, ">XREF/$index.html");
	print HTML "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
	print HTML "\n";
    print HTML "<html>\n";
	print HTML "<head>\n";
	print HTML "<title>index</title>\n";
	print HTML "<link href=\"jxref.css\" type=\"text/css\" rel=\"stylesheet\" \/>\n";	
	print HTML "</head>\n";
	print HTML "<body>\n";
	print HTML "<h3>";
	print HTML "INDEX";
	print HTML "</h3>\n";
	print HTML "<br \/>\n";
	print HTML "<br \/>\n";
	print HTML "<ul>";
	
	for($i=0; $i<@javaFiles; $i++){
	
		print "=======================================\n";
		print "No " . ($i+1) . ":  $javaFiles[$i].java\n\n";
	
		print HTML "<li>";
		print HTML "<a href = \"$javaFiles[$i].html\">$javaFiles[$i].java</a>\n";
		print HTML "</li>";	
		# Read constant pool to get method name
			$cmd1 = "javac $javaFiles[$i].java";
			$cmd2 = "javap -verbose -private $javaFiles[$i].class > $javaFiles[$i].txt";
			system($cmd1);
			system($cmd2);
			if($? == -1){
				print "command failed";
			}
		# javap same java file name to ().txt file	
			open(FILE, "$javaFiles[$i].txt") || die "Error: $!\n";

			while ($_ = <FILE>) {
				# get all Methodref
				if (index($_, "Methodref") >= 0){
					push @lines_constant_pool, $_;
				}
				# get line number to identify method declare
				if($_ =~ /(line\s+\d+)(.*)/){
					push @source_code_line, $1;
					#print "THis is Line " . $1 ."\n";
				}
			}
		# initialization of %hashMethodClass
		# do not forget to clean hash array after each iteration
			%hashMethodClass = ("method", "class");
			
			foreach $aLine_constant_pool (@lines_constant_pool){
				@lineArray = split /[\s]+/, $aLine_constant_pool;
				$aToken = $lineArray[6];
				@aMethod = split /[:]/, $aToken;
				# $classMethod is useful in <a ref></a>
				# use Hash to store method : class pairs and get class later for link use
				$classMethod = $aMethod[0];
				@runMethod = split /[.]/, $classMethod;
				$linkMethod = $runMethod[$#runMethod];
				$hashMethodClass{$linkMethod} = $runMethod[0];
				
				push @linkMethods, $linkMethod; # @linkMethods are all methods extracted from javap file
				# print $linkMethod . "\n";
			}
			print "==============================================================================\n";
			
			open(FILE, $javaFiles[$i].".java") || die "Error: $!\n";
			
			while ($_ = <FILE>) {
				push @lines, $_;
			}
			
			open(HTML, ">XREF/$javaFiles[$i].html");
			print HTML "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">";
			print HTML "\n";
			print HTML "<html>\n";
			print HTML "<head>\n";
			print HTML "<title>JXREF</title>\n";
			print HTML "<link href=\"jxref.css\" type=\"text/css\" rel=\"stylesheet\" \/>\n";	
			print HTML "</head>\n";
			print HTML "<body>\n";
			print HTML "<h2>";
			print HTML "Cross-Referencing Tool Jxref in Perl";
			print HTML "</h2>\n";
			print HTML "<img src = \"http://www.futek.com/images/email/02_08/CrossReference.jpg\" alt = \"cross reference\">\n";
			print HTML "<br \/>\n";
			print HTML "<br \/>\n";
			
		# ========================================================================================================
			# keep updating the currentLine
			$searchForEnd = 0; # for comment
			$searchForRightQ = 0; # for quotation
			
			for($l = 0; $l < @lines; $l++) {
				$currentLine = $lines[$l];
			#foreach $currentLine (@lines){
				$lineCNT = $l + 1;
				
			# each line there is a space, set $searchForSpace true
				$searchForSpace = 1; # for quotation
				
				chomp($currentLine);
				#print $currentLine . "\nCONTROL\n";
				print HTML "<p class = \"contents\">";
				print HTML $lineCNT . "      ";
				#print HTML ($_ + 1) . "      ";
				
				while( $currentLine ne "" ){
					
					if($searchForSpace){
						if($currentLine =~ m/(\s*)(.*)/){
							$token = $1;
							$currentLine = $2;
							$searchForSpace = 0;
							&handle_space($token);					
							print $token;
						} 
					}# end 1st if
					
					
					# comment
					elsif($searchForEnd){
						
						if($currentLine =~ /(.*\*\/)(.*$)/){
							$token = $1;
							$currentLine = $2;
							$searchForEnd = 0;
							&handle_oneLinePrint($token);
							print $token;
						}
						# mean still in multi comment
						else{
							$token = $currentLine;
							$currentLine = "";
							$searchForEnd = 1;
							&handle_oneLineEnter($token);
							print $token . "\n";
						}
						
					}
					
					
					# quotation
					elsif($searchForRightQ){
						
						if($currentLine =~ /(^.*\\\")(.*)/){
							$token = $1;
							$currentLine = $2;
							$searchForRightQ = 1;
							&handle_oneLinePrint($token);
							print $token;
						}
						elsif($currentLine =~ /(^.*\")(.*)/){
							$token = $1;
							$currentLine = $2;
							$searchForRightQ = 0;
							&handle_oneLinePrint($token);
							print $token;
						}				
						
						
					}

					
					# normal case
					else{
						# key is to add ^
						if($currentLine =~ m/(^[A-Za-z_][a-zA-Z_\d]*)(.*)/){
							$token = $1;
							$currentLine = $2;
							$searchForSpace = 1;
							&handle_method_declare($token, $lineCNT);
							&handle_id($token, $lineCNT);					
							print $token;
							#print $currentLine . "RestLine\n";
						}
						
						
						
						elsif(( $currentLine =~ /^\/\/.*$/ ) || ( $currentLine =~ /^\/\*.*\*\// )){
							# no need to use () and $1 to extract a token 
							$token = $currentLine;
							$currentLine = "";
							&handle_oneLineEnter($token);
							print $token . "\n";
						}
						elsif($currentLine =~ /^\/\*.*/){					
							$token = $currentLine;
							$currentLine = "";
							$searchForEnd = 1;
							&handle_oneLineEnter($token);
							print $token . "\n";
						}
						elsif($currentLine =~ /(^\")(.*)/){
							$token = $1;
							$currentLine = $2;
							$searchForRightQ = 1;
							&handle_oneLinePrint($token);
							print $token;

						}			
									
						
						else{
							$currentLine =~ /(.)(.*)/;
							$token = $1;
							$currentLine = $2;
							$searchForSpace = 1;
							&handle_oneLinePrint($token);				
							print $token;
						}
					} # end 1st else

				}
				#$_ = $_ + 1;
				print HTML "</p>\n";
				print "\n";
			}

			
			print HTML "<br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
						<br /><br /><br /><br /><br /><br /><br /><br /><br /><br />\n
						<br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
						<text><text/>";		
			print HTML "</body>\n</html>\n";
			close(FILE);
			close(FILE);
			close(HTML);	
			
		
		
		# clear hashtable
		%hashMethodClass = ();
		# clear all global array @ how to
		@lines_constant_pool = ();
		@source_code_line = ();
		@linkMethods = ();
		@lines = ();
		
			
	
# subroutines

		sub handle_space{
			
			my($a);
			($a) = @_;
			
			@indentSpace = split //, $a;
			$spaceSize = @indentSpace;
			#print $spaceSize;
			while($spaceSize){
				print HTML "&nbsp;";
				$spaceSize--;
			}
		}
		
		sub handle_method_declare(){
			my($t, $count);
			($t, $count) = @_;
			my($line_cnt) = "line " . "$count";
			#print "This is METHOD De " . $line_cnt . "\n";
			
			if(($line_cnt ~~ @source_code_line)!=1 && ($t ~~ @linkMethods)){
				print HTML "<a name = \"$t\"></a>";
				print HTML "<a href = \"$javaFiles[$i].html#$t\">$t</a>";
				#print "This is method De " . $count . "\n";
			}
		}
		
		sub handle_id{
			my($t, $count);
			($t, $count) = @_;
			my($line_cnt) = "line " . "$count";
			
			if($_[0] ~~ @keywords){
				print HTML "<b>$_[0]</b>";
			}
			elsif($_[0] ~~ @linkMethods){		
				$classOfMethod = $hashMethodClass{$_[0]};
				if($classOfMethod =~ m/java/){
					print HTML "<a href = \"http://docs.oracle.com/javase/7/docs/api/$classOfMethod.html#$_[0]\">$_[0]</a>";
				}
				# the left methods should be method call not declaration
				# but there are repetitive there
				elsif(($line_cnt ~~ @source_code_line)&&($classOfMethod !~ m/java/)){						
					print HTML "<a href = \"#$_[0]\">$_[0]</a>";
					print "I AM HERE!!!!!!!!!!!!!!";
				}
			}
			else{
				print HTML "$_[0]";
			}
		}
		
		sub handle_oneLinePrint{
			print HTML "$_[0]";
		}
		
		sub handle_oneLineEnter{
			print HTML "$_[0]";
			print HTML "<br />";
		}
		
	}
	
	
	print HTML "</ul>";
	print HTML "</body>\n</html>\n";



