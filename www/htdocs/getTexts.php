<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/text.inc.php");

$pdo = dbConnectDB();
if (!isLocalServer()) {
	$pdo->query("SET NAMES 'utf-8'");
}

$languageCode = httpGetLanguageCode();
$textRecords = dbGetTexts($pdo, httpGetLanguageCode());
if ($languageCode != LANGUAGE_CODE_EN) {
	$englishTextRecords = dbGetTexts($pdo, LANGUAGE_CODE_EN);
	$indexedEnglishTextRecords = array();
	foreach($englishTextRecords as $englishTextRecord) {
		$indexedEnglishTextRecords[$englishTextRecord["textId"]] = $englishTextRecord;
	}

	// overwrite with requested language
	foreach($textRecords as $textRecord) {
		$indexedEnglishTextRecords[$textRecord["textId"]] = $textRecord;
	}
	$textRecords = array_values($indexedEnglishTextRecords);
}

$utfTextRecords = array();
foreach($textRecords as $textRecord) {
	array_push($utfTextRecords, array_map("utf8_encode", $textRecord));
}

$output = array("texts" => $textRecords);
print(json_encode ($output));
?>
