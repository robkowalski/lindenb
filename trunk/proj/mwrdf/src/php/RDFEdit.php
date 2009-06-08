<?php
$wgExtensionCredits['other'][] = array(
        'name' => 'RDFEditor',
        'version' => 0.1,
        'author' => 'Pierre Lindenbaum PhD plindenbaum@yahoo.fr',
        'url' => 'http://plindenbaum.blogspot.com',
        'description' => 'java based RDF editor for MediaWiki'
);


global $wgHooks;
 
$wgHooks['AlternateEdit'][] = 'fnMyRdfEditor';



function fnMyRdfEditor(&$editpage) {
global $wgOut, $wgRequest, $wgParser, $wgUser, $wgCookiePrefix, $wgScriptPath,$wgTitle;

if(	$wgRequest->getText("java")=="false" ||
	$wgRequest->getCheck( 'wpSave' ) ||
	$wgRequest->getCheck( 'wpPreview' ) ||
    $wgRequest->getCheck( 'wpDiff' ) ||
    $wgTitle->getNamespace() != NS_MAIN ) return true;

$editpage->importFormData($wgRequest);
$editpage->initialiseForm();
$wgOut->addHTML("<applet name='mw_editor_applet' id='mw_editor_applet'" . 
        " code='org/lindenb/mwrdf/MWRdfEditor.class' ".
		" codebase='mwrdf/WEB-INF/' ".
		" archive='commons-httpclient.jar, commons-logging.jar, commons-lang.jar, commons-codec.jar, mwrdfedit.jar'" .
		" width='800' height='500' ".
             	" >\n");

$cookieNames = array("UserName", "UserID", "_session");
foreach($cookieNames as $ndx => $value) {
    $cookieKey = $wgCookiePrefix . $value;
    if (!array_key_exists($cookieKey, $_COOKIE)) continue;
	$wgOut->addHTML("\n<param name='" . htmlspecialchars($value) . "' \t value='" . htmlspecialchars($_COOKIE[$cookieKey]) . "'/>");
 }
$wgOut->addHTML("\n<param name='cookiePrefix' \t value='".htmlspecialchars($wgCookiePrefix)."'/>");
$wgOut->addHTML("\n<param name='wpSection' \t value='".htmlspecialchars($editpage->section)."'/>");
$wgOut->addHTML("\n<param name='wpEdittime' \t value='".htmlspecialchars($editpage->edittime)."'/>");
$wgOut->addHTML("\n<param name='wpScrolltop' \t value='".htmlspecialchars($editpage->scrolltop)."'/>");
$wgOut->addHTML("\n<param name='wpStarttime' \t value='".htmlspecialchars($editpage->starttime)."'/>");
$wgOut->addHTML("\n<param name='wpSummary' \t value=\"".str_replace("'","&apos;",htmlspecialchars($editpage->summary))."\"/>");
$wgOut->addHTML("\n<param name='wpAutoSummary' \t value='".md5( $editpage->summary )."'/>");
$wgOut->addHTML("\n<param name='wpEditToken' \t value=\"". $wgUser->editToken()."\"/>");
$wgOut->addHTML("\n<param name='title' \t value='".htmlentities($wgTitle->getText())."'/>");
$wgOut->addHTML("\n<param name='pageId' \t value='".$wgTitle->getArticleID()."'/>");
$wgOut->addHTML("\n<param name='revId' \t value='".$wgTitle->getLatestRevID()."'/>");
$wgOut->addHTML("\n</applet>");
//$wgOut->addHTML("<br/>RevID[".$wgTitle->getLatestRevID()."] ID [".$wgTitle->getArticleID()."][".$wgTitle->getText()."][".$wgTitle->getPartialURL()."][".$wgTitle->getDBkey()."][".$wgTitle->getFragment()."]");

$wgOut->addHTML("<br/><a href='".$wgTitle->escapeLocalURL("action=edit&java=false")."'>Standard Form</a><br/>" );

return false;
}

?>
