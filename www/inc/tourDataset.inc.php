<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");

define("TOUR_DATASET_STATUS_ACTIVE", "ACTIVE");

function dbGetActiveTourDatasets($pdo) {
	return dbGetRecords($pdo, "tour_dataset", array("status" => TOUR_DATASET_STATUS_ACTIVE), null, null);
}

function dbGetTourDatasets($pdo) {
	return dbGetRecords($pdo, "tour_dataset", array(), null, null);
}

function dbGetDatasetObjects($pdo, $tourDatasetUuid) {
	return dbGetRecords($pdo, "dataset_object", array("tourDatasetUuid" => $tourDatasetUuid), null, null);
}

function dbGetDatasetMaps($pdo, $tourDatasetUuid) {
	return dbGetRecords($pdo, "dataset_map", array("tourDatasetUuid" => $tourDatasetUuid), null, null);
}

?>
