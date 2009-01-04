<?php
/**
Author: Pierre Lindenbaum PhD
Mail: plindenbaum@yahoo.fr

Installation:
	install this file in
	
		${MWROOT}/extensions/dnaseq/dnaseq.php
	
	and add the following line at the end of ${MWROOT}/LocalSettings.php :
	
		require_once("$IP/extensions/dnaseq/dnaseq.php");
**/


/**
 * Protect against register_globals vulnerabilities.
 * This line must be present before any global variable is referenced.
**/
if(!defined('MEDIAWIKI')){
	echo("This is an extension to the MediaWiki package and cannot be run standalone.\n" );
	die(-1);
}

/* Avoid unstubbing $wgParser on setHook() too early on modern (1.12+) MW versions */
if ( defined( 'MW_SUPPORTS_PARSERFIRSTCALLINIT' ) ) {
	$wgHooks['ParserFirstCallInit'][] = 'myDnaSequence';
} else {
	$wgExtensionFunctions[] = 'myDnaSequence';
}


/**
 * An array of extension types and inside that their names, versions, authors and urls. This credit information gets added to the wiki's Special:Version page, allowing users to see which extensions are installed, and to find more information about them.
**/
$wgExtensionCredits['parserhook'][] = array(
        'name'          =>      'dnaseq',
        'version'       =>      '0.1',
        'author'        =>      '[http://plindenbaum.blogspot.com Pierre Lindenbaum]',
        'url'           =>      'http://code.google.com/p/lindenb/source/browse/trunk/proj/mediawiki/extensions/dnaseq/dnaseq.php',
        'description'   =>      'Displays a DNA sequence'
);

function myDnaSequence()
	{
	global $wgParser;
	$wgParser->setHook( 'dnaseq', 'myRenderDnaSequence' );
	return true;
	}

function myRenderDnaSequence( $input, $args, $parser )
	{
	if($input==null) return "";
	$len= strlen($input);
	$n=0;
	$html="<div style='padding: 10px; font-size:10px; border-width: thin; border: 1px black solid; white-space: pre;background-color: white;font-family: courier, monospace;line-height:13px; font-size:12px;'>";
	for($i=0;$i< $len;$i++)
		{
		$c = $input[$i];
		if(ctype_space($c) || ctype_digit($c)) continue;
		if($n % 60 == 0)
			{
			if($n!=0) $html.="<br/>";
			$html.= sprintf("%06d  ",($n+1));
			}
		else if($n % 10 ==0)
			{
			$html.=" ";
			}
		$n++;
		switch(strtolower($c))
			{
			case "a":
				$html.="<span style='color:green;'>".$c."</span>";
				break;
			case "c":
				$html.="<span style='color:blue;'>".$c."</span>";
				break;
			case "g":
				$html.="<span style='color:black;'>".$c."</span>";
				break;
			case "t":
			case "u":
				$html.="<span style='color:red'>".$c."</span>";
				break;
			default:
				$html.="<span style='text-decoration: blink;color:gray'>".$c."</span>";
				break;
			}
		if($n % 60 == 0)
			{
			$html.= sprintf("  %06d",($n));
			}
		}
	$html .= "</div>";
	return $html;
	}

?>