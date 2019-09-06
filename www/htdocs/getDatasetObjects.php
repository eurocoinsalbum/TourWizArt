<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tourDataset.inc.php");

$tourDatasetUuid = httpGetTourDatasetUuid();
$pdo = dbConnectDB();
$datasetObjects = dbGetDatasetObjects($pdo, $tourDatasetUuid);

$utfDatasetObjects = array();
foreach($datasetObjects as $datasetObject) {
	array_push($utfDatasetObjects, array_map("utf8_encode", $datasetObject));
}

$output = array("datasetObjects" => $utfDatasetObjects);
print(json_encode ($output));
?>
