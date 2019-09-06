<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/rating.inc.php");

$userUuid = httpGetUuidParam("userUuid", true);
$itemUuid = httpGetUuidParam("itemUuid", true);
$rating = httpGetIntParam("rating", true);

$pdo = dbConnectDB();

$existingRatingRecord = dbGetRating($pdo, $userUuid, $itemUuid);

if (empty($existingRatingRecord)) {
	$success = dbInsertRating($pdo, $userUuid, $itemUuid, $rating);
} else {
	$success = dbUpdateRating($pdo, $userUuid, $itemUuid, $rating);
}

$output = array("success" => $success);
print(json_encode ($output));

?>
