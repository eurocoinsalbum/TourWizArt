<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/text.inc.php");

$pdo = dbConnectDB();
if (!isLocalServer()) {
	$pdo->query("SET NAMES 'utf-8'");
}

$peopleRecords = dbGetPeople($pdo);

$utftPeopleRecords = array();
foreach($peopleRecords as $peopleRecord) {
	array_push($utftPeopleRecords, array_map("utf8_encode", $peopleRecord));
}

$output = array("peopleList" => $utftPeopleRecords);
print(json_encode ($output));
?>
