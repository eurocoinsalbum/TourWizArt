<?php
$rootDir = "..";
require_once("$rootDir/inc/dbUtils.inc.php");
require_once("$rootDir/inc/httpUtils.inc.php");
require_once("$rootDir/inc/tourDataset.inc.php");

$tourDatasetUuid = httpGetTourDatasetUuid();
$pdo = dbConnectDB();
$datasetMaps = dbGetDatasetMaps($pdo, $tourDatasetUuid);

$output = array("datasetMaps" => $datasetMaps);
print(json_encode ($output));
?>
