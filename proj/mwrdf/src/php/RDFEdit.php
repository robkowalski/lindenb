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
	$wgOut->addHTML("<param name='" . htmlspecialchars($value) . "' value='" . htmlspecialchars($_COOKIE[$cookieKey]) . "'/>");
 }
$wgOut->addHTML("<param name='cookiePrefix' value='".htmlspecialchars($wgCookiePrefix)."'/>");


$wgOut->addHTML("<param name='wpSection' value='".htmlspecialchars($editpage->section)."'/>");
$wgOut->addHTML("<param name='wpEdittime' value='".htmlspecialchars($editpage->edittime)."'/>");
$wgOut->addHTML("<param name='wpScrolltop' value='".htmlspecialchars($editpage->scrolltop)."'/>");
$wgOut->addHTML("<param name='wpStarttime' value='".htmlspecialchars($editpage->starttime)."'/>");
$wgOut->addHTML("<param name='wpSummary' value=\"".str_replace("'","&apos;",htmlspecialchars($editpage->summary))."\"/>");
$wgOut->addHTML("<param name='wpAutoSummary' value='".md5( $editpage->summary )."'/>");
$wgOut->addHTML("<param name='wpEditToken' value=\"". $wgUser->editToken()."\"/>");
$wgOut->addHTML("<param name='title' value='".htmlentities($wgTitle->getText())."'/>");
$wgOut->addHTML("<param name='pageId' value='".$wgTitle->getArticleID()."'/>");
$wgOut->addHTML("<param name='revId' value='".$wgTitle->getLatestRevID()."'/>");
$wgOut->addHTML("</applet>");
//$wgOut->addHTML("<br/>RevID[".$wgTitle->getLatestRevID()."] ID [".$wgTitle->getArticleID()."][".$wgTitle->getText()."][".$wgTitle->getPartialURL()."][".$wgTitle->getDBkey()."][".$wgTitle->getFragment()."]");

return false;
}

?>
