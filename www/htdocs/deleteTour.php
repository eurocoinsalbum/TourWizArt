<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tour.inc.php");
require_once("$rootDir/inc/text.inc.php");

$tourUuid = httpGetTourUuid();
$pdo = dbConnectDB();
$existingTourRecord = dbGetTour($pdo, $tourUuid);

dbBeginTransaction($pdo);

if (!empty($existingTourRecord)) {
	dbDeleteTextId($pdo, $existingTourRecord["titleTextId"]);
	dbDeleteTextId($pdo, $existingTourRecord["descriptionTextId"]);
	dbDeleteTourObjects($pdo, $tourUuid);
	dbDeleteTour($pdo, $tourUuid);
}

$output = array("success" => "true");
dbCommitTransaction($pdo);
print(json_encode ($output));
?>
