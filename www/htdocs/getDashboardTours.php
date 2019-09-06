<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tour.inc.php");
require_once("$rootDir/inc/tourDataset.inc.php");

define("LIMIT_TOURS", 100);
$tourDatasetUuid = httpGetUuidParam("tourDatasetUuid", false);

$pdo = dbConnectDB();

$tourRecords = array();
$tourDatasetRecords = dbGetTourDatasets($pdo);
foreach($tourDatasetRecords as $tourDatasetRecord) {
	if ($tourDatasetUuid != null && $tourDatasetUuid != $tourDatasetRecord["tourDatasetUuid"]) {
		continue;
	}
	$tourRecords = array_merge($tourRecords, dbGetToursByTourDataset($pdo, $tourDatasetRecord["tourDatasetUuid"], LIMIT_TOURS));
}

$output = array("tours" => $tourRecords);
print(json_encode ($output));

?>
