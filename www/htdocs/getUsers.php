<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/user.inc.php");

$pdo = dbConnectDB();
if (!isLocalServer()) {
	$pdo->query("SET NAMES 'utf-8'");
}

$userRecords = dbGetUsers($pdo);

$utftUserRecords = array();
foreach($userRecords as $userRecord) {
	array_push($utftUserRecords, array_map("utf8_encode", $userRecord));
}

$output = array("users" => $utftUserRecords);
print(json_encode ($output));
?>
