<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tourDataset.inc.php");

$pdo = dbConnectDB();
$tourDatasets = dbGetActiveTourDatasets($pdo);

foreach($tourDatasets as $key => $tourDataset) {
	$datasetMaps = dbGetDatasetMaps($pdo, $tourDataset["tourDatasetUuid"]);
	$tourDatasets[$key]["maps"] = $datasetMaps;
}

$output = array("tourDatasets" => $tourDatasets);
print(json_encode ($output));
?>
