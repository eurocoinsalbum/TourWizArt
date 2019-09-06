<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tour.inc.php");
require_once("$rootDir/inc/text.inc.php");
require_once("$rootDir/inc/user.inc.php");

$tourUuid = httpGetTourUuid();
$languageCode = httpGetLanguageCode();
// multi language not support now
$languageCode = LANGUAGE_CODE_EN;

$jsonObject = json_decode(file_get_contents("php://input"), true);
$tourDatasetUuid = $jsonObject["tourDatasetUuid"];
$title = $jsonObject["title"];
$description = $jsonObject["description"];
$status = $jsonObject["status"];
$accessLevel = $jsonObject["accessLevel"];
$authorUserUuid = $jsonObject["authorUserUuid"];
$iconUuid = $jsonObject["iconUuid"];

$pdo = dbConnectDB();

dbBeginTransaction($pdo);

// data checks
if ($tourUuid != $jsonObject["tourUuid"]) {
	header($http400);
	die(json_encode(array("error" => "tourUuid mismatch")));
}

$userRecord = dbGetUser($pdo, $userUuid);
if ($userUuid != $authorUserUuid && $userRecord["role"] != "ADMIN") {
	header($http400);
	die(json_encode(array("error" => "userUuid-authorUserUuid mismatch")));
}

$existingTourRecord = dbGetTour($pdo, $tourUuid);
$textRecords = array();

if (empty($existingTourRecord)) {
	$titleTextId = dbCreateTextId($pdo, $languageCode, $title);
	$descriptionTextId = dbCreateTextId($pdo, $languageCode, $description);
	$success = dbInsertTour($pdo, $tourUuid, $tourDatasetUuid, $userUuid, $titleTextId, $descriptionTextId, $status, $accessLevel, $iconUuid);
	array_push($textRecords, array("textId" => $titleTextId, "languageCode" => $languageCode, "text" => $title));
	array_push($textRecords, array("textId" => $descriptionTextId, "languageCode" => $languageCode, "text" => $description));
} else {
	// title
	if (isset($jsonObject["title"])) {
		dbUpdateTextId($pdo, $existingTourRecord["titleTextId"], $languageCode, $jsonObject["title"]);
		array_push($textRecords, array("textId" => $existingTourRecord["titleTextId"], "languageCode" => $languageCode, "text" => $jsonObject["title"]));
	}
	// description
	if (isset($jsonObject["description"])) {
		dbUpdateTextId($pdo, $existingTourRecord["descriptionTextId"], $languageCode, $jsonObject["description"]);
		array_push($textRecords, array("textId" => $existingTourRecord["descriptionTextId"], "languageCode" => $languageCode, "text" => $jsonObject["description"]));
	}
	$success = dbUpdateTour($pdo, $tourUuid, $status, $accessLevel, $iconUuid);
}

// add/update assigned objects
if (isset($jsonObject["datasetObjectUuids"])) {
	dbDeleteTourObjects($pdo, $tourUuid);
	foreach($jsonObject["datasetObjectUuids"] as $index => $jsonDatasetObject) {
		dbInsertTourObject($pdo, $tourUuid, $index, $jsonDatasetObject["datasetObjectUuid"]);
	}
}

$output = array("texts" => $textRecords);

dbCommitTransaction($pdo);
print(json_encode ($output));

?>
